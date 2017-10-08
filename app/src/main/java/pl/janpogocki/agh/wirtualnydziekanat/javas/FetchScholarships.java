package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jan on 07.10.2017.
 * Returning interpreted table of scholarships
 */

public class FetchScholarships {
    public int status;
    private String viewstateName = "__VIEWSTATE";
    private String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    private String eventValidationName = "__EVENTVALIDATION";
    private String eventTargetName = "__EVENTTARGET";

    public FetchScholarships(int setStatus) {
        status = setStatus;
    }

    public FetchScholarships() throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/Stypendia.aspx");
        fww = fw.getWebsite(true, true, "");
        fwParsed = Jsoup.parse(fww);

        String viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
        String viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
        String eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

        // Check if page with marks is not "brak danych do wyswietlenia"
        if (fwParsed.getAllElements().select("#ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_dgDane").size() > 0) {
            Elements htmlParsedGridDane = fwParsed.getAllElements().select("#ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_dgDane > tbody > .gridDane");

            // Go over every entry
            for (Element current : htmlParsedGridDane) {
                List<String> label = new ArrayList<>();
                label.add(current.select("td").get(1).text()); // name
                label.add(current.select("td").get(2).text()); // year

                LabelListAndList<String> oneEntry = new LabelListAndList<>(label);

                String eventTargetValue = current.select("td").get(5).select("a").get(0).attr("href")
                        .replace("javascript:__doPostBack('", "").replace("','')", "");

                // sending post
                POSTgenerator POSTgenerator = new POSTgenerator();
                try {
                    POSTgenerator.add(viewstateName, viewstateValue);
                    POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
                    POSTgenerator.add(eventValidationName, eventValidationValue);
                    POSTgenerator.add(eventTargetName, eventTargetValue);
                } catch (UnsupportedEncodingException e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
                String data = POSTgenerator.getGeneratedPOST();
                FetchWebsite fw2 = new FetchWebsite(Logging.URLdomain + "/Stypendia.aspx");
                String fww2 = fw2.getWebsite(true, true, data);
                Document fw2Parsed = Jsoup.parse(fww2);

                viewstateValue = fw2Parsed.getElementById(viewstateName).attr("value");
                viewstateGeneratorValue = fw2Parsed.getElementById(viewstateGeneratorName).attr("value");
                eventValidationValue = fw2Parsed.getElementById(eventValidationName).attr("value");

                Elements htmlParsedGridDane2 = fw2Parsed.getAllElements().select(".gridDane");

                // Go over every entry if exists
                for (Element current2 : htmlParsedGridDane2) {
                    List<String> listOfMonthAndAmount = new ArrayList<>();

                    listOfMonthAndAmount.add(current2.select("td").get(1).text()); // month
                    listOfMonthAndAmount.add(current2.select("td").get(2).text()); // amount

                    oneEntry.add(listOfMonthAndAmount);
                }

                Storage.scholarships.add(oneEntry);
            }

            // If there no data in list...
            if (Storage.scholarships.size() == 0)
                status = -1;
            else
                status = 0;
        } else {
            status = -1;
        }
    }

    public List<List<String>> getHeaders(){
        List<List<String>> db = new ArrayList<>();
        List<String> db2;

        for (LabelListAndList<String> current : Storage.scholarships) {
            db2 = new ArrayList<>();

            db2.add(current.getLabel().get(0));
            db2.add(current.getLabel().get(1));

            db.add(db2);
        }

        return db;
    }

    public HashMap<String, List<List<String>>> getChildren(){
        HashMap<String, List<List<String>>> db = new HashMap<>();
        List<List<String>> db2;
        List<String> db3;

        for (LabelListAndList<String> current : Storage.scholarships) {
            String subjectTitle = current.getLabel().get(0) + current.getLabel().get(1);
            db2 = new ArrayList<>();

            for (List<String> current2 : current.getList()) {
                db3 = new ArrayList<>();

                db3.add(current2.get(0));
                db3.add(current2.get(1));

                db2.add(db3);
            }

            db.put(subjectTitle, db2);
        }

        return db;
    }
}