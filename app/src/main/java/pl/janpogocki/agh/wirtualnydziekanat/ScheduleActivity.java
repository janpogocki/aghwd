package pl.janpogocki.agh.wirtualnydziekanat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import pl.janpogocki.agh.wirtualnydziekanat.javas.Appointment;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchSchedule;
import pl.janpogocki.agh.wirtualnydziekanat.javas.RecyclerViewScheduleAdapter;
import pl.janpogocki.agh.wirtualnydziekanat.javas.ScheduleUtils;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class ScheduleActivity extends Fragment {

    FirebaseAnalytics mFirebaseAnalytics;
    FetchSchedule fs;
    View root;
    TextView textView3, textView3bis;
    Context activityContext;
    RecyclerView recyclerViewSchedule;
    RecyclerViewScheduleAdapter recyclerViewScheduleAdapter;
    LinearLayoutManager layoutManager;
    AsyncTaskRunnerAutoRefresher asyncTaskRunnerAutoRefresher;

    private void animateFadeIn(TextView tv, View view, int offset){
        Animation afi = AnimationUtils.loadAnimation(view.getContext(), R.anim.fadein);
        tv.setAnimation(afi);
        afi.setDuration(250);
        afi.setStartOffset(offset);
        afi.start();
    }

    private void animateFadeOut(final TextView tv, View view, int offset){
        Animation afi = AnimationUtils.loadAnimation(view.getContext(), R.anim.fadeout);
        tv.setAnimation(afi);
        afi.setDuration(250);
        afi.setStartOffset(offset);
        afi.start();

        afi.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tv.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void enforceShowingRecyclerView(){
        RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
        RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
        RelativeLayout rlData = root.findViewById(R.id.rlData);
        FloatingActionButton fab = root.findViewById(R.id.fab);

        rlData.setVisibility(View.VISIBLE);
        rlNoData.setVisibility(View.GONE);
        rlLoader.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
    }

    public void enforceHidingRecyclerView(){
        RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
        RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
        RelativeLayout rlData = root.findViewById(R.id.rlData);
        FloatingActionButton fab = root.findViewById(R.id.fab);

        rlData.setVisibility(View.GONE);
        rlNoData.setVisibility(View.VISIBLE);
        rlLoader.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
    }

    public void scrollToNowPosition(){
        if (recyclerViewSchedule != null) {
            recyclerViewSchedule.post(new Runnable() {
                @Override
                public void run() {
                    layoutManager.scrollToPositionWithOffset(recyclerViewScheduleAdapter.getLastPastAppointement()+1, 0);
                }
            });
        }
    }

    public void goToDate(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog mDatePicker = new DatePickerDialog(activityContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                Calendar cal = Calendar.getInstance();
                cal.set(selectedYear, selectedMonth, selectedDay);
                Date selectedDate =  cal.getTime();

                for (int i=0; i<recyclerViewScheduleAdapter.getListOfAppointments().size()-1; i++){
                    Date currentDate = new Date(recyclerViewScheduleAdapter.getListOfAppointments().get(i+1).startTimestamp);

                    if (currentDate.after(selectedDate)){
                        if (recyclerViewSchedule != null) {
                            final int finalI = i;
                            recyclerViewSchedule.post(new Runnable() {
                                @Override
                                public void run() {
                                    layoutManager.scrollToPositionWithOffset(finalI+1, 0);
                                }
                            });
                        }
                        return;
                    }
                }

                if (recyclerViewSchedule != null && recyclerViewScheduleAdapter.getListOfAppointments().size() != 0) {
                    recyclerViewSchedule.post(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.scrollToPositionWithOffset(recyclerViewScheduleAdapter.getListOfAppointments().size()-1, 0);
                        }
                    });
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "go_to_date");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }, year, month, dayOfMonth);
        mDatePicker.setTitle(R.string.schedule_go_to_date);
        mDatePicker.show();
    }

    public void changeGroup(){
        int checkedItem = findPositionInArray(recyclerViewScheduleAdapter.getListOfGroupIds(),
                PreferenceManager.getDefaultSharedPreferences(activityContext).getFloat(Storage.getUniversityStatusHash() + "_choosen_eaiib_group", -1));
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(R.string.choose_group);
        builder.setSingleChoiceItems(translateGroupNames(recyclerViewScheduleAdapter.getListOfGroupIds()), checkedItem, null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "change_group");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                PreferenceManager.getDefaultSharedPreferences(activityContext).edit().putFloat(Storage.getUniversityStatusHash() + "_choosen_eaiib_group",
                        Float.parseFloat(String.valueOf(recyclerViewScheduleAdapter.getListOfGroupIds()[((AlertDialog) dialogInterface).getListView().getCheckedItemPosition()])))
                        .apply();

                onDestroyView();
                onResume();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.show();
    }

    public void showViewSettings(){
        final int valuesCount = activityContext.getResources().getStringArray(R.array.schedule_view_settings_values).length;
        boolean [] usersCheckedItems = new boolean[valuesCount];

        for (int i=0; i<valuesCount; i++){
            boolean defaultValue = i == 4;

            usersCheckedItems[i] = PreferenceManager.getDefaultSharedPreferences(activityContext).getBoolean(Storage.getUniversityStatusHash() + "_schedule_view_settings_" + i, defaultValue);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(R.string.schedule_view_preferences);
        builder.setMultiChoiceItems(R.array.schedule_view_settings_values, usersCheckedItems, null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "change_view_settings");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                for (int j=0; j<valuesCount; j++){
                    PreferenceManager.getDefaultSharedPreferences(activityContext).edit()
                            .putBoolean(Storage.getUniversityStatusHash() + "_schedule_view_settings_" + j, ((AlertDialog) dialogInterface).getListView().isItemChecked(j))
                            .apply();
                }

                onDestroyView();
                onResume();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.show();
    }

    public void showEventSettings(final Appointment currentAppointment){
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(currentAppointment.name);

        if (currentAppointment.aghEvent) {
            builder.setItems(R.array.schedule_agh_event_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0){
                        // copy to mycal
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "copy_to_mycal");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.copyAppointment(activityContext, currentAppointment, null);
                    }
                    else if (i == 1){
                        // copy all like this one to mycal
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "copy_all_to_mycal");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.copyAppointment(activityContext, currentAppointment, recyclerViewScheduleAdapter.getListOfAppointments());
                    }
                    else if (i == 2){
                        // tag "hide"
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_hide");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 0, null);
                    }
                    else if (i == 3){
                        // tag "hide" to all like this one
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_hide_all");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 0, recyclerViewScheduleAdapter.getListOfAppointments());
                    }
                    else if (i == 4){
                        // tag "wazne"
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_important");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 1, null);
                    }
                    else if (i == 5){
                        // tag "kolokwium"
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_kolokwium");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 2, null);
                    }
                    else if (i == 6){
                        // tag "egzamin"
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_exam");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 3, null);
                    }

                    dialogInterface.dismiss();
                    onDestroyView();
                    onResume();
                }
            });
        }
        else {
            builder.setItems(R.array.schedule_non_agh_event_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0){
                        // tag "wazne"
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_important");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 1, null);
                    }
                    else if (i == 1){
                        // tag "kolokwium"
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_kolokwium");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 2, null);
                    }
                    else if (i == 2){
                        // tag "egzamin"
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tag_exam");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.addOrChangeOrRemoveTag(activityContext, currentAppointment, 3, null);
                    }
                    else if (i == 3){
                        // edit event
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "edit");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        editEvent(currentAppointment);
                    }
                    else if (i == 4){
                        // delete event
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "remove");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        ScheduleUtils.removeAppointment(activityContext, currentAppointment);
                    }

                    dialogInterface.dismiss();

                    // if not edit event => refresh
                    if (i != 3) {
                        onDestroyView();
                        onResume();
                    }
                }
            });
        }

        builder.show();
    }

    private void createNewEvent(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.schedule_create_new_event_layout, null);
        builder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextTimeFrom = dialogView.findViewById(R.id.editTextTimeFrom);
        final EditText editTextTimeTo = dialogView.findViewById(R.id.editTextTimeTo);
        final EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        final EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        editTextDate.setText(String.format(Locale.US, "%02d.%02d.%04d", dayOfMonth, month, year));
        editTextTimeFrom.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.add(Calendar.MINUTE, 30);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        editTextTimeTo.setText(String.format(Locale.US, "%02d:%02d", hour, minute));

        editTextTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = Integer.parseInt(editTextTimeFrom.getText().toString().split(":")[0]);
                int minute = Integer.parseInt(editTextTimeFrom.getText().toString().split(":")[1]);
                TimePickerDialog mTimePicker = new TimePickerDialog(activityContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        editTextTimeFrom.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, selectedHour);
                        cal.set(Calendar.MINUTE, selectedMinute);
                        cal.add(Calendar.HOUR_OF_DAY, 1);
                        cal.add(Calendar.MINUTE, 30);
                        String endTime = String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                        editTextTimeTo.setText(endTime);
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });

        editTextTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = Integer.parseInt(editTextTimeTo.getText().toString().split(":")[0]);
                int minute = Integer.parseInt(editTextTimeTo.getText().toString().split(":")[1]);
                TimePickerDialog mTimePicker = new TimePickerDialog(activityContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        editTextTimeTo.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfMonth = Integer.parseInt(editTextDate.getText().toString().split("\\.")[0]);
                int month = Integer.parseInt(editTextDate.getText().toString().split("\\.")[1]) - 1;
                int year = Integer.parseInt(editTextDate.getText().toString().split("\\.")[2]);
                DatePickerDialog mDatePicker = new DatePickerDialog(activityContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        editTextDate.setText(String.format(Locale.US, "%02d.%02d.%04d", selectedDay, selectedMonth+1, selectedYear));
                    }
                }, year, month, dayOfMonth);
                mDatePicker.show();
            }
        });

        builder.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "create");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                    String dateAndTimeOfStartOfLesson = editTextDate.getText().toString() + " " + editTextTimeFrom.getText().toString();
                    String dateAndTimeOfStopOfLesson = editTextDate.getText().toString() + " " + editTextTimeTo.getText().toString();

                    long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
                    long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());
                    String name = editTextName.getText().toString().trim();

                    String description;
                    if (editTextDescription.getText().toString().trim().length() > 0)
                        description = editTextDescription.getText().toString().trim();
                    else
                        description = "(brak opisu)";

                    String location;
                    if (editTextLocation.getText().toString().trim().length() > 0)
                        location = editTextLocation.getText().toString().trim();
                    else
                        location = "(brak lokalizacji)";

                    boolean lecture = false;
                    boolean aghEvent = false;
                    int tag = -1;
                    double group = 0;
                    boolean showDateBar = false;

                    Appointment newAppointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, aghEvent, tag, group, showDateBar);
                    ScheduleUtils.saveNewAppointment(activityContext, newAppointment);
                    onDestroyView();
                    onResume();
                } catch (ParseException e){
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
        });

        builder.setNegativeButton(R.string.action_cancel, null);

        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void editEvent(final Appointment oldAppointment){
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.schedule_create_new_event_layout, null);
        builder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextTimeFrom = dialogView.findViewById(R.id.editTextTimeFrom);
        final EditText editTextTimeTo = dialogView.findViewById(R.id.editTextTimeTo);
        final EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        final EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oldAppointment.startTimestamp + TimeZone.getDefault().getOffset(oldAppointment.startTimestamp));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        editTextDate.setText(String.format(Locale.US, "%02d.%02d.%04d", dayOfMonth, month, year));
        editTextTimeFrom.setText(String.format(Locale.US, "%02d:%02d", hour, minute));

        cal.setTimeInMillis(oldAppointment.stopTimestamp + TimeZone.getDefault().getOffset(oldAppointment.stopTimestamp));
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        editTextTimeTo.setText(String.format(Locale.US, "%02d:%02d", hour, minute));

        editTextName.setText(oldAppointment.name);
        editTextDescription.setText(oldAppointment.description);
        editTextLocation.setText(oldAppointment.location);

        editTextTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = Integer.parseInt(editTextTimeFrom.getText().toString().split(":")[0]);
                int minute = Integer.parseInt(editTextTimeFrom.getText().toString().split(":")[1]);
                TimePickerDialog mTimePicker = new TimePickerDialog(activityContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        editTextTimeFrom.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, selectedHour);
                        cal.set(Calendar.MINUTE, selectedMinute);
                        cal.add(Calendar.HOUR_OF_DAY, 1);
                        cal.add(Calendar.MINUTE, 30);
                        String endTime = String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                        editTextTimeTo.setText(endTime);
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });

        editTextTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = Integer.parseInt(editTextTimeTo.getText().toString().split(":")[0]);
                int minute = Integer.parseInt(editTextTimeTo.getText().toString().split(":")[1]);
                TimePickerDialog mTimePicker = new TimePickerDialog(activityContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        editTextTimeTo.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfMonth = Integer.parseInt(editTextDate.getText().toString().split("\\.")[0]);
                int month = Integer.parseInt(editTextDate.getText().toString().split("\\.")[1]) - 1;
                int year = Integer.parseInt(editTextDate.getText().toString().split("\\.")[2]);
                DatePickerDialog mDatePicker = new DatePickerDialog(activityContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        editTextDate.setText(String.format(Locale.US, "%02d.%02d.%04d", selectedDay, selectedMonth+1, selectedYear));
                    }
                }, year, month, dayOfMonth);
                mDatePicker.show();
            }
        });

        builder.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "edit");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "custom_schedule");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                    String dateAndTimeOfStartOfLesson = editTextDate.getText().toString() + " " + editTextTimeFrom.getText().toString();
                    String dateAndTimeOfStopOfLesson = editTextDate.getText().toString() + " " + editTextTimeTo.getText().toString();

                    long startTimestamp = df.parse(dateAndTimeOfStartOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStartOfLesson).getTime());
                    long stopTimestamp = df.parse(dateAndTimeOfStopOfLesson).getTime() - TimeZone.getDefault().getOffset(df.parse(dateAndTimeOfStopOfLesson).getTime());
                    String name = editTextName.getText().toString().trim();

                    String description;
                    if (editTextDescription.getText().toString().trim().length() > 0)
                        description = editTextDescription.getText().toString().trim();
                    else
                        description = "(brak opisu)";

                    String location;
                    if (editTextLocation.getText().toString().trim().length() > 0)
                        location = editTextLocation.getText().toString().trim();
                    else
                        location = "(brak lokalizacji)";

                    boolean lecture = false;
                    boolean aghEvent = false;
                    int tag = -1;
                    double group = 0;
                    boolean showDateBar = false;

                    Appointment newAppointment = new Appointment(startTimestamp, stopTimestamp, name, description, location, lecture, aghEvent, tag, group, showDateBar);
                    ScheduleUtils.editAppointment(activityContext, oldAppointment, newAppointment);
                    onDestroyView();
                    onResume();
                } catch (ParseException e){
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
        });

        builder.setNegativeButton(R.string.action_cancel, null);

        final AlertDialog dialog = builder.show();

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private int findPositionInArray(CharSequence [] array, Float object){
        for (int i=0; i<array.length; i++){
            if (object.equals(Float.parseFloat(String.valueOf(array[i]))))
                return i;
        }
        return -1;
    }

    private CharSequence [] translateGroupNames(CharSequence [] array){
        CharSequence [] returned = new CharSequence[array.length];

        for (int i=0; i<array.length; i++){
            if (array[i].equals("-1.0"))
                returned[i] = "Wszystkie";
            else if (array[i].equals("0.0"))
                returned[i] = "Tylko wykłady";
            else if (Float.parseFloat(String.valueOf(array[i])) % 1 == 0)
                returned[i] = "Grupa " + String.valueOf(array[i]).split("\\.")[0];
            else if (array[i].equals("9999.0"))
                returned[i] = "Niestandardowa";
            else
                returned[i] = "Grupa " + String.valueOf(array[i]).split("\\.")[0] + String.valueOf((char) (96+Integer.parseInt(array[i].toString().split("\\.")[1])));
        }

        return returned;
    }

    private void refreshSchedule() {
        if (Storage.schedule == null || Storage.schedule.size() == 0){
            // There's no downloaded data. Do that.

            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, root);
        }
        else {
            showSchedule();
        }
    }

    public void refreshScheduleFromMenu(){
        RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
        RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);
        RelativeLayout rlData = root.findViewById(R.id.rlData);
        FloatingActionButton fab = root.findViewById(R.id.fab);

        rlData.setVisibility(View.GONE);
        rlOffline.setVisibility(View.GONE);
        rlLoader.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);

        if (asyncTaskRunnerAutoRefresher != null && !asyncTaskRunnerAutoRefresher.isCancelled())
            asyncTaskRunnerAutoRefresher.cancel(true);

        asyncTaskRunnerAutoRefresher = new AsyncTaskRunnerAutoRefresher();

        Storage.schedule = null;

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, root);
    }

    private void showSchedule(){
        try {
            asyncTaskRunnerAutoRefresher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (IllegalStateException e){
            Log.i("aghwd", "aghwd", e);
        }
    }

    private void setRecyclerViewScheduleAdapter() throws Exception{
        recyclerViewScheduleAdapter = new RecyclerViewScheduleAdapter(activityContext, this);
    }

    private void firstRunRecyclerView(){
        layoutManager = new LinearLayoutManager(activityContext, LinearLayoutManager.VERTICAL, false);
        recyclerViewSchedule = root.findViewById(R.id.recyclerViewSchedule);
        recyclerViewSchedule.setLayoutManager(layoutManager);
        recyclerViewSchedule.setNestedScrollingEnabled(false);
        recyclerViewSchedule.setAdapter(recyclerViewScheduleAdapter);
        recyclerViewScheduleAdapter.showOrHideRecyclerView();
        scrollToNowPosition();
    }

    private void refreshRecyclerView(){
        recyclerViewScheduleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);
        root = inflater.inflate(R.layout.activity_schedule, container, false);

        textView3 = root.findViewById(R.id.textView3);
        textView3bis = root.findViewById(R.id.textView3bis);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEvent();
            }
        });
        fab.setVisibility(View.GONE);

        RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
        rlLoader.setVisibility(View.VISIBLE);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.schedule), null);

        asyncTaskRunnerAutoRefresher = new AsyncTaskRunnerAutoRefresher();
        refreshSchedule();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (asyncTaskRunnerAutoRefresher != null && !asyncTaskRunnerAutoRefresher.isCancelled())
            asyncTaskRunnerAutoRefresher.cancel(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (asyncTaskRunnerAutoRefresher != null && !asyncTaskRunnerAutoRefresher.isCancelled())
            asyncTaskRunnerAutoRefresher.cancel(true);
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                fs = new FetchSchedule();

                return root;
            } catch (Exception e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(View result){
            final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);

            rlLoader.setVisibility(View.GONE);

            if (fs == null || isError){
                Storage.schedule = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshSchedule();
                    }
                });
            }
            else if (fs.status == -1){
                Storage.scheduleStatus = fs.status;
                showSchedule();
            }
            else {
                // Have it, show it
                Storage.scheduleStatus = fs.status;
                showSchedule();
            }

        }

    }

    private class AsyncTaskRunnerAutoRefresher extends AsyncTask<View, View, View> {
        boolean firstRun;

        @Override
        protected View doInBackground(View... params) {
            firstRun = true;

            while (!isCancelled()){
                if (firstRun) {
                    try {
                        setRecyclerViewScheduleAdapter();
                        publishProgress();
                    } catch (Exception e) {
                        Log.i("aghwd", "aghwd", e);
                        Storage.appendCrash(e);
                    }
                }

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Log.i("aghwd", "aghwd", e);
                }

                firstRun = false;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(View... values) {
            if (firstRun) {
                ((MainActivity) activityContext).showScheduleButtons(true, Storage.scheduleStatus);
                firstRunRecyclerView();
            }
            else
                refreshRecyclerView();
        }
    }

}