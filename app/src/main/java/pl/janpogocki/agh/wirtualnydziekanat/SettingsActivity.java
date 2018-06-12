package pl.janpogocki.agh.wirtualnydziekanat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchUniversityStatus;
import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SettingsActivity extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    FirebaseAnalytics mFirebaseAnalytics;
    Context activityContext;
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityContext = getActivity();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);
        addPreferencesFromResource(R.xml.settings);

        view = ((MainActivity) activityContext).findViewById(R.id.frameLayoutMainV7);

        RememberPassword rememberPassword = new RememberPassword(activityContext);
        if (!rememberPassword.isRemembered()){
            getPreferenceManager().getSharedPreferences().edit().putBoolean("marks_notifications", false).apply();
            getPreferenceScreen().findPreference("marks_notifications").setEnabled(false);
            getPreferenceScreen().findPreference("marks_notifications").setDefaultValue(false);
            getPreferenceScreen().findPreference("marks_notifications").setSummary(getPreferenceScreen()
                    .findPreference("marks_notifications").getSummary() + "\n\n" + getString(R.string.only_with_remember_password));
        }

        Preference preference_mycal = findPreference("remove_all_mycal_events");
        preference_mycal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
                builder.setTitle(R.string.settings_are_you_sure);
                builder.setMessage(getString(R.string.settings_mycal_are_you_sure_text) + " " + Storage.albumNumber + ".");

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "mycal");
                    }
                });

                builder.setNegativeButton(R.string.action_cancel, null);
                builder.show();

                return true;
            }
        });

        Preference preference_pm = findPreference("remove_all_pm_entries");
        preference_pm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
                builder.setTitle(R.string.settings_are_you_sure);
                builder.setMessage(getString(R.string.settings_pm_are_you_sure_text) + " " + Storage.albumNumber + ".");

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "pm");
                    }
                });

                builder.setNegativeButton(R.string.action_cancel, null);
                builder.show();

                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.action_settings), null);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("news_notifications")){
            if (sharedPreferences.getBoolean("news_notifications", true))
                FirebaseMessaging.getInstance().subscribeToTopic("news");
            else
                FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
        }
        else if (s.equals("night_mode")){
            ((MainActivity) activityContext).restartApp();
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        ProgressDialog progress;
        boolean isError = false;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(activityContext);
            progress.setMessage(getString(R.string.deleting_is_doing));
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                // get university status
                if (Storage.universityStatus == null || Storage.universityStatus.size() == 0){
                    new FetchUniversityStatus(false);
                }

                // get all files from dir
                File folder = new File(activityContext.getFilesDir() + "/");
                File[] listOfFiles = folder.listFiles();

                if (strings[0].equals("mycal")){
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.getName().contains(Storage.getUniversityStatusHash() + "_mycal.json")) {
                            file.delete();
                        }
                    }
                }
                else if (strings[0].equals("pm")){
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.getName().contains(Storage.getUniversityStatusHash() + "_pm")) {
                            file.delete();
                        }
                    }
                }
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
                try {
                    Snackbar.make(view, activityContext.getResources().getString(R.string.deleting_error), Snackbar.LENGTH_LONG)
                            .show();
                } catch (Exception e){
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
            else {
                try {
                    Snackbar.make(view, activityContext.getResources().getString(R.string.deleting_complete), Snackbar.LENGTH_LONG)
                            .show();
                } catch (Exception e){
                    Log.i("aghwd", "aghwd", e);
                    Storage.appendCrash(e);
                }
            }
        }
    }
}
