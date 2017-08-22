package pl.janpogocki.agh.wirtualnydziekanat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.util.Random;

import pl.janpogocki.agh.wirtualnydziekanat.javas.Logging;
import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;
import pl.janpogocki.agh.wirtualnydziekanat.javas.SunriseAndSunsetCalculator;

public class LogIn extends AppCompatActivity {

    RelativeLayout relativeLayout2, relativeLayout3, relativeLayout4;
    EditText editText, editText2;
    Button button;
    SwitchCompat switch1;
    TextView textView3, textView3bis, textView12;
    Logging logging = null;
    FirebaseAnalytics mFirebaseAnalytics;
    AsyncTaskRunner runner;
    AsyncTaskRunner2 runner2;
    Activity activity;

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void animateFadeIn(TextView tv, int offset){
        Animation afi = AnimationUtils.loadAnimation(this, R.anim.fadein);
        tv.setAnimation(afi);
        afi.setDuration(250);
        afi.setStartOffset(offset);
        afi.start();
    }

    private void animateFadeOut(final TextView tv, int offset){
        Animation afi = AnimationUtils.loadAnimation(this, R.anim.fadeout);
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

    private String getRandomTip(){
        String [] arrayOfTips = getResources().getStringArray(R.array.log_in_random_tip);
        Random random = new Random(System.currentTimeMillis());
        int randValue = random.nextInt(arrayOfTips.length);

        return arrayOfTips[randValue];
    }

    private void doLogging(String _login, String _password, String _isSave){
        textView3.setText(R.string.log_in_loading);

        // replace layouts
        relativeLayout2.setVisibility(View.GONE);
        relativeLayout3.setVisibility(View.VISIBLE);

        runner = new AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _login, _password, _isSave);

        // show tips
        animateFadeOut(textView3, 800);
        textView3bis.setText(getRandomTip());
        animateFadeIn(textView3bis, 1100);
    }

    private void logInButtonPressed(View v){
        if (editText.getText().toString().equals("") || editText2.getText().toString().equals("")){
            Snackbar.make(findViewById(R.id.relativeLayout), R.string.log_in_fill_all_inputs, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            String login = editText.getText().toString();
            String password = editText2.getText().toString();

            if (switch1.isChecked())
                doLogging(login, password, "true");
            else
                doLogging(login, password, "false");

            hideKeyboard(v);
        }
    }

    @Override
    public void onBackPressed() {
        if (runner != null && runner2 != null && (runner.getStatus() == AsyncTask.Status.RUNNING || runner2.getStatus() == AsyncTask.Status.RUNNING)){
            finish();
            System.exit(0);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load startup preferences and get user's night mode
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String nightModePreference = sharedPreferences.getString("night_mode", "false");

        if (nightModePreference.equals("true")
                || (nightModePreference.equals("auto") && !SunriseAndSunsetCalculator.isDaylight())){
            Storage.nightMode = true;
            setTheme(R.style.AppTheme_NoActionBarrNight);
        }
        else {
            Storage.nightMode = false;
            setTheme(R.style.AppTheme_NoActionBarr);
        }

        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        activity = this;
        runner = new AsyncTaskRunner();
        runner2 = new AsyncTaskRunner2();
        loadActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(this, getString(R.string.logging), this.getClass().getSimpleName());
    }

    private void loadActivity(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            googleApiAvailability.makeGooglePlayServicesAvailable(LogIn.this)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            loadActivity();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            finish();
                            System.exit(0);
                        }
                    });
        }
        else {
            setContentView(R.layout.activity_log_in);

            relativeLayout2 = (RelativeLayout) findViewById(R.id.relativeLayout2);
            relativeLayout3 = (RelativeLayout) findViewById(R.id.relativeLayout3);
            relativeLayout4 = (RelativeLayout) findViewById(R.id.relativeLayout4);
            editText = (EditText) findViewById(R.id.editText);
            editText2 = (EditText) findViewById(R.id.editText2);
            button = (Button) findViewById(R.id.button);
            switch1 = (SwitchCompat) findViewById(R.id.switch1);
            textView3 = (TextView) findViewById(R.id.textView3);
            textView3bis = (TextView) findViewById(R.id.textView3bis);
            textView12 = (TextView) findViewById(R.id.textView12);
            RememberPassword rp = new RememberPassword(this);

            if (rp.isRemembered()) {
                // layout listiner
                relativeLayout4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            relativeLayout4.setVisibility(View.GONE);
                            RememberPassword rp = new RememberPassword(LogIn.this);
                            doLogging(rp.getLogin(), rp.getPassword(), "true");
                        } catch (IOException e) {
                            Log.i("aghwd", "No login or password saved", e);
                        }
                    }
                });

                // do firstly if everything is ok
                try {
                    doLogging(rp.getLogin(), rp.getPassword(), "true");
                } catch (IOException e) {
                    Log.i("aghwd", "No login or password saved", e);
                }

            } else {
                relativeLayout2.setVisibility(View.VISIBLE);

                textView12.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (switch1.isChecked())
                            switch1.setChecked(false);
                        else
                            switch1.setChecked(true);
                    }
                });

                editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_GO) {
                            logInButtonPressed(editText2.getRootView());
                            return true;
                        }
                        return false;
                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logInButtonPressed(v);
                    }
                });
            }
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, Logging, Logging> {
        String login, password, isSave;

        @Override
        protected Logging doInBackground(String... params) {
            try {
                login = params[0];
                password = params[1];
                isSave = params[2];
                logging = new Logging(login, password, Boolean.parseBoolean(isSave), LogIn.this);
                return logging;
            } catch (Exception e) {
                Log.i("aghwd", "Internet error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Logging result){
            RememberPassword rp = new RememberPassword(LogIn.this);
            try {
                if (logging.status == -3 || logging.status == -4) {
                    // cannot login because of technical work
                    relativeLayout3.setVisibility(View.GONE);

                    if (rp.isRemembered())
                        relativeLayout4.setVisibility(View.VISIBLE);
                    else
                        relativeLayout2.setVisibility(View.VISIBLE);

                    if (logging.status == -3) {
                        Snackbar.make(findViewById(R.id.relativeLayout0), R.string.log_in_fail_technical_works, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else {
                        Snackbar.make(findViewById(R.id.relativeLayout0), R.string.log_in_fail_unknown_error, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else if (logging.status == -2) {
                    // agh index is not ready to view
                    relativeLayout2.setVisibility(View.VISIBLE);
                    relativeLayout3.setVisibility(View.GONE);
                    editText2.setText("");

                    // build dialog window
                    final AlertDialog.Builder builder = new AlertDialog.Builder(relativeLayout2.getContext());
                    builder.setTitle(R.string.log_in_fail_incomplete_index_title)
                            .setMessage(R.string.log_in_fail_incomplete_index_content)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {}
                            });

                    builder.create().show();
                } else if (logging.status == -1) {
                    // error login/passwd
                    relativeLayout2.setVisibility(View.VISIBLE);
                    relativeLayout3.setVisibility(View.GONE);
                    editText2.setText("");
                    Snackbar.make(findViewById(R.id.relativeLayout0), R.string.log_in_fail_wrong_data, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (logging.status == 0) {
                    // is ok, log in
                    String firebaseIsRemembered;
                    if (rp.isRemembered())
                        firebaseIsRemembered = "with_remember_password";
                    else
                        firebaseIsRemembered = "without_remember_password";

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, firebaseIsRemembered);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

                    // Jump to MainActivity
                    Intent openMarks = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(openMarks);
                } else if (logging.status == 1) {
                    // is ok, log in
                    // hide loader
                    relativeLayout3.setVisibility(View.GONE);

                    // build dialog window
                    final CharSequence[] multiKierunekLabelNamesCharSequence = new CharSequence[Storage.multiKierunekLabelNames.size()];
                    Storage.multiKierunekLabelNames.toArray(multiKierunekLabelNamesCharSequence);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(relativeLayout2.getContext());
                    Storage.choosenMultiKierunekValue = Storage.multiKierunekValues.get(0);
                    builder.setTitle(R.string.log_in_multikierunek)
                            .setSingleChoiceItems(multiKierunekLabelNamesCharSequence, 0,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int item) {
                                            Storage.choosenMultiKierunekValue = Storage.multiKierunekValues.get(item);
                                        }
                                    })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // show loader
                                    relativeLayout3.setVisibility(View.VISIBLE);

                                    // do logging in background
                                    runner2 = new AsyncTaskRunner2();
                                    runner2.executeOnExecutor(THREAD_POOL_EXECUTOR);
                                }
                            })
                            .setCancelable(false);

                    if (!activity.isFinishing())
                        builder.create().show();
                }
            } catch(NullPointerException e){
                Log.i("aghwd", "Internet error", e);
                relativeLayout3.setVisibility(View.GONE);
                Snackbar.make(findViewById(R.id.relativeLayout), R.string.log_in_fail_server_down, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if (rp.isRemembered())
                    relativeLayout4.setVisibility(View.VISIBLE);
                else
                    relativeLayout2.setVisibility(View.VISIBLE);
            }
        }
    }

    private class AsyncTaskRunner2 extends AsyncTask<Logging, Logging, Logging> {
        @Override
        protected Logging doInBackground(Logging... params) {
            try {
                logging.loggingAfterKierunekChoice();
                return logging;
            } catch (Exception e) {
                Log.i("aghwd", "Logging afterKierunekChoice", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Logging result){
            if (logging.status == -2){
                // agh index is not ready to view
                relativeLayout2.setVisibility(View.VISIBLE);
                relativeLayout3.setVisibility(View.GONE);
                editText2.setText("");

                // build dialog window
                final AlertDialog.Builder builder = new AlertDialog.Builder(relativeLayout2.getContext());
                builder.setTitle(R.string.log_in_fail_incomplete_index_title)
                        .setMessage(R.string.log_in_fail_incomplete_index_content)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {}
                        });

                if (!activity.isFinishing())
                    builder.create().show();
            }
            else {
                RememberPassword rp = new RememberPassword(LogIn.this);
                String firebaseIsRemembered;
                if (rp.isRemembered())
                    firebaseIsRemembered = "with_remember_password";
                else
                    firebaseIsRemembered = "without_remember_password";

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, firebaseIsRemembered);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

                // Jump to MainActivity
                Intent openMarks = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(openMarks);
            }
        }
    }
}
