package tk.lemmsh.mmfinvoker;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;

/**
 * Created by lemmsh on 7/26/15.
 */
public class EchoServerExample {

    public static void main(String[] args) throws IOException {
        File file = new File("shared-echo.shm");
        MMapServer mMapServer = new MMapServer(file, 8000010, 1000, request -> request, Throwable::printStackTrace);
        mMapServer.start();
    }

}
