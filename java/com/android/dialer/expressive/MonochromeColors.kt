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

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val MonochromeLightColors = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E2E2),
    onPrimaryContainer = Color.Black,
    inversePrimary = Color(0xFFE2E2E2),
    secondary = Color(0xFF333333),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAEAEA),
    onSecondaryContainer = Color(0xFF171717),
    tertiary = Color(0xFF444444),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0F0F0),
    onTertiaryContainer = Color(0xFF171717),
    background = Color.White,
    onBackground = Color(0xFF171717),
    surface = Color.White,
    onSurface = Color(0xFF171717),
    surfaceVariant = Color(0xFFE2E2E2),
    onSurfaceVariant = Color(0xFF555555),
    surfaceTint = Color.Black,
    inverseSurface = Color(0xFF222222),
    inverseOnSurface = Color.White,
    error = Color.Black,
    onError = Color.White,
    errorContainer = Color(0xFFE2E2E2),
    onErrorContainer = Color.Black,
    outline = Color(0xFF767676),
    outlineVariant = Color(0xFFCCCCCC),
    scrim = Color.Black,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFDDDDDD),
    surfaceContainer = Color(0xFFF2F2F2),
    surfaceContainerHigh = Color(0xFFEBEBEB),
    surfaceContainerHighest = Color(0xFFE2E2E2),
    surfaceContainerLow = Color(0xFFF8F8F8),
    surfaceContainerLowest = Color.White,
    primaryFixed = Color(0xFFE2E2E2),
    primaryFixedDim = Color(0xFFC6C6C6),
    onPrimaryFixed = Color.Black,
    onPrimaryFixedVariant = Color(0xFF333333),
    secondaryFixed = Color(0xFFE2E2E2),
    secondaryFixedDim = Color(0xFFC6C6C6),
    onSecondaryFixed = Color.Black,
    onSecondaryFixedVariant = Color(0xFF333333),
    tertiaryFixed = Color(0xFFE2E2E2),
    tertiaryFixedDim = Color(0xFFC6C6C6),
    onTertiaryFixed = Color.Black,
    onTertiaryFixedVariant = Color(0xFF333333)
)

internal val MonochromeDarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color.White,
    inversePrimary = Color.Black,
    secondary = Color(0xFFE2E2E2),
    onSecondary = Color(0xFF171717),
    secondaryContainer = Color(0xFF292929),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFC6C6C6),
    onTertiary = Color(0xFF171717),
    tertiaryContainer = Color(0xFF383838),
    onTertiaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color(0xFFF2F2F2),
    surface = Color.Black,
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFBDBDBD),
    surfaceTint = Color.White,
    inverseSurface = Color(0xFFE2E2E2),
    inverseOnSurface = Color(0xFF171717),
    error = Color.White,
    onError = Color.Black,
    errorContainer = Color(0xFF333333),
    onErrorContainer = Color.White,
    outline = Color(0xFF8E8E8E),
    outlineVariant = Color(0xFF424242),
    scrim = Color.Black,
    surfaceBright = Color(0xFF333333),
    surfaceDim = Color.Black,
    surfaceContainer = Color(0xFF171717),
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerHighest = Color(0xFF2E2E2E),
    surfaceContainerLow = Color(0xFF0F0F0F),
    surfaceContainerLowest = Color.Black,
    primaryFixed = Color(0xFFE2E2E2),
    primaryFixedDim = Color(0xFFC6C6C6),
    onPrimaryFixed = Color.Black,
    onPrimaryFixedVariant = Color(0xFF333333),
    secondaryFixed = Color(0xFFE2E2E2),
    secondaryFixedDim = Color(0xFFC6C6C6),
    onSecondaryFixed = Color.Black,
    onSecondaryFixedVariant = Color(0xFF333333),
    tertiaryFixed = Color(0xFFE2E2E2),
    tertiaryFixedDim = Color(0xFFC6C6C6),
    onTertiaryFixed = Color.Black,
    onTertiaryFixedVariant = Color(0xFF333333)
)
