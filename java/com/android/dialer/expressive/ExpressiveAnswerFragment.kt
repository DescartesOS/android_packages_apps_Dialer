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

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.android.dialer.expressive

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.android.dialer.R
import com.android.incallui.answer.protocol.*

class ExpressiveAnswerFragment : ExpressiveCallFragment(), AnswerScreen {
    override val answering = true
    private lateinit var answerDelegate: AnswerScreenDelegate
    private var responses by mutableStateOf<List<String>>(emptyList())
    private var showResponses by mutableStateOf(false)
    private var takingAction by mutableStateOf(false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        answerDelegate = (requireActivity() as AnswerScreenDelegateFactory).newAnswerScreenDelegate(this)
        super.onViewCreated(view, savedInstanceState)
        if (allowAnswerAndRelease()) answerDelegate.onAnswerAndReleaseButtonEnabled()
    }
    override fun onDestroyView() {
        if (allowAnswerAndRelease()) answerDelegate.onAnswerAndReleaseButtonDisabled()
        answerDelegate.onAnswerScreenUnready()
        super.onDestroyView()
    }
    @Composable
    override fun CallGestureContainer(content: @Composable () -> Unit) {
        IncomingCallSwipe(enabled = !takingAction && !showResponses,
            onAnswer = { if (!takingAction) { takingAction = true; answerDelegate.onAnswer(false) } },
            onReject = { if (!takingAction) { takingAction = true; answerDelegate.onReject() } }, content = content)
    }
    @Composable
    override fun AnswerActions() {
        LaunchedEffect(takingAction) {
            if (takingAction) { kotlinx.coroutines.delay(5000); takingAction = false }
        }
        IncomingCallHandle(enabled = !takingAction && !showResponses)
        if (primaryInfoState.answeringDisconnectsOngoingCall()) Text(stringResource(R.string.expressive_answer_ends_call), Modifier.padding(12.dp))
        if (allowAnswerAndRelease()) TextButton(onClick = { takingAction = true; answerDelegate.onAnswerAndReleaseCall() }, enabled = !takingAction) {
            Text(stringResource(R.string.expressive_answer_release))
        }
        if (responses.isNotEmpty()) FilledTonalButton(onClick = { showResponses = true }, enabled = !takingAction,
            modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp), shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)) {
            Text(stringResource(R.string.expressive_reply))
        }
        if (takingAction) Text(stringResource(R.string.expressive_connecting), Modifier.padding(8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite })
        if (showResponses) ModalBottomSheet(onDismissRequest = { showResponses = false }) {
            responses.forEach { response ->
                TextButton(onClick = { showResponses = false; takingAction = true; answerDelegate.onRejectCallWithMessage(response) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) { Text(response) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    override fun getCallId(): String = requireArguments().getString("call_id")!!
    override fun isRttCall() = false
    override fun isVideoCall() = false
    override fun isVideoUpgradeRequest() = false
    override fun allowAnswerAndRelease() = requireArguments().getBoolean("release")
    override fun isActionTimeout() = ::answerDelegate.isInitialized && answerDelegate.isActionTimeout
    override fun setTextResponses(textResponses: List<String>) { responses = textResponses }
    override fun hasPendingDialogs() = showResponses
    override fun dismissPendingDialogs() { showResponses = false }
    override fun getAnswerScreenFragment(): Fragment = this
    companion object {
        @JvmStatic fun create(callId: String, release: Boolean) = ExpressiveAnswerFragment().apply {
            arguments = Bundle().apply { putString("call_id", callId); putBoolean("release", release) }
        }
    }
}
