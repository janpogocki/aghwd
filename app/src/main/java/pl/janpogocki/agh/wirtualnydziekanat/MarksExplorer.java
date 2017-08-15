package pl.janpogocki.agh.wirtualnydziekanat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.javas.ExpandableListAdapter;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchMarks;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchWebsite;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Logging;
import pl.janpogocki.agh.wirtualnydziekanat.javas.POSTgenerator;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class MarksExplorer extends Fragment {

    FirebaseAnalytics mFirebaseAnalytics;
    ExpandableListAdapter listAdapter;
    List<List<String>> listDataHeader;
    HashMap<String, List<List<String>>> listDataChild;
    FetchMarks fm;

    RelativeLayout rlLoader, rlData;
    TextView textView3, textView3bis;

    Boolean goBack = false;
    String viewstateName = "__VIEWSTATE";
    String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    String eventValidationName = "__EVENTVALIDATION";
    String buttonBackName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butPop";
    String buttonForwardName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butNas";

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

    private void setProgressAnimate(ProgressBar pb, int progressTo) {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", 0, progressTo * 100);
        animation.setDuration(1500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setStartDelay(900);
        animation.start();
    }

    private void animateTextView(final TextView textview, int initialValue, int finalValue, final Boolean isDouble) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.setDuration(1500);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (isDouble)
                    textview.setText(String.valueOf(Double.parseDouble(valueAnimator.getAnimatedValue().toString())/100));
                else
                    textview.setText(valueAnimator.getAnimatedValue().toString());
            }
        });

        valueAnimator.setStartDelay(900);
        valueAnimator.start();
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

        listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);

        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        expandableListView.setAdapter(listAdapter);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showResults(View view){
        TextView textViewAvgSemester, textViewAvgYear, textViewECTS;
        ProgressBar progressBarAvgSemester, progressBarAvgYear, progressBarECTS;
        RelativeLayout relativeLayoutProgressBars, relativeLayoutExpListView;

        textViewAvgSemester = (TextView) view.findViewById(R.id.textViewAvgSemester);
        textViewAvgYear = (TextView) view.findViewById(R.id.textViewAvgYear);
        textViewECTS = (TextView) view.findViewById(R.id.textViewECTS);
        progressBarAvgSemester = (ProgressBar) view.findViewById(R.id.progressBarAvgSemester);
        progressBarAvgYear = (ProgressBar) view.findViewById(R.id.progressBarAvgYear);
        progressBarECTS = (ProgressBar) view.findViewById(R.id.progressBarECTS);
        relativeLayoutProgressBars = (RelativeLayout) view.findViewById(R.id.relativeLayoutProgressBars);
        relativeLayoutExpListView = (RelativeLayout) view.findViewById(R.id.relativeLayoutExpListView);

        textViewAvgSemester.setText("0.0");
        textViewAvgYear.setText("0.0");
        textViewECTS.setText("0");
        progressBarAvgSemester.setProgress(0);
        progressBarAvgYear.setProgress(0);
        progressBarECTS.setProgress(0);

        animateFadeIn(relativeLayoutProgressBars, view, 500);
        animateFadeIn(relativeLayoutExpListView, view, 1200);

        animateTextView(textViewAvgSemester, 0, (int) (fm.amountAvgSemester*100), true);
        animateTextView(textViewAvgYear, 0, (int) (fm.amountAvgYear*100), true);
        animateTextView(textViewECTS, 0, fm.amountECTS, false);

        setProgressAnimate(progressBarAvgSemester, (int) ((fm.amountAvgSemester*100)-200));
        setProgressAnimate(progressBarAvgYear, (int) ((fm.amountAvgYear*100)-200));
        setProgressAnimate(progressBarECTS, fm.amountECTS*10);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding headers data
        listDataHeader = fm.getHeaders();

        // Adding child data (header, child)
        listDataChild = fm.getChildren();
    }

    private void refreshMarks(ViewGroup root) {
        for (int i=0; i<Storage.summarySemesters.size()-1; i++){
            Storage.currentSemesterHTML.remove(i);
        }

        Storage.currentSemesterListPointer = Storage.summarySemesters.size()-1;
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(root);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);
    }

    private void goThroughSemester() {
        if (Storage.currentSemester < Storage.currentSemesterListPointer) {
            // go back
            goBack = true;
        }

        while (Storage.currentSemester != Storage.currentSemesterListPointer){
            String tempCurrentSemesterHTML = Storage.currentSemesterHTML.get(Storage.currentSemesterListPointer);

            if (goBack)
                Storage.currentSemesterListPointer--;
            else
                Storage.currentSemesterListPointer++;

            // step +/- 1 semester
            if (Storage.currentSemesterHTML.get(Storage.currentSemesterListPointer) == null){
                // get and save semester HTML
                Document fwParsed = Jsoup.parse(tempCurrentSemesterHTML);
                String viewstateValue = fwParsed.getElementById(viewstateName).attr("value");
                String viewstateGeneratorValue = fwParsed.getElementById(viewstateGeneratorName).attr("value");
                String eventValidationValue = fwParsed.getElementById(eventValidationName).attr("value");

                POSTgenerator POSTgenerator = new POSTgenerator();
                try {
                    POSTgenerator.add(viewstateName, viewstateValue);
                    POSTgenerator.add(viewstateGeneratorName, viewstateGeneratorValue);
                    POSTgenerator.add(eventValidationName, eventValidationValue);

                    if (goBack) {
                        String buttonBackValue = "Poprzedni";
                        POSTgenerator.add(buttonBackName, buttonBackValue);
                    }
                    else {
                        String buttonForwardValue = "Następny";
                        POSTgenerator.add(buttonForwardName, buttonForwardValue);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String data = POSTgenerator.getGeneratedPOST();

                FetchWebsite fw = new FetchWebsite(Logging.URLdomain + "/OcenyP.aspx");
                String fww = fw.getWebsite(true, true, data);
                Storage.currentSemesterHTML.put(Storage.currentSemesterListPointer, fww);
            }
        }
    }

    // MAIN THREAD
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_marks_explorer, null);
        rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);
        rlData = (RelativeLayout) root.findViewById(R.id.rlData);
        textView3 = (TextView) root.findViewById(R.id.textView3);
        textView3bis = (TextView) root.findViewById(R.id.textView3bis);

        // do in background

        if ("semester".equals(Storage.sharedPreferencesStartScreen) && Storage.firstRunMarksExplorer) {
            Storage.firstRunMarksExplorer = false;
        }
        else {
            rlLoader.setVisibility(View.VISIBLE);
            Storage.firstRunMarksExplorer = false;
        }

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(root);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.menu_marks), null);
    }

    private class AsyncTaskRunner extends AsyncTask<ViewGroup, ViewGroup, ViewGroup> {
        ViewGroup root;
        Boolean isError = false;

        @Override
        protected ViewGroup doInBackground(ViewGroup... params) {
            try {
                root = params[0];
                goThroughSemester();
                fm = new FetchMarks(Storage.currentSemesterHTML.get(Storage.currentSemester));

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
                    Bundle bundle = new Bundle();
                    bundle.putString("activity", "marks");
                    mFirebaseAnalytics.logEvent("swipe_refresh", bundle);

                    rlData.setVisibility(View.GONE);
                    rlLoader.setVisibility(View.VISIBLE);
                    srl.setRefreshing(false);
                    srl.setEnabled(false);
                    refreshMarks(root);
                    srl.setEnabled(true);
                }
            });

            if (fm == null || isError){
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
            else if (fm.status == -1) {
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

                // TODO scrollable progressbars?
                /*expandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    int mPosition = 0;
                    int mOffset = 0;
                    int roznica = 0;

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        int position = expandableListView.getFirstVisiblePosition();
                        View v = expandableListView.getChildAt(0);
                        int offset = (v == null) ? 0 : v.getTop();

                        RelativeLayout.LayoutParams rlpbParams = (RelativeLayout.LayoutParams) relativeLayoutProgressBars.getLayoutParams();

                        if (mPosition < position || (mPosition == position && mOffset < offset)){
                            // Scrolled up
                            roznica = roznica+scrollState;
                            rlpbParams.setMargins(0, roznica, 0, 0);
                            relativeLayoutProgressBars.setLayoutParams(rlpbParams);

                        } else {
                            // Scrolled down
                            roznica = roznica-scrollState;
                            rlpbParams.setMargins(0, roznica, 0, 0);
                            relativeLayoutProgressBars.setLayoutParams(rlpbParams);

                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                });*/
            }

        }

    }

}