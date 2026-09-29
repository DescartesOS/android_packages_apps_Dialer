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

package com.android.dialer.contactphoto;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.android.dialer.R;

public final class CallerPhotoFallbacks {
  private static final int[] PHOTOS = {
    R.drawable.caller_fallback_img1,
    R.drawable.caller_fallback_img2,
    R.drawable.caller_fallback_img4,
    R.drawable.caller_fallback_img5,
    R.drawable.caller_fallback_img6,
    R.drawable.caller_fallback_img7,
  };

  private CallerPhotoFallbacks() {}

  @DrawableRes
  public static int forCall(@NonNull String uniqueCallId) {
    return PHOTOS[Math.floorMod(uniqueCallId.hashCode(), PHOTOS.length)];
  }
}
