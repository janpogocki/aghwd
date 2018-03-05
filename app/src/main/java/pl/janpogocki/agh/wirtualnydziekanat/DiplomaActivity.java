package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchDiploma;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class DiplomaActivity extends Fragment {

    View root;
    FirebaseAnalytics mFirebaseAnalytics;
    FetchDiploma fd;
    Context activityContext;

    private void refreshDiploma(View root) {
        if (Storage.diploma == null || Storage.diploma.size() == 0){
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

            showDiploma(root);
        }
    }

    private void showDiploma(final View root){
        ListView listViewGroups = root.findViewById(R.id.listViewGroups);

        ListAdapter listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (Storage.diploma != null && Storage.diploma.size() > 0)
                    return Storage.diploma.get(0).size();
                else
                    return 0;
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
                    convertView = infalInflater.inflate(R.layout.summary_list_item, parent, false);
                }

                TextView textViewHeader = convertView.findViewById(R.id.textViewHeader);
                TextView textView2ndLine = convertView.findViewById(R.id.textView2ndLine);
                TextView textView3rdLine = convertView.findViewById(R.id.textView3rdLine);

                textViewHeader.setText(nameLeft);
                textView2ndLine.setText(nameRight);
                textView3rdLine.setVisibility(View.GONE);

                return convertView;
            }
        };

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

        root = inflater.inflate(R.layout.activity_groups, container, false);

        refreshDiploma(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.diploma), null);
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                fd = new FetchDiploma();

                return root;
            } catch (Exception e) {
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
            final RelativeLayout rlNoData = root.findViewById(R.id.rlNoData);

            rlLoader.setVisibility(View.GONE);

            if (fd == null || isError){
                Storage.diploma = null;
                rlOffline.setVisibility(View.VISIBLE);
                try {
                    Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                            .show();
                } catch (Exception e){
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }

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