package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.MainActivity;
import pl.janpogocki.agh.wirtualnydziekanat.R;

/**
 * Created by Jan on 12.09.2017.
 * Class generating viewing files
 */

public class ExpandableListAdapterFiles extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private Context _context;
    // header titles
    private List<List<String>> _listDataHeader;
    // child data in format of header title, child title
    private HashMap<String, List<List<String>>> _listDataChild;
    private View _view;

    public ExpandableListAdapterFiles(Context context, List<List<String>> listDataHeader, HashMap<String, List<List<String>>> listChildData, View view) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._view = view;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return null;
    }

    public Object getChild(int groupPosition, int childPosititon, int dataPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).get(0) + this._listDataHeader.get(groupPosition).get(1) + this._listDataHeader.get(groupPosition).get(2)).get(childPosititon).get(dataPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childFilename = (String) getChild(groupPosition, childPosition, 0);
        String childDesc = (String) getChild(groupPosition, childPosition, 1);
        String childData = (String) getChild(groupPosition, childPosition, 2);
        final String childURL = (String) getChild(groupPosition, childPosition, 3);
        final String childPOST = (String) getChild(groupPosition, childPosition, 4);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.files_list_item, null);
        }

        TextView txtListChildCategory = convertView.findViewById(R.id.textViewCategory);
        TextView txtListChildData = convertView.findViewById(R.id.textViewData);
        TextView txtListChildUwagi = convertView.findViewById(R.id.textViewUwagi);

        txtListChildCategory.setText(childFilename);
        txtListChildData.setText(childDesc);
        txtListChildUwagi.setText(childData);

        RelativeLayout rlDataItem = convertView.findViewById(R.id.rlDataItem);
        rlDataItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ask for permission to write_external_storage
                if (ContextCompat.checkSelfPermission(_context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((MainActivity) _context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);

                    Snackbar.make(view, R.string.download_permission_info, Snackbar.LENGTH_LONG)
                            .show();
                }
                else {
                    // Download file to downloads
                    AsyncTaskRunner runner = new AsyncTaskRunner();
                    runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, childURL, childPOST);
                }
            }
        });

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).get(0) + this._listDataHeader.get(groupPosition).get(1) + this._listDataHeader.get(groupPosition).get(2)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    public Object getGroup(int groupPosition, int dataPosition) {
        return this._listDataHeader.get(groupPosition).get(dataPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerSubject = (String) getGroup(groupPosition, 0);
        String headerTeacher = (String) getGroup(groupPosition, 1);
        String headerLessonType = (String) getGroup(groupPosition, 2);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.files_list_group, null);
        }

        TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);
        TextView textViewSubTitle1 = convertView.findViewById(R.id.textViewSubTitle1);
        TextView textViewSubTitle2 = convertView.findViewById(R.id.textViewSubTitle2);
        textViewTitle.setText(headerSubject);
        textViewSubTitle1.setText(headerTeacher);
        textViewSubTitle2.setText(headerLessonType);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        boolean isError = false;
        ProgressDialog progress;
        String filename;
        FetchWebsite fw;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(_context);
            progress.setMessage(_context.getString(R.string.downloading_file));
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            fw = new FetchWebsite(Logging.URLdomain + params[0]);
            try {
                filename = fw.getAndSaveFile(true, true, params[1], Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/");
            } catch (Exception e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
                isError = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progress.dismiss();
            
            if (isError) {
                Snackbar.make(_view, R.string.download_error, Snackbar.LENGTH_LONG)
                        .show();
            }
            else {
                Snackbar.make(_view, R.string.download_ok, Snackbar.LENGTH_LONG)
                        .show();

                DownloadManager downloadManager = (DownloadManager) _context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (downloadManager != null) {
                    downloadManager.addCompletedDownload(fw.getDownloadFilename(),
                            _context.getString(R.string.app_name),
                            true,
                            URLConnection.guessContentTypeFromName(filename),
                            filename,
                            fw.getContentLenght(),
                            true);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "file_download");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "files");
                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(_context);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }
    }
}
