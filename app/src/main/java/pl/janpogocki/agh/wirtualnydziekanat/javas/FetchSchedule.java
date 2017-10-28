package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.util.Log;

import org.json.JSONArray;
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
import java.util.Collections;
import java.util.Comparator;
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

        if (Storage.universityStatus != null && Storage.universityStatus.size() > 6
                && Storage.universityStatus.get(1).contains("Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej")){
            FetchEaiibSchedule();
        }
        else {
            FetchDziekanatXPSchedule();
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
            }
            else {
                // sort by startTime
                Collections.sort(list, new Comparator<Appointment>() {
                    @Override
                    public int compare(final Appointment object1, final Appointment object2) {
                        return Long.valueOf(object1.startTimestamp).compareTo(object2.startTimestamp);
                    }
                });

                Storage.schedule = list;
                status = 1;
            }
        }
    }

}
