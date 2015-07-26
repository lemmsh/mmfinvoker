package tk.lemmsh.mmfinvoker;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by lemmsh on 7/26/15.
 */
public class ServerExample {

    public static void main(String[] args) throws IOException {

        File file = new File("shared.shm");
        MMapServer mMapServer = new MMapServer(file, 8000, 1000, new MMapServerLogic() {
            @Override
            public byte[] apply(byte[] request) throws Exception {
                ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(request));
                String data = (String) input.readObject();
                input.close();
//                System.out.println("sha of " + data + " requested");
                HashCode hashCode = Hashing.sha512().hashString(data, Charset.defaultCharset());
                return hashCode.asBytes();
            }
        }, new ExceptionHandler() {
            @Override
            public void handle(Exception e) {
                e.printStackTrace();
            }
        });
        mMapServer.start();

    }


}
