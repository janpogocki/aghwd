package pl.janpogocki.agh.wirtualnydziekanat.javas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 15.07.2016.
 * Logging to Wirtualna Uczelnia class
 */
public class Logging {
    public int status = -1;
    public static String URLdomain = "https://dziekanat.agh.edu.pl";
    private String viewstateName = "__VIEWSTATE";
    private String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    private String eventValidationName = "__EVENTVALIDATION";
    private String viewstateValue, viewstateGeneratorValue, eventValidationValue;
    private String tempAlbumNumber = "";

    // establish connetion, send POST data and get session cookies
    public Logging(String albumNumber, String password) throws Exception {
        String URLaddress = URLdomain + "/Logowanie2.aspx";
        String albumNumberName = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtIdent";
        String passwordName = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtHaslo";
        String ktoName = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$rbKto";
        String buttonLogujName = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$butLoguj";

        // Get VIEWSTATE & VIEWSTATEGENERATOR
        FetchWebsite fw = new FetchWebsite(URLaddress);
        String fwString = fw.getWebsite(false, true, "");
        Document fwParsed = Jsoup.parse(fwString);
        viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
        viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");

        // Create data variable for sent values to server
        POSTgenerator POSTgenerator = new POSTgenerator();
        POSTgenerator.add(albumNumberName, albumNumber);
        POSTgenerator.add(passwordName, password);
        POSTgenerator.add(ktoName, "student");
        POSTgenerator.add(buttonLogujName, "Zaloguj");
        POSTgenerator.add(viewstateName, viewstateValue);
        POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
        String data = POSTgenerator.getGeneratedPOST();

        // Connect to login
        fw = new FetchWebsite(URLaddress);
        String fww = fw.getWebsite(true, true, data);

        // If everything is OK, then we are parsing Wynik2.aspx to get info's
        if (fw.getLocationHTTP().equals("/Ogloszenia.aspx")){
            tempAlbumNumber = albumNumber;
            getUserInfos();
        }
        else if (fw.getLocationHTTP().equals("/KierunkiStudiow.aspx")) {
            Storage.multiKierunekClear();

            fw = new FetchWebsite(URLdomain + fw.getLocationHTTP());
            fww = fw.getWebsite(true, true, "");

            // Parsing HTML and save to list all study subjects and theirs id's
            fwParsed = Jsoup.parse(fww);
            for (int i=0; i>=0; i++){
                if (fwParsed.select("#ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_rbKierunki_" + i).size() > 0){
                    String kierunekValue = fwParsed.getElementById("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_rbKierunki_" + i).attr("value");
                    String kierunekLabelName = fwParsed.getElementsByTag("label").get(i).ownText();

                    Storage.multiKierunekValues.add(kierunekValue);
                    Storage.multiKierunekLabelNames.add(kierunekLabelName);
                }
                else
                    break;
            }

            // Gather data from hidden inputs
            viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
            viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
            eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

            status = 1;
            Storage.multiKierunek = true;
        }
    }

    // Function gathers all infos about student and latest marks and sets var status
    private void getUserInfos() throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(URLdomain + "/Wynik2.aspx");
        fww = fw.getWebsite(true, true, "");

        fwParsed = Jsoup.parse(fww);
        Storage.nameAndSurname = fwParsed.select("#ctl00_ctl00_ContentPlaceHolder_wumasterWhoIsLoggedIn").get(0).ownText().split(" – nr albumu: ")[0];
        Storage.albumNumber = tempAlbumNumber;
        Storage.photoUserURL = URLdomain + "/" + fwParsed.getElementsByTag("img").attr("src");
        Storage.peselNumber = fwParsed.select(".tabDwuCzesciowaPLeft").get(4).ownText();

        // Jump to profile photo
        fw = new FetchWebsite(Storage.photoUserURL);
        Storage.photoUser = fw.getBitmap(true, true);

        // Count semesters at PrzebiegStudiow.aspx
        fw = new FetchWebsite(URLdomain + "/PrzebiegStudiow.aspx");
        fww = fw.getWebsite(true, true, "");
        fwParsed = Jsoup.parse(fww);
        Elements tableRows = fwParsed.select(".gridDane");

        List<List<String>> list = new ArrayList<>();
        for (Element current : tableRows){
            Elements current2 = current.select("td");
            List<String> list2 = new ArrayList<>();

            for (Element current3 : current2){
                String current3ownTextTrimmed = current3.ownText().trim();
                if (current3ownTextTrimmed.equals(""))
                    list2.add("-");
                else
                    list2.add(current3ownTextTrimmed);
            }

            // Add if student status is different from absolwent
            if (!list2.get(7).toLowerCase().contains("absolwent"))
                list.add(list2);
        }

        // Checks whether list is not empty
        if (list.size() > 0){
            Storage.currentSemester = Storage.currentSemesterListPointer = Storage.currentSemesterListPointerPartialMarks = tableRows.size()-1;
            Storage.summarySemesters = list;

            // Jump to OcenyP.aspx, get info latest marks
            fw = new FetchWebsite(URLdomain + "/OcenyP.aspx");
            fww = fw.getWebsite(true, true, "");
            Storage.currentSemesterHTML.put(Storage.currentSemester, fww);

            status = 0;
        }
        else {
            status = -2;
        }
    }

    // Function called when multiKierunek exists
    public void loggingAfterKierunekChoice() throws Exception {
        String choosenMultiKierunekName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$rbKierunki";
        String choosenMultiKierunekButtonName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$Button1";
        String choosenMultiKierunekButtonValue = "Wybierz";

        // Create data variable for sent values to server
        POSTgenerator POSTgenerator = new POSTgenerator();
        POSTgenerator.add(choosenMultiKierunekName, Storage.choosenMultiKierunekValue);
        POSTgenerator.add(choosenMultiKierunekButtonName, choosenMultiKierunekButtonValue);
        POSTgenerator.add(viewstateName, viewstateValue);
        POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
        POSTgenerator.add(eventValidationName, eventValidationValue);
        String data = POSTgenerator.getGeneratedPOST();

        // Send POST to KierunkiStudiow.aspx
        FetchWebsite fw = new FetchWebsite(URLdomain + "/KierunkiStudiow.aspx");
        String fww = fw.getWebsite(true, true, data);

        // Get Wynik2.aspx and get another required info
        getUserInfos();
    }
}


