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

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.dialer.R
import com.android.incallui.DialpadFragment

object ComposeInCallDialpad {
    @JvmStatic fun create(fragment: DialpadFragment, field: EditText): ComposeView = ComposeView(fragment.requireContext()).apply {
        id = R.id.expressive_dtmf
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            var digits by remember { mutableStateOf(field.text.toString()) }
            DisposableEffect(field) {
                val watcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { digits = s.toString() }
                    override fun afterTextChanged(s: Editable?) {}
                }
                field.addTextChangedListener(watcher)
                onDispose { field.removeTextChangedListener(watcher); fragment.onExpressiveDigit('0', false) }
            }
            DialerExpressiveTheme {
                Surface(shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .65f)
                        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(digits, Modifier.weight(1f).padding(horizontal = 12.dp),
                                    style = MaterialTheme.typography.headlineMedium, maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                FilledTonalIconButton(onClick = { fragment.requireActivity().onBackPressed() }) {
                                    Symbol(DialerSymbol.Close, stringResource(R.string.expressive_close_keypad))
                                }
                            }
                        }
                        listOf("123", "456", "789", "*0#").forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { digit ->
                                    DtmfKey(fragment, digit, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DtmfKey(fragment: DialpadFragment, digit: Char, modifier: Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .92f else 1f, spring(.65f, 500f), label = "dtmf press")
    val radius by animateDpAsState(if (pressed) 16.dp else 32.dp, spring(.7f, 500f), label = "dtmf shape")
    val background by animateColorAsState(if (pressed) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh, tween(100), label = "dtmf color")
    val foreground by animateColorAsState(if (pressed) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface, tween(100), label = "dtmf content")
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    Surface(modifier.padding(vertical = 4.dp).heightIn(min = 60.dp)
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .semantics {
            role = Role.Button
            contentDescription = digit.toString()
            onClick {
                scope.launch {
                    pressed = true
                    fragment.onExpressiveDigit(digit, true)
                    try { delay(120) } finally { pressed = false; fragment.onExpressiveDigit(digit, false) }
                }
                true
            }
        }.onKeyEvent { event ->
            if (event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.DirectionCenter) {
                if (event.type == KeyEventType.KeyDown && !pressed) {
                    pressed = true; fragment.onExpressiveDigit(digit, true)
                } else if (event.type == KeyEventType.KeyUp) {
                    pressed = false; fragment.onExpressiveDigit(digit, false)
                }
                true
            } else false
        }.focusable().pointerInput(fragment, digit) {
            detectTapGestures(onPress = {
                pressed = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                fragment.onExpressiveDigit(digit, true)
                try { tryAwaitRelease() } finally {
                    pressed = false; fragment.onExpressiveDigit(digit, false)
                }
            })
        }, shape = RoundedCornerShape(radius), color = background, contentColor = foreground) {
        Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Text(digit.toString(), style = MaterialTheme.typography.headlineMedium)
        }
    }
}
