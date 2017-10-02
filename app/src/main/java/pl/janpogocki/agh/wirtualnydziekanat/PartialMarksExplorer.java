package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.javas.AnimatedExpandableListView;
import pl.janpogocki.agh.wirtualnydziekanat.javas.ExpandableListAdapterPartialMarks;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchPartialMarks;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchWebsite;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Logging;
import pl.janpogocki.agh.wirtualnydziekanat.javas.POSTgenerator;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class PartialMarksExplorer extends Fragment {

    View root;
    FirebaseAnalytics mFirebaseAnalytics;
    ExpandableListAdapterPartialMarks listAdapter;
    List<List<String>> listDataHeader;
    HashMap<String, List<List<String>>> listDataChild;
    FetchPartialMarks fpm = null;
    Context activityContext;

    RelativeLayout rlLoader, rlData;
    TextView textView3, textView3bis;

    Boolean goBack = false;
    String viewstateName = "__VIEWSTATE";
    String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    String eventValidationName = "__EVENTVALIDATION";
    String eventTargetName = "__EVENTTARGET";
    String asyncPostName = "__ASYNCPOST";
    String buttonBackName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butPop";
    String buttonForwardName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butNas";
    String scriptManager1Name = "ctl00$ctl00$ScriptManager1";
    String scriptManager1Pattern = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$Panel2Panel|";
    String expandAllName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$chb_ExpColAll";

    private void animateFadeIn(RelativeLayout layout, View view, int offset){
        Animation afi = AnimationUtils.loadAnimation(view.getContext(), R.anim.fadein);
        layout.setAnimation(afi);
        afi.setDuration(500);
        afi.setStartOffset(offset);
        afi.start();
    }

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

    public void exploreMarks(View view) {
        prepareListData();

        listAdapter = new ExpandableListAdapterPartialMarks(activityContext, listDataHeader, listDataChild);

        AnimatedExpandableListView expandableListView = view.findViewById(R.id.expandableListView);
        expandableListView.setAdapter(listAdapter);
    }

    private void showResults(View view){
        RelativeLayout relativeLayoutExpListView;
        relativeLayoutExpListView = view.findViewById(R.id.relativeLayoutExpListView);
        animateFadeIn(relativeLayoutExpListView, view, 500);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding headers data
        listDataHeader = fpm.getHeaders();

        // Adding child data (header, child)
        listDataChild = fpm.getChildren();
    }

    private void refreshMarks(View root) {
        for (int i=0; i<Storage.summarySemesters.size()-1; i++){
            Storage.currentSemesterPartialMarksHTML.remove(i);
        }

        Storage.currentSemesterListPointerPartialMarks = Storage.summarySemesters.size()-1;
        PartialMarksExplorer.AsyncTaskRunner runner = new PartialMarksExplorer.AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);
    }

    private List<String> fetchAjaxResponse(String _viewstateValue, String _viewstateGeneratorValue, String _eventValidationValue, String _fww) throws Exception {
        List<String> tempCurrentSemesterPartialMarksHTML = new ArrayList<>();
        String viewstateValue = _viewstateValue;
        String viewstateGeneratorValue = _viewstateGeneratorValue;
        String eventValidationValue = _eventValidationValue;
        String fww = _fww;

        // Get number of pages; if it can't be counted - save "0" to tempCurrentSemesterPartialMarksHTML
        Document fwParsed = Jsoup.parse(fww);
        Elements elementsNumberOfPages = fwParsed.select("div.rgWrap.rgInfoPart");

        if (elementsNumberOfPages.isEmpty()){
            tempCurrentSemesterPartialMarksHTML.add("0");
        }
        else {
            int numberOfPages = Integer.valueOf(elementsNumberOfPages.text().split(",")[0].split(" z ")[1]);

            // Expand marks
            POSTgenerator POSTgenerator = new POSTgenerator();
            try {
                POSTgenerator.add(viewstateName, viewstateValue);
                POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
                POSTgenerator.add(eventValidationName, eventValidationValue);
                POSTgenerator.add(asyncPostName, "true");
                POSTgenerator.add(scriptManager1Name, scriptManager1Pattern + expandAllName);
                POSTgenerator.add(eventTargetName, expandAllName);
                POSTgenerator.add(expandAllName, "on");
            } catch (UnsupportedEncodingException e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }
            String data = POSTgenerator.getGeneratedPOST();
            FetchWebsite fw = new FetchWebsite(Logging.URLdomain + "/OcenyCzast.aspx");
            fww = fw.getWebsite(true, true, data);

            // save changed ASP.NET's parameters for next iterations
            viewstateValue = fww.split("\\|" + viewstateName + "\\|")[1].split("\\|")[0];
            viewstateGeneratorValue = fww.split("\\|" + viewstateGeneratorName + "\\|")[1].split("\\|")[0];
            eventValidationValue = fww.split("\\|" + eventValidationName + "\\|")[1].split("\\|")[0];

            // Get name of 'next page' button // 3 i 4 dlatego, bo "| Storna"
            fwParsed = Jsoup.parse(fww.split("\\|")[3] + fww.split("\\|")[4]);
            String nextPageBtnName = fwParsed.getElementsByClass("rgPageNext").attr("name");

            tempCurrentSemesterPartialMarksHTML.add(fww.split("\\|")[3] + fww.split("\\|")[4]);

            // Loop saving next subpages
            for (int i = 0; i < numberOfPages-1; i++) {
                // Send POST next btn
                POSTgenerator = new POSTgenerator();
                try {
                    POSTgenerator.add(viewstateName, viewstateValue);
                    POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
                    POSTgenerator.add(eventValidationName, eventValidationValue);
                    POSTgenerator.add(asyncPostName, "true");
                    POSTgenerator.add(scriptManager1Name, scriptManager1Pattern + nextPageBtnName);
                    POSTgenerator.add(eventTargetName, nextPageBtnName);
                    POSTgenerator.add(expandAllName, "on");
                } catch (UnsupportedEncodingException e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
                data = POSTgenerator.getGeneratedPOST();
                fw = new FetchWebsite(Logging.URLdomain + "/OcenyCzast.aspx");
                fww = fw.getWebsite(true, true, data);

                // save changed ASP.NET's parameters for next iterations
                viewstateValue = fww.split("\\|" + viewstateName + "\\|")[1].split("\\|")[0];
                viewstateGeneratorValue = fww.split("\\|" + viewstateGeneratorName + "\\|")[1].split("\\|")[0];
                eventValidationValue = fww.split("\\|" + eventValidationName + "\\|")[1].split("\\|")[0];

                tempCurrentSemesterPartialMarksHTML.add(fww.split("\\|")[3] + fww.split("\\|")[4]);
            }
        }

        return tempCurrentSemesterPartialMarksHTML;
    }

    private void goThroughSemester() throws Exception {
        for (int i=0; i<Storage.summarySemesters.size()-1; i++){
            Storage.currentSemesterPartialMarksHTML.remove(i);
        }

        Storage.currentSemesterListPointerPartialMarks = Storage.summarySemesters.size()-1;
        FetchWebsite fw = new FetchWebsite(Logging.URLdomain + "/OcenyCzast.aspx");
        String fww = fw.getWebsite(true, true, "");

        Document fwParsed = Jsoup.parse(fww);
        String viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
        String viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
        String eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

        if (Storage.currentSemester < Storage.currentSemesterListPointerPartialMarks) {
            // go back
            goBack = true;
        }

        Boolean ajaxResponse = false;
        do {
            if (Storage.currentSemester == Storage.currentSemesterListPointerPartialMarks) {
                if (Storage.currentSemesterPartialMarksHTML.get(Storage.currentSemesterListPointerPartialMarks) == null){
                    // expand marks, count pages, save all pages to Storage
                    if (ajaxResponse)
                        Storage.currentSemesterPartialMarksHTML.put(Storage.currentSemesterListPointerPartialMarks, fetchAjaxResponse(viewstateValue, viewstateGeneratorValue, eventValidationValue, fww.split("\\|")[3] + "|" + fww.split("\\|")[4]));
                    else
                        Storage.currentSemesterPartialMarksHTML.put(Storage.currentSemesterListPointerPartialMarks, fetchAjaxResponse(viewstateValue, viewstateGeneratorValue, eventValidationValue, fww));

                    break;
                }
                else {
                    break;
                }
            }
            else {
                if (goBack)
                    Storage.currentSemesterListPointerPartialMarks--;
                else
                    Storage.currentSemesterListPointerPartialMarks++;

                // step +/- 1 semester
                if (Storage.currentSemesterPartialMarksHTML.get(Storage.currentSemesterListPointerPartialMarks) == null){
                    POSTgenerator POSTgenerator = new POSTgenerator();
                    try {
                        POSTgenerator.add(viewstateName, viewstateValue);
                        POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
                        POSTgenerator.add(eventValidationName, eventValidationValue);
                        POSTgenerator.add(asyncPostName, "true");

                        if (goBack) {
                            POSTgenerator.add(eventTargetName, buttonBackName);
                            POSTgenerator.add(scriptManager1Name, scriptManager1Pattern + buttonBackName);
                        }
                        else {
                            POSTgenerator.add(eventTargetName, buttonForwardName);
                            POSTgenerator.add(scriptManager1Name, scriptManager1Pattern + buttonForwardName);
                        }

                    } catch (UnsupportedEncodingException e) {
                        Log.i("aghwd", "aghwd", e);
                        Storage.appendCrash(e);
                    }

                    String data = POSTgenerator.getGeneratedPOST();

                    fw = new FetchWebsite(Logging.URLdomain + "/OcenyCzast.aspx");
                    fww = fw.getWebsite(true, true, data);

                    // save changed ASP.NET's parameters for next iterations
                    viewstateValue = fww.split("\\|" + viewstateName + "\\|")[1].split("\\|")[0];
                    viewstateGeneratorValue = fww.split("\\|" + viewstateGeneratorName + "\\|")[1].split("\\|")[0];
                    eventValidationValue = fww.split("\\|" + eventValidationName + "\\|")[1].split("\\|")[0];
                }
            }
            ajaxResponse = true;
        } while (true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    // MAIN THREAD
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);

        root = inflater.inflate(R.layout.activity_partial_marks_explorer, container, false);
        rlLoader = root.findViewById(R.id.rlLoader);
        rlData = root.findViewById(R.id.rlData);
        textView3 = root.findViewById(R.id.textView3);
        textView3bis = root.findViewById(R.id.textView3bis);

        // do in background
        rlLoader.setVisibility(View.VISIBLE);
        PartialMarksExplorer.AsyncTaskRunner runner = new PartialMarksExplorer.AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.partial_marks), null);
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                goThroughSemester();
                fpm = new FetchPartialMarks(Storage.currentSemesterPartialMarksHTML.get(Storage.currentSemester));

                publishProgress();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }

                return root;
            } catch (Exception e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
                isError = true;
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(View... params){
            exploreMarks(root);
            rlLoader.setVisibility(View.GONE);

        }

        @Override
        protected void onPostExecute(View result){
            final RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
            final SwipeRefreshLayout srl = root.findViewById(R.id.swiperefresh);

            srl.setColorSchemeResources(R.color.colorPrimary);
            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "partial_marks");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "swipe_refresh");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    rlData.setVisibility(View.GONE);
                    rlLoader.setVisibility(View.VISIBLE);
                    srl.setRefreshing(false);
                    srl.setEnabled(false);
                    refreshMarks(root);
                    srl.setEnabled(true);
                }
            });

            if (fpm == null || isError){
                rlOffline.setVisibility(View.VISIBLE);
                rlLoader.setVisibility(View.GONE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);

                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshMarks(root);
                    }
                });
            }
            else if (fpm.status == -1) {
                rlNoData.setVisibility(View.VISIBLE);
                rlLoader.setVisibility(View.GONE);
            }
            else {
                final AnimatedExpandableListView expandableListView = root.findViewById(R.id.expandableListView);
                rlData.setVisibility(View.VISIBLE);
                showResults(root);

                expandableListView.setOnGroupClickListener(new AnimatedExpandableListView.OnGroupClickListener() {

                    int previousGroup = -1;

                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {
                        int diff = 0;

                        if (previousGroup != -1 && previousGroup != groupPosition){
                            expandableListView.collapseGroupWithAnimation(previousGroup);

                            expandableListView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    expandableListView.setSelection(groupPosition);
                                }
                            }, 50);
                        }
                        else {
                            diff = 350;
                            previousGroup = -1;
                        }

                        expandableListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (expandableListView.isGroupExpanded(groupPosition)) {
                                    expandableListView.collapseGroupWithAnimation(groupPosition);
                                } else {
                                    expandableListView.expandGroupWithAnimation(groupPosition);
                                }
                            }
                        }, 350-diff);

                        previousGroup = groupPosition;

                        expandableListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                expandableListView.smoothScrollToPositionFromTop(groupPosition, 0);
                            }
                        }, 650-diff);

                        return true;
                    }
                });
            }

        }

    }

}
