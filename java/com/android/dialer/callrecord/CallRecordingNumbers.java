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

package com.android.dialer.callrecord;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Locale;

/** *Still keep her every voice recording */
public final class CallRecordingNumbers {
  private CallRecordingNumbers() {}

  public static String countryIso(Context context) {
    TelephonyManager telephony = context.getSystemService(TelephonyManager.class);
    String country = telephony == null ? null : telephony.getNetworkCountryIso();
    if (TextUtils.isEmpty(country) && telephony != null) {
      country = telephony.getSimCountryIso();
    }
    if (TextUtils.isEmpty(country)) {
      country = Locale.getDefault().getCountry();
    }
    return country.toUpperCase(Locale.ROOT);
  }

  public static String key(String number, String countryIso) {
    if (TextUtils.isEmpty(number)) {
      return "";
    }
    number = number.trim();
    if (PhoneNumberUtils.isUriNumber(number)) {
      return number;
    }
    String network = PhoneNumberUtils.normalizeNumber(
        PhoneNumberUtils.extractNetworkPortion(number));
    if (TextUtils.isEmpty(network)) {
      return "";
    }
    String e164 = PhoneNumberUtils.formatNumberToE164(network, countryIso);
    return (e164 == null ? network : e164) + PhoneNumberUtils.extractPostDialPortion(number);
  }
}
