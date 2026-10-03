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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    Column(
        modifier.alpha(if (enabled) 1f else .38f)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(68.dp)
                .clip(CircleShape)
                .semantics {
                    this.selected = selected
                    contentDescription = label
                }
                .clickable(enabled = enabled, role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            shape = CircleShape,
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
