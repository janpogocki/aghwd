package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 24.02.2017.
 * Class fetching Diploma
 */

public class FetchDiploma {
    public int status;

    public FetchDiploma() throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/PracaDyp.aspx");
        fww = fw.getWebsite(true, true, "");
        fwParsed = Jsoup.parse(fww);
        Elements tableRows1 = fwParsed.select(".tabDwuCzesciowaLLeft");
        Elements tableRows2 = fwParsed.select(".tabDwuCzesciowaPLeft");

        List<List<String>> list = new ArrayList<>();

        // 1 kolumna
        Boolean added = false;
        List<String> list2 = new ArrayList<>();
        for (Element current : tableRows1) {
            list2.add(current.ownText());
            added = true;
        }

        if (added)
            list.add(list2);

        // 2 kolumna
        added = false;
        list2 = new ArrayList<>();
        for (Element current : tableRows2) {
            list2.add(current.ownText());
            added = true;
        }

        if (added)
            list.add(list2);

        // If there is no data to show
        if (list.size() == 0)
            status = -1;
        else {
            Storage.diploma = list;
            status = 0;
        }
    }
}
