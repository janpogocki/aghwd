package pl.janpogocki.agh.wirtualnydziekanat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
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
import java.util.Locale;

import pl.janpogocki.agh.wirtualnydziekanat.javas.AnimatedExpandableListView;
import pl.janpogocki.agh.wirtualnydziekanat.javas.ExpandableListAdapter;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchMarks;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchWebsite;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Logging;
import pl.janpogocki.agh.wirtualnydziekanat.javas.POSTgenerator;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class MarksExplorer extends Fragment {

    View root;
    FirebaseAnalytics mFirebaseAnalytics;
    ExpandableListAdapter listAdapter;
    List<List<String>> listDataHeader;
    HashMap<String, List<List<String>>> listDataChild;
    FetchMarks fm;
    Context activityContext;

    RelativeLayout rlLoader, rlData;
    TextView textView3, textView3bis;

    Boolean goBack = false;
    String viewstateName = "__VIEWSTATE";
    String viewstateGeneratorName = "__VIEWSTATEGENERATOR";
    String eventValidationName = "__EVENTVALIDATION";
    String buttonBackName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butPop";
    String buttonForwardName = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butNas";

    private void setProgressAnimate(ProgressBar pb, int progressTo) {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", 0, progressTo * 100);
        animation.setDuration(1500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setStartDelay(900);
        animation.start();
    }

    private void animateTextView(final TextView textview, int initialValue, int finalValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.setDuration(1500);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textview.setText(valueAnimator.getAnimatedValue().toString());
            }
        });

        valueAnimator.setStartDelay(900);
        valueAnimator.start();
    }

    private void animateTextView(final TextView textview, float initialValue, float finalValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(initialValue, finalValue);
        valueAnimator.setDuration(1500);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textview.setText(String.format(Locale.US, "%.2f", (float) valueAnimator.getAnimatedValue()));
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

        listAdapter = new ExpandableListAdapter(activityContext, listDataHeader, listDataChild);

        AnimatedExpandableListView expandableListView = view.findViewById(R.id.expandableListView);
        expandableListView.setAdapter(listAdapter);
    }

    private void showResults(View view){
        TextView textViewAvgSemester, textViewAvgYear, textViewECTS;
        ProgressBar progressBarAvgSemester, progressBarAvgYear, progressBarECTS;
        RelativeLayout relativeLayoutProgressBars, relativeLayoutExpListView;

        textViewAvgSemester = view.findViewById(R.id.textViewAvgSemester);
        textViewAvgYear = view.findViewById(R.id.textViewAvgYear);
        textViewECTS = view.findViewById(R.id.textViewECTS);
        progressBarAvgSemester = view.findViewById(R.id.progressBarAvgSemester);
        progressBarAvgYear = view.findViewById(R.id.progressBarAvgYear);
        progressBarECTS = view.findViewById(R.id.progressBarECTS);
        relativeLayoutProgressBars = view.findViewById(R.id.relativeLayoutProgressBars);
        relativeLayoutExpListView = view.findViewById(R.id.relativeLayoutExpListView);

        textViewAvgSemester.setText("0.00");
        textViewAvgYear.setText("0.00");
        textViewECTS.setText("0");
        progressBarAvgSemester.setProgress(0);
        progressBarAvgYear.setProgress(0);
        progressBarECTS.setProgress(0);

        animateFadeIn(relativeLayoutProgressBars, view, 500);
        animateFadeIn(relativeLayoutExpListView, view, 1200);

        animateTextView(textViewAvgSemester, 0f, fm.amountAvgSemester);
        animateTextView(textViewAvgYear, 0f, fm.amountAvgYear);
        animateTextView(textViewECTS, 0, fm.amountECTS);

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

    private void refreshMarks(View root) {
        for (int i=0; i<Storage.summarySemesters.size()-1; i++){
            Storage.currentSemesterHTML.remove(i);
        }

        Storage.currentSemesterListPointer = Storage.summarySemesters.size()-1;
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);
    }

    private void goThroughSemester() throws Exception {
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
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }

                String data = POSTgenerator.getGeneratedPOST();

                FetchWebsite fw = new FetchWebsite(Logging.URLdomain + "/OcenyP.aspx");
                String fww = fw.getWebsite(true, true, data);
                Storage.currentSemesterHTML.put(Storage.currentSemesterListPointer, fww);
            }
        }
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

        root = inflater.inflate(R.layout.activity_marks_explorer, container, false);
        rlLoader = root.findViewById(R.id.rlLoader);
        rlData = root.findViewById(R.id.rlData);
        textView3 = root.findViewById(R.id.textView3);
        textView3bis = root.findViewById(R.id.textView3bis);

        // do in background

        if ("semester".equals(Storage.sharedPreferencesStartScreen) && Storage.firstRunMarksExplorer) {
            Storage.firstRunMarksExplorer = false;
        }
        else {
            rlLoader.setVisibility(View.VISIBLE);
            Storage.firstRunMarksExplorer = false;
        }

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.final_marks), null);
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                goThroughSemester();
                fm = new FetchMarks(Storage.currentSemesterHTML.get(Storage.currentSemester));

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
            ((MainActivity) activityContext).enableDisableSemesterSpinner(true);

            final RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);
            final SwipeRefreshLayout srl = root.findViewById(R.id.swiperefresh);

            srl.setColorSchemeResources(R.color.colorPrimary);
            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "marks");
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

            if (fm == null || isError){
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
            else if (fm.status == -1) {
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