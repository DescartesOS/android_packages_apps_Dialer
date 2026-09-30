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

package com.android.dialer.expressive

import android.content.Context
import android.provider.CallLog.Calls
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.dialer.R
import com.android.dialer.blockreportspam.BlockReportSpamDialogInfo
import com.android.dialer.blockreportspam.ShowBlockReportSpamDialogNotifier
import java.util.Locale

internal fun showCarrierBlockDialog(context: Context, number: String, report: Boolean) {
    val info = BlockReportSpamDialogInfo.newBuilder()
        .setNormalizedNumber(number)
        .setCountryIso(Locale.getDefault().country)
        .setCallType(Calls.INCOMING_TYPE)
        .build()
    if (report) {
        ShowBlockReportSpamDialogNotifier.notifyShowDialogToBlockNumberAndOptionallyReportSpam(context, info)
    } else {
        ShowBlockReportSpamDialogNotifier.notifyShowDialogToBlockNumber(context, info)
    }
}

@Composable
internal fun CarrierSpamActionButtons(number: String) {
    if (number.isBlank()) return
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { showCarrierBlockDialog(context, number, false) }, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.expressive_block_number))
        }
        OutlinedButton(onClick = { showCarrierBlockDialog(context, number, true) }, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.expressive_report_spam))
        }
    }
}
