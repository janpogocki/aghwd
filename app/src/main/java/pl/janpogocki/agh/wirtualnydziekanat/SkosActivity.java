package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchSkos;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SkosActivity extends Fragment {

    public ViewGroup root;
    FetchSkos fs;
    ListView listViewGroups;
    ListAdapter listAdapter;

    public Fragment newInstance(Context context) {
        SkosActivity f = new SkosActivity();
        return f;
    }

    public void searchTyping(String val){
        if (listAdapter != null && listViewGroups != null) {
            List<List<String>> list = filterList(val);
            RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
            RelativeLayout rlNoRecords = (RelativeLayout) root.findViewById(R.id.rlNoRecords);

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

    private void refreshSkos(ViewGroup root) {
        if (Storage.skosList == null || Storage.skosList.size() == 0){
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

            showSkos(root);
        }
    }

    public void backButtonPressedWhenBrowserOpened(){
        RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
        RelativeLayout rlBrowser = (RelativeLayout) root.findViewById(R.id.rlBrowser);
        rlBrowser.setVisibility(View.GONE);
        rlData.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).showSearchButton(true);
        Storage.openedBrowser = false;
    }

    private void switch2browser(final String _url){
        // close searchbar
        ((MainActivity) getActivity()).showSearchButton(false);

        Storage.openedBrowser = true;
        Storage.oneMoreBackPressedButtonMeansExit = false;

        ((MainActivity) getActivity()).hideKeyboard(getView());

        final RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
        final RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);
        final RelativeLayout rlBrowser = (RelativeLayout) root.findViewById(R.id.rlBrowser);
        WebView webView = (WebView) root.findViewById(R.id.webView);

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
                webView.loadUrl("javascript:(function() {var anchors = document.getElementsByTagName(\"a\");for (var i = 0; i < anchors.length; i++) {anchors[i].style.cssText = \"color:#555\";}document.body.innerHTML = document.querySelector(\"div.c-col.vcard\").outerHTML;})()");
                rlLoader.setVisibility(View.GONE);
                rlBrowser.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showSkos(final ViewGroup root){
        ((MainActivity) getActivity()).showSearchButton(true);
        listViewGroups = (ListView) root.findViewById(R.id.listViewGroups);

        listAdapter = new SearchAdapter(Storage.skosList);
        listViewGroups.setAdapter(listAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.activity_skos, container, false);

        refreshSkos(root);
        return root;
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
            String nameAndSurname = list.get(0).get(position);
            String functions = list.get(1).get(position);
            final String personURL = list.get(2).get(position);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) root.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.summary_list_item, null);
            }

            TextView textViewHeader = (TextView) convertView.findViewById(R.id.textViewHeader);
            TextView textView2ndLine = (TextView) convertView.findViewById(R.id.textView2ndLine);
            TextView textView3rdLine = (TextView) convertView.findViewById(R.id.textView3rdLine);
            LinearLayout rlDataItem = (LinearLayout) convertView.findViewById(R.id.rlDataItem);

            textViewHeader.setText(nameAndSurname);
            textView2ndLine.setText(functions);
            textView3rdLine.setText(personURL);
            textView3rdLine.setVisibility(View.GONE);

            rlDataItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch2browser(personURL);
                }
            });

            return convertView;
        }
    }

    private class AsyncTaskRunner extends AsyncTask<ViewGroup, ViewGroup, ViewGroup> {
        ViewGroup root;
        Boolean isError = false;

        @Override
        protected ViewGroup doInBackground(ViewGroup... params) {
            try {
                root = params[0];

                fs = new FetchSkos(getActivity());

                return root;
            } catch (Exception e) {
                e.printStackTrace();
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(ViewGroup result){
            final RelativeLayout rlData = (RelativeLayout) root.findViewById(R.id.rlData);
            final RelativeLayout rlLoader = (RelativeLayout) root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = (RelativeLayout) root.findViewById(R.id.rlOffline);

            rlLoader.setVisibility(View.GONE);

            if (fs == null || fs.status == -1 || isError){
                Storage.skosList = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root.findViewById(R.id.activity_skos), "Problem z połączeniem sieciowym", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

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

}
