package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 23.02.2017.
 * Class fetching university status - tok studiów
 */

public class FetchUniversityStatus {
    public int status;

    public FetchUniversityStatus(Boolean _fetchSyllabus) throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/TokStudiow.aspx");
        fww = fw.getWebsite(true, true, "");
        fwParsed = Jsoup.parse(fww);
        Elements tableRows = fwParsed.select(".tabDwuCzesciowaPLeft");

        List<String> list = new ArrayList<>();
        for (Element current : tableRows){
            list.add(current.text());
        }

        // If there is no data to show
        if (list.size() == 0)
            status = -1;
        else {
            Storage.universityStatus = list;
            status = 0;
        }

        if (_fetchSyllabus)
            new FetchSyllabus();
    }
}
