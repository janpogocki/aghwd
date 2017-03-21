package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchDiploma;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class DiplomaActivity extends Fragment {

    FetchDiploma fd;

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

    private void refreshDiploma(ViewGroup root) {
        if (Storage.diploma == null || Storage.diploma.size() == 0){
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

            showDiploma(root);
        }
    }

    private void showDiploma(final ViewGroup root){
        ListView listViewGroups = (ListView) root.findViewById(R.id.listViewGroups);

        ListAdapter listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return Storage.diploma.get(0).size();
            }

            @Override
            public Object getItem(int position) {
                return Storage.diploma.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String nameLeft = Storage.diploma.get(0).get(position);
                String nameRight = Storage.diploma.get(1).get(position);

                if (convertView == null) {
                    LayoutInflater infalInflater = (LayoutInflater) root.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater.inflate(R.layout.summary_list_item, null);
                }

                TextView textViewHeader = (TextView) convertView.findViewById(R.id.textViewHeader);
                TextView textView2ndLine = (TextView) convertView.findViewById(R.id.textView2ndLine);
                TextView textView3rdLine = (TextView) convertView.findViewById(R.id.textView3rdLine);

                textViewHeader.setText(nameLeft);
                textView2ndLine.setText(nameRight);
                textView3rdLine.setVisibility(View.GONE);

                return convertView;
            }
        };

        listViewGroups.setAdapter(listAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_groups, null);

        refreshDiploma(root);

        return root;
    }

    private class AsyncTaskRunner extends AsyncTask<ViewGroup, ViewGroup, ViewGroup> {
        ViewGroup root;
        Boolean isError = false;

        @Override
        protected ViewGroup doInBackground(ViewGroup... params) {
            try {
                root = params[0];

                fd = new FetchDiploma();

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

            if (fd == null || isError){
                Storage.diploma = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root.findViewById(R.id.activity_groups), R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshDiploma(root);
                    }
                });
            }
            else if (fd.status == -1){
                rlNoData.setVisibility(View.VISIBLE);
            }
            else {
                // Have it, show it
                rlData.setVisibility(View.VISIBLE);
                showDiploma(root);
            }

        }

    }

}