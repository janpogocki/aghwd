package pl.janpogocki.agh.wirtualnydziekanat;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Enumeration;
import java.util.Properties;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchWebsite;
import pl.janpogocki.agh.wirtualnydziekanat.javas.POSTgenerator;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

import static android.app.Activity.RESULT_OK;

public class FeedbackActivity extends Fragment {

    FirebaseAnalytics mFirebaseAnalytics;
    Context activityContext;
    EditText editTextEmail, editTextFeedback;
    String s;
    View root;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);

        root = inflater.inflate(R.layout.activity_feedback, container, false);
        editTextEmail = root.findViewById(R.id.editTextEmail);
        editTextFeedback = root.findViewById(R.id.editTextFeedback);

        editTextEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextEmail.getText().toString().equals("")) {
                    try {
                        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                                false, null, null, null, null);
                        startActivityForResult(intent, 1);
                    } catch (ActivityNotFoundException e) {
                        Log.i("aghwd", "aghwd", e);
                        Storage.appendCrash(e);
                    }
                }
            }
        });

        editTextFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editTextFeedback.getText().toString().equals(""))
                    ((MainActivity) activityContext).showSendButton(false);
                else
                    ((MainActivity) activityContext).showSendButton(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        TextView googlePlayLink = root.findViewById(R.id.googlePlayLink);
        googlePlayLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "clicked");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "google_play_link");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                final String appPackageName = "pl.janpogocki.agh.wirtualnydziekanat";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    Storage.appendCrash(anfe);
                }
            }
        });

        return root;
    }

    public void sendFeedback(){
        s = "E-mail: " + editTextEmail.getText().toString() + "\n\n"
                + editTextFeedback.getText().toString() + "\n\n==========";
        Activity a = getActivity();

        try {
            PackageInfo pInfo = a.getPackageManager().getPackageInfo(
                    a.getPackageName(), PackageManager.GET_META_DATA);
            s += "\nAPP Package Name: " + a.getPackageName();
            s += "\nAPP Version Name: " + pInfo.versionName;
            s += "\nAPP Version Code: " + pInfo.versionCode;
            s += "\n";
        } catch (PackageManager.NameNotFoundException e) {
            Storage.appendCrash(e);
        }
        s += "\nOS Version: " + System.getProperty("os.version") + " ("
                + android.os.Build.VERSION.INCREMENTAL + ")";
        s += "\nOS API Level: " + android.os.Build.VERSION.SDK;
        s += "\nDevice: " + android.os.Build.DEVICE;
        s += "\nModel (and Product): " + android.os.Build.MODEL + " ("
                + android.os.Build.PRODUCT + ")";

        // more from
        // http://developer.android.com/reference/android/os/Build.html :
        s += "\nManufacturer: " + android.os.Build.MANUFACTURER;
        s += "\nOther TAGS: " + android.os.Build.TAGS;

        s += "\nscreenWidth: "
                + a.getWindow().getWindowManager().getDefaultDisplay()
                .getWidth();
        s += "\nscreenHeigth: "
                + a.getWindow().getWindowManager().getDefaultDisplay()
                .getHeight();
        s += "\nKeyboard available: "
                + (a.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS);

        s += "\nTrackball available: "
                + (a.getResources().getConfiguration().navigation == Configuration.NAVIGATION_TRACKBALL);
        s += "\nSD Card state: " + Environment.getExternalStorageState();
        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        String key = "";
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            s += "\n> " + key + " = " + p.get(key);
        }

        s += "\n\n==========\n\n";
        s += Storage.feedbackCrashList;

        LinearLayout rlData = root.findViewById(R.id.rlData);
        RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
        rlData.setVisibility(View.GONE);
        rlLoader.setVisibility(View.VISIBLE);

        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
        asyncTaskRunner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.send_feedback), null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            editTextEmail.setText(accountName);
        }
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                POSTgenerator POSTgenerator = new POSTgenerator();
                POSTgenerator.add("content", s);
                FetchWebsite fw = new FetchWebsite("https://api.janpogocki.pl/aghwd/send_feedback.php");
                fw.getWebsite(false, false, POSTgenerator.getGeneratedPOST());

                return root;
            } catch (Exception e) {
                Storage.appendCrash(e);
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(View result){
            final RelativeLayout rlLoader = root.findViewById(R.id.rlLoader);
            final RelativeLayout rlOffline = root.findViewById(R.id.rlOffline);
            final RelativeLayout rlSent = root.findViewById(R.id.rlSent);

            rlLoader.setVisibility(View.GONE);

            if (isError){
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

                        sendFeedback();
                    }
                });
            }
            else {
                rlSent.setVisibility(View.VISIBLE);
                Storage.feedbackCrashList = "";
            }

        }

    }
}
