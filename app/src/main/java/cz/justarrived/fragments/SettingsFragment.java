package cz.justarrived.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import cz.justarrived.Constants;
import cz.justarrived.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.fragmented_preferences);

    PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    PreferenceScreen preferenceScreen = getPreferenceScreen();
    int preferencesCount = preferenceScreen.getPreferenceCount();
    for (int i = 0; i < preferencesCount; ++i) {
      Preference preference = preferenceScreen.getPreference(i);
      String key = preference.getKey();

      refreshPreference(preferences, key, preference);
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    Preference preference = getPreferenceScreen().findPreference(s);

    refreshPreference(sharedPreferences, s, preference);
  }

  private void refreshPreference(SharedPreferences sharedPreferences, String key, Preference preference) {
    if (key == null || preference == null || key.equals(Constants.SMS_OR_NOTIFY_KEY)) {
      return;
    }
    String newValue = "";

    if (key.equals(Constants.TRIGGER_RADIUS_KEY)) {
      String value = sharedPreferences.getString(Constants.TRIGGER_RADIUS_KEY, "");
      String suffix = "m";
      if (!value.isEmpty()) {
        newValue = value + suffix;
      }
    }
    if (!newValue.isEmpty()) {
      preference.setSummary(newValue);
    }

  }
}
