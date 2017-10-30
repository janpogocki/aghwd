package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import pl.janpogocki.agh.wirtualnydziekanat.R;
import pl.janpogocki.agh.wirtualnydziekanat.ScheduleActivity;

/**
 * Created by Jan on 22.10.2017.
 * Adapter displaying events
 */

public class RecyclerViewScheduleAdapter extends RecyclerView.Adapter<RecyclerViewScheduleAdapter.ViewHolder> {

    private Context c;
    private int lastPastAppointement;
    private List<Appointment> listOfAppointments;
    private List<Double> listOfGroupIds;
    private ScheduleActivity scheduleActivity;

    public RecyclerViewScheduleAdapter(Context c, ScheduleActivity scheduleActivity) {
        this.c = c;
        this.lastPastAppointement = 0;
        this.scheduleActivity = scheduleActivity;

        prepareFinalListOfAppointments();
    }

    public int getLastPastAppointement() {
        return lastPastAppointement;
    }

    public List<Appointment> getListOfAppointments() {
        return listOfAppointments;
    }

    public CharSequence [] getListOfGroupIds() {
        CharSequence [] returned = new CharSequence[listOfGroupIds.size()];

        for (int i=0; i<listOfGroupIds.size(); i++){
            returned[i] = String.valueOf(listOfGroupIds.get(i));
        }

        return returned;
    }

    private boolean isToDeleteEvent(Appointment currentAppointment){
        for (int i=0; i<c.getResources().getStringArray(R.array.schedule_view_settings_values).length; i++){
            boolean defaultValue = i == 4;
            boolean currentViewSetting = PreferenceManager.getDefaultSharedPreferences(c)
                    .getBoolean(Storage.getUniversityStatusHash() + "_schedule_view_settings_" + i, defaultValue);

            if (i == 0 && currentViewSetting && currentAppointment.aghEvent)
                return true;
            else if (i == 1 && currentViewSetting && !currentAppointment.aghEvent)
                return true;
            else if (i == 2 && currentViewSetting && currentAppointment.lecture)
                return true;
            else if (i == 3 && currentViewSetting && currentAppointment.tag == -1)
                return true;
            else if (i == 4 && currentViewSetting && currentAppointment.tag == 0)
                return true;
            else if (i == 5 && currentViewSetting && currentAppointment.tag == 1)
                return true;
            else if (i == 6 && currentViewSetting && currentAppointment.tag == 2)
                return true;
            else if (i == 7 && currentViewSetting && currentAppointment.tag == 3)
                return true;
        }

        return false;
    }

    private void prepareFinalListOfAppointments(){
        listOfAppointments = new ArrayList<>();
        listOfGroupIds = new ArrayList<>();
        String lastDate = "";
        long currentTime = System.currentTimeMillis();

        listOfGroupIds.add(-1.0);

        // read file with tags
        JSONObject jsonObject = null;
        String filename = Storage.getUniversityStatusHash() + "_tags.json";
        File file = new File(c.getFilesDir() + "/" + filename);

        if (file.exists()){
            try {
                StringBuilder jsonFromFile = new StringBuilder();
                FileInputStream inputStream = c.openFileInput(filename);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = r.readLine()) != null) {
                    jsonFromFile.append(line);
                }
                r.close();
                inputStream.close();

                jsonObject = new JSONObject(jsonFromFile.toString());
            } catch (Exception e){
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }
        }

        // insert events from mycal.json
        String filenameMyCal = Storage.getUniversityStatusHash() + "_mycal.json";
        File fileMyCal = new File(c.getFilesDir() + "/" + filenameMyCal);

        if (fileMyCal.exists()){
            try {
                StringBuilder jsonFromMyCal = new StringBuilder();
                FileInputStream inputStream = c.openFileInput(filenameMyCal);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = r.readLine()) != null) {
                    jsonFromMyCal.append(line);
                }
                r.close();
                inputStream.close();

                JSONArray jsonArrayMyCal = new JSONArray(jsonFromMyCal.toString());

                // iterate and add events
                for (int i=0; i<jsonArrayMyCal.length(); i++){
                    long startTimestamp = jsonArrayMyCal.getJSONObject(i).getLong("startTimestamp");
                    long stopTimestamp = jsonArrayMyCal.getJSONObject(i).getLong("stopTimestamp");
                    String name = jsonArrayMyCal.getJSONObject(i).getString("name");
                    String description = jsonArrayMyCal.getJSONObject(i).getString("description");
                    String location = jsonArrayMyCal.getJSONObject(i).getString("location");
                    boolean lecture = jsonArrayMyCal.getJSONObject(i).getBoolean("lecture");
                    boolean aghEvent = jsonArrayMyCal.getJSONObject(i).getBoolean("aghEvent");
                    int tag = jsonArrayMyCal.getJSONObject(i).getInt("tag");
                    double group = jsonArrayMyCal.getJSONObject(i).getDouble("group");
                    boolean showDateBar = jsonArrayMyCal.getJSONObject(i).getBoolean("showDateBar");

                    Appointment currentAppointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, aghEvent, tag, group, showDateBar);

                    // check for tag
                    try {
                        String currentTagName = currentAppointment.startTimestamp + currentAppointment.stopTimestamp + currentAppointment.name + currentAppointment.description + currentAppointment.aghEvent;
                        if (jsonObject != null && jsonObject.has(currentTagName) && jsonObject.getInt(currentTagName) != -1) {
                            currentAppointment.tag = jsonObject.getInt(currentTagName);
                        }
                        else
                            currentAppointment.tag = -1;
                    } catch (Exception e){
                        Log.i("aghwd", "aghwd", e);
                        Storage.appendCrash(e);
                    }

                    listOfAppointments.add(currentAppointment);
                }
            } catch (Exception e){
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }
        }

        // insert events from AGH calendar
        for (int i=0; i<Storage.schedule.size(); i++){
            Appointment currentAppointment = Storage.schedule.get(i);

            // add group id if not exists
            if (!listOfGroupIds.contains(currentAppointment.group))
                listOfGroupIds.add(currentAppointment.group);

            // check for tag
            try {
                String currentTagName = currentAppointment.startTimestamp + currentAppointment.stopTimestamp + currentAppointment.name + currentAppointment.description + currentAppointment.aghEvent;
                if (jsonObject != null && jsonObject.has(currentTagName) && jsonObject.getInt(currentTagName) != -1) {
                    currentAppointment.tag = jsonObject.getInt(currentTagName);
                }
                else
                    currentAppointment.tag = -1;
            } catch (Exception e){
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }

            listOfAppointments.add(currentAppointment);
        }

        // sort all appointments by startTime
        Collections.sort(listOfAppointments, new Comparator<Appointment>() {
            @Override
            public int compare(final Appointment object1, final Appointment object2) {
                return Long.valueOf(object1.startTimestamp).compareTo(object2.startTimestamp);
            }
        });

        // sort group ids
        Collections.sort(listOfGroupIds);

        // remove unnecessary events (groups) if eaiib
        if (Storage.universityStatus.get(1).contains("Elektrotechniki, Automatyki, Informatyki i Inżynierii Biomedycznej")) {
            double selectedGroup = Double.parseDouble(String.valueOf(PreferenceManager.getDefaultSharedPreferences(c).getFloat(Storage.getUniversityStatusHash() + "_choosen_eaiib_group", -1)));

            if (selectedGroup == -1) {
                // only delete which isToDeleteEvent()
                for (Appointment current : new ArrayList<>(listOfAppointments)) {
                    if (isToDeleteEvent(current))
                        listOfAppointments.remove(current);
                }
            } else if (selectedGroup == 0) {
                // only lectures save
                for (Appointment current : new ArrayList<>(listOfAppointments)) {
                    if (current.group != 0 || isToDeleteEvent(current))
                        listOfAppointments.remove(current);
                }
            } else if (selectedGroup % 1 == 0) {
                // only group X.0 and X.1, X.2, ... save and lectures ofc
                for (Appointment current : new ArrayList<>(listOfAppointments)) {
                    if (((int) current.group != selectedGroup && current.group != 0) || isToDeleteEvent(current))
                        listOfAppointments.remove(current);
                }
            } else if (selectedGroup % 1 > 0) {
                // only X.1 save and lectures ofc
                for (Appointment current : new ArrayList<>(listOfAppointments)) {
                    if ((current.group != selectedGroup && current.group != 0) || isToDeleteEvent(current))
                        listOfAppointments.remove(current);
                }
            }
        }
        else {
            // only delete this events which isToDeleteEvent()
            for (Appointment current : new ArrayList<>(listOfAppointments)) {
                if (isToDeleteEvent(current))
                    listOfAppointments.remove(current);
            }
        }

        // find the latest past event
        for (int i=0; i<listOfAppointments.size(); i++){
            Appointment currentAppointment = listOfAppointments.get(i);
            long hourStartTime = currentAppointment.startTimestamp + TimeZone.getDefault().getOffset(currentAppointment.startTimestamp);
            Date hourStartDate = new Date(hourStartTime);
            long hourStopTime = currentAppointment.stopTimestamp + TimeZone.getDefault().getOffset(currentAppointment.stopTimestamp);
            Date hourStopDate = new Date(hourStopTime);

            // show date bar or not bool
            String fullDateString = getFullDateString(hourStartDate);
            if (fullDateString.equals(lastDate))
                currentAppointment.showDateBar = false;
            else {
                currentAppointment.showDateBar = true;
                lastDate = fullDateString;
            }

            // if event is past - set var
            if (new Date(currentTime).after(hourStopDate))
                lastPastAppointement = i;
        }

    }

    private String getFullDateString(Date currentDate){
        String [] nameOfDayOfWeek = {"niedziela", "poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota"};
        String [] nameOfMonths = {"stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca", "lipca", "sierpnia", "września", "października", "listopada", "grudnia"};

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        return nameOfDayOfWeek[cal.get(Calendar.DAY_OF_WEEK)-1] + ", " + cal.get(Calendar.DAY_OF_MONTH) + " " + nameOfMonths[cal.get(Calendar.MONTH)];
    }

    private String getCountdownTime(long diff){
        if (diff <= 60*60*1000)
            return (int) Math.ceil(diff/(double)(60*1000)) + " min";
        else if (diff <= 24*60*60*1000){
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(diff);
            cal.add(Calendar.MINUTE, 1);
            return String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        else if (diff <= 48*60*60*1000)
            return "1 dzień";
        else
            return (int) Math.round(diff/(double)(24*60*60*1000)) + " dni";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.schedule_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewScheduleAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.setIsRecyclable(false);

        viewHolder.textViewHeader.setText(listOfAppointments.get(i).name);
        viewHolder.textView2ndLine.setText(listOfAppointments.get(i).description);
        viewHolder.textView3rdLine.setText(listOfAppointments.get(i).location);

        // convert timestamps to regular hours
        long hourStartTime = listOfAppointments.get(i).startTimestamp + TimeZone.getDefault().getOffset(listOfAppointments.get(i).startTimestamp);
        long hourStopTime = listOfAppointments.get(i).stopTimestamp + TimeZone.getDefault().getOffset(listOfAppointments.get(i).stopTimestamp);

        Date hourStartDate = new Date(hourStartTime);
        Date hourStopDate = new Date(hourStopTime);

        Calendar cal = Calendar.getInstance();
        cal.setTime(hourStartDate);
        String hourStart = String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        cal.setTime(hourStopDate);
        String hourStop = String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        viewHolder.textViewHourStart.setText(hourStart);
        viewHolder.textViewHourStop.setText(hourStop);

        // show tag
        if (listOfAppointments.get(i).tag != -1){
            viewHolder.textViewTag.setVisibility(View.VISIBLE);
            viewHolder.textViewTag.setText(c.getResources().getStringArray(R.array.schedule_tag_names)[listOfAppointments.get(i).tag]);
        }

        // show or not myCalIcon
        if (!listOfAppointments.get(i).aghEvent)
            viewHolder.imageViewMyCal.setVisibility(View.VISIBLE);

        // count and show countdown
        long currentTime = System.currentTimeMillis();
        Date currentDate = new Date(currentTime);

        if (currentDate.before(hourStartDate)){
            long diff = hourStartTime - currentTime;
            viewHolder.textViewHourCountdown.setText("za\n" + getCountdownTime(diff));

            if (diff <= 15*60*1000)
                viewHolder.textViewHourCountdown.setTextColor(c.getResources().getColor(R.color.colorPrimary));
            else if (diff <= 60*60*1000)
                viewHolder.textViewHourCountdown.setTextColor(c.getResources().getColor(R.color.colorOrange));
        }
        else if ((currentDate.equals(hourStartDate) || currentDate.after(hourStartDate)) && currentDate.before(hourStopDate)){
            long diff = hourStopTime - currentTime;
            viewHolder.textViewHourCountdown.setText("trwa\njeszcze\n" + getCountdownTime(diff));
            viewHolder.textViewHourCountdown.setTextColor(c.getResources().getColor(R.color.colorGreen));
        }
        else {
            viewHolder.textViewHourCountdown.setText("minęło");
            viewHolder.textViewHeader.setTextColor(c.getResources().getColor(R.color.colorGrayText));
            viewHolder.textView2ndLine.setTextColor(c.getResources().getColor(R.color.colorGrayText));
            viewHolder.textView3rdLine.setTextColor(c.getResources().getColor(R.color.colorGrayText));
            viewHolder.textViewHourStart.setTextColor(c.getResources().getColor(R.color.colorGrayText));
            viewHolder.textViewHourStop.setTextColor(c.getResources().getColor(R.color.colorGrayText));
            viewHolder.textViewHourCountdown.setTextColor(c.getResources().getColor(R.color.colorGrayText));
        }

        // show or not big date layout
        String fullDateString = getFullDateString(hourStartDate);
        viewHolder.textViewDate.setText(fullDateString);

        if (listOfAppointments.get(i).showDateBar)
            viewHolder.rlBigDate.setVisibility(View.VISIBLE);
        else
            viewHolder.rlBigDate.setVisibility(View.GONE);

        // onClickListener
        viewHolder.scheduleClickableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleActivity.showEventSettings(listOfAppointments.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfAppointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewHeader, textView2ndLine, textView3rdLine, textViewTag, textViewHourStart, textViewHourStop, textViewHourCountdown, textViewDate;
        private RelativeLayout rlBigDate;
        private LinearLayout scheduleClickableLayout;
        private AppCompatImageView imageViewMyCal;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewHeader = itemView.findViewById(R.id.textViewHeader);
            textView2ndLine = itemView.findViewById(R.id.textView2ndLine);
            textView3rdLine = itemView.findViewById(R.id.textView3rdLine);
            textViewTag = itemView.findViewById(R.id.textViewTag);
            textViewHourStart = itemView.findViewById(R.id.textViewHourStart);
            textViewHourStop = itemView.findViewById(R.id.textViewHourStop);
            textViewHourCountdown = itemView.findViewById(R.id.textViewHourCountdown);
            textViewDate = itemView.findViewById(R.id.textViewDate);

            rlBigDate = itemView.findViewById(R.id.rlBigDate);
            scheduleClickableLayout = itemView.findViewById(R.id.scheduleClickableLayout);
            imageViewMyCal = itemView.findViewById(R.id.imageViewMyCal);
        }
    }
}