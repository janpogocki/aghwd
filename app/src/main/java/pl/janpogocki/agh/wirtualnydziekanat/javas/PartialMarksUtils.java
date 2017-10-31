package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by Jan on 31.10.2017.
 * Partial Marks Utils Class
 */

public class PartialMarksUtils {
    public static void saveNewPartialMark(Context c, PartialMark partialMark){
        try {
            String filename = Storage.getUniversityStatusHash() + "_pm_" + partialMark.currentSemester + ".json";
            File file = new File(c.getFilesDir() + "/" + filename);

            if (file.exists()) {
                StringBuilder jsonFromFile = new StringBuilder();
                FileInputStream inputStream = c.openFileInput(filename);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = r.readLine()) != null) {
                    jsonFromFile.append(line);
                }
                r.close();
                inputStream.close();

                JSONArray jsonArray = new JSONArray(jsonFromFile.toString());

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", partialMark.subjectName + partialMark.lectureName);
                jsonObject.put("mark", partialMark.mark);
                jsonObject.put("title", partialMark.title);
                jsonObject.put("timestamp", partialMark.timestamp);
                jsonObject.put("description", partialMark.description);

                jsonArray.put(jsonObject);

                // save to file
                PrintWriter out = new PrintWriter(file);
                out.print(jsonArray.toString());
                out.close();
            } else {
                JSONArray jsonArray = new JSONArray();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", partialMark.subjectName + partialMark.lectureName);
                jsonObject.put("mark", partialMark.mark);
                jsonObject.put("title", partialMark.title);
                jsonObject.put("timestamp", partialMark.timestamp);
                jsonObject.put("description", partialMark.description);

                jsonArray.put(jsonObject);

                // save to file
                PrintWriter out = new PrintWriter(file);
                out.print(jsonArray.toString());
                out.close();
            }
        } catch (Exception e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }
    }

    public static void editPartialMark(Context c, PartialMark oldPartialMark, PartialMark newPartialMark){
        removePartialMark(c, oldPartialMark);
        saveNewPartialMark(c, newPartialMark);
    }

    public static void removePartialMark(Context c, PartialMark partialMark){
        try {
            String filename = Storage.getUniversityStatusHash() + "_pm_" + partialMark.currentSemester + ".json";
            File file = new File(c.getFilesDir() + "/" + filename);

            if (file.exists()) {
                StringBuilder jsonFromFile = new StringBuilder();
                FileInputStream inputStream = c.openFileInput(filename);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = r.readLine()) != null) {
                    jsonFromFile.append(line);
                }
                r.close();
                inputStream.close();

                JSONArray jsonArray = new JSONArray(jsonFromFile.toString());
                JSONArray newJsonArray = new JSONArray();

                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject currentJsonObject = jsonArray.getJSONObject(i);

                    if (!currentJsonObject.getString("id").equals(partialMark.subjectName + partialMark.lectureName)
                            || !currentJsonObject.getString("mark").equals(partialMark.mark)
                            || !currentJsonObject.getString("title").equals(partialMark.title)
                            || currentJsonObject.getLong("timestamp") != partialMark.timestamp
                            || !currentJsonObject.getString("description").equals(partialMark.description)) {
                                newJsonArray.put(currentJsonObject);
                            }
                }

                // save to file
                PrintWriter out = new PrintWriter(file);
                out.print(newJsonArray.toString());
                out.close();
            }
        } catch (Exception e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }
    }
}
