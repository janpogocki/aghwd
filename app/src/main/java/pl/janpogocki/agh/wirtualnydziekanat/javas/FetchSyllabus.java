package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Jan on 23.02.2017.
 * Class fetching AGH Syllabus
 */

public class FetchSyllabus {
    public static String URLdomainSyllabus = "http://syllabuskrk.agh.edu.pl";

    public FetchSyllabus() {
        FetchWebsite fw;
        String fww, rokRozpoczecia, nazwaWydzialu, kierunek, specjalizacja, level;
        Document fwParsed;

        rokRozpoczecia = Storage.summarySemesters.get(0).get(0).replaceAll("/", "-").trim();
        nazwaWydzialu = Storage.universityStatus.get(1);
        kierunek = Storage.universityStatus.get(2);
        specjalizacja = Storage.universityStatus.get(3);
        level = Storage.universityStatus.get(5);

        fw = new FetchWebsite(FetchSyllabus.URLdomainSyllabus + "/" + rokRozpoczecia + "/pl/treasuries/academy_units/offer");
        fww = fw.getWebsiteHTTP(false, false, "");

        // Check website existence
        if (fw.getResponseCode() / 100 == 2){
            fwParsed = Jsoup.parse(fww);
            String linkDepartment = FetchSyllabus.URLdomainSyllabus + fwParsed.select("a.department-link.table-item-link:contains(" + nazwaWydzialu + ")").get(0).attr("href");

            fw = new FetchWebsite(linkDepartment);
            fww = fw.getWebsiteHTTP(false, false, "");

            fwParsed = Jsoup.parse(fww);

            if (level.contains("pierwszego"))
                Storage.syllabusURL = FetchSyllabus.URLdomainSyllabus + fwParsed.select("a.table-item-link:contains(" + kierunek + ")").get(0).attr("href");
            else
                Storage.syllabusURL = FetchSyllabus.URLdomainSyllabus + fwParsed.select("a.table-item-link:contains(" + kierunek + " - " + specjalizacja + ")").get(0).attr("href");
        }
    }
}
