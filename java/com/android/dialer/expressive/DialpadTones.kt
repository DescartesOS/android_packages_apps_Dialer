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
 * limitations under the License.
 */

package com.android.dialer.expressive

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner

@Composable
internal fun rememberDialpadTones(): DialpadTones {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalView.current.findViewTreeLifecycleOwner()
    val tones = remember(context) { DialpadTones(context) }
    DisposableEffect(tones, lifecycleOwner) {
        val lifecycle = lifecycleOwner?.lifecycle
        tones.resumed = lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> tones.resumed = true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> {
                    tones.resumed = false
                    tones.release()
                }
                else -> Unit
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
            tones.resumed = false
            tones.release()
        }
    }
    return tones
}

internal class DialpadTones(private val context: Context) {
    var resumed = false
    private var generator: ToneGenerator? = null

    fun play(digit: String) {
        if (!resumed) return
        val tone = when (digit) {
            "0" -> ToneGenerator.TONE_DTMF_0
            "1" -> ToneGenerator.TONE_DTMF_1
            "2" -> ToneGenerator.TONE_DTMF_2
            "3" -> ToneGenerator.TONE_DTMF_3
            "4" -> ToneGenerator.TONE_DTMF_4
            "5" -> ToneGenerator.TONE_DTMF_5
            "6" -> ToneGenerator.TONE_DTMF_6
            "7" -> ToneGenerator.TONE_DTMF_7
            "8" -> ToneGenerator.TONE_DTMF_8
            "9" -> ToneGenerator.TONE_DTMF_9
            "*" -> ToneGenerator.TONE_DTMF_S
            "#" -> ToneGenerator.TONE_DTMF_P
            else -> return
        }
        try {
            val enabled = Settings.System.getInt(
                context.contentResolver, Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (!enabled || audio?.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
            val player = generator ?: ToneGenerator(AudioManager.STREAM_DTMF, 80).also { generator = it }
            player.startTone(tone, 150)
        } catch (error: RuntimeException) {
            Log.w("DialpadTones", "Unable to play local dialing tone", error)
            release()
        }
    }

    fun release() {
        val previous = generator ?: return
        generator = null
        try {
            previous.stopTone()
        } catch (error: RuntimeException) {
            Log.w("DialpadTones", "Unable to stop local dialing tone", error)
        } finally {
            try {
                previous.release()
            } catch (error: RuntimeException) {
                Log.w("DialpadTones", "Unable to release local dialing tone", error)
            }
        }
    }
}
