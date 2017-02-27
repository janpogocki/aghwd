package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchSchedule;
import pl.janpogocki.agh.wirtualnydziekanat.javas.POSTgenerator;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class ScheduleActivity extends Fragment {

    FetchSchedule fs;
    ViewGroup root;
    String postValue = "";
    String viewstateName = "__VIEWSTATE";
    String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    String eventValidationName = "__EVENTVALIDATION";

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

    public void changeWeek(String direction) {
        ((MainActivity) getActivity()).showScheduleButtons(false);
        RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);
        RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
        RelativeLayout rlNoData = (RelativeLayout) root.findViewById(R.id.rlNoData);

        rlLoader.setVisibility(View.VISIBLE);
        rlData.setVisibility(View.GONE);
        rlNoData.setVisibility(View.GONE);

        // dates
        Calendar cal = Calendar.getInstance();
        String todayDay = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        todayDay = (todayDay.length()==1 ? ("0"+todayDay):(todayDay));

        String todayMonth = String.valueOf(cal.get(Calendar.MONTH)+1);
        todayMonth = (todayMonth.length()==1 ? ("0"+todayMonth):(todayMonth));

        String todayYear = String.valueOf(cal.get(Calendar.YEAR));

        // post generator
        POSTgenerator POSTgenerator = new POSTgenerator();

        try {
            POSTgenerator.add(viewstateName, fs.getViewstateValue());
            POSTgenerator.add(viewstateGeneratorName, fs.getViewstateGeneratorValue());
            POSTgenerator.add(eventValidationName, fs.getEventValidationValue());

            if (direction.equals("Poprzedni"))
                POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butP", direction);
            else
                POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butN", direction);

            POSTgenerator.add("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$rbJak", "Tygodniowo");

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
            e.printStackTrace();
        }

        postValue = POSTgenerator.getGeneratedPOST();

        rlLoader.setVisibility(View.VISIBLE);
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(root);
    }

    private void refreshSchedule(ViewGroup root) {
        if (Storage.schedule == null || Storage.schedule.size() == 0){
            // There's no downloaded data. Do that.
            RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);

            rlLoader.setVisibility(View.VISIBLE);
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(root);
        }
        else {
            // Have it, show it.
            RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);

            rlData.setVisibility(View.VISIBLE);

            showSchedule(root);
        }
    }

    private void showSchedule(final ViewGroup root){
        ((MainActivity) getActivity()).showScheduleButtons(true);
        ListView listViewGroups = (ListView) root.findViewById(R.id.listViewGroups);

        ListAdapter listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return Storage.schedule.size();
            }

            @Override
            public Object getItem(int position) {
                return Storage.schedule.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String dateSchedule = Storage.schedule.get(position).get(0);
                String hourFromSchedule = Storage.schedule.get(position).get(1);
                String hourToSchedule = Storage.schedule.get(position).get(2);
                String subjectSchedule = Storage.schedule.get(position).get(3);
                String teacherSchedule = Storage.schedule.get(position).get(4);
                String roomSchedule = Storage.schedule.get(position).get(5);
                String formSubjectSchedule = Storage.schedule.get(position).get(6);

                if (convertView == null) {
                    LayoutInflater infalInflater = (LayoutInflater) root.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater.inflate(R.layout.summary_list_item, null);
                }

                TextView textViewHeader = (TextView) convertView.findViewById(R.id.textViewHeader);
                TextView textView2ndLine = (TextView) convertView.findViewById(R.id.textView2ndLine);
                TextView textView3rdLine = (TextView) convertView.findViewById(R.id.textView3rdLine);

                textViewHeader.setText(dateSchedule + "\n" + hourFromSchedule + " - " + hourToSchedule);
                textView2ndLine.setText(subjectSchedule + " (" + formSubjectSchedule + ")\n" + teacherSchedule);
                textView3rdLine.setText(roomSchedule);

                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
                String dateAndTimeOfEndOfLesson = dateSchedule.split(" ")[0] + " " + hourToSchedule;
                java.text.DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                Date dateLesson = null;

                try {
                    dateLesson = df.parse(dateAndTimeOfEndOfLesson);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date nowDate = new Date();

                if (nowDate.before(dateLesson))
                    checkBox.setChecked(false);
                else
                    checkBox.setChecked(true);

                return convertView;
            }
        };

        listViewGroups.setAdapter(listAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.activity_groups, null);

        refreshSchedule(root);

        return root;
    }

    private class AsyncTaskRunner extends AsyncTask<ViewGroup, ViewGroup, ViewGroup> {
        ViewGroup root;
        Boolean isError = false;

        @Override
        protected ViewGroup doInBackground(ViewGroup... params) {
            try {
                root = params[0];

                fs = new FetchSchedule(postValue);

                return root;
            } catch (Exception e) {
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(ViewGroup result){
            final RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
            final RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = (RelativeLayout) root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = (RelativeLayout) root.findViewById(R.id.rlNoData);

            rlLoader.setVisibility(View.GONE);

            if (fs == null || isError){
                Storage.groupsAndModules = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root.findViewById(R.id.activity_groups), "Problem z połączeniem sieciowym", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

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
                ((MainActivity) getActivity()).showScheduleButtons(true);
                rlNoData.setVisibility(View.VISIBLE);
            }
            else {
                // Have it, show it
                rlData.setVisibility(View.VISIBLE);
                showSchedule(root);
            }

        }

    }

}