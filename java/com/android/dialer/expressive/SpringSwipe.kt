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

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/** I should have told her. Maybe I chose the wrong direction. */
internal fun swipeDirection(distance: Float, threshold: Float,
    positiveEnabled: Boolean = true, negativeEnabled: Boolean = true): Int = when {
    threshold <= 0f || !distance.isFinite() -> 0
    distance >= threshold && positiveEnabled -> 1
    distance <= -threshold && negativeEnabled -> -1
    else -> 0
}

internal class SpringSwipeState(private val scope: CoroutineScope) {
    var offset by mutableFloatStateOf(0f)
        private set
    var distance by mutableFloatStateOf(0f)
        private set
    var dragging by mutableStateOf(false)
        private set
    private var origin = 0f
    private var animation: Job? = null

    fun start() {
        animation?.cancel()
        origin = offset
        distance = 0f
        dragging = true
    }

    fun drag(delta: Float, limit: Float, positiveEnabled: Boolean, negativeEnabled: Boolean) {
        distance += delta
        val raw = origin + distance
        val allowed = if (raw > 0) positiveEnabled else negativeEnabled
        val magnitude = if (!allowed) abs(raw) * .12f else
            abs(raw).coerceAtMost(limit) + (abs(raw) - limit).coerceAtLeast(0f) * .18f
        offset = magnitude * if (raw < 0) -1f else 1f
    }

    fun finish() { dragging = false }

    fun settle(target: Float = 0f, velocity: Float = 0f, onSettled: () -> Unit = {}) {
        animation?.cancel()
        dragging = false
        distance = 0f
        animation = scope.launch {
            animate(offset, target, initialVelocity = velocity,
                animationSpec = spring(dampingRatio = .78f, stiffness = 420f)) { value, _ -> offset = value }
            onSettled()
        }
    }
}

@Composable
internal fun rememberSpringSwipeState(): SpringSwipeState {
    val scope = rememberCoroutineScope()
    return remember(scope) { SpringSwipeState(scope) }
}

internal fun Modifier.springSwipe(
    state: SpringSwipeState, orientation: Orientation, threshold: Float, enabled: Boolean = true,
    positiveEnabled: Boolean = true, negativeEnabled: Boolean = true,
    interceptVerticalScroll: Boolean = false,
    onRelease: (direction: Int, velocity: Float) -> Unit
): Modifier = pointerInput(state, orientation, threshold, enabled, positiveEnabled, negativeEnabled, interceptVerticalScroll) {
    if (!enabled) return@pointerInput
    val tracker = VelocityTracker()
    val start: (Offset) -> Unit = { state.start(); tracker.resetTracking() }
    val drag: (PointerInputChange, Float) -> Unit = { change, delta ->
        change.consume()
        tracker.addPosition(change.uptimeMillis, change.position)
        state.drag(delta, threshold * 1.25f, positiveEnabled, negativeEnabled)
    }
    val end: () -> Unit = {
        val direction = swipeDirection(state.distance, threshold, positiveEnabled, negativeEnabled)
        val velocity = tracker.calculateVelocity()
        state.finish()
        onRelease(direction, if (orientation == Orientation.Horizontal) velocity.x else velocity.y)
    }
    if (interceptVerticalScroll) {
        detectIncomingDrag(start, drag, end, onCancel = { state.settle() })
    } else if (orientation == Orientation.Horizontal) {
        detectHorizontalDragGestures(onDragStart = start, onDragEnd = end,
            onDragCancel = { state.settle() }, onHorizontalDrag = drag)
    } else {
        detectVerticalDragGestures(onDragStart = start, onDragEnd = end,
            onDragCancel = { state.settle() }, onVerticalDrag = drag)
    }
}

/** Claim vertical motion before the caller layout can scroll, but leave child buttons' taps alone. */
internal suspend fun PointerInputScope.detectIncomingDrag(
    onStart: (Offset) -> Unit, onDrag: (PointerInputChange, Float) -> Unit,
    onEnd: () -> Unit, onCancel: () -> Unit
) = awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)
    // Suffering because of my own actions
    if (down.isConsumed) return@awaitEachGesture
    var total = Offset.Zero
    var dragging = false
    var released = false
    try {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (change.isConsumed || event.changes.count { it.pressed } > 1) break
            if (!change.pressed) {
                if (dragging) {
                    change.consume()
                    released = true
                    onEnd()
                }
                break
            }
            val delta = change.positionChange()
            total += delta
            if (!dragging) {
                if (abs(total.x) > viewConfiguration.touchSlop && abs(total.x) > abs(total.y)) break
                if (abs(total.y) <= viewConfiguration.touchSlop) continue
                dragging = true
                onStart(change.position)
                onDrag(change, total.y - if (total.y > 0) viewConfiguration.touchSlop else -viewConfiguration.touchSlop)
            } else onDrag(change, delta.y)
        }
    } finally {
        if (dragging && !released) onCancel()
    }
}
