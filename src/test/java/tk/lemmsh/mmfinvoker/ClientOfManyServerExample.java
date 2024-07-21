package tk.lemmsh.mmfinvoker;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by lemmsh on 7/26/15.
 */
public class ClientOfManyServerExample {

    public static void main(String[] args) throws IOException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(60);

        List<MMapClient> clients = Lists.newArrayList();
        for (int i = 0; i < 20; i++) {
            MMapClient mMapClient = new MMapClient(
                    new File("shared" + i + ".shm"),
                    8000, 1000, 1000);
            clients.add(mMapClient);
        }
        int i = 0;
        for (final MMapClient client : clients) {
            i ++;
            final byte j = (byte)i;
            executorService.execute(() -> {
                try{
                    long start = System.currentTimeMillis();
                    for (int k = 0; k < 1000; k++) {
                        byte[] ask = client.ask(new byte[]{1, 2, 3, j});
                        if (ask.length > 4)
                            throw new IllegalStateException("adsf");
                        LockSupport.parkNanos(1000);
                    }
                    long stop = System.currentTimeMillis();
                    System.out.println(j + " took " + (stop - start)/1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);


    }

}
