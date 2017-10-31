package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Created by Jan on 30.10.2017.
 * Class fetching Teacher Schedule
 */

public class FetchTeacherSchedule {
    public int status;
    public Bitmap bitmapCaptcha;
    private String nameAndSurname;

    // for WU.XP
    private String viewstateName = "__VIEWSTATE";
    private String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    private String eventValidationName = "__EVENTVALIDATION";
    private String viewstateValue, viewstateGeneratorValue, eventValidationValue, teacherID;

    public FetchTeacherSchedule(String nameAndSurname) throws Exception {
        this.nameAndSurname = nameAndSurname;

        while (true){
            if (FetchEaiibSchedule())
                break;

            FetchDziekanatXPSchedule();
            break;
        }
    }

    private void FetchDziekanatXPSchedule() throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzinTok.aspx");
        fww = fw.getWebsiteIsolated(true, true, "");
        fwParsed = Jsoup.parse(fww);

        viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
        viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
        eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

        String captchaURL = fwParsed.getElementsByTag("img").get(0).attr("src");
        FetchWebsite fw2 = new FetchWebsite(Logging.URLdomain + "/" + captchaURL);
        bitmapCaptcha = fw2.getBitmapIsolated(true, true);

        // search teacher in the list
        String revertedNameAndSurname;
        if (nameAndSurname.contains(","))
            revertedNameAndSurname = nameAndSurname.split(",")[0].split(" ")[1] + " " + nameAndSurname.split(",")[0].split(" ")[0];
        else
            revertedNameAndSurname = nameAndSurname.split(" ")[1] + nameAndSurname.split(" ")[0];

        Elements namesWUXP = fwParsed.select("div#ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlProwadzacy_DropDown > div > ul > li");
        int rcbListCounter = 0;
        boolean foundNameAndSurnameOnTheList = false;
        while (rcbListCounter < namesWUXP.size()){
            if (namesWUXP.get(rcbListCounter).ownText().contains(revertedNameAndSurname)) {
                nameAndSurname = namesWUXP.get(rcbListCounter).ownText().trim();
                foundNameAndSurnameOnTheList = true;
                break;
            }

            rcbListCounter++;
        }

        if (!foundNameAndSurnameOnTheList)
            status = -1;
        else {
            // search teacherID no. rcbListCounter
            String[] valuesExploded = fww.split(Pattern.quote("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlProwadzacy_ClientState\",\"collapseAnimation\":\"{\\\"duration\\\":450}\",\"expandAnimation\":\"{\\\"duration\\\":450}\",\"itemData\":[{"))[1].split(Pattern.quote("}]"))[0].split(Pattern.quote("},{"));

            JSONObject jsonObject = new JSONObject("{" + valuesExploded[rcbListCounter] + "}");
            teacherID = jsonObject.getString("value");

            status = -2;
        }
    }

    public void continueFetchDziekanatXPScheduleAfterCaptcha(String captchaText) throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;
        boolean badCaptcha = false;

        // post generator
        POSTgenerator POSTgenerator = new POSTgenerator();

        try {
            POSTgenerator.add(viewstateName, viewstateValue);
            POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
            POSTgenerator.add(eventValidationName, eventValidationValue);

            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlWydzial", "Fizyki i Informatyki Stosowanej, Inżynierii Metali i Informatyki Przemysłowej, Geodezji Górniczej i Inżynierii Środowiska, Elektrotechniki, Automatyki, Informatyki i Elektroniki, Międzywydziałowa Szkoła Energetyki, Inżynierii Mechanicznej i Robotyki, Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej, Centrum AGH UNESCO, Informatyki, Elektroniki i Telekomunikacji, Górnictwa i Geoinżynierii, Matematyki Stosowanej, Geologii, Geofizyki i Ochrony Środowiska, Humanistyczny, Energetyki i Paliw, Metali Nieżelaznych, Odlewnictwa, Inżynierii Materiałowej i Ceramiki, Wiertnictwa, Nafty i Gazu, Międzywydziałowa Szkoła Inżynierii Biomedycznej, Zarządzania, IAESTE AGH");
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlWydzial_ClientState", "{\"logEntries\":[],\"value\":\"10\",\"text\":\"Fizyki i Informatyki Stosowanej, Inżynierii Metali i Informatyki Przemysłowej, Geodezji Górniczej i Inżynierii Środowiska, Elektrotechniki, Automatyki, Informatyki i Elektroniki, Międzywydziałowa Szkoła Energetyki, Inżynierii Mechanicznej i Robotyki, Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej, Centrum AGH UNESCO, Informatyki, Elektroniki i Telekomunikacji, Górnictwa i Geoinżynierii, Matematyki Stosowanej, Geologii, Geofizyki i Ochrony Środowiska, Humanistyczny, Energetyki i Paliw, Metali Nieżelaznych, Odlewnictwa, Inżynierii Materiałowej i Ceramiki, Wiertnictwa, Nafty i Gazu, Międzywydziałowa Szkoła Inżynierii Biomedycznej, Zarządzania, IAESTE AGH\",\"enabled\":true,\"checkedIndices\":[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20],\"checkedItemsTextOverflows\":false}");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlKierunek", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlTyp", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlRodzaj", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlRodzaj", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlForma", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlSemestr", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlDays", "Poniedziałek, Wtorek, Środa, Czwartek, Piątek, Sobota, Niedziela");
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlDays_ClientState", "{\"logEntries\":[],\"value\":\"1\",\"text\":\"Poniedziałek, Wtorek, Środa, Czwartek, Piątek, Sobota, Niedziela\",\"enabled\":true,\"checkedIndices\":[0,1,2,3,4,5,6],\"checkedItemsTextOverflows\":false}");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlPrzedmiot", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlProwadzacy", nameAndSurname);
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlProwadzacy_ClientState", "{\"logEntries\":[],\"value\":\"" + teacherID + "\",\"text\":\"dr hab. Andrzej Adamczak\",\"enabled\":true,\"checkedIndices\":[],\"checkedItemsTextOverflows\":false}");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlSala", "Wszystkie");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$btn_Filtruj", "Wyszukaj zajęcia");
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$captcha$ctl04", captchaText);
        } catch (UnsupportedEncodingException e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzinTok.aspx");
        fww = fw.getWebsiteIsolated(true, true, POSTgenerator.getGeneratedPOST());
        fwParsed = Jsoup.parse(fww);

        Elements tableRows = fwParsed.select(".gridDane");

        List<Appointment> list = new ArrayList<>();

        if (fww.contains("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_lblMessage"))
            badCaptcha = true;
        else {
            for (Element current : tableRows) {
                try {
                    Elements current2 = current.select("td");

                    String dateAndTimeOfStartOfLesson = current2.get(2).ownText().split(" ")[0] + " " + current2.get(3).ownText();
                    String dateAndTimeOfStopOfLesson = current2.get(2).ownText().split(" ")[0] + " " + current2.get(4).ownText();
                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

                    long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
                    long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());
                    String name = current2.get(0).ownText();
                    String description = current2.get(1).ownText() + " (" + current2.get(6).ownText() + ")" + "\n" + current2.get(10).ownText() + ", " + current2.get(11).ownText();
                    String location = current2.get(5).ownText();
                    boolean lecture = false;
                    double group = 0;

                    Appointment appointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, true, -1, group, false);

                    list.add(appointment);
                } catch (ParseException e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
        }

        if (badCaptcha){
            status = -2;
        }
        // If there is no data to show
        else if (list.size() == 0) {
            status = -1;
        }
        else {
            Storage.teacherSchedule = list;
            status = 0;
        }
    }

    private boolean FetchEaiibSchedule() throws Exception {
        String URLdomainEaiibSchedule = "http://planzajec.eaiib.agh.edu.pl";

        FetchWebsite fw;
        String fww;
        Document fwParsed;
        String statusEaiib;

        fw = new FetchWebsite(URLdomainEaiibSchedule + "/view/employee/3");
        fww = fw.getWebsiteHTTP(false, false, "");
        fwParsed = Jsoup.parse(fww);

        String nameAndSurnameToSearch;
        if (nameAndSurname.contains(","))
            nameAndSurnameToSearch = nameAndSurname.split(",")[0];
        else
            nameAndSurnameToSearch = nameAndSurname;

        try {
            statusEaiib = fwParsed.select("select#view-select > option:contains(" + nameAndSurnameToSearch +")").get(0).attr("value");
        } catch (IndexOutOfBoundsException e){
            statusEaiib = "NOT_FOUND";
            Storage.appendCrash(e);
        }

        if (statusEaiib.equals("NOT_FOUND")){
            status = -1;
            return false;
        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            String startYear = String.valueOf(cal.get(Calendar.YEAR) - 1);
            String endYear = String.valueOf(cal.get(Calendar.YEAR) + 1);

            fw = new FetchWebsite(URLdomainEaiibSchedule + "/view/employee/" + statusEaiib + "/events?start=" + startYear + "-01-01&end=" + endYear + "-12-31");
            fww = fw.getWebsiteHTTP(false, false, "");

            JSONArray jsonArray = new JSONArray(fww);
            List<Appointment> list = new ArrayList<>();

            for (int i=0; i<jsonArray.length(); i++){
                String dateAndTimeOfStartOfLesson = jsonArray.getJSONObject(i).getString("start").split("\\+")[0].replace("T", " ");
                String dateAndTimeOfStopOfLesson = jsonArray.getJSONObject(i).getString("end").split("\\+")[0].replace("T", " ");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
                long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());

                String [] descBR = jsonArray.getJSONObject(i).getString("title")
                        .replaceAll("<p class=\"popover-only\">[a-zA-Z0-9 \\-/ęóąśłżźćńĘÓĄŚŁŻŹĆŃ]{1,}</p> ", "").split("<br/>");

                double group = 0;
                boolean lecture = false;
                String location = descBR[1].split(",")[0].replace("Sala: ", "").trim();
                String name, description;

                if (descBR[0].toLowerCase().contains("grupa")){
                    String [] descBR0split = descBR[0].split(", ");

                    name = descBR[0].replace(", " + descBR0split[descBR0split.length-2].trim() + ", " + descBR0split[descBR0split.length-1], "");
                    description = descBR[1].split(", ")[2].trim() + " " + descBR[1].split(", ")[1].replace("prowadzący: ", "").trim() + "\n"
                            + descBR0split[descBR0split.length-2].trim() + ", " + descBR0split[descBR0split.length-1];

                    if (descBR.length > 2)
                        description = description + "\n" + descBR[2].replace("Informacja: ", "").trim();
                }
                else {
                    String [] descBR0split = descBR[0].split(", ");

                    name = descBR[0].replace(", " + descBR0split[descBR0split.length-1], "");
                    description = descBR[1].split(", ")[2].trim() + " " + descBR[1].split(", ")[1].replace("prowadzący: ", "").trim() + "\n"
                            + descBR0split[descBR0split.length-1];

                    if (descBR.length > 2)
                        description = description + "\n" + descBR[2].replace("Informacja: ", "").trim();
                }

                Appointment appointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, true, -1, group, false);
                list.add(appointment);
            }

            // If there is no data to show
            if (list.size() == 0) {
                status = -1;
                return false;
            }
            else {
                Storage.teacherSchedule = list;
                status = 1;
                return true;
            }
        }
    }

}
