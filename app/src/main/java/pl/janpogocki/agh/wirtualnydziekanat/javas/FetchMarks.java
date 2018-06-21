package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Jan on 21.07.2016.
 * Returning interpreted table with marks
 */

public class FetchMarks {
    private List<LabelAndList<LabelAndList<String>>> database = new ArrayList<>();
    private HashMap<String, String> databaseNewMarks = new HashMap<>();
    private HashMap<String, String> databaseOldMarks = new HashMap<>();
    public int amountECTS, status;
    public float amountAvgSemester, amountAvgYear;

    public FetchMarks(Context c, String HTML2interprete, int currentSemester) throws Exception {
        Document htmlParsed = Jsoup.parse(HTML2interprete);

        // Check if page with marks is not "brak danych do wyswietlenia"
        if (htmlParsed.getAllElements().select(".gridDane").size() > 0) {
            if (Storage.universityStatus == null || Storage.universityStatus.size() == 0){
                new FetchUniversityStatus(false);
            }

            prepareCacheDataFromJSON(c, currentSemester);

            Elements htmlParsedGridDane = htmlParsed.getAllElements().select(".gridDane");

            // Go over every entry
            for (Element current : htmlParsedGridDane) {
                String htmlParsedSubjectName = current.select("td").get(0).ownText();

                // If subject list exists & subject name on it - true, else - false
                int subjectExists = -1;
                if (database.size() > 0) {
                    int i = 0;
                    for (LabelAndList<LabelAndList<String>> current2 : database) {
                        if (htmlParsedSubjectName.equals(current2.getLabel())) {
                            subjectExists = i;
                            break;
                        }
                        i++;
                    }
                }

                // Gather data about marks, ECTSes etc.
                LabelAndList<String> marks = new LabelAndList<>(current.select("td").get(2).ownText());
                for (int i = 0; i <= 10; i++) {
                    if (current.select("td").get(i).select("span.ocena").size() == 2) {
                        marks.add(current.select("td").get(i).select(".ocena").get(0).ownText()
                                + " " + current.select("td").get(i).select(".ocena").get(1).ownText());
                    } else if (current.select("td").get(i).select("span.ocena").size() == 1) {
                        marks.add(current.select("td").get(i).select(".ocena").get(0).ownText());
                    } else
                        marks.add(current.select("td").get(i).ownText());
                }

                // Add new subject, and save more data (marks, ECTSes) about this kind of lessons
                StringBuilder jsonMarks;
                if (subjectExists != -1) {
                    jsonMarks = new StringBuilder(databaseNewMarks.get(htmlParsedSubjectName));
                    database.get(subjectExists).add(marks);
                } else {
                    jsonMarks = new StringBuilder();
                    LabelAndList<LabelAndList<String>> subjectAndLesson = new LabelAndList<>(htmlParsedSubjectName);
                    subjectAndLesson.add(marks);
                    database.add(subjectAndLesson);
                }

                // add to json database
                for (int i = 4; i <= 6; i++)
                    jsonMarks.append(marks.getList().get(i));
                databaseNewMarks.put(htmlParsedSubjectName, jsonMarks.toString());
            }

            // If there no data in list...
            if (database.size() == 0) {
                status = -1;
            } else {
                // Gathering infos about AVGs and ECTS
                String[] htmlParsedAvgECTS = htmlParsed.select("#ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_litSredniaSuma").get(0).html().split("<br>");
                List<String> amountAvgSemesterStr = Arrays.asList(htmlParsedAvgECTS[0].split(": "));
                List<String> amountAvgYearStr = Arrays.asList(htmlParsedAvgECTS[1].split(": "));
                List<String> amountECTSStr = Arrays.asList(htmlParsedAvgECTS[2].split(": "));

                if (amountAvgSemesterStr.size() == 2)
                    amountAvgSemester = Float.parseFloat(htmlParsedAvgECTS[0].split(": ")[1]);
                else
                    amountAvgSemester = 0;

                if (amountAvgYearStr.size() == 2)
                    amountAvgYear = Float.parseFloat(htmlParsedAvgECTS[1].split(": ")[1]);
                else
                    amountAvgYear = 0;

                if (amountECTSStr.size() == 2)
                    amountECTS = Integer.parseInt(htmlParsedAvgECTS[2].split(": ")[1]);
                else
                    amountECTS = 0;

                saveNewMarksCacheToJSON(c, currentSemester);

                status = 0;
            }
        } else {
            status = -1;
        }
    }

    private void prepareCacheDataFromJSON(Context c, int currentSemester) throws Exception {
        String filenameMarks = Storage.getUniversityStatusHash() + "_m_" + currentSemester + ".json";
        File filePM = new File(c.getFilesDir() + "/" + filenameMarks);

        if (filePM.exists()){
            StringBuilder jsonFromMarks = new StringBuilder();
            FileInputStream inputStream = c.openFileInput(filenameMarks);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null) {
                jsonFromMarks.append(line);
            }
            r.close();
            inputStream.close();

            JSONObject jsonObjectMarks = new JSONObject(jsonFromMarks.toString());
            Iterator<?> jsonKeys = jsonObjectMarks.keys();

            // iterate and add subjects from JSON file
            while (jsonKeys.hasNext()){
                String key = (String) jsonKeys.next();
                databaseOldMarks.put(key, jsonObjectMarks.getString(key));
            }
        }
    }

    private void saveNewMarksCacheToJSON(Context c, int currentSemester) throws Exception {
        String filename = Storage.getUniversityStatusHash() + "_m_" + currentSemester + ".json";
        File file = new File(c.getFilesDir() + "/" + filename);

        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, String> entry : databaseNewMarks.entrySet()){
            jsonObject.put(entry.getKey(), entry.getValue());
        }

        // save to file
        PrintWriter out = new PrintWriter(file);
        out.print(jsonObject.toString());
        out.close();
    }

    public List<List<String>> getHeaders(){
        List<List<String>> db = new ArrayList<>();
        List<String> db2;

        for (LabelAndList<LabelAndList<String>> current : database) {
            db2 = new ArrayList<>();
            db2.add(current.getLabel());
            for (LabelAndList<String> current2 : current.getList()) {
                // If ECTS != 0 then get ECTS and get FinalMark
                if (!current2.getList().get(10).equals("0")) {
                    db2.add(current2.getList().get(10));
                    // If mark exists...
                    if (current2.getList().get(3).length() > 1 && current2.getList().get(3).contains(" "))
                        db2.add(current2.getList().get(3).split(" ")[0]);
                    else if (current2.getList().get(3).length() > 1 && !current2.getList().get(3).contains(" "))
                        db2.add(current2.getList().get(3));
                    else
                        db2.add("");
                    break;
                }
            }
            if (db2.size() == 1){
                db2.add("0");
                db2.add("");
            }
            String examStatus = "no";
            for (LabelAndList<String> current2 : current.getList()) {
                // If isset "Egzamin" set yes and break loop
                if (current2.getList().get(2).equals("Egzamin")) {
                    examStatus = "yes";
                    break;
                }
            }
            db2.add(examStatus);

            String newMarkStatus;
            if (databaseOldMarks.containsKey(current.getLabel())
                    && !databaseOldMarks.get(current.getLabel()).equals(databaseNewMarks.get(current.getLabel())))
                newMarkStatus = "yes";
            else
                newMarkStatus = "no";
            db2.add(newMarkStatus);

            db.add(db2);
        }

        return db;
    }

    public HashMap<String, List<List<String>>> getChildren(){
        HashMap<String, List<List<String>>> db = new HashMap<>();
        List<List<String>> db2;
        List<String> db3;

        for (LabelAndList<LabelAndList<String>> current : database) {
            String subjectTitle = current.getLabel();
            db2 = new ArrayList<>();
            for (LabelAndList<String> current2 : current.getList()) {
                db3 = new ArrayList<>();
                db3.add(current2.getLabel());
                db3.add(current2.getList().get(9));
                for (int i=4; i<=6; i++){
                    if (current2.getList().get(i).length() > 1 && current2.getList().get(i).contains(" "))
                        db3.add(current2.getList().get(i).split(" ")[0] + "\n" + current2.getList().get(i).split(" ")[1]);
                    else if (current2.getList().get(i).length() > 1 && !current2.getList().get(i).contains(" "))
                        db3.add(current2.getList().get(i));
                    else
                        db3.add(" ");
                }
                db3.add(current2.getList().get(7));

                db2.add(db3);
                db.put(subjectTitle, db2);
            }
        }

        return db;
    }
}
