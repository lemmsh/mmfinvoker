package tk.lemmsh.mmfinvoker;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.junit.Assert;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by lemmsh on 7/26/15.
 */
public class ClientExample {

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        File file = new File("shared.shm");

        Random random = new Random(12345);

        MMapClient mMapClient = new MMapClient(file, 8000, 1000, 5000);
        for (int i = 0; i < 100000; i++) {
            String randS = random.ints().limit(10).toString();
            String data = "data" + randS;

            ByteArrayOutputStream array = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(array);
            output.writeObject(data);
            output.close();
            byte[] response = mMapClient.ask(array.toByteArray());

            HashCode controlData = Hashing.sha512().hashString(data, Charset.defaultCharset());
            Assert.assertArrayEquals(controlData.asBytes(), response);
            LockSupport.parkNanos(10);
        }

    }

}
