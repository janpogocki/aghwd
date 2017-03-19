package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.view.SubMenu;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Menu MenuWithActionBar = null;
    SearchView searchView = null;
    SkosActivity skosactivity = null;
    ScheduleActivity scheduleactivity = null;

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
        MenuWithActionBar.findItem(R.id.action_search).setVisible(val);
    }

    public void showScheduleButtons(Boolean val){
        MenuWithActionBar.findItem(R.id.action_previous_week).setVisible(val);
        MenuWithActionBar.findItem(R.id.action_next_week).setVisible(val);
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
        searchView.setQueryHint("Szukaj nazwiska...");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if savedInstanceState is not null (then LogIn)
        if (!Storage.loggedIn && savedInstanceState == null){
            Intent openLogIn = new Intent(getApplicationContext(), LogIn.class);
            startActivity(openLogIn);
        }
        else {
            Storage.loggedIn = false;
            Storage.oneMoreBackPressedButtonMeansExit = false;

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
            imageView.setImageBitmap(Storage.photoUser);

            // Generate semesters entries
            MenuItem menuNV = navigationView.getMenu().findItem(R.id.semester);
            SubMenu subMenuMenuMV = menuNV.getSubMenu();
            for (int i = 0; i < Storage.summarySemesters.size(); i++) {
                subMenuMenuMV.add(R.id.semesterItems, i, i, "Semestr " + Storage.getSemesterNumberById(i)).setIcon(R.drawable.ic_menu_semester);
            }
            navigationView.getMenu().findItem(R.id.semester).getSubMenu().setGroupCheckable(R.id.semesterItems, true, true);

            // If multidirectionars enable switch dir
            if (Storage.multiKierunek)
                navigationView.getMenu().findItem(R.id.nav_relogging).setVisible(true);

            // Load startup preferences and show user's startup screen
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Storage.sharedPreferencesStartScreen = sharedPreferences.getString("default_start_screen", "semester");

            if ("summary".equals(Storage.sharedPreferencesStartScreen)){
                navigationView.getMenu().findItem(R.id.nav_summary).setChecked(true);
                setTitle("Podsumowanie");
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.SummaryActivity"));
                tx.commit();
            }
            else if ("schedule".equals(Storage.sharedPreferencesStartScreen)){
                navigationView.getMenu().findItem(R.id.nav_schedule).setChecked(true);
                setTitle("Podział godzin");
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                Fragment scheduleactivity_ = new ScheduleActivity();
                tx.replace(R.id.frameLayoutMain, scheduleactivity_);
                tx.commit();
                scheduleactivity = (ScheduleActivity) scheduleactivity_;
            }
            else {
                // Load newest semester and check it on sidebar
                setTitle("Semestr " + Storage.getSemesterNumberById(Storage.currentSemester));
                navigationView.getMenu().findItem(R.id.semester).getSubMenu().getItem(Storage.currentSemester).setChecked(true);
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.MarksExplorer"));
                tx.commit();
            }
        }
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
            if (!Storage.oneMoreBackPressedButtonMeansExit){
                Toast.makeText(MainActivity.this, "Stuknij ponownie, aby zakończyć", Toast.LENGTH_SHORT).show();
                Storage.oneMoreBackPressedButtonMeansExit = true;
            }
            else {
                Storage.oneMoreBackPressedButtonMeansExit = true;
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

        if ("semester".equals(Storage.sharedPreferencesStartScreen))
            MenuWithActionBar.findItem(R.id.action_change_marks).setVisible(true).setTitle(R.string.menu_marks);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_marks) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();

            if (item.getTitle().equals(getString(R.string.menu_marks))) {
                item.setTitle(R.string.menu_partial_marks);
                tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.PartialMarksExplorer"));
            } else {
                item.setTitle(R.string.menu_marks);
                tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.MarksExplorer"));
            }

            tx.commit();
        }
        else if (id == R.id.action_search) {
            setupSearchView();
        }
        else if (id == R.id.action_previous_week) {
            scheduleactivity.changeWeek("Poprzedni");
        }
        else if (id == R.id.action_next_week) {
            scheduleactivity.changeWeek("Następny");
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Storage.oneMoreBackPressedButtonMeansExit = false;
        Storage.openedBrowser = false;
        navigationView.getMenu().findItem(R.id.semester).getSubMenu().getItem(Storage.currentSemester).setChecked(false);
        MenuWithActionBar.findItem(R.id.action_change_marks).setVisible(false);

        showSearchButton(false);
        showScheduleButtons(false);
        resetMainLayoutVisibility(true);

        if (id == R.id.nav_about) {
            setTitle("O aplikacji");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.AboutActivity"));
            tx.commit();
        } else if (id == R.id.nav_settings) {
            setTitle("Ustawienia");
            resetMainLayoutVisibility(false);
            android.app.FragmentTransaction tx = getFragmentManager().beginTransaction();
            android.app.Fragment settingsactivity_ = new SettingsActivity();
            tx.replace(R.id.frameLayoutMainV7, settingsactivity_);
            tx.commit();
        } else if (id == R.id.nav_summary) {
            setTitle("Podsumowanie");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.SummaryActivity"));
            tx.commit();
        } else if (id == R.id.nav_groups) {
            setTitle("Moduły i grupy");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.GroupsActivity"));
            tx.commit();
        } else if (id == R.id.nav_diploma) {
            setTitle("Praca dyplomowa");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.DiplomaActivity"));
            tx.commit();
        } else if (id == R.id.nav_schedule) {
            setTitle("Podział godzin");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment scheduleactivity_ = new ScheduleActivity();
            tx.replace(R.id.frameLayoutMain, scheduleactivity_);
            tx.commit();
            scheduleactivity = (ScheduleActivity) scheduleactivity_;
        } else if (id == R.id.nav_syllabus) {
            setTitle("Syllabus");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.SyllabusActivity"));
            tx.commit();
        } else if (id == R.id.nav_skos) {
            setTitle("SkOs");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            Fragment skosactivity_ = new SkosActivity();
            tx.replace(R.id.frameLayoutMain, skosactivity_);
            tx.commit();
            skosactivity = (SkosActivity) skosactivity_;

            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);
        } else if (id == R.id.nav_logout) {
            RememberPassword rp = new RememberPassword(this);

            if (rp.isRemembered())
                rp.remove();

            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else if (id == R.id.nav_relogging) {
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else if (id >= 0 && id <= 40){
            // Marks
            MenuWithActionBar.findItem(R.id.action_change_marks).setVisible(true).setTitle(R.string.menu_marks);
            Storage.currentSemester = id;
            setTitle("Semestr " + Storage.getSemesterNumberById(id));
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.MarksExplorer"));
            tx.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
