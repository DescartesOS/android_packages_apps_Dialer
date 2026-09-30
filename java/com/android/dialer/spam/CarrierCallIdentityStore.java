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

package com.android.dialer.spam;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;

public final class CarrierCallIdentityStore {
  private static final String PREFS = "carrier_call_identity";
  private static final String SPAM_SUFFIX = ".spam";
  private static final String VERIFICATION_SUFFIX = ".verification";

  private CarrierCallIdentityStore() {}

  public static void put(
      Context context, Uri handle, PhoneAccountHandle account, CarrierCallIdentity identity) {
    if (handle == null || identity == null) {
      return;
    }
    context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putInt(key(handle, account) + SPAM_SUFFIX, identity.getSpamStatus())
        .putInt(key(handle, account) + VERIFICATION_SUFFIX, identity.getVerificationStatus())
        .apply();
  }

  public static CarrierCallIdentity get(
      Context context, String number, PhoneAccountHandle account) {
    if (number == null || number.isEmpty()) {
      return CarrierCallIdentity.unknown();
    }
    String key = key(number, account);
    SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    return new CarrierCallIdentity(
        prefs.getInt(key + SPAM_SUFFIX, CarrierCallIdentity.SPAM_UNKNOWN),
        prefs.getInt(key + VERIFICATION_SUFFIX, CarrierCallIdentity.VERIFICATION_UNKNOWN));
  }

  private static String key(Uri handle, PhoneAccountHandle account) {
    return key(handle.getSchemeSpecificPart(), account);
  }

  private static String key(String number, PhoneAccountHandle account) {
    String normalized = PhoneNumberUtils.normalizeNumber(number);
    String accountId = account == null ? "" : account.getId();
    return normalized + "|" + accountId;
  }
}
