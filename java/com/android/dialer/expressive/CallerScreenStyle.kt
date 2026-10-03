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

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager

internal const val CALLER_STYLE_FULLSCREEN = "fullscreen"
internal const val CALLER_STYLE_COOKIE = "cookie"
internal const val CALLER_STYLE_HYPEROS = "hyperos"
internal const val ROUNDED_CALL_CONTROLS_KEY = "rounded_call_screen_controls"

object CallerScreenStylePreferences {
    const val KEY = "caller_screen_style"
    const val LEGACY_KEY = "fullscreen_caller_photo"

    @JvmStatic
    fun readAndMigrate(preferences: SharedPreferences): String {
        val stored = preferences.getString(KEY, null)
        if (stored == CALLER_STYLE_FULLSCREEN || stored == CALLER_STYLE_COOKIE || stored == CALLER_STYLE_HYPEROS)
            return stored
        val style = if (preferences.getBoolean(LEGACY_KEY, true)) CALLER_STYLE_FULLSCREEN else CALLER_STYLE_COOKIE
        preferences.edit().putString(KEY, style).apply()
        return style
    }
}

@Composable
internal fun rememberCallerScreenStyle(): String {
    val context = LocalContext.current
    val preferences = remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
    var style by remember(preferences) { mutableStateOf(CallerScreenStylePreferences.readAndMigrate(preferences)) }
    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key == CallerScreenStylePreferences.KEY || key == CallerScreenStylePreferences.LEGACY_KEY)
                style = CallerScreenStylePreferences.readAndMigrate(preferences)
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        style = CallerScreenStylePreferences.readAndMigrate(preferences)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return style
}

@Composable
internal fun rememberRoundedCallControls(): Boolean {
    val context = LocalContext.current
    val preferences = remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
    var rounded by remember(preferences) { mutableStateOf(preferences.getBoolean(ROUNDED_CALL_CONTROLS_KEY, false)) }
    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key == ROUNDED_CALL_CONTROLS_KEY)
                rounded = preferences.getBoolean(ROUNDED_CALL_CONTROLS_KEY, false)
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        rounded = preferences.getBoolean(ROUNDED_CALL_CONTROLS_KEY, false)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return rounded
}

@Composable
internal fun rememberHyperOsForeground(photo: ImageBitmap?, hasFallback: Boolean): Color {
    val themeForeground = MaterialTheme.colorScheme.onSurface
    return remember(photo, hasFallback, themeForeground) {
        if (photo == null) {
            if (hasFallback) Color.White else themeForeground
        } else runCatching {
            val pixels = photo.toPixelMap()
            var luminance = 0f
            for (y in 1..5) for (x in 1..5) {
                luminance += pixels[(pixels.width * x / 6).coerceAtMost(pixels.width - 1),
                    (pixels.height * y / 6).coerceAtMost(pixels.height - 1)].luminance()
            }
            if (luminance / 25f > .5f) Color.Black else Color.White
        }.getOrDefault(themeForeground)
    }
}

@Composable
internal fun HyperOsCallerBackground(photo: ImageBitmap?, modifier: Modifier = Modifier, fallbackPhotoResId: Int = 0) {
    val painter = when {
        photo != null -> remember(photo) { BitmapPainter(photo) }
        fallbackPhotoResId != 0 -> painterResource(fallbackPhotoResId)
        else -> null
    }
    val radius = with(LocalDensity.current) { 48.dp.toPx() }
    val blur = remember(radius) { BlurEffect(radius, radius, TileMode.Clamp) }
    Box(modifier.clipToBounds().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        if (painter != null) Image(painter, contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                scaleX = 1.08f
                scaleY = 1.08f
                renderEffect = blur
            }, contentScale = ContentScale.Crop)
    }
}
