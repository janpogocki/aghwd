package pl.janpogocki.agh.wirtualnydziekanat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;

import pl.janpogocki.agh.wirtualnydziekanat.javas.Logging;
import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class LogIn extends AppCompatActivity {

    RelativeLayout relativeLayout2, relativeLayout3, relativeLayout4;
    EditText editText, editText2;
    Button button;
    Switch switch1;
    TextView textView12;
    Logging logging = null;

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void doLogging(String _login, String _password, String _isSave){
        // replace layouts
        relativeLayout2.setVisibility(View.GONE);
        relativeLayout3.setVisibility(View.VISIBLE);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(_login, _password, _isSave);
    }

    private void logInButtonPressed(View v){
        if (editText.getText().toString().equals("") || editText2.getText().toString().equals("")){
            Snackbar.make(findViewById(R.id.relativeLayout), "Wprowadź dane logowania", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            String login = editText.getText().toString();
            String password = editText2.getText().toString();

            if (switch1.isChecked())
                doLogging(login, password, "true");
            else
                doLogging(login, password, "");

            hideKeyboard(v);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        System.exit(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        relativeLayout2 = (RelativeLayout) findViewById(R.id.relativeLayout2);
        relativeLayout3 = (RelativeLayout) findViewById(R.id.relativeLayout3);
        relativeLayout4 = (RelativeLayout) findViewById(R.id.relativeLayout4);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        button = (Button) findViewById(R.id.button);
        switch1 = (Switch) findViewById(R.id.switch1);
        textView12 = (TextView) findViewById(R.id.textView12);
        RememberPassword rp = new RememberPassword(this);

        if (rp.isRemembered()){
            // layout listiner
            relativeLayout4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        relativeLayout4.setVisibility(View.GONE);
                        RememberPassword rp = new RememberPassword(LogIn.this);
                        doLogging(rp.getLogin(), rp.getPassword(), "");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            // do firstly if everything is ok
            try {
                doLogging(rp.getLogin(), rp.getPassword(), "");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
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
                public void onClick(View v){
                    logInButtonPressed(v);
                }
            });
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
                logging = new Logging(params[0], params[1]);
                return logging;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Logging result){
            RememberPassword rp = new RememberPassword(LogIn.this);
            try {
                if (logging.status == -2) {
                    // agh index is not ready to view
                    relativeLayout2.setVisibility(View.VISIBLE);
                    relativeLayout3.setVisibility(View.GONE);
                    editText2.setText("");

                    // build dialog window
                    final AlertDialog.Builder builder = new AlertDialog.Builder(relativeLayout2.getContext());
                    builder.setTitle("Ups...")
                            .setMessage("Logowanie przebiegło pomyślnie, ale wygląda na to, że Twój indeks nie jest jeszcze uzupełniony wymaganą ilością danych. Aplikacja będzie zapewne działała poprawnie już wkrótce.")
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
                    Snackbar.make(findViewById(R.id.relativeLayout0), "Logowanie nie powiodło się - zły numer albumu lub hasło", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (logging.status == 0) {
                    // is ok, log in
                    if (isSave.length() > 1) {
                        try {
                            rp.save(login, password);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    relativeLayout3.setVisibility(View.GONE);
                    Storage.loggedIn = true;

                    // Jump to MainActivity
                    Intent openMarks = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(openMarks);
                } else if (logging.status == 1) {
                    // is ok, log in
                    if (isSave.length() > 1) {
                        try {
                            rp.save(login, password);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // hide loader
                    relativeLayout3.setVisibility(View.GONE);

                    // build dialog window
                    final CharSequence[] multiKierunekLabelNamesCharSequence = new CharSequence[Storage.multiKierunekLabelNames.size()];
                    Storage.multiKierunekLabelNames.toArray(multiKierunekLabelNamesCharSequence);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(relativeLayout2.getContext());
                    Storage.choosenMultiKierunekValue = Storage.multiKierunekValues.get(0);
                    builder.setTitle("Wybierz kierunek")
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
                                    AsyncTaskRunner2 runner2 = new AsyncTaskRunner2();
                                    runner2.execute();
                                }
                            })
                            .setCancelable(false);

                    builder.create().show();
                }
            } catch(NullPointerException e){
                e.printStackTrace();
                relativeLayout3.setVisibility(View.GONE);
                Snackbar.make(findViewById(R.id.relativeLayout), "Błąd połączenia z serwerem", Snackbar.LENGTH_LONG)
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
                e.printStackTrace();
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
                builder.setTitle("Ups...")
                        .setMessage("Logowanie przebiegło pomyślnie, ale wygląda na to, że Twój indeks nie jest jeszcze uzupełniony wymaganą ilością danych. Aplikacja będzie zapewne działała poprawnie już wkrótce.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {}
                        });

                builder.create().show();
            }
            else {
                relativeLayout3.setVisibility(View.GONE);
                Storage.loggedIn = true;

                // Jump to MainActivity
                Intent openMarks = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(openMarks);
            }
        }

    }
}
