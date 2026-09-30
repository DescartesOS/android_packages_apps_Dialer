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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
private fun rememberCallerPhotoPainter(photo: ImageBitmap?, @DrawableRes fallbackPhotoResId: Int): Painter? = when {
    photo != null -> remember(photo) { BitmapPainter(photo) }
    fallbackPhotoResId != 0 -> painterResource(fallbackPhotoResId)
    else -> null
}

@Composable
internal fun CallerPortrait(photo: ImageBitmap?, modifier: Modifier = Modifier, @DrawableRes fallbackPhotoResId: Int = 0) {
    val colors = MaterialTheme.colorScheme
    val base = colors.surfaceContainerLow
    val painter = rememberCallerPhotoPainter(photo, fallbackPhotoResId)
    Box(modifier.background(base)) {
        if (painter != null) {
            Image(painter, contentDescription = null, modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
        } else {
            Canvas(Modifier.fillMaxSize()) {
                val unit = size.minDimension
                val head = Offset(size.width * .5f, size.height * .3f)
                drawRect(Brush.radialGradient(listOf(colors.surfaceContainerHighest, base),
                    center = head, radius = unit * .8f))
                drawCircle(colors.onSurface.copy(alpha = .14f), radius = unit * .16f, center = head)
                drawOval(colors.onSurface.copy(alpha = .10f),
                    Offset(head.x - unit * .42f, head.y + unit * .2f), Size(unit * .84f, unit * .72f))
            }
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to base.copy(alpha = .9f), .22f to base.copy(alpha = .7f),
            .38f to base.copy(alpha = .18f), .6f to base.copy(alpha = .86f), .78f to base, 1f to base)))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RoundedCallerAvatar(photo: ImageBitmap?, modifier: Modifier = Modifier, @DrawableRes fallbackPhotoResId: Int = 0) {
    val colors = MaterialTheme.colorScheme
    val painter = rememberCallerPhotoPainter(photo, fallbackPhotoResId)
    Surface(modifier, shape = MaterialShapes.Cookie9Sided.toShape(), color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer) {
        if (painter != null) {
            Image(painter, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(contentAlignment = Alignment.Center) {
                Symbol(DialerSymbol.Person, modifier = Modifier.fillMaxSize(.8f)
                    .graphicsLayer { alpha = .65f })
            }
        }
    }
}
