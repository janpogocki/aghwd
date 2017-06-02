package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Jan on 23.02.2017.
 * Class fetching AGH Syllabus
 */

public class FetchSyllabus {
    public static String URLdomainSyllabus = "https://www.syllabus.agh.edu.pl";

    public FetchSyllabus() {
        FetchWebsite fw;
        String fww, rokRozpoczecia, nazwaWydzialu, kierunek, specjalizacja, typeOfStudy, level;
        Document fwParsed;

        Storage.syllabusURL = Storage.syllabusURLlinkDepartment = "";

        rokRozpoczecia = Storage.summarySemesters.get(0).get(0).replaceAll("/", "-").trim();
        nazwaWydzialu = Storage.universityStatus.get(1);
        kierunek = Storage.universityStatus.get(2).toLowerCase();
        specjalizacja = Storage.universityStatus.get(3).toLowerCase();
        typeOfStudy = Storage.universityStatus.get(4).toLowerCase();
        level = Storage.universityStatus.get(5).toLowerCase();

        fw = new FetchWebsite(URLdomainSyllabus + "/" + rokRozpoczecia + "/pl/treasuries/academy_units/offer");
        fww = fw.getWebsiteSyllabus(false, false, "");

        // Check website existence
        if (fw.getResponseCode() / 100 == 2){
            fwParsed = Jsoup.parse(fww);
            String linkDepartment = URLdomainSyllabus + fwParsed.select("a.department-link.table-item-link:containsOwn(" + nazwaWydzialu + ")").get(0).attr("href");

            fw = new FetchWebsite(linkDepartment);
            fww = fw.getWebsiteSyllabus(false, false, "");

            if (fw.getResponseCode() / 100 == 2) {
                Storage.syllabusURLlinkDepartment = linkDepartment;
                fwParsed = Jsoup.parse(fww.toLowerCase());

                // choose type of studies
                int studyType;
                if (typeOfStudy.contains("niestacjonarne"))
                    studyType = 1;
                else
                    studyType = 0;

                if (level.contains("pierwszego"))
                    Storage.syllabusURL = URLdomainSyllabus + fwParsed.select("a.table-item-link:matches(" + kierunek + "(?! -))").get(studyType).attr("href");
                else if (specjalizacja.length() > 0)
                    Storage.syllabusURL = URLdomainSyllabus + fwParsed.select("a.table-item-link:matches(" + kierunek + " - " + specjalizacja + ")").get(studyType).attr("href");
            }
        }
    }
}
