package tk.lemmsh.mmfinvoker;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * Created by lemmsh on 7/26/15.
 */
public class EchoClientExample {

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        File echo = new File("shared-echo.shm");

        MMapClient echoMMapClient = new MMapClient(echo, 8000010, 1000, 5000);
        byte[] data = new byte[8000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)i;
        }
        for (int i = 0; i < 100000; i++) {
            byte[] response = echoMMapClient.ask(data);
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            byte[] response = echoMMapClient.ask(data);
        }
        long stop = System.currentTimeMillis();
        System.out.println("avg. call time is " + (double)(stop - start)/1000);

    }


}
