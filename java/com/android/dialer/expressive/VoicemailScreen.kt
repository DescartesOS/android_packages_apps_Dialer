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

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package com.android.dialer.expressive

import android.content.ContentUris
import android.database.ContentObserver
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.VoicemailContract.Voicemails
import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.android.dialer.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.*

data class VoiceMessage(val id: Long, val number: String, val date: Long, val duration: Long,
    val transcript: String, val available: Boolean) {
    val uri: Uri get() = ContentUris.withAppendedId(Voicemails.CONTENT_URI, id)
}

@Composable
fun VoicemailScreen(onCallVoicemail: () -> Unit, onSettings: () -> Unit, bottomInset: Dp = 0.dp) {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<VoiceMessage>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var unavailable by remember { mutableStateOf(false) }
    var revision by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf<Long?>(null) }
    var preparing by remember { mutableStateOf<Long?>(null) }
    var playbackError by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    fun stopPlayback() {
        val previous = player
        player = null
        playing = null
        preparing = null
        previous?.setOnPreparedListener(null)
        previous?.setOnCompletionListener(null)
        previous?.setOnErrorListener(null)
        previous?.release()
    }
    val lifecycleOwner = LocalView.current.findViewTreeLifecycleOwner()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) stopPlayback()
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            stopPlayback()
        }
    }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { revision++ }
        }
        context.contentResolver.registerContentObserver(Voicemails.CONTENT_URI, true, observer)
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
    LaunchedEffect(revision) {
        try {
            messages = withContext(Dispatchers.IO) {
                buildList {
                    context.contentResolver.query(Voicemails.CONTENT_URI,
                        arrayOf(Voicemails._ID, Voicemails.NUMBER, Voicemails.DATE, Voicemails.DURATION,
                            Voicemails.TRANSCRIPTION, Voicemails.HAS_CONTENT), null, null, "${Voicemails.DATE} DESC")?.use { c ->
                        while (c.moveToNext()) add(VoiceMessage(c.getLong(0), c.getString(1).orEmpty(), c.getLong(2),
                            c.getLong(3), c.getString(4).orEmpty(), c.getInt(5) != 0))
                    }
                }
            }
            unavailable = false
        } catch (e: CancellationException) { throw e } catch (_: Exception) { unavailable = true }
        finally { loading = false }
    }
    if (loading) { ExpressiveLoading(stringResource(R.string.expressive_loading)); return }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 104.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            ExpressiveTile(onCallVoicemail, Modifier.fillMaxWidth(), selected = true) {
                Symbol(DialerSymbol.Voicemail)
                Text(stringResource(R.string.expressive_call_voicemail), Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.labelLargeEmphasized)
            }
        }
        if (messages.isEmpty() || unavailable) item {
            EmptyDialerState(DialerSymbol.Voicemail, stringResource(R.string.expressive_no_voicemail),
                stringResource(R.string.expressive_voicemail_body), stringResource(R.string.expressive_settings), onSettings)
        }
        items(messages, key = { it.id }) { message ->
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(message.number.ifBlank { stringResource(R.string.expressive_unknown) }, style = MaterialTheme.typography.titleMediumEmphasized)
                    Text(DateUtils.getRelativeTimeSpanString(message.date).toString() + " · " + DateUtils.formatElapsedTime(message.duration),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (message.transcript.isNotBlank()) Text(message.transcript)
                    if (preparing == message.id) ExpressiveLoading(stringResource(R.string.expressive_loading))
                    else FilledTonalButton(onClick = {
                        if (playing == message.id) {
                            stopPlayback()
                        } else {
                            stopPlayback()
                            preparing = message.id
                            playbackError = false
                            try {
                                val next = MediaPlayer()
                                player = next
                                next.apply {
                                    setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                                    setDataSource(context, message.uri)
                                    setOnPreparedListener {
                                        if (player === it) {
                                            it.start(); playing = message.id; preparing = null
                                        }
                                    }
                                    setOnCompletionListener { if (player === it) stopPlayback() }
                                    setOnErrorListener { failed, _, _ ->
                                        if (player === failed) { stopPlayback(); playbackError = true }
                                        true
                                    }
                                    prepareAsync()
                                }
                            } catch (_: Exception) {
                                stopPlayback(); playbackError = true
                            }
                        }
                    }, enabled = message.available) {
                        Symbol(if (playing == message.id) DialerSymbol.Pause else DialerSymbol.Play)
                        Text(stringResource(if (playing == message.id) R.string.expressive_stop else R.string.expressive_play), Modifier.padding(start = 8.dp))
                    }
                    if (!message.available) Text(stringResource(R.string.expressive_voicemail_download), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (playbackError) item { Text(stringResource(R.string.expressive_playback_failed), color = MaterialTheme.colorScheme.error) }
    }
}
