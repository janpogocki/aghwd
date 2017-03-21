package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.javas.ExpandableListAdapterPartialMarks;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchPartialMarks;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchWebsite;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Logging;
import pl.janpogocki.agh.wirtualnydziekanat.javas.POSTgenerator;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class PartialMarksExplorer extends Fragment {

    ExpandableListAdapterPartialMarks listAdapter;
    List<List<String>> listDataHeader;
    HashMap<String, List<List<String>>> listDataChild;
    FetchPartialMarks fpm = null;

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

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

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

        listAdapter = new ExpandableListAdapterPartialMarks(getContext(), listDataHeader, listDataChild);

        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        expandableListView.setAdapter(listAdapter);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showResults(View view){
        RelativeLayout relativeLayoutExpListView;
        relativeLayoutExpListView = (RelativeLayout) view.findViewById(R.id.relativeLayoutExpListView);
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

    private void refreshMarks(ViewGroup root) {
        for (int i=0; i<Storage.summarySemesters.size()-1; i++){
            Storage.currentSemesterPartialMarksHTML.remove(i);
        }

        Storage.currentSemesterListPointerPartialMarks = Storage.summarySemesters.size()-1;
        PartialMarksExplorer.AsyncTaskRunner runner = new PartialMarksExplorer.AsyncTaskRunner();
        runner.execute(root);
    }

    private List<String> fetchAjaxResponse(String _viewstateValue, String _viewstateGeneratorValue, String _eventValidationValue, String _fww) {
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
                e.printStackTrace();
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
                    e.printStackTrace();
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

    private void goThroughSemester() {
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
                        e.printStackTrace();
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

    // MAIN THREAD
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_partial_marks_explorer, null);
        rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);
        rlData = (RelativeLayout) root.findViewById(R.id.rlData);
        textView3 = (TextView) root.findViewById(R.id.textView3);
        textView3bis = (TextView) root.findViewById(R.id.textView3bis);

        // do in background
        rlLoader.setVisibility(View.VISIBLE);
        PartialMarksExplorer.AsyncTaskRunner runner = new PartialMarksExplorer.AsyncTaskRunner();
        runner.execute(root);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);

        return root;
    }

    private class AsyncTaskRunner extends AsyncTask<ViewGroup, ViewGroup, ViewGroup> {
        ViewGroup root;
        Boolean isError = false;

        @Override
        protected ViewGroup doInBackground(ViewGroup... params) {
            try {
                root = params[0];

                goThroughSemester();
                fpm = new FetchPartialMarks(Storage.currentSemesterPartialMarksHTML.get(Storage.currentSemester));

                publishProgress();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return root;
            } catch (Exception e) {
                isError = true;
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(ViewGroup... params){
            exploreMarks(root);
            rlLoader.setVisibility(View.GONE);

        }

        @Override
        protected void onPostExecute(ViewGroup result){
            final RelativeLayout rlOffline = (RelativeLayout) root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = (RelativeLayout) root.findViewById(R.id.rlNoData);
            final SwipeRefreshLayout srl = (SwipeRefreshLayout) root.findViewById(R.id.swiperefresh);

            srl.setColorSchemeResources(R.color.colorPrimary);
            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
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
                Snackbar.make(root.findViewById(R.id.relativeLayoutMain), R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);

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
                final ExpandableListView expandableListView = (ExpandableListView) root.findViewById(R.id.expandableListView);
                rlData.setVisibility(View.VISIBLE);
                showResults(root);

                expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                    int previousGroup = -1;

                    @Override
                    public void onGroupExpand(int groupPosition) {
                        if(groupPosition > previousGroup && previousGroup != -1) {
                            expandableListView.setSelection(0);
                            expandableListView.collapseGroup(previousGroup);
                        }
                        else if (groupPosition < previousGroup && previousGroup != -1){
                            expandableListView.collapseGroup(previousGroup);
                            expandableListView.setSelection(0);
                        }
                        previousGroup = groupPosition;

                        expandableListView.setSelection(groupPosition);
                    }
                });
            }

        }

    }

}
