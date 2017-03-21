package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 25.02.2017.
 * Class fetching SkOs database
 */

public class FetchSkos {
    public int status;

    public FetchSkos(Context context) {
        FetchWebsite fw;
        String fww;
        String filename = "skos.txt";

        List<List<String>> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();

        // check file datestamp
        File file = new File(context.getFilesDir() + "/" + filename);

        // if file not exist or is outdated
        if (!file.exists() || System.currentTimeMillis()-file.lastModified() > 86400000*7) {
            fw = new FetchWebsite("https://api.janpogocki.pl/aghwd/skos.html");
            fww = fw.getWebsite(false, false, "");

            try {
                FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fww.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // read names from saved file
        try {
            FileInputStream inputStream = context.openFileInput(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int i = 0;
            while ((line = r.readLine()) != null) {
                if (i%3 == 0)
                    list1.add(line);
                else if (i%3 == 1)
                    list2.add(line);
                else if (i%3 == 2)
                    list3.add(line);

                i++;
            }
            r.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list1.size() > 0 && list1.size() == list2.size() && list2.size() == list3.size()){
            list.add(list1);
            list.add(list2);
            list.add(list3);

            Storage.skosList = list;
            status = 0;
        }
        else
            status = -1;
    }
}
