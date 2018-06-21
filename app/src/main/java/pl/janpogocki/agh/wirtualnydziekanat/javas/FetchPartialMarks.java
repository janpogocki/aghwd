package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Jan on 19.02.2017.
 * Returning interpreted table of partial marks
 */

public class FetchPartialMarks {
    public int status;
    private List<LabelAndList<LabelAndList<List<String>>>> database = new ArrayList<>();
    private List<PartialMark> listOfJsonPartialMarks = new ArrayList<>();

    public FetchPartialMarks(Context c, List<String> HTML2interprete, int currentSemester) throws Exception {
        if (HTML2interprete == null || HTML2interprete.isEmpty() || HTML2interprete.get(0).equals("0"))
            status = -1;
        else {
            status = -1;

            if (Storage.universityStatus == null || Storage.universityStatus.size() == 0){
                new FetchUniversityStatus(false);
            }

            database = new ArrayList<>();
            listOfJsonPartialMarks = new ArrayList<>();
            try {
                prepareDataFromJSON(c, currentSemester);
            } catch (Exception e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }

            Storage.currentSemesterPartialMarksSubjects = new HashMap<>();

            for (String subpage : HTML2interprete){
                Document htmlParsed = Jsoup.parse(subpage);
                String tableName = "ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_rg_Przedmioty_ctl00";
                Elements htmlParsedTableTbodyTR = htmlParsed.select("#" + tableName + " > tbody > tr");

                // Go over every entry
                for (int i = 0; i < htmlParsedTableTbodyTR.size(); i = i+2){
                    LabelAndList<List<String>> db2;

                    // tr with subject and lesson name
                    Elements subjectAndLessons = htmlParsedTableTbodyTR.get(i).getElementsByTag("td");
                    String subjectName = subjectAndLessons.get(1).ownText();
                    String lessonName = subjectAndLessons.get(2).ownText();

                    // add subjectName to Storage if not exists
                    if (!Storage.currentSemesterPartialMarksSubjects.keySet().contains(subjectName)) {
                        Storage.currentSemesterPartialMarksSubjects.put(subjectName, new ArrayList<String>());
                    }

                    // add lessonName to key
                    Storage.currentSemesterPartialMarksSubjects.get(subjectName).add(lessonName);

                    // Fetch marks in table
                    Elements marksTR = htmlParsedTableTbodyTR.get(i+1).getElementsByTag("td").get(1)
                            .getElementsByTag("div").get(0).select(".rgMasterTable > tbody > tr");

                    db2 = new LabelAndList<>(lessonName);

                    // Check if there are marks
                    // todo: more than 10 marks in subject creates subpage
                    if (marksTR.size() > 0 && marksTR.get(0).select(".rgNoRecords").first() == null){
                        for (Element oneTrElement : marksTR){
                            List<String> db3 = new ArrayList<>();
                            db3.add(oneTrElement.getElementsByTag("td").get(1).ownText()); // nazwa
                            db3.add(oneTrElement.getElementsByTag("td").get(2).ownText()); // ocena
                            db3.add(oneTrElement.getElementsByTag("td").get(3).ownText()); // data
                            db3.add(oneTrElement.getElementsByTag("td").get(4).ownText()); // prowadzacy
                            db3.add(oneTrElement.getElementsByTag("td").get(6).ownText()); // uwagi
                            db3.add("agh_mark"); // wyroznik agh_mark
                            db3.add(subjectName); // przedmiot
                            db3.add(lessonName); // typ zajec
                            db3.add(String.valueOf(currentSemester)); // obecny semestr

                            db2.add(db3);
                        }
                    }

                    // add marks from json
                    if (listOfJsonPartialMarks != null && listOfJsonPartialMarks.size() > 0) {
                        for (PartialMark current : listOfJsonPartialMarks) {
                            if (current.subjectName.equals(subjectName + lessonName)) {
                                long currentTimestamp = current.timestamp + TimeZone.getDefault().getOffset(current.timestamp);
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(currentTimestamp);
                                String currentDate = String.format(Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

                                List<String> db3 = new ArrayList<>();
                                db3.add(current.title); // nazwa
                                db3.add(current.mark); // ocena
                                db3.add(currentDate); // data
                                db3.add(""); // prowadzacy
                                db3.add(current.description); // uwagi
                                db3.add("user_mark"); // wyroznik agh_mark
                                db3.add(subjectName); // przedmiot
                                db3.add(lessonName); // typ zajec
                                db3.add(String.valueOf(currentSemester)); // obecny semestr

                                db2.add(db3);
                            }
                        }
                    }

                    // add to database if db2.size() > 0 => there is some marks
                    if (db2.getList().size() > 0){
                        LabelAndList<LabelAndList<List<String>>> db1 = new LabelAndList<>(subjectName);
                        db1.add(db2);
                        database.add(db1);
                        status = 0;
                    }
                }
            }

            if (database.isEmpty())
                status = -1;
        }
    }

    private void prepareDataFromJSON(Context c, int currentSemester) throws Exception {
        // import and sort data from json
        String filenamePM = Storage.getUniversityStatusHash() + "_pm_" + currentSemester + ".json";
        File filePM = new File(c.getFilesDir() + "/" + filenamePM);

        if (filePM.exists()){
            StringBuilder jsonFromPM = new StringBuilder();
            FileInputStream inputStream = c.openFileInput(filenamePM);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null) {
                jsonFromPM.append(line);
            }
            r.close();
            inputStream.close();

            JSONArray jsonArrayPM = new JSONArray(jsonFromPM.toString());

            // iterate and add marks
            for (int i=0; i<jsonArrayPM.length(); i++){
                String id = jsonArrayPM.getJSONObject(i).getString("id");
                String mark = jsonArrayPM.getJSONObject(i).getString("mark");
                String title = jsonArrayPM.getJSONObject(i).getString("title");
                long timestamp = jsonArrayPM.getJSONObject(i).getLong("timestamp");
                String description = jsonArrayPM.getJSONObject(i).getString("description");

                PartialMark currentPartialMark = new PartialMark(mark, title, id, "", timestamp, description, String.valueOf(currentSemester));

                listOfJsonPartialMarks.add(currentPartialMark);
            }

            // sort marks by timestamp
            Collections.sort(listOfJsonPartialMarks, new Comparator<PartialMark>() {
                @Override
                public int compare(final PartialMark object1, final PartialMark object2) {
                    return Long.valueOf(object1.timestamp).compareTo(object2.timestamp);
                }
            });
        }
    }

    public List<List<String>> getHeaders(){
        List<List<String>> db = new ArrayList<>();
        List<String> db2;

        for (LabelAndList<LabelAndList<List<String>>> current : database) {
            db2 = new ArrayList<>();

            try {
                db2.add(current.getLabel());
                db2.add(current.getList().get(0).getLabel());
                db.add(db2);
            } catch (Exception e){
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }
        }

        return db;
    }

    public HashMap<String, List<List<String>>> getChildren(){
        HashMap<String, List<List<String>>> db = new HashMap<>();
        List<List<String>> db2;
        List<String> db3;

        for (LabelAndList<LabelAndList<List<String>>> current : database) {
            try {
                String subjectTitle = current.getLabel() + current.getList().get(0).getLabel();
                db2 = new ArrayList<>();
                for (List<String> current2 : current.getList().get(0).getList()) {
                    db3 = new ArrayList<>();

                    db3.addAll(current2);

                    db2.add(db3);
                    db.put(subjectTitle, db2);
                }
            } catch (Exception e){
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }
        }

        return db;
    }
}