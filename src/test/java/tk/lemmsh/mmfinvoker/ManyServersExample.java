package tk.lemmsh.mmfinvoker;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicLongMap;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by lemmsh on 7/26/15.
 */
public class ManyServersExample {

    public static void main(String[] args) throws IOException {

        final AtomicLongMap<Integer> map = AtomicLongMap.create();

        List<MMapServer> serverList = Lists.newArrayList();
        for (int i = 0; i < 20; i++) {
            final int finalI = i;
            MMapServer mMapServer = new MMapServer(
                    new File("shared" + i + ".shm"),
                    8000, 1000, new MMapServerLogic() {
                @Override
                public byte[] apply(byte[] request) throws Exception {
                    System.out.println("req " + finalI);
                    map.addAndGet(finalI, 1);
                    return request;
                }
            }, new ExceptionHandler() {
                @Override
                public void handle(Exception e) {
                    e.printStackTrace();
                }
            });
            mMapServer.start();
            serverList.add(mMapServer);
        }



    }

}
