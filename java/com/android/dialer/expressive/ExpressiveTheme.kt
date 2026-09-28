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

import android.os.Build
import android.app.Activity
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager

/** Red like her lips */
internal val EndCallRed = Color(0xFFBA1A1A)

private const val MONOCHROME_THEME = "monochrome_theme"
private val LocalMonochromeTheme = staticCompositionLocalOf { false }
private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
internal fun monochromeImageFilter(): ColorFilter? = if (LocalMonochromeTheme.current) GrayscaleFilter else null

@Composable
internal fun endCallContainerColor(): Color = if (LocalMonochromeTheme.current) MaterialTheme.colorScheme.primary else EndCallRed

@Composable
internal fun endCallContentColor(): Color = if (LocalMonochromeTheme.current) MaterialTheme.colorScheme.onPrimary else Color.White

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DialerExpressiveTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val preferences = remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
    var monochrome by remember(preferences) { mutableStateOf(preferences.getBoolean(MONOCHROME_THEME, false)) }
    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key == MONOCHROME_THEME) {
                monochrome = preferences.getBoolean(MONOCHROME_THEME, false)
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        monochrome = preferences.getBoolean(MONOCHROME_THEME, false)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val colors = if (monochrome) {
        if (dark) MonochromeDarkColors else MonochromeLightColors
    } else if (Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (dark) darkColorScheme() else lightColorScheme()
    val view = LocalView.current
    SideEffect {
        var owner = context
        while (owner is ContextWrapper && owner !is Activity) owner = owner.baseContext
        (owner as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(12.dp), small = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(24.dp), large = RoundedCornerShape(32.dp),
            extraLarge = RoundedCornerShape(40.dp)
        ),
        typography = Typography(
            displaySmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 46.sp),
            headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp)
        ),
        content = { CompositionLocalProvider(LocalMonochromeTheme provides monochrome, content = content) }
    )
}
