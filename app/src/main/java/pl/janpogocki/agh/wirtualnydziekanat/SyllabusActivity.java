package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchSyllabus;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchUniversityStatus;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SyllabusActivity extends Fragment {

    FetchUniversityStatus fus;
    TextView textView3, textView3bis;
    Context activityContext;

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
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

    private void refreshSyllabusWebView(View root){
        final RelativeLayout rlWebViewLoader = (RelativeLayout) root.findViewById(R.id.rlWebViewLoader);
        ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progressBarWebView);
        progressBar.setScaleY(2f);

        WebView webView = (WebView) root.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        if (!"".equals(Storage.syllabusURL) && !"".equals(Storage.syllabusURLlinkDepartment))
            webView.loadUrl(Storage.syllabusURL);
        else if ("".equals(Storage.syllabusURL) && !"".equals(Storage.syllabusURLlinkDepartment))
            webView.loadUrl(Storage.syllabusURLlinkDepartment);
        else
            webView.loadUrl(FetchSyllabus.URLdomainSyllabus);

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    WebView webView = (WebView) v;

                    switch(keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                            if(webView.canGoBack())
                            {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }

                return false;
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                rlWebViewLoader.setVisibility(View.VISIBLE);

                // check if not syllabuskrk.agh.edu.pl domain
                if (!url.contains(FetchSyllabus.URLdomainSyllabus))
                    view.stopLoading();
            }

            @Override
            public void onPageFinished (WebView webView, String url){
                if (!Storage.syllabusURL.equals("") && (url.contains(Storage.syllabusURL) || Storage.syllabusURL.contains(url)))
                    webView.loadUrl("javascript:(function() {document.getElementById('sidebar-feedback-icon').style.cssText='display:none';document.getElementsByClassName('semester-wrapper')[" + Storage.getSemesterNumberById(Storage.summarySemesters.size()-1) + "-1].scrollIntoView();})()");
                else
                    webView.loadUrl("javascript:(function() {document.getElementById('sidebar-feedback-icon').style.cssText='display:none';})()");
                rlWebViewLoader.setVisibility(View.GONE);
            }
        });
    }

    private void refreshSyllabus(View root){
        if (Storage.universityStatus == null || Storage.universityStatus.size() == 0 || "".equals(Storage.syllabusURL)){
            // There's no downloaded data. Do that.
            RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);

            rlLoader.setVisibility(View.VISIBLE);
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(root);

            // wait for change loading subtitle
            animateFadeOut(textView3, root, 3000);
            animateFadeIn(textView3bis, root, 3250);
        }
        else {
            // Have it, show it.
            RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
            rlData.setVisibility(View.VISIBLE);
            refreshSyllabusWebView(root);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.syllabus), null);

        View root = inflater.inflate(R.layout.activity_syllabus, container, false);
        textView3 = (TextView) root.findViewById(R.id.textView3);
        textView3bis = (TextView) root.findViewById(R.id.textView3bis);

        refreshSyllabus(root);

        return root;
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        View root;
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                root = params[0];

                fus = new FetchUniversityStatus(true);

                return root;
            } catch (Exception e) {
                Log.i("aghwd", "FetchUniversityStatus error", e);
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(View result){
            final RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
            final RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = (RelativeLayout) root.findViewById(R.id.rlOffline);
            final RelativeLayout rlNoData = (RelativeLayout) root.findViewById(R.id.rlNoData);

            rlLoader.setVisibility(View.GONE);

            if (fus == null || isError){
                Storage.universityStatus = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshSyllabus(root);
                    }
                });
            }
            else if (fus.status == -1){
                rlNoData.setVisibility(View.VISIBLE);
            }
            else {
                // Have it, show it
                rlData.setVisibility(View.VISIBLE);
                refreshSyllabus(root);
            }
        }

    }

}