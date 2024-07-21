package tk.lemmsh.mmfinvoker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lemmsh on 7/26/15.
 */
public class MMapClient {

    final MappedByteBuffer mem;
    final int bufferSize;
    final int granularity;
    final int timeout;
    final ReentrantLock lock = new ReentrantLock();

    public MMapClient(File file, int bufferSize, int granularity, int timeout) throws IOException {
        this.bufferSize = bufferSize;
        this.granularity = granularity;
        this.timeout = timeout;
        FileChannel fc = new RandomAccessFile(file, "rw").getChannel();
        mem = fc.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize + 5);
    }

    public byte[] ask(byte[] request) throws TimeoutException, InterruptedException {
        boolean locked = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        if (!locked) {
            throw new TimeoutException("timed out waiting for resource");
        }
        try{
            if (request.length > bufferSize) {
                throw new IllegalArgumentException("request length if greater than buffer: " + request.length);
            }

            long started = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            while (mem.hasRemaining() && mem.get(0) == Protocol.SERVER_BUSY) {
                LockSupport.parkNanos(granularity);
                if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - started > timeout)
                    throw new TimeoutException("got timeout on request - the server is busy");
            }
            mem.put(new byte[]{0, 0, 0, 0, 0});
            mem.position(1);
            mem.putInt(request.length);
            mem.put(request);
            mem.put(0, Protocol.REQUEST_START);
            mem.position(0);

            while (true) {
                if (mem.hasRemaining() && mem.get(0) == Protocol.RESPONSE_START || mem.get(0) == Protocol.SERVER_ERR) {
                    if (mem.get(0) == Protocol.SERVER_ERR) {
                        mem.put(0, (byte)0);
                        throw new RuntimeException("server returned error");
                    }
                    break;
                } else {
                    LockSupport.parkNanos(granularity);
                    if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - started > timeout)
                        throw new TimeoutException("got timeout on request");
                }
            }

            mem.position(1);
            int length = mem.getInt();
            byte[] response = new byte[length];
            mem.get(response);
            mem.position(0);
            mem.put(new byte[5]);
            mem.position(0);
            return response;
        } finally {
            lock.unlock();
        }
    }

}
