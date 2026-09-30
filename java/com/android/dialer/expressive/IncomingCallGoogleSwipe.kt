/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.android.dialer.expressive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.android.dialer.R
import kotlin.math.abs

private val GoogleSwipeThreshold = 88.dp
private val GoogleSwipeRed = Color(0xFFE53935)
private val GoogleSwipeGreen = Color(0xFF16A05D)
private val LocalGoogleSwipe = staticCompositionLocalOf<SpringSwipeState> {
    error("IncomingCallGoogleHandle must be inside IncomingCallGoogleSwipe")
}

@Composable
internal fun IncomingCallGoogleSwipe(
    enabled: Boolean,
    onAnswer: () -> Unit,
    onReject: () -> Unit,
    content: @Composable () -> Unit
) {
    val swipe = rememberSpringSwipeState()
    val threshold = with(LocalDensity.current) { GoogleSwipeThreshold.toPx() }
    val haptic = LocalHapticFeedback.current
    val answerLabel = stringResource(R.string.expressive_answer)
    val rejectLabel = stringResource(R.string.expressive_decline)
    val gestureLabel = stringResource(R.string.expressive_google_swipe_accessibility)

    LaunchedEffect(enabled) { if (!enabled) swipe.settle() }
    Box(Modifier.fillMaxSize().semantics {
        contentDescription = gestureLabel
        role = Role.Button
        if (enabled) {
            onClick(answerLabel) { onAnswer(); true }
            customActions = listOf(CustomAccessibilityAction(rejectLabel) { onReject(); true })
        } else disabled()
    }.focusable(enabled).springSwipe(
        swipe, Orientation.Horizontal, threshold, enabled = enabled
    ) { action, velocity ->
        if (action != 0) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        swipe.settle(velocity = velocity)
        when (action) {
            -1 -> onReject()
            1 -> onAnswer()
        }
    }) {
        CompositionLocalProvider(LocalGoogleSwipe provides swipe, content = content)
    }
}

@Composable
internal fun IncomingCallGoogleHandle(enabled: Boolean) {
    val swipe = LocalGoogleSwipe.current
    val threshold = with(LocalDensity.current) { GoogleSwipeThreshold.toPx() }
    val progress = (abs(swipe.offset) / threshold).coerceIn(0f, 1f)
    val draggingLeft = swipe.offset < 0f
    val draggingRight = swipe.offset > 0f
    val feedbackVisible = enabled && (swipe.dragging || progress > .001f)
    val accent = when {
        draggingLeft -> GoogleSwipeRed
        draggingRight -> GoogleSwipeGreen
        else -> GoogleSwipeGreen
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val trackBrush = when {
        draggingLeft -> Brush.horizontalGradient(
            listOf(GoogleSwipeRed.copy(alpha = .72f * progress), track, track))
        draggingRight -> Brush.horizontalGradient(
            listOf(track, track, GoogleSwipeGreen.copy(alpha = .72f * progress)))
        else -> Brush.horizontalGradient(listOf(track, track))
    }
    val icon = if (feedbackVisible && draggingLeft) DialerSymbol.EndCall else DialerSymbol.Phone
    val leftLabel = stringResource(R.string.expressive_decline)
    val rightLabel = stringResource(R.string.expressive_answer)

    Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)
        .height(82.dp).graphicsLayer { alpha = if (enabled) 1f else .5f },
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(trackBrush, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f))
            if (feedbackVisible) {
                drawRoundRect(
                    Brush.horizontalGradient(listOf(accent.copy(alpha = .08f), accent.copy(alpha = .38f))),
                    topLeft = if (draggingLeft) androidx.compose.ui.geometry.Offset.Zero
                    else androidx.compose.ui.geometry.Offset(size.width / 2f, 0f),
                    size = androidx.compose.ui.geometry.Size(size.width / 2f, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
                )
            }
            drawRoundRect(
                Color.White.copy(alpha = .22f),
                style = Stroke(width = 1.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
            )
        }
        Row(Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(leftLabel, color = MaterialTheme.colorScheme.onSurface)
            Text(rightLabel, color = MaterialTheme.colorScheme.onSurface)
        }
        Surface(Modifier.size(64.dp).graphicsLayer { translationX = swipe.offset },
            shape = CircleShape, color = Color.White, contentColor = accent,
            shadowElevation = 2.dp) {
            Box(contentAlignment = Alignment.Center) {
                Symbol(icon, description = if (draggingLeft) leftLabel else rightLabel,
                    modifier = Modifier.size(30.dp))
            }
        }
    }
}
