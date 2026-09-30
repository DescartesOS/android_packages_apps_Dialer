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

public final class CarrierCallIdentity {
  public static final int SPAM_UNKNOWN = 0;
  public static final int SPAM_NOT_SPAM = 1;
  public static final int SPAM_SUSPECTED = 2;
  public static final int SPAM_CONFIRMED = 3;

  public static final int VERIFICATION_UNKNOWN = 0;
  public static final int VERIFICATION_VERIFIED = 1;
  public static final int VERIFICATION_UNVERIFIED = 2;

  private final int spamStatus;
  private final int verificationStatus;

  public CarrierCallIdentity(int spamStatus, int verificationStatus) {
    this.spamStatus = spamStatus;
    this.verificationStatus = verificationStatus;
  }

  public int getSpamStatus() {
    return spamStatus;
  }

  public int getVerificationStatus() {
    return verificationStatus;
  }

  public boolean isSpam() {
    return spamStatus == SPAM_CONFIRMED;
  }

  public static CarrierCallIdentity unknown() {
    return new CarrierCallIdentity(SPAM_UNKNOWN, VERIFICATION_UNKNOWN);
  }

  public CarrierCallIdentity merge(CarrierCallIdentity other) {
    if (other == null) {
      return this;
    }
    return new CarrierCallIdentity(
        spamStatus == SPAM_UNKNOWN ? other.spamStatus : spamStatus,
        verificationStatus == VERIFICATION_UNKNOWN
            ? other.verificationStatus
            : verificationStatus);
  }
}
