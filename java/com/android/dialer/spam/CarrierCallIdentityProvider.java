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
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.Connection;

public interface CarrierCallIdentityProvider {
  String EXTRA_CARRIER_SPAM_STATUS = "com.android.dialer.extra.CARRIER_SPAM_STATUS";

  CarrierCallIdentity identify(Context context, Call.Details details);

  static CarrierCallIdentityProvider get(Context context) {
    return Holder.provider;
  }

  static void set(CarrierCallIdentityProvider provider) {
    Holder.provider = provider == null ? PlatformCarrierCallIdentityProvider.INSTANCE : provider;
  }

  final class Holder {
    private static CarrierCallIdentityProvider provider = PlatformCarrierCallIdentityProvider.INSTANCE;

    private Holder() {}
  }

  final class PlatformCarrierCallIdentityProvider implements CarrierCallIdentityProvider {
    private static final PlatformCarrierCallIdentityProvider INSTANCE =
        new PlatformCarrierCallIdentityProvider();

    private PlatformCarrierCallIdentityProvider() {}

    @Override
    public CarrierCallIdentity identify(Context context, Call.Details details) {
      int verification = CarrierCallIdentity.VERIFICATION_UNKNOWN;
      switch (details.getCallerNumberVerificationStatus()) {
        case Connection.VERIFICATION_STATUS_PASSED:
          verification = CarrierCallIdentity.VERIFICATION_VERIFIED;
          break;
        case Connection.VERIFICATION_STATUS_FAILED:
          verification = CarrierCallIdentity.VERIFICATION_UNVERIFIED;
          break;
        default:
          break;
      }

      int spam = CarrierCallIdentity.SPAM_UNKNOWN;
      Bundle extras = details.getExtras();
      if (extras != null && extras.containsKey(EXTRA_CARRIER_SPAM_STATUS)) {
        int value = extras.getInt(EXTRA_CARRIER_SPAM_STATUS, CarrierCallIdentity.SPAM_UNKNOWN);
        if (value >= CarrierCallIdentity.SPAM_UNKNOWN
            && value <= CarrierCallIdentity.SPAM_CONFIRMED) {
          spam = value;
        }
      }
      return new CarrierCallIdentity(spam, verification);
    }
  }
}
