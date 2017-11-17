package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

/**
 * Created by Jan on 27.02.2017.
 * Class fetching Schedule
 */

public class FetchSchedule {
    public int status;

    public FetchSchedule() throws Exception {
        if (Storage.universityStatus == null || Storage.universityStatus.size() == 0){
            new FetchUniversityStatus(false);
        }

        if (Storage.schedule == null || Storage.schedule.size() == 0) {
            if (Storage.universityStatus.get(1).contains("Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej")) {
                FetchEaiibSchedule();
            } else if (Storage.universityStatus.get(1).contains("Humanistyczny")
                    || Storage.universityStatus.get(1).contains("Energetyki i Paliw")
                    || Storage.universityStatus.get(1).contains("Fizyki i Informatyki Stosowanej")
                    || Storage.universityStatus.get(1).contains("Geodezji Górniczej i Inżynierii Środowiska")
                    || Storage.universityStatus.get(1).contains("Geologii, Geofizyki i Ochrony Środowiska")) {
                FetchUniTimeSchedule();
            } else {
                FetchDziekanatXPSchedule();
            }
        }
    }

    private void FetchDziekanatXPSchedule() throws Exception {
        String viewstateName = "__VIEWSTATE";
        String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
        String eventValidationName = "__EVENTVALIDATION";
        String viewstateValue, viewstateGeneratorValue, eventValidationValue;

        FetchWebsite fw;
        String fww;
        Document fwParsed;

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzin.aspx");
        fww = fw.getWebsite(true, true, "");
        fwParsed = Jsoup.parse(fww);

        viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
        viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
        eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

        // post generator
        POSTgenerator POSTgenerator = new POSTgenerator();

        try {
            // dates
            Calendar cal = Calendar.getInstance();
            String todayDay = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            todayDay = (todayDay.length()==1 ? ("0"+todayDay):(todayDay));

            String todayMonth = String.valueOf(cal.get(Calendar.MONTH)+1);
            todayMonth = (todayMonth.length()==1 ? ("0"+todayMonth):(todayMonth));

            String todayYear = String.valueOf(cal.get(Calendar.YEAR));

            POSTgenerator.add(viewstateName, viewstateValue);
            POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
            POSTgenerator.add(eventValidationName, eventValidationValue);

            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$rbJak", "Semestralnie");

            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$radDataOd", todayYear + "-" + todayMonth + "-" + todayDay);
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$radDataOd$dateInput", todayDay + "." + todayMonth + "." + todayYear);
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_radDataOd_dateInput_ClientState", "{\"enabled\":true,\"emptyMessage\":\"\",\"validationText\":\"" + todayYear + "-" + todayMonth + "-" + todayDay + "-00-00-00\",\"valueAsString\":\"" + todayYear + "-" + todayMonth + "-" + todayDay + "-00-00-00\",\"minDateStr\":\"1980-01-01-00-00-00\",\"maxDateStr\":\"2099-12-31-00-00-00\"}");
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_radDataOd_calendar_SD", "[]");
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_radDataOd_calendar_AD", "[[1980,1,1],[2099,12,30],[" + todayYear + "," + todayMonth + "," + todayDay + "]]");

            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$radDataDo", todayYear + "-" + todayMonth + "-" + todayDay);
            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$radDataDo$dateInput", todayDay + "." + todayMonth + "." + todayYear);
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_radDataDo_dateInput_ClientState", "{\"enabled\":true,\"emptyMessage\":\"\",\"validationText\":\"" + todayYear + "-" + todayMonth + "-" + todayDay + "-00-00-00\",\"valueAsString\":\"" + todayYear + "-" + todayMonth + "-" + todayDay + "-00-00-00\",\"minDateStr\":\"1980-01-01-00-00-00\",\"maxDateStr\":\"2099-12-31-00-00-00\"}");
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_radDataDo_calendar_SD", "[]");
            POSTgenerator.add("ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_radDataDo_calendar_AD", "[[1980,1,1],[2099,12,30],[" + todayYear + "," + todayMonth + "," + todayDay + "]]");
        } catch (UnsupportedEncodingException e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }

        fw = new FetchWebsite(Logging.URLdomain + "/PodzGodzin.aspx");
        fww = fw.getWebsite(true, true, POSTgenerator.getGeneratedPOST());
        fwParsed = Jsoup.parse(fww);

        Elements tableRows = fwParsed.select(".gridDane");

        List<Appointment> list = new ArrayList<>();
        for (Element current : tableRows){
            try {
                Elements current2 = current.select("td");

                String dateAndTimeOfStartOfLesson = current2.get(0).ownText().split(" ")[0] + " " + current2.get(1).ownText();
                String dateAndTimeOfStopOfLesson = current2.get(0).ownText().split(" ")[0] + " " + current2.get(2).ownText();
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

                long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
                long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());
                String name = current2.get(3).ownText();
                String description = current2.get(9).ownText() + "\n" + current2.get(4).ownText();
                String location = current2.get(5).ownText();
                boolean lecture = current2.get(9).ownText().toLowerCase().contains("wykład");
                double group = 0;

                Appointment appointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, true, -1, group, false);

                list.add(appointment);
            } catch (ParseException e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }
        }

        // If there is no data to show
        if (list.size() == 0) {
            status = -1;
        }
        else {
            Storage.schedule = list;
            status = 0;
        }
    }

    private void FetchEaiibSchedule() throws Exception {
        String URLdomainEaiibSchedule = "http://planzajec.eaiib.agh.edu.pl";

        FetchWebsite fw;
        String fww;
        Document fwParsed;
        String statusEaiib;

        fw = new FetchWebsite(URLdomainEaiibSchedule + "/");
        fww = fw.getWebsiteHTTP(false, false, "");
        fwParsed = Jsoup.parse(fww);

        String kierunek = Storage.universityStatus.get(2);
        String specjalnosc = Storage.universityStatus.get(3);
        String formaStudiow = Storage.universityStatus.get(4).substring(0,1).toUpperCase() + Storage.universityStatus.get(4).substring(1);
        String poziomStudiow = Storage.universityStatus.get(5);

        try {
            if (poziomStudiow.contains("pierwszego") && kierunek.contains("Elektrotechnika") && specjalnosc.length() > 0)
                statusEaiib = fwParsed.select("select#view-select > option:matches(" + kierunek + " - " + formaStudiow + " - Studia I stopnia - Blok obieralny " + specjalnosc.replaceAll("\"", "").substring(specjalnosc.replaceAll("\"", "").length() - 1) + " - [a-zA-Z]{0,} [0-9]{4}\\/[0-9]{4}\\/S" + Storage.getSemesterNumberById(Storage.summarySemesters.size() - 1) + ")").get(0).attr("value");
            else if (poziomStudiow.contains("pierwszego"))
                statusEaiib = fwParsed.select("select#view-select > option:matches(" + kierunek + " - " + formaStudiow + " - Studia I stopnia - [a-zA-Z]{0,} [0-9]{4}\\/[0-9]{4}\\/S" + Storage.getSemesterNumberById(Storage.summarySemesters.size() - 1) + ")").get(0).attr("value");
            else
                statusEaiib = fwParsed.select("select#view-select > option:matches(" + kierunek + " - " + specjalnosc + " - " + formaStudiow + " - Studia II stopnia - [a-zA-Z]{0,} [0-9]{4}\\/[0-9]{4}\\/S" + Storage.getSemesterNumberById(Storage.summarySemesters.size() - 1) + ")").get(0).attr("value");
        } catch (IndexOutOfBoundsException e){
            statusEaiib = "NOT_FOUND";
            Storage.appendCrash(e);
        }

        if (statusEaiib.equals("NOT_FOUND")){
            status = -1;
        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            String startYear = String.valueOf(cal.get(Calendar.YEAR) - 1);
            String endYear = String.valueOf(cal.get(Calendar.YEAR) + 1);

            fw = new FetchWebsite(URLdomainEaiibSchedule + "/view/timetable/" + statusEaiib + "/events?start=" + startYear + "-01-01&end=" + endYear + "-12-31");
            fww = fw.getWebsiteHTTP(false, false, "");

            JSONArray jsonArray = new JSONArray(fww);
            List<Appointment> list = new ArrayList<>();

            for (int i=0; i<jsonArray.length(); i++){
                String dateAndTimeOfStartOfLesson = jsonArray.getJSONObject(i).getString("start").split("\\+")[0].replace("T", " ");
                String dateAndTimeOfStopOfLesson = jsonArray.getJSONObject(i).getString("end").split("\\+")[0].replace("T", " ");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
                long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());

                String [] descBR = jsonArray.getJSONObject(i).getString("title").split("<br/>");

                double group = jsonArray.getJSONObject(i).getDouble("group");
                boolean lecture = jsonArray.getJSONObject(i).getDouble("group") == 0;
                String location = descBR[1].split(",")[0].replace("Sala: ", "").trim();
                String name, description;

                if (descBR[0].toLowerCase().contains("grupa")){
                    String [] descBR0split = descBR[0].split(", ");

                    name = descBR[0].replace(", " + descBR0split[descBR0split.length-2].trim() + ", " + descBR0split[descBR0split.length-1], "");
                    description = descBR0split[descBR0split.length-2].trim() + ", " + descBR0split[descBR0split.length-1] + "\n"
                            + descBR[1].split(", ")[2].trim() + " " + descBR[1].split(", ")[1].replace("prowadzący: ", "").trim();

                    if (descBR.length > 2)
                        description = description + "\n" + descBR[2].replace("Informacja: ", "").trim();
                }
                else {
                    String [] descBR0split = descBR[0].split(", ");

                    name = descBR[0].replace(", " + descBR0split[descBR0split.length-1], "");
                    description = descBR0split[descBR0split.length-1] + "\n"
                            + descBR[1].split(", ")[2].trim() + " " + descBR[1].split(", ")[1].replace("prowadzący: ", "").trim();

                    if (descBR.length > 2)
                        description = description + "\n" + descBR[2].replace("Informacja: ", "").trim();
                }

                Appointment appointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, true, -1, group, false);
                list.add(appointment);
            }

            // If there is no data to show
            if (list.size() == 0) {
                status = -1;
            }
            else {
                Storage.schedule = list;
                status = 1;
            }
        }
    }

    private void FetchUniTimeSchedule() throws Exception {
        String dictionaryURL = "http://sprawdz.plan.agh.edu.pl/aghwd_unitime.php";

        FetchWebsite fw, fw2;
        String fww2, specjalnosc, stopien, rodzaj;

        String fww;
        specjalnosc = Storage.universityStatus.get(3).trim();

        if (Storage.universityStatus.get(5).contains("pierwszego"))
            stopien = "1";
        else if (Storage.universityStatus.get(5).contains("drugiego"))
            stopien = "2";
        else
            stopien = "3";

        if (Storage.universityStatus.get(4).contains("stacjonarne"))
            rodzaj = "S";
        else
            rodzaj = "N";

        fw2 = new FetchWebsite(dictionaryURL +
                "?wydzial=" + Storage.universityStatus.get(1) +
                "&kierunek=" + Storage.universityStatus.get(2).replace("+", "%2B") +
                "&specjalnosc=" + specjalnosc.replace("+", "%2B") +
                "&stopien=" + stopien +
                "&semestr=" + Storage.getSemesterNumberById(Storage.summarySemesters.size()-1) +
                "&rodzaj=" + rodzaj);
        fww2 = fw2.getWebsiteHTTP(false, false, "");

        boolean planAvailable;
        if (fww2.contains("null")) {
            planAvailable = false;
            status = -1;
        }
        else
            planAvailable = true;

        if (planAvailable) {
            fw = new FetchWebsite("https://plan.agh.edu.pl/UniTime/export?output=meetings.csv&type=curriculum&name=" + fww2.replace("+", "%2B").replace("%2BS", "+S") + "&sort=1&term=" + ScheduleUtils.getSemesterUniTimeName());
            fww = fw.getWebsiteGETSecure(false, false, "");

            // parse CSV with timetable
            String[] csvHeader = {"Name", "Section", "Type", "Title", "Date", "Published Start", "Published End", "Location", "Capacity", "Instructor / Sponsor", "Email", "Requested Services", "Approved"};
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(csvHeader);
            Reader reader = new StringReader(fww);
            CSVParser csvParser = new CSVParser(reader, csvFormat);

            List<Appointment> list = new ArrayList<>();
            List<CSVRecord> csvRecords = csvParser.getRecords();
            csvRecords.remove(0);
            for (CSVRecord record : csvRecords) {
                if (record.get("Published Start").replace("noon", "12:00").contains(":")
                        && record.get("Published End").replace("noon", "12:00").contains(":")) {
                    String dateAndTimeOfStartOfLesson = record.get("Date") + " " + record.get("Published Start").replace("noon", "12:00");
                    String dateAndTimeOfStopOfLesson = record.get("Date") + " " + record.get("Published End").replace("noon", "12:00");
                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                    df.setLenient(true);

                    long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
                    long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());
                    String name = record.get("Title").replace("\n", "");
                    String description = "Grupa " + record.get("Section") + ", " + record.get("Type").replace("\n", "") + "\n" + record.get("Instructor / Sponsor");
                    String location = record.get("Location").replace("\n", "");

                    boolean lecture;
                    if (record.get("Type").toLowerCase().contains("wykład"))
                        lecture = true;
                    else
                        lecture = false;

                    double group;
                    if (lecture)
                        group = 0;
                    else {
                        if (record.get("Section").matches("[0-9]+"))
                            group = Double.parseDouble(record.get("Section"));
                        else if (record.get("Section").matches("[0-9]+[a-z]")) {
                            group = Double.parseDouble(record.get("Section").replaceAll("[a-z]", ""))
                                    + (((((int) record.get("Section").replaceAll("[0-9]+", "").charAt(0)) - 96)) / (double) 10);
                        } else
                            group = 9999;
                    }

                    Appointment appointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, true, -1, group, false);

                    list.add(appointment);
                }
            }

            // If there is no data to show
            if (list.size() == 0) {
                status = -1;
            } else {
                Storage.schedule = list;
                status = 1;
            }
        }
    }

}
