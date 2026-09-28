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
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class RecordingPlayback(private val context: Context) {
    var mediaId: Long? by mutableStateOf(null)
        private set
    var preparing by mutableStateOf(false)
        private set
    var failed by mutableStateOf(false)
        private set
    var positionMillis by mutableIntStateOf(0)
        private set
    var durationMillis by mutableIntStateOf(0)
        private set
    private var player: MediaPlayer? = null
    private var focus: AudioFocusRequest? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgress = object : Runnable {
        override fun run() {
            val current = player ?: return
            if (preparing) return
            try {
                positionMillis = current.currentPosition.coerceIn(0, durationMillis)
                handler.postDelayed(this, 250L)
            } catch (_: Exception) { fail() }
        }
    }
    private val audio = context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()

    fun play(id: Long, uri: Uri) {
        stop()
        failed = false
        try {
            val next = MediaPlayer()
            player = next
            mediaId = id
            preparing = true
            next.setAudioAttributes(attributes)
            next.setDataSource(context, uri)
            next.setOnPreparedListener { ready ->
                if (player === ready) {
                    try {
                        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(attributes)
                            .setOnAudioFocusChangeListener({ change ->
                                if (player === ready && change != AudioManager.AUDIOFOCUS_GAIN) stop()
                            }, handler).build()
                        focus = request
                        if (audio.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            durationMillis = ready.duration.coerceAtLeast(0)
                            ready.start()
                            preparing = false
                            handler.post(updateProgress)
                        } else fail()
                    } catch (_: Exception) { fail() }
                }
            }
            next.setOnCompletionListener { if (player === it) stop() }
            next.setOnErrorListener { broken, _, _ ->
                if (player === broken) fail()
                true
            }
            next.prepareAsync()
        } catch (_: Exception) { fail() }
    }

    private fun fail() {
        stop()
        failed = true
    }

    fun stop() {
        handler.removeCallbacks(updateProgress)
        val previous = player
        player = null
        mediaId = null
        preparing = false
        positionMillis = 0
        durationMillis = 0
        previous?.setOnPreparedListener(null)
        previous?.setOnCompletionListener(null)
        previous?.setOnErrorListener(null)
        previous?.release()
        focus?.let { audio.abandonAudioFocusRequest(it) }
        focus = null
    }
}
