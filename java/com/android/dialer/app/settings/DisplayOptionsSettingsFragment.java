/*
 * Copyright (C) 2015 The Android Open Source Project
 * Copyright (C) 2023 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.dialer.app.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import com.android.dialer.R;
import com.android.dialer.expressive.CallerScreenStylePreferences;

public class DisplayOptionsSettingsFragment extends com.android.dialer.expressive.ExpressivePreferenceFragment
    implements Preference.OnPreferenceChangeListener {

  private SharedPreferences mPrefs;
  private ListPreference mCallerScreenStyle;

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
    mPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
    String callerStyle = CallerScreenStylePreferences.readAndMigrate(mPrefs);
    addPreferencesFromResource(R.xml.display_options_settings);

    findPreference(getString(R.string.display_options_sort_list_by_key)).setVisible(
        getResources().getBoolean(R.bool.config_sort_order_user_changeable));
    findPreference(getString(R.string.display_options_view_names_as_key)).setVisible(
        getResources().getBoolean(R.bool.config_display_order_user_changeable));

    Preference callerPreference = findPreference(CallerScreenStylePreferences.KEY);
    if (callerPreference instanceof ListPreference) {
      mCallerScreenStyle = (ListPreference) callerPreference;
      mCallerScreenStyle.setValue(callerStyle);
      mCallerScreenStyle.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
      mCallerScreenStyle.setOnPreferenceChangeListener(this);
    }

  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object objValue) {
    if (preference == mCallerScreenStyle && objValue instanceof String) {
      return mCallerScreenStyle.findIndexOfValue((String) objValue) >= 0;
    }
    return false;
  }
}
