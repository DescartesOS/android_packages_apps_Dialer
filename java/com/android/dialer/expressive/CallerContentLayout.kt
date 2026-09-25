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

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun CallerContentLayout(
    viewportHeight: Dp, centerOffset: Dp, identityAtTop: Boolean, modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    identity: @Composable ColumnScope.() -> Unit,
    actions: @Composable ColumnScope.() -> Unit
) {
    Layout(modifier = modifier, content = {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { header() }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, content = identity)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, content = actions)
    }) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        val headerView = measurables[0].measure(loose)
        val identityView = measurables[1].measure(loose)
        val actionsView = measurables[2].measure(loose)
        val gap = 16.dp.roundToPx()
        val height = maxOf(viewportHeight.roundToPx(), headerView.height + identityView.height + actionsView.height + gap * 2)
        val top = headerView.height + gap
        val bottom = height - actionsView.height - gap - identityView.height
        val center = viewportHeight.roundToPx() / 2 + centerOffset.roundToPx()
        val identityY = if (identityAtTop) top else (center - identityView.height / 2).coerceIn(top, bottom)
        layout(constraints.maxWidth, height) {
            headerView.placeRelative(0, 0)
            identityView.placeRelative(0, identityY)
            actionsView.placeRelative(0, height - actionsView.height)
        }
    }
}
