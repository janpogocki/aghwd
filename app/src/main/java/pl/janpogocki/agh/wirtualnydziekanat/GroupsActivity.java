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

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchGroups;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class GroupsActivity extends Fragment {

    FetchGroups fg;

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

    private void refreshGroups(ViewGroup root) {
        if (Storage.groupsAndModules == null || Storage.groupsAndModules.size() == 0){
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

            showGroups(root);
        }
    }

    private void showGroups(final ViewGroup root){
        ListView listViewGroups = (ListView) root.findViewById(R.id.listViewGroups);

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

                TextView textViewHeader = (TextView) convertView.findViewById(R.id.textViewHeader);
                TextView textView2ndLine = (TextView) convertView.findViewById(R.id.textView2ndLine);
                TextView textView3rdLine = (TextView) convertView.findViewById(R.id.textView3rdLine);

                textViewHeader.setText(nameGroup);
                textView2ndLine.setText(formGroup);
                textView3rdLine.setText(shortcutGroup);

                return convertView;
            }
        };

        listViewGroups.setAdapter(listAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_groups, null);

        refreshGroups(root);

        return root;
    }

    private class AsyncTaskRunner extends AsyncTask<ViewGroup, ViewGroup, ViewGroup> {
        ViewGroup root;
        Boolean isError = false;

        @Override
        protected ViewGroup doInBackground(ViewGroup... params) {
            try {
                root = params[0];

                fg = new FetchGroups();

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

            if (fg.status == -1){
                rlNoData.setVisibility(View.VISIBLE);
            }
            else if (Storage.groupsAndModules.size() == 0 || isError){
                Storage.groupsAndModules = null;
                rlOffline.setVisibility(View.VISIBLE);
                Snackbar.make(root.findViewById(R.id.activity_groups), "Problem z połączeniem sieciowym", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                rlOffline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlOffline.setVisibility(View.GONE);
                        rlLoader.setVisibility(View.VISIBLE);

                        refreshGroups(root);
                    }
                });
            }
            else {
                // Have it, show it
                rlData.setVisibility(View.VISIBLE);
                showGroups(root);
            }

        }

    }

}