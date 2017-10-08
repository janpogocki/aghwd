package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.javas.AnimatedExpandableListView;
import pl.janpogocki.agh.wirtualnydziekanat.javas.ExpandableListAdapterScholarships;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchScholarships;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class ScholarshipsActivity extends Fragment {

    View root;
    FirebaseAnalytics mFirebaseAnalytics;
    ExpandableListAdapterScholarships listAdapter;
    List<List<String>> listDataHeader;
    HashMap<String, List<List<String>>> listDataChild;
    FetchScholarships fs = null;
    Context activityContext;

    RelativeLayout rlLoader, rlData;
    TextView textView3, textView3bis;

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

        listAdapter = new ExpandableListAdapterScholarships(activityContext, listDataHeader, listDataChild);

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
        listDataHeader = fs.getHeaders();

        // Adding child data (header, child)
        listDataChild = fs.getChildren();
    }

    private void refreshMarks(View root) {
        ScholarshipsActivity.AsyncTaskRunner runner = new ScholarshipsActivity.AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);
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

        root = inflater.inflate(R.layout.activity_files, container, false);
        rlLoader = root.findViewById(R.id.rlLoader);
        rlData = root.findViewById(R.id.rlData);
        textView3 = root.findViewById(R.id.textView3);
        textView3bis = root.findViewById(R.id.textView3bis);

        // do in background
        rlLoader.setVisibility(View.VISIBLE);
        ScholarshipsActivity.AsyncTaskRunner runner = new ScholarshipsActivity.AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // wait for change loading subtitle
        animateFadeOut(textView3, root, 3000);
        animateFadeIn(textView3bis, root, 3250);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.scholarships), null);
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                if (Storage.scholarships == null || Storage.scholarships.size() == 0)
                    fs = new FetchScholarships();
                else
                    fs = new FetchScholarships(0);

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
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "scholarships");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "swipe_refresh");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    Storage.scholarships = new ArrayList<>();

                    rlData.setVisibility(View.GONE);
                    rlLoader.setVisibility(View.VISIBLE);
                    srl.setRefreshing(false);
                    srl.setEnabled(false);
                    refreshMarks(root);
                    srl.setEnabled(true);
                }
            });

            if (fs == null || isError){
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
            else if (fs.status == -1) {
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
