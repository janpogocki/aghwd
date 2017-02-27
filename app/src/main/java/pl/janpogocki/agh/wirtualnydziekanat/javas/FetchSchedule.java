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
    public int status;
    String viewstateName = "__VIEWSTATE";
    String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    String eventValidationName = "__EVENTVALIDATION";
    String viewstateValue, viewstateGeneratorValue, eventValidationValue;

    public FetchSchedule(String _post) {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzin.aspx");
        fww = fw.getWebsite(true, true, _post);
        fwParsed = Jsoup.parse(fww);

        viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
        viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
        eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

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
