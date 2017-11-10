package pl.janpogocki.agh.wirtualnydziekanat.javas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.CharBuffer;

/**
 * Created by Jan on 04.11.2017.
 * Generating File ready to send via POST method
 */
public class BigPOSTgenerator {
    private BufferedWriter writer;
    private boolean firstArg;

    public BigPOSTgenerator(String filename) throws IOException {
        firstArg = true;

        File file = new File(filename);
        if (file.exists())
            file.delete();

        writer = new BufferedWriter(new FileWriter(filename));
    }

    public void add(String _arg1, String _arg2) throws IOException {
        if (!firstArg) {
            writer.write("&");
        }

        writer.write(URLEncoder.encode(_arg1, "UTF-8"));
        writer.write("=");
        writer.write(URLEncoder.encode(_arg2, "UTF-8"));

        writer.flush();

        firstArg = false;
    }

    public void add(String _arg1, InputStream _arg2) throws IOException {
        if (!firstArg) {
            writer.write("&");
        }

        writer.write(URLEncoder.encode(_arg1, "UTF-8"));
        writer.write("=");
        writer.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(_arg2));

        // Read Server Response
        CharBuffer buffer = CharBuffer.allocate(8192);

        while(reader.read(buffer) >= 0)
        {
            buffer.flip();
            // Append server response in string
            writer.write(URLEncoder.encode(buffer.toString(), "UTF-8"));

            buffer.clear();
            writer.flush();
        }

        firstArg = false;
    }

    public void closeFile() throws IOException {
        writer.close();
    }
}
