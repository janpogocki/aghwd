package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.javas.Appointment;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchSkos;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchTeacherSchedule;
import pl.janpogocki.agh.wirtualnydziekanat.javas.RecyclerViewTeacherScheduleAdapter;
import pl.janpogocki.agh.wirtualnydziekanat.javas.ScheduleUtils;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SkosActivity extends Fragment {

    FirebaseAnalytics mFirebaseAnalytics;
    View root;
    Context activityContext;
    FetchSkos fs;
    FetchTeacherSchedule fts;
    ListView listViewGroups;
    ListAdapter listAdapter;
    RecyclerView recyclerViewTeacherSchedule;
    RecyclerViewTeacherScheduleAdapter recyclerViewTeacherScheduleAdapter;
    LinearLayoutManager layoutManager;
    AsyncTaskRunnerAutoRefresher asyncTaskRunnerAutoRefresher;

    public void searchTyping(String val){
        if (listAdapter != null && listViewGroups != null) {
            List<List<String>> list = filterList(val);
            RelativeLayout rlData = root.findViewById(R.id.rlData);
            RelativeLayout rlNoRecords = root.findViewById(R.id.rlNoRecords);

            if (list != null) {
                rlData.setVisibility(View.VISIBLE);
                rlNoRecords.setVisibility(View.GONE);
                listAdapter = new SearchAdapter(list);
                listViewGroups.setAdapter(listAdapter);
            }
            else {
                // no records
                rlData.setVisibility(View.GONE);
                rlNoRecords.setVisibility(View.VISIBLE);
            }
        }
    }

    private List<List<String>> filterList(String query){
        List<List<String>> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();

        int counter = 0;

        for (int i=0; i<Storage.skosList.get(0).size(); i++){
            if (Storage.skosList.get(0).get(i).toLowerCase().startsWith(query.toLowerCase())){
                list1.add(Storage.skosList.get(0).get(i));
                list2.add(Storage.skosList.get(1).get(i));
                list3.add(Storage.skosList.get(2).get(i));
                counter++;
            }
        }

        if (counter > 0){
            list.add(list1);
            list.add(list2);
            list.add(list3);
            return list;
        }
        else if (counter == 0 && query.length() == 0)
            return Storage.skosList;
        else
            return null;
    }

    private void refreshSkos(View root) {
        if (Storage.skosList == null || Storage.skosList.size() == 0){
            // There's no downloaded data. Do that.
            RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);

            rlLoader.setVisibility(View.VISIBLE);
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            // Have it, show it.
            RelativeLayout rlData = root.findViewById(R.id.rlData);

            rlData.setVisibility(View.VISIBLE);

            showSkos(root);
        }
    }

    public void backButtonPressedWhenBrowserOpened(){
        RelativeLayout rlData = root.findViewById(R.id.rlData);
        RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
        RelativeLayout rlTeacherSchedule = root.findViewById(R.id.rlTeacherSchedule);
        LinearLayout rlBrowser = root.findViewById(R.id.rlBrowser);

        if (rlTeacherSchedule.getVisibility() == View.VISIBLE)
            mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.skos), null);

        rlBrowser.setVisibility(View.GONE);
        rlNoData.setVisibility(View.GONE);
        rlTeacherSchedule.setVisibility(View.GONE);
        rlData.setVisibility(View.VISIBLE);
        ((MainActivity) activityContext).showSearchButton(true);
        Storage.openedBrowser = false;

        if (asyncTaskRunnerAutoRefresher != null && !asyncTaskRunnerAutoRefresher.isCancelled())
            asyncTaskRunnerAutoRefresher.cancel(true);
    }

    public void showEventSettings(final Appointment currentAppointment){
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(currentAppointment.name);

        builder.setItems(R.array.teacher_schedule_event_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    // copy to mycal
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "copy_to_mycal");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "skos_schedule");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    ScheduleUtils.copyAppointment(activityContext, currentAppointment, null);
                }
                else if (i == 1){
                    // copy all like this one to mycal
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "copy_all_to_mycal");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "skos_schedule");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    ScheduleUtils.copyAppointment(activityContext, currentAppointment, recyclerViewTeacherScheduleAdapter.getListOfAppointments());
                }

                dialogInterface.dismiss();
                onDestroyView();
                onResume();
            }
        });

        builder.show();
    }

    private void refreshTeacherSchedule(String nameAndSurname, String url){
        AsyncTaskScheduleBegin runnerBegin = new AsyncTaskScheduleBegin();
        runnerBegin.setVars(nameAndSurname, url, activityContext);
        runnerBegin.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showCaptcha(final String nameAndSurname, final String url) throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.skos_teacher_schedule_captcha, null);
        builder.setView(dialogView);

        ImageView imageViewCaptcha = dialogView.findViewById(R.id.imageViewCaptcha);
        final EditText editTextCaptcha = dialogView.findViewById(R.id.editTextCaptcha);

        builder.setTitle(R.string.rewrite_captcha);
        imageViewCaptcha.setImageBitmap(fts.bitmapCaptcha);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AsyncTaskScheduleAfterCaptcha runnerAfterCaptcha = new AsyncTaskScheduleAfterCaptcha();
                runnerAfterCaptcha.setCaptchaText(editTextCaptcha.getText().toString().trim());
                runnerAfterCaptcha.setNameAndSurname(nameAndSurname);
                runnerAfterCaptcha.setSkosUrl(url);
                runnerAfterCaptcha.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
                LinearLayout rlBrowser = root.findViewById(R.id.rlBrowser);

                rlLoader.setVisibility(View.GONE);
                rlBrowser.setVisibility(View.VISIBLE);
            }
        });

        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.setCanceledOnTouchOutside(false);

        editTextCaptcha.addTextChangedListener(new TextWatcher() {
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

    private void scrollToNowPosition(){
        if (recyclerViewTeacherSchedule != null) {
            recyclerViewTeacherSchedule.post(new Runnable() {
                @Override
                public void run() {
                    layoutManager.scrollToPositionWithOffset(recyclerViewTeacherScheduleAdapter.getLastPastAppointement()+1, 0);
                }
            });
        }
    }

    private void showTeacherSchedule(){
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.skos_teacher_schedule), null);
        asyncTaskRunnerAutoRefresher = new AsyncTaskRunnerAutoRefresher();
        asyncTaskRunnerAutoRefresher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void firstRunRecyclerView(){
        layoutManager = new LinearLayoutManager(activityContext, LinearLayoutManager.VERTICAL, false);
        recyclerViewTeacherSchedule = root.findViewById(R.id.recyclerViewSchedule);
        recyclerViewTeacherSchedule.setLayoutManager(layoutManager);
        recyclerViewTeacherSchedule.setNestedScrollingEnabled(false);
        recyclerViewTeacherScheduleAdapter = new RecyclerViewTeacherScheduleAdapter(activityContext, this);
        recyclerViewTeacherSchedule.setAdapter(recyclerViewTeacherScheduleAdapter);
        scrollToNowPosition();
    }

    private void refreshRecyclerView(){
        recyclerViewTeacherScheduleAdapter.notifyDataSetChanged();
    }

    private void switch2browser(final String _nameAndSurname, final String _url){
        // close searchbar
        ((MainActivity) activityContext).showSearchButton(false);
        ((MainActivity) activityContext).hideKeyboard(getView());

        final RelativeLayout rlData = root.findViewById(R.id.rlData);
        final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
        final LinearLayout rlBrowser = root.findViewById(R.id.rlBrowser);
        WebView webView = root.findViewById(R.id.webView);

        // schedule button
        LinearLayout layoutSearchTeacherSchedule = root.findViewById(R.id.layoutSearchTeacherSchedule);
        layoutSearchTeacherSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshTeacherSchedule(_nameAndSurname, _url);
            }
        });

        rlData.setVisibility(View.GONE);
        rlLoader.setVisibility(View.VISIBLE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(_url);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // check if not domain
                if (!url.equals(_url))
                    view.stopLoading();
            }

            @Override
            public void onPageFinished (WebView webView, String url){
                if (Storage.nightMode)
                    webView.loadUrl("javascript:(function() {var anchors = document.getElementsByTagName(\"a\");for (var i = 0; i < anchors.length; i++) {anchors[i].style.cssText = \"color:#ffffff\";}document.body.innerHTML = document.querySelector(\"div.c-col.vcard\").outerHTML;document.getElementsByTagName(\"body\")[0].style.cssText = \"background:#1E1E1E;color:#ffffff\"})()");
                else
                    webView.loadUrl("javascript:(function() {var anchors = document.getElementsByTagName(\"a\");for (var i = 0; i < anchors.length; i++) {anchors[i].style.cssText = \"color:#555\";}document.body.innerHTML = document.querySelector(\"div.c-col.vcard\").outerHTML;})()");

                if (webView.getProgress() == 100){
                    rlLoader.setVisibility(View.GONE);
                    rlBrowser.setVisibility(View.VISIBLE);

                    Storage.openedBrowser = true;
                }
            }
        });
    }

    private void showSkos(final View root){
        ((MainActivity) activityContext).showSearchButton(true);
        listViewGroups = root.findViewById(R.id.listViewGroups);

        listAdapter = new SearchAdapter(Storage.skosList);
        listViewGroups.setAdapter(listAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);

        root = inflater.inflate(R.layout.activity_skos, container, false);

        refreshSkos(root);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (!asyncTaskRunnerAutoRefresher.isCancelled())
            asyncTaskRunnerAutoRefresher.cancel(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!asyncTaskRunnerAutoRefresher.isCancelled())
            asyncTaskRunnerAutoRefresher.cancel(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.skos), null);

        asyncTaskRunnerAutoRefresher = new AsyncTaskRunnerAutoRefresher();
    }

    private class SearchAdapter extends BaseAdapter {
        List<List<String>> list;

        public SearchAdapter(List<List<String>> _list){
            list = _list;
        }

        @Override
        public int getCount() {
            return list.get(0).size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String nameAndSurname = list.get(0).get(position);
            String functions = list.get(1).get(position);
            final String personURL = list.get(2).get(position);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) root.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.summary_list_item, parent, false);
            }

            TextView textViewHeader = convertView.findViewById(R.id.textViewHeader);
            TextView textView2ndLine = convertView.findViewById(R.id.textView2ndLine);
            TextView textView3rdLine = convertView.findViewById(R.id.textView3rdLine);
            LinearLayout rlDataItem = convertView.findViewById(R.id.rlDataItem);

            rlDataItem.setFocusable(true);
            rlDataItem.setClickable(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                rlDataItem.setBackground(activityContext.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}).getDrawable(0));
            else
                rlDataItem.setBackgroundDrawable(activityContext.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}).getDrawable(0));

            textViewHeader.setText(nameAndSurname);
            textView2ndLine.setText(functions);
            textView3rdLine.setText(personURL);
            textView3rdLine.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView2ndLine.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
            }
            else
                textView2ndLine.setTextAppearance(root.getContext(), R.style.TextAppearance_AppCompat_Small);

            rlDataItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch2browser(nameAndSurname, personURL);
                }
            });

            return convertView;
        }
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                fs = new FetchSkos(activityContext);

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

            rlLoader.setVisibility(View.GONE);

            if (fs == null || fs.status == -1 || isError){
                Storage.skosList = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshSkos(root);
                    }
                });
            }
            else {
                // Have it, show it
                rlData.setVisibility(View.VISIBLE);
                showSkos(root);
            }

        }
    }

    private class AsyncTaskScheduleBegin extends AsyncTask<View, View, View> {
        private String nameAndSurname;
        private String skosUrl;
        private Context c;
        Boolean isError = false;

        public void setVars(String nameAndSurname, String skosUrl, Context c) {
            this.nameAndSurname = nameAndSurname;
            this.skosUrl = skosUrl;
            this.c = c;
        }

        @Override
        protected void onPreExecute() {
            final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
            final LinearLayout rlBrowser = root.findViewById(R.id.rlBrowser);

            rlBrowser.setVisibility(View.GONE);
            rlLoader.setVisibility(View.VISIBLE);

            Storage.openedBrowser = false;
        }

        @Override
        protected View doInBackground(View... views) {
            try {
                fts = new FetchTeacherSchedule(c, nameAndSurname, skosUrl);

                return root;
            } catch (Exception e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(View view) {
            final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
            final RelativeLayout rlTeacherSchedule = root.findViewById(R.id.rlTeacherSchedule);

            rlLoader.setVisibility(View.GONE);
            Storage.openedBrowser = true;

            if (fts == null || isError){
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshTeacherSchedule(nameAndSurname, skosUrl);
                    }
                });
            }
            // -1 means no data to show
            else if (fts.status == -1){
                rlNoData.setVisibility(View.VISIBLE);
            }
            // -2 means WU.XP captcha
            else if (fts.status == -2){
                try {
                    showCaptcha(nameAndSurname, skosUrl);
                } catch (Exception e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
            else {
                // Have it, show it
                rlTeacherSchedule.setVisibility(View.VISIBLE);
                showTeacherSchedule();
            }
        }
    }

    private class AsyncTaskScheduleAfterCaptcha extends AsyncTask<View, View, View> {
        Boolean isError = false;
        private String captchaText;
        private String nameAndSurname;
        private String skosUrl;

        public void setNameAndSurname(String nameAndSurname) {
            this.nameAndSurname = nameAndSurname;
        }

        public void setCaptchaText(String captchaText) {
            this.captchaText = captchaText;
        }

        public void setSkosUrl(String skosUrl) {
            this.skosUrl = skosUrl;
        }

        @Override
        protected void onPreExecute() {
            final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
            rlLoader.setVisibility(View.VISIBLE);
            Storage.openedBrowser = false;
        }

        @Override
        protected View doInBackground(View... views) {
            try {
                fts.continueFetchDziekanatXPScheduleAfterCaptcha(captchaText);

                return root;
            } catch (Exception e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(View view) {
            final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
            final RelativeLayout rlTeacherSchedule = root.findViewById(R.id.rlTeacherSchedule);

            rlLoader.setVisibility(View.GONE);
            Storage.openedBrowser = true;

            if (fts == null || isError){
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshTeacherSchedule(nameAndSurname, skosUrl);
                    }
                });
            }
            // -1 means no data to show
            else if (fts.status == -1){
                rlNoData.setVisibility(View.VISIBLE);
            }
            // -2 means WU.XP captcha
            else if (fts.status == -2){
                try {
                    showCaptcha(nameAndSurname, skosUrl);
                } catch (Exception e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
            // -3 means WU.XP bad captcha - once again all
            else if (fts.status == -3){
                fts = null;
                refreshTeacherSchedule(nameAndSurname, skosUrl);
            }
            else {
                // Have it, show it
                rlTeacherSchedule.setVisibility(View.VISIBLE);
                showTeacherSchedule();
            }
        }
    }

    private class AsyncTaskRunnerAutoRefresher extends AsyncTask<View, View, View> {
        boolean firstRun;

        @Override
        protected View doInBackground(View... params) {
            firstRun = true;

            while (!isCancelled()){
                publishProgress();

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
            if (firstRun)
                firstRunRecyclerView();
            else
                refreshRecyclerView();
        }
    }

}
