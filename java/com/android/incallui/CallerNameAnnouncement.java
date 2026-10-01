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

package com.android.incallui;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.android.dialer.R;
import com.android.incallui.call.CallList;
import com.android.incallui.call.DialerCall;
import com.android.incallui.call.DialerCallListener;
import com.android.incallui.call.state.DialerCallState;

import java.util.Locale;

final class CallerNameAnnouncement implements InCallPresenter.InCallStateListener,
    DialerCallListener, ContactInfoCache.ContactInfoCacheCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {
  static final String KEY = "caller_name_announcement";

  private final Context context;
  private final SharedPreferences preferences;
  private final ContactInfoCache contactInfoCache;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private TextToSpeech tts;
  private DialerCall call;
  private String callId;
  private boolean announced;
  private boolean ttsReady;

  CallerNameAnnouncement(@NonNull Context context, @NonNull ContactInfoCache cache) {
    this.context = context.getApplicationContext();
    Context deviceContext = this.context.createDeviceProtectedStorageContext();
    preferences = PreferenceManager.getDefaultSharedPreferences(deviceContext);
    contactInfoCache = cache;
    preferences.registerOnSharedPreferenceChangeListener(this);
  }

  void start(DialerCall incomingCall) {
    if (call != null && incomingCall != null && callId != null
        && callId.equals(incomingCall.getId())) return;
    stop();
    if (!enabled() || incomingCall == null || incomingCall.getState() != DialerCallState.INCOMING
        || incomingCall.isBlocked() || !isAudibleEnvironment()
        || CallList.getInstance().getActiveCall() != null) return;
    call = incomingCall;
    callId = incomingCall.getId();
    announced = false;
    call.addListener(this);
    contactInfoCache.findInfo(incomingCall, true, this);
  }

  void stop() {
    if (call != null) call.removeListener(this);
    call = null;
    callId = null;
    announced = false;
    if (tts != null) {
      try { tts.stop(); tts.shutdown(); } catch (RuntimeException ignored) {}
      tts = null;
      ttsReady = false;
    }
  }

  void destroy() {
    stop();
    preferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  private boolean enabled() { return preferences.getBoolean(KEY, false); }

  private boolean isAudibleEnvironment() {
    AudioManager audio = context.getSystemService(AudioManager.class);
    if (audio == null || audio.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) return false;
    TelecomManager telecom = context.getSystemService(TelecomManager.class);
    NotificationManager notifications = context.getSystemService(NotificationManager.class);
    if (notifications != null
        && notifications.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL) {
      return false;
    }
    return telecom == null || !telecom.isInCall() || CallList.getInstance().getActiveCall() == null;
  }

  private boolean stillValid() {
    return enabled() && call != null && callId != null && callId.equals(call.getId())
        && call.getState() == DialerCallState.INCOMING && !call.isBlocked() && !announced
        && isAudibleEnvironment();
  }

  private void speak(String text) {
    if (!stillValid() || TextUtils.isEmpty(text)) return;
    if (tts == null) {
      tts = new TextToSpeech(context, status -> {
        if (status == TextToSpeech.SUCCESS) {
          ttsReady = true;
          speak(text);
        } else stop();
      });
      return;
    }
    if (!ttsReady) return;
    try {
      announced = true;
      tts.setLanguage(Locale.getDefault());
      tts.setAudioAttributes(new AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, new Bundle(), callId);
    } catch (RuntimeException e) { stop(); }
  }

  @Override public void onContactInfoComplete(String id, ContactInfoCache.ContactCacheEntry entry) {
    if (!stillValid() || !callId.equals(id)) return;
    boolean privateNumber = call.getNumberPresentation() != TelecomManager.PRESENTATION_ALLOWED;
    String name = entry == null ? null : entry.namePrimary;
    if (TextUtils.isEmpty(name) && entry != null) name = entry.nameAlternative;
    if (!privateNumber && !TextUtils.isEmpty(name) && !name.equals(entry.number)) {
      speak(context.getString(R.string.caller_announcement_incoming, name));
    } else {
      speak(context.getString(privateNumber ? R.string.caller_announcement_private
          : R.string.caller_announcement_unknown));
    }
  }

  @Override public void onImageLoadComplete(String id, ContactInfoCache.ContactCacheEntry entry) {}
  @Override public void onStateChange(InCallPresenter.InCallState oldState,
      InCallPresenter.InCallState newState, CallList calls) {
    if (newState.isIncoming()) start(calls.getIncomingCall()); else stop();
  }
  @Override public void onDialerCallDisconnect() { stop(); }
  @Override public void onDialerCallUpdate() { if (!stillValid()) stop(); }
  @Override public void onSharedPreferenceChanged(SharedPreferences p, String key) {
    if (KEY.equals(key) && !enabled()) stop();
  }
  @Override public void onDialerCallChildNumberChange() {}
  @Override public void onDialerCallLastForwardedNumberChange() {}
  @Override public void onDialerCallUpgradeToVideo() {}
  @Override public void onDialerCallSessionModificationStateChange() {}
  @Override public void onWiFiToLteHandover() {}
  @Override public void onHandoverToWifiFailure() {}
  @Override public void onInternationalCallOnWifi() {}
}
