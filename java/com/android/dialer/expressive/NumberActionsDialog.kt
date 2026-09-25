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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.dialer.R

@Composable
internal fun NumberActionsDialog(
    name: String, number: String, starred: Boolean, saved: Boolean,
    onDismiss: () -> Unit, onCall: () -> Unit, onVideo: () -> Unit, onMessage: () -> Unit,
    onFavorite: () -> Unit, onCopy: () -> Unit, onShare: () -> Unit, onEdit: () -> Unit,
    onPhoto: () -> Unit, onRemovePhoto: () -> Unit, onDelete: (() -> Unit)? = null
) {
    val photoState = rememberCallerPhotoState(number, 512)
    // I still keep here photo in my wallet
    val photo = photoState.image
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(36.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(Modifier.widthIn(max = 400.dp).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (photo != null) Image(photo, null, Modifier.size(96.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    else Surface(Modifier.size(96.dp), shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(name.trim().firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.displaySmall)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(name, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (name != number) Text(number, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberAction(DialerSymbol.Phone, stringResource(R.string.expressive_call), Modifier.weight(1f), onCall)
                    NumberAction(DialerSymbol.Message, stringResource(R.string.expressive_message), Modifier.weight(1f), onMessage)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberAction(DialerSymbol.Video, stringResource(R.string.expressive_video_call), Modifier.weight(1f), onVideo)
                    NumberAction(if (saved) DialerSymbol.Star else DialerSymbol.Add,
                        stringResource(if (!saved) R.string.expressive_save else if (starred) R.string.expressive_unfavorite else R.string.expressive_favorite),
                        Modifier.weight(1f), onFavorite)
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilledIconButton(onClick = onCopy, modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                        Symbol(DialerSymbol.Copy, stringResource(R.string.expressive_copy_number))
                    }
                    FilledIconButton(onClick = onShare, modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface)) {
                        Symbol(DialerSymbol.Share, stringResource(R.string.expressive_share_number))
                    }
                    OutlinedIconButton(onClick = onEdit, modifier = Modifier.size(56.dp)) {
                        Symbol(DialerSymbol.Edit, stringResource(if (saved) R.string.expressive_edit_contact else R.string.expressive_create_contact))
                    }
                }
                NumberAction(DialerSymbol.Photo, stringResource(if (photo == null) R.string.expressive_set_caller_photo
                    else R.string.expressive_change_caller_photo), Modifier.fillMaxWidth(), onPhoto)
                if (photoState.hasOverride) TextButton(onClick = onRemovePhoto, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.expressive_remove_caller_photo))
                }
                if (onDelete != null) TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.expressive_delete_history), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun NumberAction(icon: DialerSymbol, label: String, modifier: Modifier, onClick: () -> Unit) {
    ExpressiveTile(onClick, modifier, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
        Symbol(icon, modifier = Modifier.size(20.dp))
        Text(label, Modifier.padding(start = 10.dp), style = MaterialTheme.typography.labelLarge)
    }
}
