package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jan on 19.02.2017.
 * Returning interpreted table of partial marks
 */

public class FetchPartialMarks {
    private List<LabelAndList<LabelAndList<List<String>>>> database = new ArrayList<>();
    public int status;
    private String tableName = "ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_rg_Przedmioty_ctl00";

    public FetchPartialMarks(List<String> HTML2interprete){
        if (HTML2interprete == null || HTML2interprete.isEmpty() || HTML2interprete.get(0).equals("0"))
            status = -1;
        else {
            status = -1;

            for (String subpage : HTML2interprete){
                Document htmlParsed = Jsoup.parse(subpage);
                Elements htmlParsedTableTbodyTR = htmlParsed.select("#" + tableName + " > tbody > tr");

                // Go over every entry
                for (int i = 0; i < htmlParsedTableTbodyTR.size(); i = i+2){
                    LabelAndList<List<String>> db2 = null;

                    // tr with subject and lesson name
                    Elements subjectAndLessons = htmlParsedTableTbodyTR.get(i).getElementsByTag("td");
                    String subjectName = subjectAndLessons.get(1).ownText();
                    String lessonName = subjectAndLessons.get(2).ownText();

                    // Fetch marks in table
                    Elements marksTR = htmlParsedTableTbodyTR.get(i+1).getElementsByTag("td").get(1)
                            .getElementsByTag("div").get(0).select(".rgMasterTable > tbody > tr");

                    // Check if there are marks
                    if (marksTR.get(0).select(".rgNoRecords").first() == null){
                        db2 = new LabelAndList<>(lessonName);

                        for (Element oneTrElement : marksTR){
                            List<String> db3 = new ArrayList<>();
                            db3.add(oneTrElement.getElementsByTag("td").get(1).ownText()); // nazwa
                            db3.add(oneTrElement.getElementsByTag("td").get(2).ownText()); // ocena
                            db3.add(oneTrElement.getElementsByTag("td").get(3).ownText()); // data
                            db3.add(oneTrElement.getElementsByTag("td").get(4).ownText()); // prowadzacy
                            db3.add(oneTrElement.getElementsByTag("td").get(6).ownText()); // uwagi

                            db2.add(db3);
                        }

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

    public List<List<String>> getHeaders(){
        List<List<String>> db = new ArrayList<>();
        List<String> db2;

        for (LabelAndList<LabelAndList<List<String>>> current : database) {
            db2 = new ArrayList<>();
            db2.add(current.getLabel());
            db2.add(current.getList().get(0).getLabel());
            db.add(db2);
        }

        return db;
    }

    public HashMap<String, List<List<String>>> getChildren(){
        HashMap<String, List<List<String>>> db = new HashMap<>();
        List<List<String>> db2;
        List<String> db3;

        for (LabelAndList<LabelAndList<List<String>>> current : database) {
            String subjectTitle = current.getLabel() + current.getList().get(0).getLabel();
            db2 = new ArrayList<>();
            for (List<String> current2 : current.getList().get(0).getList()) {
                db3 = new ArrayList<>();

                for (int i=0; i<5; i++){
                    db3.add(current2.get(i));
                }

                db2.add(db3);
                db.put(subjectTitle, db2);
            }
        }

        return db;
    }
}