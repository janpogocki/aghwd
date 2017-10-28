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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;
import java.util.Locale;

import pl.janpogocki.agh.wirtualnydziekanat.javas.Appointment;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchSchedule;
import pl.janpogocki.agh.wirtualnydziekanat.javas.RecyclerViewScheduleAdapter;
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

    public void changeGroup(){
        int checkedItem = findPositionInArray(recyclerViewScheduleAdapter.getListOfGroupIds(),
                PreferenceManager.getDefaultSharedPreferences(activityContext).getFloat(Storage.getUniversityStatusHash() + "_choosen_eaiib_group", -1));
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(R.string.choose_group);
        builder.setSingleChoiceItems(translateGroupNames(recyclerViewScheduleAdapter.getListOfGroupIds()), checkedItem, null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
            boolean defaultValue = i == 3;

            usersCheckedItems[i] = PreferenceManager.getDefaultSharedPreferences(activityContext).getBoolean(Storage.getUniversityStatusHash() + "_schedule_view_settings_" + i, defaultValue);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(R.string.schedule_view_preferences);
        builder.setMultiChoiceItems(R.array.schedule_view_settings_values, usersCheckedItems, null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

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

    public void showEventSettings(Appointment currentAppointment){
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(currentAppointment.name);

        if (currentAppointment.aghEvent) {
            builder.setItems(R.array.schedule_agh_event_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
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
                    dialogInterface.dismiss();
                    onDestroyView();
                    onResume();
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

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextTimeFrom = dialogView.findViewById(R.id.editTextTimeFrom);
        final EditText editTextTimeTo = dialogView.findViewById(R.id.editTextTimeTo);
        final EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);
        final EditText editTextRepeat = dialogView.findViewById(R.id.editTextRepeat);
        final CheckBox checkBoxRepeat = dialogView.findViewById(R.id.checkBoxRepeat);
        TextView textViewRepeat1 = dialogView.findViewById(R.id.textViewRepeat1);
        TextView textViewRepeat2 = dialogView.findViewById(R.id.textViewRepeat2);

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
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
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
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
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mDatePicker = new DatePickerDialog(activityContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        editTextDate.setText(String.format(Locale.US, "%02d.%02d.%04d", selectedDay, selectedMonth, selectedYear));
                    }
                }, year, month, dayOfMonth);
                mDatePicker.show();
            }
        });

        View.OnClickListener textViewRepeatOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextRepeat.requestFocus();
                InputMethodManager imm = (InputMethodManager) activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editTextRepeat, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        };

        textViewRepeat1.setOnClickListener(textViewRepeatOnClickListener);
        textViewRepeat2.setOnClickListener(textViewRepeatOnClickListener);
        checkBoxRepeat.setOnClickListener(textViewRepeatOnClickListener);

        builder.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // todo
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

        editTextRepeat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0 && Integer.parseInt(charSequence.toString().trim()) > 0)
                    checkBoxRepeat.setChecked(true);
                else
                    checkBoxRepeat.setChecked(false);
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
            else
                returned[i] = "Grupa " + String.valueOf(array[i]).split("\\.")[0] + String.valueOf((char) (96+Integer.parseInt(array[i].toString().split("\\.")[1])));
        }

        return returned;
    }

    private void refreshSchedule(View root) {
        if (Storage.schedule == null || Storage.schedule.size() == 0){
            // There's no downloaded data. Do that.
            RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);

            rlLoader.setVisibility(View.VISIBLE);
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(root);

            // wait for change loading subtitle
            animateFadeOut(textView3, root, 3000);
            animateFadeIn(textView3bis, root, 3250);
        }
        else {
            // Have it, show it.
            RelativeLayout rlData = root.findViewById(R.id.rlData);
            FloatingActionButton fab = root.findViewById(R.id.fab);

            rlData.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);

            showSchedule();
        }
    }

    private void showSchedule(){
        ((MainActivity) activityContext).showScheduleButtons(true, Storage.scheduleStatus);

        asyncTaskRunnerAutoRefresher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void firstRunRecyclerView(){
        layoutManager = new LinearLayoutManager(activityContext, LinearLayoutManager.VERTICAL, false);
        recyclerViewSchedule = root.findViewById(R.id.recyclerViewSchedule);
        recyclerViewSchedule.setLayoutManager(layoutManager);
        recyclerViewSchedule.setNestedScrollingEnabled(false);
        recyclerViewScheduleAdapter = new RecyclerViewScheduleAdapter(activityContext, this);
        recyclerViewSchedule.setAdapter(recyclerViewScheduleAdapter);
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

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.schedule), null);

        asyncTaskRunnerAutoRefresher = new AsyncTaskRunnerAutoRefresher();
        refreshSchedule(root);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        asyncTaskRunnerAutoRefresher.cancel(true);
    }

    @Override
    public void onPause() {
        super.onPause();
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
            final RelativeLayout rlData = root.findViewById(R.id.rlData);
            final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
            final FloatingActionButton fab = root.findViewById(R.id.fab);

            rlLoader.setVisibility(View.GONE);

            if (fs == null || isError){
                Storage.groupsAndModules = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshSchedule(root);
                    }
                });
            }
            else if (fs.status == -1){
                Storage.scheduleStatus = fs.status;
                rlNoData.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
            }
            else {
                // Have it, show it
                Storage.scheduleStatus = fs.status;
                rlData.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                showSchedule();
            }

        }

    }

    private class AsyncTaskRunnerAutoRefresher extends AsyncTask<View, View, View> {
        boolean firstRun;

        @Override
        protected View doInBackground(View... params) {
            firstRun = true;
            Log.e("autorefresher", "ok");

            while (!isCancelled()){
                publishProgress();

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }

                firstRun = false;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(View... values) {
            if (firstRun)
                firstRunRecyclerView();
            else
                refreshRecyclerView();
        }
    }

}