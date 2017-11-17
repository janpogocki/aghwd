package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import pl.janpogocki.agh.wirtualnydziekanat.R;
import pl.janpogocki.agh.wirtualnydziekanat.SkosActivity;

/**
 * Created by Jan on 30.10.2017.
 * Adapter displaying events
 */

public class RecyclerViewTeacherScheduleAdapter extends RecyclerView.Adapter<RecyclerViewTeacherScheduleAdapter.ViewHolder> {

    private Context c;
    private int lastPastAppointement;
    private List<Appointment> listOfAppointments;
    private SkosActivity skosActivity;

    public RecyclerViewTeacherScheduleAdapter(Context c, SkosActivity skosActivity) {
        this.c = c;
        this.lastPastAppointement = 0;
        this.skosActivity = skosActivity;

        prepareFinalListOfAppointments();
    }

    public int getLastPastAppointement() {
        return lastPastAppointement;
    }

    public List<Appointment> getListOfAppointments() {
        return listOfAppointments;
    }

    private void prepareFinalListOfAppointments(){
        listOfAppointments = new ArrayList<>();
        String lastDate = "";
        long currentTime = System.currentTimeMillis();

        // insert events from AGH calendar
        listOfAppointments.addAll(Storage.teacherSchedule);

        // sort all appointments by startTime
        Collections.sort(listOfAppointments, new Comparator<Appointment>() {
            @Override
            public int compare(final Appointment object1, final Appointment object2) {
                return Long.valueOf(object1.startTimestamp).compareTo(object2.startTimestamp);
            }
        });

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.schedule_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewTeacherScheduleAdapter.ViewHolder viewHolder, final int i) {
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

        // count and show countdown
        long currentTime = System.currentTimeMillis();
        Date currentDate = new Date(currentTime);

        if (currentDate.before(hourStartDate)){
            viewHolder.textViewHourCountdown.setText(ScheduleUtils.getCountdownTime(hourStartTime, currentTime, true));

            long diff = hourStartTime - currentTime;
            if (diff <= 15*60*1000)
                viewHolder.textViewHourCountdown.setTextColor(c.getResources().getColor(R.color.colorPrimary));
            else if (diff <= 60*60*1000)
                viewHolder.textViewHourCountdown.setTextColor(c.getResources().getColor(R.color.colorOrange));
        }
        else if ((currentDate.equals(hourStartDate) || currentDate.after(hourStartDate)) && currentDate.before(hourStopDate)){
            viewHolder.textViewHourCountdown.setText(ScheduleUtils.getCountdownTime(hourStartTime, currentTime, false));
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
                skosActivity.showEventSettings(listOfAppointments.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfAppointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewHeader, textView2ndLine, textView3rdLine, textViewHourStart, textViewHourStop, textViewHourCountdown, textViewDate;
        private RelativeLayout rlBigDate;
        private LinearLayout scheduleClickableLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewHeader = itemView.findViewById(R.id.textViewHeader);
            textView2ndLine = itemView.findViewById(R.id.textView2ndLine);
            textView3rdLine = itemView.findViewById(R.id.textView3rdLine);
            textViewHourStart = itemView.findViewById(R.id.textViewHourStart);
            textViewHourStop = itemView.findViewById(R.id.textViewHourStop);
            textViewHourCountdown = itemView.findViewById(R.id.textViewHourCountdown);
            textViewDate = itemView.findViewById(R.id.textViewDate);

            rlBigDate = itemView.findViewById(R.id.rlBigDate);
            scheduleClickableLayout = itemView.findViewById(R.id.scheduleClickableLayout);
        }
    }
}