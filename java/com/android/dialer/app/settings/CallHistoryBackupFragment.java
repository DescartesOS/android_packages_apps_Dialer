/*
 * Copyright (C) 2024-2026 Lunaris AOSP
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

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.WRITE_CALL_LOG;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.android.dialer.R;
import com.android.dialer.common.LogUtil;
import com.android.dialer.phonenumbercache.CallLogQuery;
import com.android.dialer.util.PermissionsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallHistoryBackupFragment
    extends com.android.dialer.expressive.ExpressivePreferenceFragment {
  private static final String MIME_TYPE = "application/json";
  private static final String FILE_NAME = "dialer_call_history.json";
  private static final String FORMAT = "dialer-call-history";
  private static final int VERSION = 1;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private ActivityResultLauncher<String> exportLauncher;
  private ActivityResultLauncher<String[]> importLauncher;
  private String pendingExport;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    exportLauncher = registerForActivityResult(
        new ActivityResultContracts.CreateDocument(MIME_TYPE), uri -> {
          if (uri != null && pendingExport != null) writeExport(uri, pendingExport);
        });
    importLauncher = registerForActivityResult(
        new ActivityResultContracts.OpenDocument(), uri -> {
          if (uri != null) readImport(uri);
        });
  }

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
    setPreferenceScreen(getPreferenceManager().createPreferenceScreen(requireContext()));

    androidx.preference.Preference export = new androidx.preference.Preference(requireContext());
    export.setKey("export_call_history");
    export.setTitle(R.string.export_call_history_title);
    export.setSummary(R.string.export_call_history_summary);
    export.setOnPreferenceClickListener(preference -> {
      if (!PermissionsUtil.hasPermission(requireContext(), READ_CALL_LOG)) {
        showResult(R.string.call_history_permission_required);
        return true;
      }
      executor.execute(this::buildExport);
      return true;
    });
    getPreferenceScreen().addPreference(export);

    androidx.preference.Preference importPreference =
        new androidx.preference.Preference(requireContext());
    importPreference.setKey("import_call_history");
    importPreference.setTitle(R.string.import_call_history_title);
    importPreference.setSummary(R.string.import_call_history_summary);
    importPreference.setOnPreferenceClickListener(preference -> {
      if (!PermissionsUtil.hasPermission(requireContext(), WRITE_CALL_LOG)) {
        showResult(R.string.call_history_permission_required);
        return true;
      }
      importLauncher.launch(new String[] {MIME_TYPE, "text/*", "application/octet-stream"});
      return true;
    });
    getPreferenceScreen().addPreference(importPreference);
  }

  private void buildExport() {
    JSONArray calls = new JSONArray();
    ContentResolver resolver = requireContext().getContentResolver();
    try (Cursor cursor = resolver.query(
        Calls.CONTENT_URI,
        CallLogQuery.getProjection(),
        null,
        null,
        Calls.DATE + " ASC")) {
      if (cursor != null) {
        while (cursor.moveToNext()) {
          JSONObject call = new JSONObject();
          putNullable(call, "number", cursor.getString(CallLogQuery.NUMBER));
          call.put("date", cursor.getLong(CallLogQuery.DATE));
          call.put("duration", cursor.getLong(CallLogQuery.DURATION));
          call.put("type", cursor.getInt(CallLogQuery.CALL_TYPE));
          putNullable(call, "countryIso", cursor.getString(CallLogQuery.COUNTRY_ISO));
          call.put("numberPresentation", cursor.getInt(CallLogQuery.NUMBER_PRESENTATION));
          call.put("features", cursor.getInt(CallLogQuery.FEATURES));
          if (!cursor.isNull(CallLogQuery.DATA_USAGE)) {
            call.put("dataUsage", cursor.getLong(CallLogQuery.DATA_USAGE));
          }
          putNullable(call, "postDialDigits", cursor.getString(CallLogQuery.POST_DIAL_DIGITS));
          putNullable(call, "viaNumber", cursor.getString(CallLogQuery.VIA_NUMBER));
          calls.put(call);
        }
      }
      JSONObject root = new JSONObject();
      root.put("format", FORMAT);
      root.put("version", VERSION);
      root.put("calls", calls);
      pendingExport = root.toString(2);
      requireActivity().runOnUiThread(() -> {
        if (pendingExport != null) exportLauncher.launch(FILE_NAME);
      });
    } catch (Exception e) {
      LogUtil.e("CallHistoryBackupFragment.buildExport", "Unable to export call history", e);
      showResult(R.string.call_history_export_failed);
    }
  }

  private void writeExport(Uri uri, String contents) {
    executor.execute(() -> {
      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
          requireContext().getContentResolver().openOutputStream(uri), StandardCharsets.UTF_8))) {
        writer.write(contents);
        showResult(R.string.call_history_export_complete);
      } catch (Exception e) {
        LogUtil.e("CallHistoryBackupFragment.writeExport", "Unable to write call history", e);
        showResult(R.string.call_history_export_failed);
      }
    });
  }

  private void readImport(Uri uri) {
    executor.execute(() -> {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(
          requireContext().getContentResolver().openInputStream(uri), StandardCharsets.UTF_8))) {
        StringBuilder contents = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) contents.append(line);
        int imported = importCalls(new JSONObject(contents.toString()));
        requireContext().getContentResolver().notifyChange(Calls.CONTENT_URI, null);
        showResult(getString(R.string.call_history_import_complete, imported));
      } catch (Exception e) {
        LogUtil.e("CallHistoryBackupFragment.readImport", "Unable to import call history", e);
        showResult(R.string.call_history_import_failed);
      }
    });
  }

  private int importCalls(JSONObject root) throws JSONException, IOException {
    if (!FORMAT.equals(root.optString("format")) || root.optInt("version", -1) != VERSION) {
      throw new JSONException("Unsupported call history backup");
    }
    JSONArray calls = root.optJSONArray("calls");
    if (calls == null) throw new JSONException("Missing calls");
    List<ContentValues> values = new ArrayList<>();
    for (int i = 0; i < calls.length(); i++) {
      JSONObject call = calls.getJSONObject(i);
      if (!call.has("date") || !call.has("type") || !call.has("numberPresentation")) {
        throw new JSONException("Invalid call entry");
      }
      ContentValues value = new ContentValues();
      putNullable(value, Calls.NUMBER, call.optString("number", null));
      value.put(Calls.DATE, call.getLong("date"));
      value.put(Calls.DURATION, call.optLong("duration", 0));
      value.put(Calls.TYPE, call.getInt("type"));
      value.put(Calls.NUMBER_PRESENTATION, call.getInt("numberPresentation"));
      putNullable(value, Calls.COUNTRY_ISO, call.optString("countryIso", null));
      value.put(Calls.FEATURES, call.optInt("features", 0));
      if (call.has("dataUsage")) value.put(Calls.DATA_USAGE, call.optLong("dataUsage"));
      putNullable(value, Calls.POST_DIAL_DIGITS, call.optString("postDialDigits", null));
      putNullable(value, Calls.VIA_NUMBER, call.optString("viaNumber", null));
      values.add(value);
    }

    ContentResolver resolver = requireContext().getContentResolver();
    for (ContentValues value : values) resolver.insert(Calls.CONTENT_URI, value);
    return values.size();
  }

  private static void putNullable(JSONObject object, String key, String value) throws JSONException {
    if (value == null) object.put(key, JSONObject.NULL); else object.put(key, value);
  }

  private static void putNullable(ContentValues values, String key, String value) {
    if (value == null) values.putNull(key); else values.put(key, value);
  }

  private void showResult(int stringRes) {
    if (isAdded()) requireActivity().runOnUiThread(
        () -> Toast.makeText(requireContext(), stringRes, Toast.LENGTH_SHORT).show());
  }

  private void showResult(CharSequence message) {
    if (isAdded()) requireActivity().runOnUiThread(
        () -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onDestroy() {
    executor.shutdownNow();
    super.onDestroy();
  }
}
