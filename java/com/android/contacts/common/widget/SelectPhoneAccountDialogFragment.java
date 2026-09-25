/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2023-2025 The LineageOS Project
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

package com.android.contacts.common.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.telecom.PhoneAccountHandle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.dialer.protos.ProtoParsers;

/**
 * Dialog that allows the user to select a phone accounts for a given action. Optionally provides
 * the choice to set the phone account as default.
 */
public class SelectPhoneAccountDialogFragment extends DialogFragment {

  private static final String ARG_OPTIONS = "options";

  private static final String ARG_IS_DEFAULT_CHECKED = "is_default_checked";

  private SelectPhoneAccountDialogOptions options =
      SelectPhoneAccountDialogOptions.getDefaultInstance();
  private SelectPhoneAccountListener listener;

  private boolean isDefaultChecked;
  private boolean isSelected;

  /** Create new fragment instance. */
  public static SelectPhoneAccountDialogFragment newInstance(
      SelectPhoneAccountDialogOptions options, SelectPhoneAccountListener listener) {
    SelectPhoneAccountDialogFragment fragment = new SelectPhoneAccountDialogFragment();
    fragment.setListener(listener);
    Bundle arguments = new Bundle();
    ProtoParsers.put(arguments, ARG_OPTIONS, options);
    fragment.setArguments(arguments);
    return fragment;
  }

  public void setListener(SelectPhoneAccountListener listener) {
    this.listener = listener;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(ARG_IS_DEFAULT_CHECKED, isDefaultChecked);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    options =
        ProtoParsers.getTrusted(
            getArguments(), ARG_OPTIONS, SelectPhoneAccountDialogOptions.getDefaultInstance());
    if (savedInstanceState != null) {
      isDefaultChecked = savedInstanceState.getBoolean(ARG_IS_DEFAULT_CHECKED);
    }
    isSelected = false;

    return com.android.dialer.expressive.ExpressiveDialogs.createSimPicker(
        requireContext(), options, isDefaultChecked,
        checked -> isDefaultChecked = checked,
        which -> {
          if (isSelected || !options.getEntries(which).getEnabled()) {
            return;
          }
          isSelected = true;
          PhoneAccountHandle selectedAccountHandle =
              SelectPhoneAccountDialogOptionsUtil.getPhoneAccountHandle(options.getEntries(which));
          Bundle result = new Bundle();
          result.putParcelable(
              SelectPhoneAccountListener.EXTRA_SELECTED_ACCOUNT_HANDLE, selectedAccountHandle);
          result.putBoolean(SelectPhoneAccountListener.EXTRA_SET_DEFAULT, isDefaultChecked);
          result.putString(SelectPhoneAccountListener.EXTRA_CALL_ID, getCallId());
          if (listener != null) {
            listener.onReceiveResult(SelectPhoneAccountListener.RESULT_SELECTED, result);
          }
        });
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    if (!isSelected && listener != null) {
      Bundle result = new Bundle();
      result.putString(SelectPhoneAccountListener.EXTRA_CALL_ID, getCallId());
      listener.onReceiveResult(SelectPhoneAccountListener.RESULT_DISMISSED, result);
    }
    super.onCancel(dialog);
  }

  @Nullable
  private String getCallId() {
    return options.getCallId();
  }

  public static class SelectPhoneAccountListener extends ResultReceiver {

    static final int RESULT_SELECTED = 1;
    static final int RESULT_DISMISSED = 2;

    static final String EXTRA_SELECTED_ACCOUNT_HANDLE = "extra_selected_account_handle";
    static final String EXTRA_SET_DEFAULT = "extra_set_default";
    static final String EXTRA_CALL_ID = "extra_call_id";

    protected SelectPhoneAccountListener() {
      super(new Handler(Looper.getMainLooper()));
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      if (resultCode == RESULT_SELECTED) {
        onPhoneAccountSelected(
            resultData.getParcelable(EXTRA_SELECTED_ACCOUNT_HANDLE, PhoneAccountHandle.class),
            resultData.getBoolean(EXTRA_SET_DEFAULT),
            resultData.getString(EXTRA_CALL_ID));
      } else if (resultCode == RESULT_DISMISSED) {
        onDialogDismissed(resultData.getString(EXTRA_CALL_ID));
      }
    }

    public void onPhoneAccountSelected(
        PhoneAccountHandle selectedAccountHandle, boolean setDefault, @Nullable String callId) {}

    public void onDialogDismissed(@Nullable String callId) {}
  }

}
