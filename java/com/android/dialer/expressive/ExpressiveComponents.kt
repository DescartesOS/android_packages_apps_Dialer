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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/** Thanks chatgpt, even tho all icons looks bullshit */
enum class DialerSymbol(val path: String) {
    Expand("M6,8 L12,14 L18,8 L19.4,9.4 L12,16.8 L4.6,9.4 Z"),
    Check("M9,16.17 L4.83,12 L3.41,13.41 L9,19 L21,7 L19.59,5.59 Z"),
    Sim("M6,2 L14,2 L20,8 L20,22 L4,22 L4,4 Z M7,10 L7,19 L17,19 L17,10 Z M9,12 L11,12 L11,14 L9,14 Z M13,12 L15,12 L15,14 L13,14 Z M9,16 L15,16 L15,17 L9,17 Z"),
    Bell("M12,2 A2,2 0,0 0,10 4 L10,4.4 C6,5.4 6,8 6,11 L6,16 L3,19 L21,19 L18,16 L18,11 C18,8 18,5.4 14,4.4 L14,4 A2,2 0,0 0,12 2 M9,21 A3,3 0,0 0,15 21 Z"),
    Vibrate("M8,2 L16,2 L16,22 L8,22 Z M10,5 L10,19 L14,19 L14,5 Z M4,6 L6,6 L6,18 L4,18 Z M0,9 L2,9 L2,15 L0,15 Z M18,6 L20,6 L20,18 L18,18 Z M22,9 L24,9 L24,15 L22,15 Z"),
    Accessibility("M12,2 A2,2 0,1 0,12 6 A2,2 0,0 0,12 2 M3,7 L21,7 L21,9 L15,10 L15,14 L18,22 L15,22 L12,16 L9,22 L6,22 L9,14 L9,10 L3,9 Z"),
    Lock("M7,10 L7,7 A5,5 0,0 1,17 7 L17,10 L20,10 L20,22 L4,22 L4,10 Z M9,10 L15,10 L15,7 A3,3 0,0 0,9 7 Z M11,14 L11,18 L13,18 L13,14 Z"),
    Info("M12,2 A10,10 0,1 0,12 22 A10,10 0,0 0,12 2 M11,6 L13,6 L13,8 L11,8 Z M11,10 L13,10 L13,18 L11,18 Z"),
    Backspace("M22,3 L7,3 L0,12 L7,21 L22,21 Z M10.4,7 L9,8.4 L12.6,12 L9,15.6 L10.4,17 L14,13.4 L17.6,17 L19,15.6 L15.4,12 L19,8.4 L17.6,7 L14,10.6 Z"),
    Copy("M8,2 L20,2 L20,18 L8,18 Z M4,6 L6,6 L6,20 L16,20 L16,22 L4,22 Z"),
    Share("M18,2 A3,3 0,1 0,18 8 A3,3 0,0 0,18 2 M6,9 A3,3 0,1 0,6 15 A3,3 0,0 0,6 9 M18,16 A3,3 0,1 0,18 22 A3,3 0,0 0,18 16 M7,10 L16,5 L17,7 L8,12 Z M7,12 L17,17 L16,19 L6,14 Z"),
    Edit("M3,17 L3,21 L7,21 L19,9 L15,5 Z M17,3 L19,1 L23,5 L21,7 Z"),
    Photo("M3,5 L7,5 L9,2 L15,2 L17,5 L21,5 L21,21 L3,21 Z M12,8 A5,5 0,1 0,12 18 A5,5 0,0 0,12 8 M12,10 A3,3 0,1 1,12 16 A3,3 0,0 1,12 10"),
    Work("M8,3 L16,3 L16,6 L22,6 L22,21 L2,21 L2,6 L8,6 Z M10,5 L10,6 L14,6 L14,5 Z M4,10 L4,12 L20,12 L20,10 Z"),
    Headset("M12,2 C5,2 2,7 2,12 L2,21 L8,21 L8,12 L4,12 C4,2 20,2 20,12 L16,12 L16,21 L22,21 L22,12 C22,7 19,2 12,2 Z"),
    Bluetooth("M11,2 L18,8 L14,12 L18,16 L11,22 L11,14 L6,19 L4.6,17.6 L10.2,12 L4.6,6.4 L6,5 L11,10 Z M13,6 L13,10 L15,8 Z M13,14 L13,18 L15,16 Z"),
    EndCall("M12,7 C7,7 3,9 1,12 L4,16 L8,14 L8,11 C10,10 14,10 16,11 L16,14 L20,16 L23,12 C21,9 17,7 12,7 Z"),
    Phone("M6.62,10.79 C8.06,13.62 10.38,15.94 13.21,17.38 L15.41,15.18 C15.69,14.9 16.08,14.82 16.43,14.93 C17.55,15.3 18.75,15.5 20,15.5 C20.55,15.5 21,15.95 21,16.5 L21,20 C21,20.55 20.55,21 20,21 C10.61,21 3,13.39 3,4 C3,3.45 3.45,3 4,3 L7.5,3 C8.05,3 8.5,3.45 8.5,4 C8.5,5.25 8.7,6.45 9.07,7.57 C9.18,7.92 9.1,8.31 8.82,8.59 Z"),
    History("M12,2 A10,10 0,1 0,22 12 A10,10 0,0 0,12 2 M12,4 A8,8 0,1 1,4 12 A8,8 0,0 1,12 4 M11,6 L13,6 L13,11.5 L17,14 L16,15.7 L11,12.5 Z"),
    People("M9,3 A4,4 0,1 0,9 11 A4,4 0,0 0,9 3 M16,4 A3,3 0,0 1,16 10 L16,8 A1,1 0,0 0,16 6 Z M9,13 C4,13 2,15 2,18 L2,21 L16,21 L16,18 C16,15 14,13 9,13 M18,13 C21,14 22,16 22,18 L22,21 L18,21 Z"),
    Star("M12,2 L15.1,8.3 L22,9.3 L17,14.2 L18.2,21 L12,17.8 L5.8,21 L7,14.2 L2,9.3 L8.9,8.3 Z"),
    Search("M10,2 A8,8 0,1 0,15 16 L21,22 L22,21 L16,15 A8,8 0,0 0,10 2 M10,4 A6,6 0,1 1,10 16 A6,6 0,0 1,10 4"),
    Back("M20,11 L7.8,11 L13.4,5.4 L12,4 L4,12 L12,20 L13.4,18.6 L7.8,13 L20,13 Z"),
    Close("M6,4.6 L12,10.6 L18,4.6 L19.4,6 L13.4,12 L19.4,18 L18,19.4 L12,13.4 L6,19.4 L4.6,18 L10.6,12 L4.6,6 Z"),
    More("M12,3 A2,2 0,1 0,12 7 A2,2 0,0 0,12 3 M12,10 A2,2 0,1 0,12 14 A2,2 0,0 0,12 10 M12,17 A2,2 0,1 0,12 21 A2,2 0,0 0,12 17"),
    Keypad("M5,3 L9,3 L9,7 L5,7 Z M11,3 L15,3 L15,7 L11,7 Z M17,3 L21,3 L21,7 L17,7 Z M5,9 L9,9 L9,13 L5,13 Z M11,9 L15,9 L15,13 L11,13 Z M17,9 L21,9 L21,13 L17,13 Z M5,15 L9,15 L9,19 L5,19 Z M11,15 L15,15 L15,19 L11,19 Z M17,15 L21,15 L21,19 L17,19 Z"),
    Voicemail("M6,5 A6,6 0,1 0,6 17 L18,17 A6,6 0,1 0,12 11 A6,6 0,0 0,13.5 15 L10.5,15 A6,6 0,0 0,6 5 M6,7 A4,4 0,1 1,6 15 A4,4 0,0 1,6 7 M18,7 A4,4 0,1 1,18 15 A4,4 0,0 1,18 7"),
    Delete("M7,4 L9,4 L10,2 L14,2 L15,4 L19,4 L19,6 L5,6 L5,4 Z M6,8 L18,8 L17,22 L7,22 Z"),
    Message("M3,3 L21,3 L21,18 L7,18 L3,22 Z M6,7 L18,7 L18,9 L6,9 Z M6,11 L15,11 L15,13 L6,13 Z"),
    Add("M11,4 L13,4 L13,11 L20,11 L20,13 L13,13 L13,20 L11,20 L11,13 L4,13 L4,11 L11,11 Z"),
    Mic("M12,2 C10,2 9,3 9,5 L9,11 C9,15 15,15 15,11 L15,5 C15,3 14,2 12,2 M5,10 L7,10 L7,11 C7,18 17,18 17,11 L17,10 L19,10 L19,11 C19,15 16,18 13,18 L13,21 L16,21 L16,23 L8,23 L8,21 L11,21 L11,18 C8,18 5,15 5,11 Z"),
    Speaker("M3,9 L7,9 L12,4 L12,20 L7,15 L3,15 Z M14,8 C18,9 18,15 14,16 L14,14 C16,13 16,11 14,10 Z M16,4 C24,7 24,17 16,20 L16,18 C21,15 21,9 16,6 Z"),
    Pause("M6,4 L10,4 L10,20 L6,20 Z M14,4 L18,4 L18,20 L14,20 Z"),
    Stop("M6,6 L18,6 L18,18 L6,18 Z"),
    Play("M7,3 L21,12 L7,21 Z"),
    Swap("M4,7 L16,7 L13,4 L14.4,2.6 L20.8,9 L4,9 Z M20,15 L8,15 L11,18 L9.6,19.4 L3.2,13 L20,13 Z"),
    Video("M3,5 L16,5 L16,9 L22,5 L22,19 L16,15 L16,19 L3,19 Z"),
    Record("M12,3 A9,9 0,1 0,12 21 A9,9 0,0 0,12 3"),
    Block("M12,2 A10,10 0,1 0,12 22 A10,10 0,0 0,12 2 M12,4 A8,8 0,0 1,18.3 7.1 L7.1,18.3 A8,8 0,0 1,12 4 M16.9,5.7 L5.7,16.9 A8,8 0,0 0,16.9 5.7"),
    Settings("M10,2 L14,2 L15,5 L18,6 L21,5 L23,9 L20,12 L21,15 L23,16 L21,20 L18,19 L15,20 L14,23 L10,23 L9,20 L6,19 L3,20 L1,16 L4,13 L3,10 L1,9 L3,5 L6,6 L9,5 Z M12,8 A4,4 0,1 0,12 16 A4,4 0,0 0,12 8")
}

@Composable
fun Symbol(symbol: DialerSymbol, description: String? = null, modifier: Modifier = Modifier.size(24.dp)) {
    val vector = remember(symbol) {
        ImageVector.Builder(symbol.name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(PathParser().parsePathString(symbol.path).toNodes(),
                fill = androidx.compose.ui.graphics.SolidColor(Color.Black),
                pathFillType = androidx.compose.ui.graphics.PathFillType.EvenOdd)
        }.build()
    }
    Icon(vector, description, modifier)
}

@Composable
fun ExpressiveTile(
    onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false,
    enabled: Boolean = true, onLongClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    highlightOnPress: Boolean = false, onLongClickLabel: String? = null,
    color: Color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .94f else 1f, spring(.6f, 500f), label = "press")
    val radius by animateDpAsState(if (pressed) 16.dp else if (selected) 22.dp else 32.dp, spring(.65f, 500f), label = "shape")
    val background by animateColorAsState(if (pressed && highlightOnPress) MaterialTheme.colorScheme.primary else color,
        tween(100), label = "tile color")
    val foreground by animateColorAsState(if (pressed && highlightOnPress) MaterialTheme.colorScheme.onPrimary else contentColor,
        tween(100), label = "tile content")
    val haptic = LocalHapticFeedback.current
    Surface(modifier.graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (enabled) 1f else .38f },
        shape = RoundedCornerShape(radius), color = background, contentColor = foreground) {
        Row(Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 56.dp)
            .combinedClickable(enabled = enabled, interactionSource = interaction,
                indication = LocalIndication.current, role = Role.Button,
                onLongClick = onLongClick, onLongClickLabel = onLongClickLabel, onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick()
                }).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
            content = content)
    }
}

@Composable
fun FloatingDialerBar(selected: Int, labels: List<String>, onSelect: (Int) -> Unit) {
    val icons = listOf(DialerSymbol.History, DialerSymbol.Star, DialerSymbol.People, DialerSymbol.Voicemail)
    val haptic = LocalHapticFeedback.current
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp, tonalElevation = 2.dp, modifier = Modifier.widthIn(max = 440.dp)) {
        Row(Modifier.padding(6.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically) {
            labels.forEachIndexed { index, label ->
                val active = selected == index
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(if (pressed) .88f else 1f, spring(.65f, 500f), label = "nav press")
                val radius by animateDpAsState(if (pressed) 16.dp else 28.dp, spring(.7f, 500f), label = "nav shape")
                val weight by animateFloatAsState(if (active) 2.2f else 1f, spring(.8f, 500f), label = "nav width")
                val color by animateColorAsState(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, label = "nav color")
                Row(Modifier.weight(weight).heightIn(min = 52.dp).graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(radius)).background(color)
                    .selectable(active, interactionSource = interaction, indication = LocalIndication.current,
                        role = Role.Tab, onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(index)
                        }).semantics { contentDescription = label }.padding(horizontal = 8.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CompositionLocalProvider(LocalContentColor provides if (active) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant) {
                        Symbol(icons[index], modifier = Modifier.size(22.dp))
                        AnimatedVisibility(active, enter = fadeIn() + expandHorizontally(spring(.8f, 500f)),
                            exit = fadeOut() + shrinkHorizontally()) {
                            Text(label, Modifier.padding(start = 6.dp).clearAndSetSemantics {}, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLoading(label: String) {
    Column(Modifier.fillMaxWidth().padding(48.dp).semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f)) {
            LoadingIndicator(Modifier.padding(12.dp).size(64.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ContactAvatar(name: String, modifier: Modifier = Modifier, large: Boolean = false, number: String = "") {
    val photo = rememberCallerPhoto(number, if (large) 256 else 128)
    Surface(modifier.size(if (large) 112.dp else 48.dp),
        shape = RoundedCornerShape(if (large) 40.dp else 18.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer) {
        Box(contentAlignment = Alignment.Center) {
            if (photo != null) Image(photo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                colorFilter = monochromeImageFilter())
            else Text(name.trim().firstOrNull()?.uppercase() ?: "?",
                style = if (large) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleLarge)
        }
    }
}
