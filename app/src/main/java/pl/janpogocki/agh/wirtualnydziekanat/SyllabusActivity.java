package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchSyllabus;
import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchUniversityStatus;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SyllabusActivity extends Fragment {

    FetchUniversityStatus fus;

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

    private void refreshSyllabusWebView(ViewGroup root){
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_syllabus, null);

        if (Storage.universityStatus == null || Storage.universityStatus.size() == 0 || Storage.syllabusURL.equals("")){
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
            refreshSyllabusWebView(root);
        }

        return root;
    }

    private class AsyncTaskRunner extends AsyncTask<ViewGroup, ViewGroup, ViewGroup> {
        ViewGroup root;
        Boolean isError = false;

        @Override
        protected ViewGroup doInBackground(ViewGroup... params) {
            try {
                root = params[0];

                fus = new FetchUniversityStatus(true);

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

            rlLoader.setVisibility(View.GONE);
            rlData.setVisibility(View.VISIBLE);

            refreshSyllabusWebView(root);
        }

    }

}