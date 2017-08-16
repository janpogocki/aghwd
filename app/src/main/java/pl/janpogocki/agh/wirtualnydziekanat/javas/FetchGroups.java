package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 21.09.2016.
 * Class fetching Groups and Modules
 */

public class FetchGroups {
    public int status;

    public FetchGroups() throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/ModulyGrupy.aspx");
        fww = fw.getWebsite(true, true, "");
        fwParsed = Jsoup.parse(fww);
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
        if (list.size() == 0)
            status = -1;
        else {
            Storage.groupsAndModules = list;
            status = 0;
        }
    }
}
