package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchGroups;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class GroupsActivity extends Fragment {

    View root;
    FirebaseAnalytics mFirebaseAnalytics;
    FetchGroups fg;
    Context activityContext;

    private void refreshGroups(View root) {
        if (Storage.groupsAndModules == null || Storage.groupsAndModules.size() == 0){
            // There's no downloaded data. Do that.
            RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);

            rlLoader.setVisibility(View.VISIBLE);
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute();
        }
        else {
            // Have it, show it.
            RelativeLayout rlData = root.findViewById(R.id.rlData);

            rlData.setVisibility(View.VISIBLE);

            showGroups(root);
        }
    }

    private void showGroups(final View root){
        ListView listViewGroups = root.findViewById(R.id.listViewGroups);

        ListAdapter listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return Storage.groupsAndModules.size();
            }

            @Override
            public Object getItem(int position) {
                return Storage.groupsAndModules.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String nameGroup = Storage.groupsAndModules.get(position).get(0);
                String shortcutGroup = Storage.groupsAndModules.get(position).get(1);
                String formGroup = Storage.groupsAndModules.get(position).get(2);

                if (convertView == null) {
                    LayoutInflater infalInflater = (LayoutInflater) root.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater.inflate(R.layout.summary_list_item, null);
                }

                TextView textViewHeader = convertView.findViewById(R.id.textViewHeader);
                TextView textView2ndLine = convertView.findViewById(R.id.textView2ndLine);
                TextView textView3rdLine = convertView.findViewById(R.id.textView3rdLine);

                textViewHeader.setText(nameGroup);
                textView2ndLine.setText(formGroup);
                textView3rdLine.setText(shortcutGroup);

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

        refreshGroups(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.groups), null);
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                fg = new FetchGroups();

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

            if (fg == null || isError){
                Storage.groupsAndModules = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root, R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshGroups(root);
                    }
                });
            }
            else if (fg.status == -1){
                rlNoData.setVisibility(View.VISIBLE);
            }
            else {
                // Have it, show it
                rlData.setVisibility(View.VISIBLE);
                showGroups(root);
            }

        }

    }

}