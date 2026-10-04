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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun HyperOsCallControl(
    symbol: DialerSymbol,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    endCall: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val foreground = LocalContentColor.current
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val radius by animateDpAsState(if (enabled && pressed) 20.dp else 34.dp,
        spring(.7f, 500f), label = "rounded control morph")
    val scale by animateFloatAsState(if (enabled && pressed) .92f else 1f,
        spring(.65f, 500f), label = "rounded control press")
    val shape = RoundedCornerShape(radius)
    Column(
        modifier.alpha(if (enabled) 1f else .38f)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(68.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(shape)
                .semantics {
                    this.selected = selected
                    contentDescription = label
                }
                .clickable(enabled = enabled, role = Role.Button,
                    interactionSource = interaction, indication = LocalIndication.current) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            shape = shape,
            color = when {
                endCall -> EndCallRed
                selected -> Color.White.copy(alpha = .94f)
                foreground == Color.White -> Color.Black.copy(alpha = .34f)
                else -> colors.surfaceContainerHigh.copy(alpha = .82f)
            },
            contentColor = when {
                endCall -> Color.White
                selected -> Color.Black
                else -> foreground
            }
        ) {
            Box(contentAlignment = Alignment.Center) { Symbol(symbol, modifier = Modifier.size(28.dp)) }
        }
        Text(label, style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center, color = foreground)
    }
}
