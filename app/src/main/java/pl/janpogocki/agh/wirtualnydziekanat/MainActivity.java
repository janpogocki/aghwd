package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Menu MenuWithActionBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        for (int i=0; i<Storage.summarySemesters.size(); i++){
            subMenuMenuMV.add(R.id.semesterItems, i, i, "Semestr " + Storage.getSemesterNumberById(i)).setIcon(R.drawable.ic_menu_semester);
        }
        navigationView.getMenu().findItem(R.id.semester).getSubMenu().setGroupCheckable(R.id.semesterItems, true, true);

        // If multidirectionars enable switch dir
        if (Storage.multiKierunek)
            navigationView.getMenu().findItem(R.id.nav_relogging).setVisible(true);

        // Load newest semester and check it on sidebar
        setTitle("Semestr " + Storage.getSemesterNumberById(Storage.currentSemester));
        navigationView.getMenu().findItem(R.id.semester).getSubMenu().getItem(Storage.currentSemester).setChecked(true);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.MarksExplorer"));
        tx.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Storage.oneMoreBackPressedButtonMeansExit = false;
        navigationView.getMenu().findItem(R.id.semester).getSubMenu().getItem(Storage.currentSemester).setChecked(false);
        MenuWithActionBar.findItem(R.id.action_change_marks).setVisible(false);

        if (id == R.id.nav_about) {
            setTitle("O aplikacji");
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.frameLayoutMain, Fragment.instantiate(MainActivity.this, "pl.janpogocki.agh.wirtualnydziekanat.AboutActivity"));
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
        } else if (id == R.id.nav_logout) {
            RememberPassword rp = new RememberPassword(this);

            if (rp.isRemembered())
                rp.remove();

            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else if (id == R.id.nav_relogging) {
            Storage.multiKierunekClear();
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else if (id >= 0 && id <= 30){
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
