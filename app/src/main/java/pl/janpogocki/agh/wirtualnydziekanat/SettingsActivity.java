package pl.janpogocki.agh.wirtualnydziekanat;

import android.preference.PreferenceFragment;
import android.os.Bundle;

public class SettingsActivity extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
