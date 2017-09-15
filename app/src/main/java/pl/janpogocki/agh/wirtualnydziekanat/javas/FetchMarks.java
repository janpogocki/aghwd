package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jan on 21.07.2016.
 * Returning interpreted table with marks
 */

public class FetchMarks {
    private List<LabelAndList<LabelAndList<String>>> database = new ArrayList<>();
    public int amountECTS, status;
    public float amountAvgSemester, amountAvgYear;

    public FetchMarks(String HTML2interprete){
        Document htmlParsed = Jsoup.parse(HTML2interprete);

        // Check if page with marks is not "brak danych do wyswietlenia"
        if (htmlParsed.getAllElements().select(".gridDane").size() > 0) {
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
                if (subjectExists != -1) {
                    database.get(subjectExists).add(marks);
                } else {
                    LabelAndList<LabelAndList<String>> subjectAndLesson = new LabelAndList<>(htmlParsedSubjectName);
                    subjectAndLesson.add(marks);
                    database.add(subjectAndLesson);
                }
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

                status = 0;
            }
        } else {
            status = -1;
        }
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
