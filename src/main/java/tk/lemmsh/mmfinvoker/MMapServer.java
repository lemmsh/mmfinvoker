package tk.lemmsh.mmfinvoker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lemmsh on 7/26/15.
 */
public class MMapServer {

    final MappedByteBuffer mem;
    final int bufferSize;
    final int granularity;
    final MMapServerLogic logic;
    final ExecutorService executor = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory()); //todo: daemon, name
    final ExceptionHandler exceptionHandler;
    volatile Future<Void> run;

    public MMapServer(File file, int bufferSize, int granularity, MMapServerLogic logic, ExceptionHandler exceptionHandler) throws IOException {
        this.bufferSize = bufferSize;
        this.granularity = granularity;
        this.logic = logic;
        this.exceptionHandler = exceptionHandler;
        FileChannel fc = new RandomAccessFile(file, "rw").getChannel();
        mem = fc.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize + 3);
    }

    public void start() {
        mem.put(new byte[bufferSize]);
        mem.position(0);
        mem.force();
        run = executor.submit(() -> {
            while(true) {
                try {
                    doStart();
                } catch (Exception e) {
                    exceptionHandler.handle(e);
                    mem.position(0);
                    mem.put(new byte[5]);
                    mem.put(0, Protocol.SERVER_ERR);
                    mem.position(0);
                }
            }
        });
    }

    public void stop() {
        if (run != null)
            run.cancel(true);
        executor.shutdownNow();
    }

    private void doStart() throws Exception {
        while (true) {
            if (mem.hasRemaining() && mem.get(0) == Protocol.REQUEST_START) {
                mem.put(0, Protocol.SERVER_BUSY);
                mem.position(1);
                int length = mem.getInt();
                byte[] request = new byte[length];
                mem.get(request);
                byte[] response = logic.apply(request);
                if (response.length > bufferSize) {
                    throw new IllegalStateException("response is too large: " + response.length);
                }
                mem.position(1);
                mem.putInt(response.length);
                mem.put(response);
                mem.put(0, Protocol.RESPONSE_START);
                mem.position(0);
            } else {
                LockSupport.parkNanos(1000);
            }
        }
    }

}
