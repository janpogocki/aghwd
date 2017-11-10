package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
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
    private Context c;
    private String nameAndSurname;
    private String skosUrl;

    // for WU.XP
    private String viewstateName = "__VIEWSTATE";
    private String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    private String eventValidationName = "__EVENTVALIDATION";
    private String eventValidationValue, viewstateGeneratorValue, teacherID;
    private InputStream viewstateValue;

    public FetchTeacherSchedule(Context c, String nameAndSurname, String skosUrl) throws Exception {
        this.c = c;
        this.nameAndSurname = nameAndSurname;
        this.skosUrl = skosUrl;

        while (true){
            if (FetchEaiibSchedule())
                break;

            if (FetchUniTimeSchedule())
                break;

            FetchDziekanatXPSchedule();
            break;
        }
    }

    private void FetchDziekanatXPSchedule() throws Exception {
        FetchWebsite fw;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzinTok.aspx");
        fw.getWebsiteWUXPTeacherSchedule(true, true, c, 0);
        fwParsed = Jsoup.parse(new File(c.getCacheDir() + "/temp_wuxp.txt"),
                "UTF-8", Logging.URLdomain + "/");

        viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
        eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

        // save viewstateValue to stream (huge data!)
        viewstateValue = new ByteArrayInputStream(fwParsed.getElementById(viewstateName).attr("value").getBytes("UTF-8"));

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
            String[] valuesExploded = fwParsed.html().split(Pattern.quote("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlProwadzacy_ClientState\",\"collapseAnimation\":\"{\\\"duration\\\":450}\",\"expandAnimation\":\"{\\\"duration\\\":450}\",\"itemData\":[{"))[1].split(Pattern.quote("}]"))[0].split(Pattern.quote("},{"));

            JSONObject jsonObject = new JSONObject("{" + valuesExploded[rcbListCounter] + "}");
            teacherID = jsonObject.getString("value");

            status = -2;
        }
    }

    public void continueFetchDziekanatXPScheduleAfterCaptcha(String captchaText) throws Exception {
        FetchWebsite fw;
        Document fwParsed;
        boolean badCaptcha = false;

        // post generator
        BigPOSTgenerator bigPOSTgenerator = new BigPOSTgenerator(c.getCacheDir() + "/temp_big_post_generator.txt");

        try {
            bigPOSTgenerator.add(viewstateName, viewstateValue);
            bigPOSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
            bigPOSTgenerator.add(eventValidationName, eventValidationValue);

            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlWydzial", "Fizyki i Informatyki Stosowanej, Inżynierii Metali i Informatyki Przemysłowej, Geodezji Górniczej i Inżynierii Środowiska, Elektrotechniki, Automatyki, Informatyki i Elektroniki, Międzywydziałowa Szkoła Energetyki, Inżynierii Mechanicznej i Robotyki, Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej, Centrum AGH UNESCO, Informatyki, Elektroniki i Telekomunikacji, Górnictwa i Geoinżynierii, Matematyki Stosowanej, Geologii, Geofizyki i Ochrony Środowiska, Humanistyczny, Energetyki i Paliw, Metali Nieżelaznych, Odlewnictwa, Inżynierii Materiałowej i Ceramiki, Wiertnictwa, Nafty i Gazu, Międzywydziałowa Szkoła Inżynierii Biomedycznej, Zarządzania, IAESTE AGH");
            bigPOSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlWydzial_ClientState", "{\"logEntries\":[],\"value\":\"10\",\"text\":\"Fizyki i Informatyki Stosowanej, Inżynierii Metali i Informatyki Przemysłowej, Geodezji Górniczej i Inżynierii Środowiska, Elektrotechniki, Automatyki, Informatyki i Elektroniki, Międzywydziałowa Szkoła Energetyki, Inżynierii Mechanicznej i Robotyki, Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej, Centrum AGH UNESCO, Informatyki, Elektroniki i Telekomunikacji, Górnictwa i Geoinżynierii, Matematyki Stosowanej, Geologii, Geofizyki i Ochrony Środowiska, Humanistyczny, Energetyki i Paliw, Metali Nieżelaznych, Odlewnictwa, Inżynierii Materiałowej i Ceramiki, Wiertnictwa, Nafty i Gazu, Międzywydziałowa Szkoła Inżynierii Biomedycznej, Zarządzania, IAESTE AGH\",\"enabled\":true,\"checkedIndices\":[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20],\"checkedItemsTextOverflows\":false}");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlKierunek", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlTyp", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlRodzaj", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlRodzaj", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlForma", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlSemestr", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlDays", "Poniedziałek, Wtorek, Środa, Czwartek, Piątek, Sobota, Niedziela");
            bigPOSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlDays_ClientState", "{\"logEntries\":[],\"value\":\"1\",\"text\":\"Poniedziałek, Wtorek, Środa, Czwartek, Piątek, Sobota, Niedziela\",\"enabled\":true,\"checkedIndices\":[0,1,2,3,4,5,6],\"checkedItemsTextOverflows\":false}");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlPrzedmiot", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlProwadzacy", nameAndSurname);
            bigPOSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_ddlProwadzacy_ClientState", "{\"logEntries\":[],\"value\":\"" + teacherID + "\",\"text\":\"dr hab. Andrzej Adamczak\",\"enabled\":true,\"checkedIndices\":[],\"checkedItemsTextOverflows\":false}");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ddlSala", "Wszystkie");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$btn_Filtruj", "Wyszukaj zajęcia");
            bigPOSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$captcha$ctl04", captchaText);
            bigPOSTgenerator.closeFile();
        } catch (UnsupportedEncodingException e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzinTok.aspx");
        fw.getWebsiteWUXPTeacherSchedule(true, true, c, 1);
        fwParsed = Jsoup.parse(new File(c.getCacheDir() + "/temp_wuxp.txt"),
                "UTF-8", Logging.URLdomain + "/");

        Elements tableRows = fwParsed.select(".gridDane");

        List<Appointment> list = new ArrayList<>();

        if (fwParsed.select("span#ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_lblMessage").size() == 1)
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

                    Appointment appointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, false, true, -1, 0, false);

                    list.add(appointment);
                } catch (ParseException e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
        }

        if (badCaptcha){
            status = -3;
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
                        .replaceAll("<p class=\"popover-only\">[a-zA-Z0-9 \\-/ęóąśłżźćńĘÓĄŚŁŻŹĆŃ]+</p> ", "").split("<br/>");

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

    private boolean FetchUniTimeSchedule() throws Exception {
        FetchWebsite fw;
        String fww;
        Document fwParsed;
        String teacherEmail;

        fw = new FetchWebsite(skosUrl);
        fww = fw.getWebsite(false, false, "");
        fwParsed = Jsoup.parse(fww);

        try {
            teacherEmail = fwParsed.select(".email").get(0).attr("data-html");
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        teacherEmail = new StringBuilder(teacherEmail
                .replace("#", "@")
                .replace("&lt;", "<")
                .replace("&gt;", ">")).reverse().toString()
                .replace("<a href=\"mailto:", "")
                .split("\" class")[0];

        try {
            fw = new FetchWebsite("https://plan.agh.edu.pl/UniTime/export?output=meetings.csv&type=person&ext=" + teacherEmail + "&sort=1&term=" + ScheduleUtils.getSemesterUniTimeName());
            fww = fw.getWebsiteGETSecure(false, false, "");
        } catch (Exception e){
            return false;
        }

        // parse CSV with timetable
        String [] csvHeader = {"Name","Section","Type","Title","Date","Published Start","Published End","Location","Capacity","Instructor / Sponsor","Email","Requested Services","Approved"};
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(csvHeader);
        Reader reader = new StringReader(fww);
        CSVParser csvParser = new CSVParser(reader, csvFormat);

        List<Appointment> list = new ArrayList<>();
        List<CSVRecord> csvRecords = csvParser.getRecords();
        csvRecords.remove(0);
        for (CSVRecord record : csvRecords){
            String dateAndTimeOfStartOfLesson = record.get("Date") + " " + record.get("Published Start");
            String dateAndTimeOfStopOfLesson = record.get("Date") + " " + record.get("Published End");
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
            df.setLenient(true);

            long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
            long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());
            String name = record.get("Title").replace("\n", "");
            String description = "Grupa " + record.get("Section") + ", " + record.get("Type").replace("\n", "") + "\n" + record.get("Instructor / Sponsor");
            String location = record.get("Location").replace("\n", "");

            Appointment appointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, false, true, -1, 0, false);

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
