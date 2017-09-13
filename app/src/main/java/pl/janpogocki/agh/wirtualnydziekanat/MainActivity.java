package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Menu MenuWithActionBar = null;
    SearchView searchView = null;
    Spinner toolbarSpinner = null;
    SkosActivity skosactivity = null;
    ScheduleActivity scheduleactivity = null;
    FeedbackActivity feedbackactivity = null;
    FirebaseAnalytics mFirebaseAnalytics;
    String currentFragmentScreen, notificationsStatusFirebase;

    public void restartApp(){
        Storage.clearStorage();

        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    public void resetMainLayoutVisibility(Boolean value) {
        FrameLayout frameLayoutMain = (FrameLayout) findViewById(R.id.frameLayoutMain);
        FrameLayout frameLayoutMainV7 = (FrameLayout) findViewById(R.id.frameLayoutMainV7);

        // true for frameLayoutMain, false for V7
        if (value){
            frameLayoutMain.setVisibility(View.VISIBLE);
            frameLayoutMainV7.setVisibility(View.GONE);
        }
        else {
            frameLayoutMain.setVisibility(View.GONE);
            frameLayoutMainV7.setVisibility(View.VISIBLE);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showSearchButton(Boolean val){
        if (MenuWithActionBar == null)
            restartApp();
        else
            MenuWithActionBar.findItem(R.id.action_search).setVisible(val);
    }

    public void showSendButton(Boolean val){
        if (MenuWithActionBar == null)
            restartApp();
        else
            MenuWithActionBar.findItem(R.id.action_send).setVisible(val);
    }

    public void showScheduleButtons(Boolean val){
        if (MenuWithActionBar == null)
            restartApp();
        else {
            MenuWithActionBar.findItem(R.id.action_previous_week).setVisible(val);
            MenuWithActionBar.findItem(R.id.action_next_week).setVisible(val);
        }
    }

    public void showSemesterSpinner(Boolean val){
        if (MenuWithActionBar == null)
            restartApp();
        else
            MenuWithActionBar.findItem(R.id.toolbarSpinner).setVisible(val);
    }

    private void showDefaultScreen(Boolean showSpinner){
        if ("summary".equals(Storage.sharedPreferencesStartScreen)) {
            navigationView.getMenu().findItem(R.id.nav_summary).setChecked(true);
            setTitle(getString(R.string.summary));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment summaryactivity_ = new SummaryActivity();
            tx.replace(R.id.frameLayoutMain, summaryactivity_);
            tx.commit();

            currentFragmentScreen = "summary";
        } else if ("schedule".equals(Storage.sharedPreferencesStartScreen)) {
            navigationView.getMenu().findItem(R.id.nav_schedule).setChecked(true);
            setTitle(getString(R.string.schedule));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment scheduleactivity_ = new ScheduleActivity();
            tx.replace(R.id.frameLayoutMain, scheduleactivity_);
            tx.commit();
            scheduleactivity = (ScheduleActivity) scheduleactivity_;

            currentFragmentScreen = "schedule";
        } else if ("semester_partial".equals(Storage.sharedPreferencesStartScreen)) {
            if (showSpinner)
                showSemesterSpinner(true);

            navigationView.getMenu().findItem(R.id.nav_partial_marks).setChecked(true);
            setTitle(getString(R.string.partial_marks));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment partialmarksexplorer_ = new PartialMarksExplorer();
            tx.replace(R.id.frameLayoutMain, partialmarksexplorer_);
            tx.commit();

            currentFragmentScreen = "semester_partial";
        } else {
            if (showSpinner)
                showSemesterSpinner(true);

            navigationView.getMenu().findItem(R.id.nav_marks).setChecked(true);
            setTitle(getString(R.string.final_marks));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment marksexplorer_ = new MarksExplorer();
            tx.replace(R.id.frameLayoutMain, marksexplorer_);
            tx.commit();

            currentFragmentScreen = "semester";
        }
    }

    public void prepareSemesterSpinner(Spinner toolbarSpinner){
        List<String> listOfSemesters = new ArrayList<>();

        for (int i = 0; i < Storage.summarySemesters.size(); i++) {
            listOfSemesters.add(getString(R.string.semester) + " " + Storage.getSemesterNumberById(i));
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getSupportActionBar().getThemedContext(),
                android.R.layout.simple_spinner_item, listOfSemesters);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toolbarSpinner.setAdapter(spinnerAdapter);
        toolbarSpinner.setSelection(listOfSemesters.size()-1);

        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (Storage.currentSemester != i) {
                    Storage.currentSemester = i;

                    if (currentFragmentScreen.equals("semester")){
                        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                        Fragment marksexplorer_ = new MarksExplorer();
                        tx.replace(R.id.frameLayoutMain, marksexplorer_);
                        tx.commit();
                    } else if (currentFragmentScreen.equals("semester_partial")){
                        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                        Fragment partialmarksexplorer_ = new PartialMarksExplorer();
                        tx.replace(R.id.frameLayoutMain, partialmarksexplorer_);
                        tx.commit();
                    } else if (currentFragmentScreen.equals("files")){
                        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                        Fragment filesactivity_ = new FilesActivity();
                        tx.replace(R.id.frameLayoutMain, filesactivity_);
                        tx.commit();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if ("semester".contains(Storage.sharedPreferencesStartScreen))
            showSemesterSpinner(true);
    }

    private void setupSearchView(){
        searchView.setIconifiedByDefault(true);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && skosactivity != null)
                    skosactivity.searchTyping(newText);

                return false;
            }
        });
        searchView.setQueryHint(getResources().getString(R.string.skos_search));
    }

    private void uncheckCheckedItem(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                item.setChecked(false);
            }
            else {
                if (item.hasSubMenu()){
                    uncheckCheckedItem(item.getSubMenu());
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if last connection to dziekanat.agh.edu.pl was 20 minutes ago - restart
        if ((System.currentTimeMillis()-Storage.timeOfLastConnection) > 60000*20){
            restartApp();
            finish();
            System.exit(0);
        }
        else {
            mFirebaseAnalytics.setUserProperty("default_view", Storage.sharedPreferencesStartScreen);
            mFirebaseAnalytics.setUserProperty("powiadomienia", notificationsStatusFirebase);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Storage.nightMode)
            setTheme(R.style.AppThemeNight);

        super.onCreate(savedInstanceState);

        if (Storage.summarySemesters == null || Storage.summarySemesters.size() == 0){
            restartApp();
            finish();
            System.exit(0);
        }
        else {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            // Setting up personal data in drawer header
            TextView textView, textView2;
            ImageView imageView;
            View headerNV = navigationView.getHeaderView(0);
            textView = (TextView) headerNV.findViewById(R.id.textView);
            textView2 = (TextView) headerNV.findViewById(R.id.textView2);
            imageView = (ImageView) headerNV.findViewById(R.id.imageView);
            textView.setText(Storage.nameAndSurname);
            textView2.setText(Storage.albumNumber);

            if (Storage.photoUser != null)
                imageView.setImageBitmap(Storage.photoUser);

            // If multidirectionars enable switch dir
            if (Storage.multiKierunek)
                navigationView.getMenu().findItem(R.id.nav_relogging).setVisible(true);

            // Load startup preferences and show user's startup screen
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Storage.sharedPreferencesStartScreen = sharedPreferences.getString("default_start_screen", "semester");

            showDefaultScreen(false);

            // Subscribe to notifications or not
            RememberPassword rememberPassword = new RememberPassword(this);
            if (rememberPassword.isRemembered()) {
                StringBuilder stringBuilder = new StringBuilder();

                if (sharedPreferences.getBoolean("marks_notifications", true)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Storage.albumNumber);
                    stringBuilder.append("marks ");
                }

                if (sharedPreferences.getBoolean("news_notifications", true)) {
                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                    stringBuilder.append("news ");
                }

                if (stringBuilder.length() == 0)
                    stringBuilder.append("N/A");

                notificationsStatusFirebase = stringBuilder.toString();
            }
        }
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);

        // current screen is different from default => return to default

        Storage.openedBrowser = false;

        showSearchButton(false);
        showSendButton(false);
        showScheduleButtons(false);
        resetMainLayoutVisibility(true);

        uncheckCheckedItem(navigationView.getMenu());
        showDefaultScreen(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (Storage.openedBrowser != null && Storage.openedBrowser) {
            skosactivity.backButtonPressedWhenBrowserOpened();
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);
        }
        else {
            if (!currentFragmentScreen.equals(Storage.sharedPreferencesStartScreen)){
                // current screen is different from default => return to default

                Storage.openedBrowser = false;

                showSearchButton(false);
                showSendButton(false);
                showScheduleButtons(false);
                showSemesterSpinner(false);
                resetMainLayoutVisibility(true);

                uncheckCheckedItem(navigationView.getMenu());

                showDefaultScreen(true);
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        MenuWithActionBar = menu;

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        setupSearchView();

        toolbarSpinner = (Spinner) menu.findItem(R.id.toolbarSpinner).getActionView();
        prepareSemesterSpinner(toolbarSpinner);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.action_search) {
            setupSearchView();
        }
        else if (id == R.id.action_previous_week) {
            scheduleactivity.changeWeek(getString(R.string.previous));
        }
        else if (id == R.id.action_next_week) {
            scheduleactivity.changeWeek(getString(R.string.next));
        }
        else if (id == R.id.action_send) {
            hideKeyboard(getWindow().getDecorView().getRootView());
            showSendButton(false);
            feedbackactivity.sendFeedback();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Storage.openedBrowser = false;

        showSearchButton(false);
        showSendButton(false);
        showScheduleButtons(false);
        showSemesterSpinner(false);
        resetMainLayoutVisibility(true);

        if (id == R.id.nav_about) {
            setTitle(getString(R.string.about_app));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment aboutactivity_ = new AboutActivity();
            tx.replace(R.id.frameLayoutMain, aboutactivity_);
            tx.commit();
            currentFragmentScreen = "about";
        } else if (id == R.id.nav_settings) {
            setTitle(getString(R.string.action_settings));
            resetMainLayoutVisibility(false);
            android.app.FragmentTransaction tx = getFragmentManager().beginTransaction();
            PreferenceFragment settingsactivity_ = new SettingsActivity();
            tx.replace(R.id.frameLayoutMainV7, settingsactivity_);
            tx.commit();
            currentFragmentScreen = "settings";
        } else if (id == R.id.nav_summary) {
            setTitle(getString(R.string.summary));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment summaryactivity_ = new SummaryActivity();
            tx.replace(R.id.frameLayoutMain, summaryactivity_);
            tx.commit();
            currentFragmentScreen = "summary";
        } else if (id == R.id.nav_groups) {
            setTitle(getString(R.string.groups));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment groupsactivity_ = new GroupsActivity();
            tx.replace(R.id.frameLayoutMain, groupsactivity_);
            tx.commit();
            currentFragmentScreen = "groups";
        } else if (id == R.id.nav_diploma) {
            setTitle(getString(R.string.diploma));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment diplomaactivity_ = new DiplomaActivity();
            tx.replace(R.id.frameLayoutMain, diplomaactivity_);
            tx.commit();
            currentFragmentScreen = "diploma";
        } else if (id == R.id.nav_schedule) {
            setTitle(getString(R.string.schedule));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment scheduleactivity_ = new ScheduleActivity();
            tx.replace(R.id.frameLayoutMain, scheduleactivity_);
            tx.commit();
            scheduleactivity = (ScheduleActivity) scheduleactivity_;
            currentFragmentScreen = "schedule";
        } else if (id == R.id.nav_syllabus) {
            setTitle(getString(R.string.syllabus));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment syllabusactivity = new SyllabusActivity();
            tx.replace(R.id.frameLayoutMain, syllabusactivity);
            tx.commit();
            currentFragmentScreen = "syllabus";
        } else if (id == R.id.nav_skos) {
            setTitle(getString(R.string.skos));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment skosactivity_ = new SkosActivity();
            tx.replace(R.id.frameLayoutMain, skosactivity_);
            tx.commit();
            skosactivity = (SkosActivity) skosactivity_;

            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);

            currentFragmentScreen = "skos";
        } else if (id == R.id.nav_feedback) {
            setTitle(getString(R.string.send_feedback));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment feedbackactivity_ = new FeedbackActivity();
            tx.replace(R.id.frameLayoutMain, feedbackactivity_);
            tx.commit();
            feedbackactivity = (FeedbackActivity) feedbackactivity_;
            currentFragmentScreen = "feedback";
        } else if (id == R.id.nav_logout) {
            RememberPassword rp = new RememberPassword(this);

            if (rp.isRemembered())
                rp.remove();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getString(R.string.logout));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "navbar_action");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            FirebaseMessaging.getInstance().unsubscribeFromTopic(Storage.albumNumber);
            FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
            PreferenceManager.getDefaultSharedPreferences(this).edit().remove("marks_notifications").apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit().remove("news_notifications").apply();

            restartApp();
        } else if (id == R.id.nav_relogging) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getString(R.string.relogging));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "navbar_action");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            restartApp();
        } else if (id == R.id.nav_marks) {
            showSemesterSpinner(true);
            setTitle(getString(R.string.final_marks));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment marksexplorer_ = new MarksExplorer();
            tx.replace(R.id.frameLayoutMain, marksexplorer_);
            tx.commit();
            currentFragmentScreen = "semester";
        } else if (id == R.id.nav_partial_marks) {
            showSemesterSpinner(true);
            setTitle(getString(R.string.partial_marks));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment partialmarksexplorer_ = new PartialMarksExplorer();
            tx.replace(R.id.frameLayoutMain, partialmarksexplorer_);
            tx.commit();
            currentFragmentScreen = "semester_partial";
        } else if (id == R.id.nav_files) {
            showSemesterSpinner(true);
            setTitle(getString(R.string.files));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment filesactivity = new FilesActivity();
            tx.replace(R.id.frameLayoutMain, filesactivity);
            tx.commit();
            currentFragmentScreen = "files";
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
