package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SettingsActivity extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    FirebaseAnalytics mFirebaseAnalytics;
    Context activityContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityContext = getActivity();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);
        addPreferencesFromResource(R.xml.settings);

        /*RememberPassword rememberPassword = new RememberPassword(activityContext);
        if (!rememberPassword.isRemembered()){
            getPreferenceManager().getSharedPreferences().edit().putBoolean("marks_notifications", false).apply();
            getPreferenceScreen().findPreference("marks_notifications").setEnabled(false);
            getPreferenceScreen().findPreference("marks_notifications").setDefaultValue(false);
            getPreferenceScreen().findPreference("marks_notifications").setSummary(getPreferenceScreen()
                    .findPreference("marks_notifications").getSummary() + "\n\n" + getString(R.string.only_with_remember_password));
        }*/
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
        /*if (s.equals("news_notifications")){
            if (sharedPreferences.getBoolean("news_notifications", true))
                FirebaseMessaging.getInstance().subscribeToTopic("news");
            else
                FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
        }
        else*/ if (s.equals("night_mode")){
            ((MainActivity) activityContext).restartApp();
        }
    }
}
