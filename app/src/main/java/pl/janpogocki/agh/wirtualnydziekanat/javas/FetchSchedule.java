package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 27.02.2017.
 * Class fetching Schedule
 */

public class FetchSchedule {
    public static final String URLdomainEaiibSchedule = "http://planzajec.eaiib.agh.edu.pl";
    public int status;
    private String viewstateName = "__VIEWSTATE";
    private String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    private String eventValidationName = "__EVENTVALIDATION";
    private String viewstateValue, viewstateGeneratorValue, eventValidationValue;

    public FetchSchedule(String _post) throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzin.aspx");
        fww = fw.getWebsite(true, true, _post);
        fwParsed = Jsoup.parse(fww);

        viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
        viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
        eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

        // Check dates
        Elements datesRange = fwParsed.select("#ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_lblData");
        if (datesRange.size() == 1)
            Storage.scheduleDates = datesRange.get(0).ownText().replace("od: ", "").replace(" do: ", " - ");

        Elements tableRows = fwParsed.select(".gridDane");

        List<List<String>> list = new ArrayList<>();
        for (Element current : tableRows){
            Elements current2 = current.select("td");
            List<String> list2 = new ArrayList<>();

            for (Element current3 : current2){
                list2.add(current3.ownText());
            }

            list.add(list2);
        }

        // If there is no data to show
        if (list.size() == 0) {
            status = -1;

            if (Storage.universityStatus == null || Storage.universityStatus.size() == 0){
                new FetchUniversityStatus(false);
            }

            if (Storage.universityStatus != null && Storage.universityStatus.size() > 6 && Storage.universityStatus.get(1).contains("Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej")){
                // find EAIIB's schedule
                fw = new FetchWebsite(URLdomainEaiibSchedule + "/");
                fww = fw.getWebsiteHTTP(false, false, "");
                fwParsed = Jsoup.parse(fww);

                String kierunek = Storage.universityStatus.get(2);
                String specjalnosc = Storage.universityStatus.get(3);
                String formaStudiow = Storage.universityStatus.get(4).substring(0,1).toUpperCase() + Storage.universityStatus.get(4).substring(1);
                String poziomStudiow = Storage.universityStatus.get(5);

                String statusStr = "2";

                try {
                    if (poziomStudiow.contains("pierwszego") && kierunek.contains("Elektrotechnika") && specjalnosc.length() > 0)
                        statusStr = statusStr + fwParsed.select("select#view-select > option:matches(" + kierunek + " - " + formaStudiow + " - Studia I stopnia - Blok obieralny " + specjalnosc.replaceAll("\"", "").substring(specjalnosc.replaceAll("\"", "").length() - 1) + " - [a-zA-Z]{0,} [0-9]{4}\\/[0-9]{4}\\/S" + Storage.getSemesterNumberById(Storage.summarySemesters.size() - 1) + ")").get(0).attr("value");
                    else if (poziomStudiow.contains("pierwszego"))
                        statusStr = statusStr + fwParsed.select("select#view-select > option:matches(" + kierunek + " - " + formaStudiow + " - Studia I stopnia - [a-zA-Z]{0,} [0-9]{4}\\/[0-9]{4}\\/S" + Storage.getSemesterNumberById(Storage.summarySemesters.size() - 1) + ")").get(0).attr("value");
                    else
                        statusStr = statusStr + fwParsed.select("select#view-select > option:matches(" + kierunek + " - " + specjalnosc + " - " + formaStudiow + " - Studia II stopnia - [a-zA-Z]{0,} [0-9]{4}\\/[0-9]{4}\\/S" + Storage.getSemesterNumberById(Storage.summarySemesters.size() - 1) + ")").get(0).attr("value");
                } catch (IndexOutOfBoundsException e){
                    statusStr = "2000";
                    Storage.appendCrash(e);
                }

                status = Integer.parseInt(statusStr);
            }
        }
        else {
            Storage.schedule = list;
            status = 0;
        }
    }

    public String getEventValidationValue() {
        return eventValidationValue;
    }

    public String getViewstateGeneratorValue() {
        return viewstateGeneratorValue;
    }

    public String getViewstateValue() {
        return viewstateValue;
    }
}
