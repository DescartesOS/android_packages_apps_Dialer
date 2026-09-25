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

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

internal fun sanitizeDialpadNumber(number: String): String =
    number.filter { it.isDigit() || it in "+*#,;" }.take(128)

internal fun normalizeDialpadInput(value: TextFieldValue): TextFieldValue {
    val text = sanitizeDialpadNumber(value.text)
    if (text == value.text) return value
    return TextFieldValue(text, TextRange(
        sanitizeDialpadNumber(value.text.take(value.selection.start)).length,
        sanitizeDialpadNumber(value.text.take(value.selection.end)).length
    ))
}

internal fun replaceDialpadSelection(value: TextFieldValue, replacement: String): TextFieldValue {
    val start = value.selection.min
    return TextFieldValue(
        value.text.replaceRange(start, value.selection.max, replacement),
        TextRange(start + replacement.length)
    )
}

internal fun deleteDialpadSelection(value: TextFieldValue): TextFieldValue = when {
    !value.selection.collapsed -> replaceDialpadSelection(value, "")
    value.selection.start == 0 -> value
    else -> replaceDialpadSelection(value.copy(selection = TextRange(
        value.selection.start - 1, value.selection.start)), "")
}
