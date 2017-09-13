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
 * Created by Jan on 11.09.2017.
 * Returning interpreted table of files
 */

public class FetchFiles {
    private List<List<String>> database = new ArrayList<>();
    public int status;
    private String viewstateName = "__VIEWSTATE";
    private String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    private String eventValidationName = "__EVENTVALIDATION";
    private String eventTargetName = "__EVENTTARGET";

    public FetchFiles(String HTML2interprete, int currentSemester) throws Exception {
        if (Storage.currentFilesDocsHTML.get(currentSemester) != null) {
            database = new ArrayList<>(Storage.currentFilesDocsHTML.get(currentSemester));
            status = 0;
        }
        else {
            Document htmlParsed = Jsoup.parse(HTML2interprete);
            String viewstateValue = htmlParsed.getElementById(viewstateName).attr("value");
            String viewstateGeneratorValue = htmlParsed.getElementById(viewstateGeneratorName).attr("value");
            String eventValidationValue = htmlParsed.getElementById(eventValidationName).attr("value");

            // Check if page with marks is not "brak danych do wyswietlenia"
            if (htmlParsed.getAllElements().select(".gridDane").size() > 0) {
                Elements htmlParsedGridDane = htmlParsed.getAllElements().select(".gridDane");

                // Go over every entry
                for (Element current : htmlParsedGridDane) {
                    String htmlParsedHasDocs = current.select("td").get(5).text();

                    if (htmlParsedHasDocs.contains("Pobierz")) {
                        // Save subject infos, send POST, save returned URL and save returned HTML
                        List<String> oneGroupOfDocs = new ArrayList<>();
                        oneGroupOfDocs.add(current.select("td").get(0).ownText()); //subject name
                        oneGroupOfDocs.add(current.select("td").get(2).ownText()); //teacher
                        oneGroupOfDocs.add(current.select("td").get(7).ownText()); //form of lessons

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
                        FetchWebsite fw = new FetchWebsite(Logging.URLdomain + "/Prowadzacy.aspx");
                        fw.getWebsite(true, true, data);

                        String returnedURL = fw.getLocationHTTP();
                        oneGroupOfDocs.add(returnedURL); // returned URL

                        fw = new FetchWebsite(Logging.URLdomain + returnedURL);
                        String fww = fw.getWebsite(true, true, "");
                        oneGroupOfDocs.add(fww); // returned HTML

                        // save list to main database
                        database.add(oneGroupOfDocs);
                    }
                }

                // If there no data in list...
                if (database.size() == 0) {
                    status = -1;
                } else {
                    // Save database to Storage
                    Storage.currentFilesDocsHTML.put(currentSemester, database);

                    status = 0;
                }
            } else {
                status = -1;
            }
        }
    }

    public List<List<String>> getHeaders(){
        List<List<String>> db = new ArrayList<>();
        List<String> db2;

        for (List<String> current : database) {
            db2 = new ArrayList<>();

            for (int j=0; j<=2; j++)
                db2.add(current.get(j));

            db.add(db2);
        }

        return db;
    }

    public HashMap<String, List<List<String>>> getChildren(){
        HashMap<String, List<List<String>>> db = new HashMap<>();
        List<List<String>> db2;
        List<String> db3;

        for (List<String> current : database) {
            String subjectTitle = current.get(0) + current.get(1) + current.get(2);
            db2 = new ArrayList<>();

            Document htmlParsed = Jsoup.parse(current.get(4));
            String viewstateValue = htmlParsed.getElementById(viewstateName).attr("value");
            String viewstateGeneratorValue = htmlParsed.getElementById(viewstateGeneratorName).attr("value");
            String eventValidationValue = htmlParsed.getElementById(eventValidationName).attr("value");
            Elements htmlParsedGridDane = htmlParsed.getAllElements().select(".gridDane");

            for (Element oneFileTR : htmlParsedGridDane){
                db3 = new ArrayList<>();

                db3.add(oneFileTR.select("td").get(0).text());
                db3.add(oneFileTR.select("td").get(1).ownText());
                db3.add(oneFileTR.select("td").get(2).ownText());
                db3.add(current.get(3));

                String eventTargetValue = oneFileTR.select("td").get(0).select("a").get(0).attr("href")
                        .replace("javascript:__doPostBack('", "").replace("','')", "");

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

                db3.add(POSTgenerator.getGeneratedPOST());
                db2.add(db3);
            }

            db.put(subjectTitle, db2);
        }

        return db;
    }
}