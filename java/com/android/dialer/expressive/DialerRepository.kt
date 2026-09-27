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

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CallLog.Calls
import android.provider.BlockedNumberContract
import android.provider.BlockedNumberContract.BlockedNumbers
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Person(val id: Long, val lookupKey: String, val name: String, val number: String, val starred: Boolean) {
    val uri: Uri get() = ContactsContract.Contacts.getLookupUri(id, lookupKey)
}
data class DialerData(val people: List<Person> = emptyList(), val calls: List<RecentCall> = emptyList(),
    val contactsAllowed: Boolean = false, val callsAllowed: Boolean = false,
    val recentGroups: List<RecentCallGroup> = groupConsecutiveCalls(calls))
internal data class NumberBlockState(val blocked: Boolean, val available: Boolean)

class DialerRepository(private val context: Context) {
    fun has(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    suspend fun load(): DialerData = withContext(Dispatchers.IO) {
        val people = mutableListOf<Person>()
        val calls = mutableListOf<RecentCall>()
        val contactsAllowed = has(Manifest.permission.READ_CONTACTS)
        val callsAllowed = has(Manifest.permission.READ_CALL_LOG)
        val telephony = context.getSystemService(TelephonyManager::class.java)
        val country = (telephony?.networkCountryIso?.takeIf { it.isNotBlank() }
            ?: telephony?.simCountryIso?.takeIf { it.isNotBlank() }
            ?: Locale.getDefault().country).uppercase(Locale.ROOT)
        val numberKeys = mutableMapOf<String, String>()
        if (contactsAllowed) context.contentResolver.query(Phone.CONTENT_URI,
            arrayOf(Phone.CONTACT_ID, Phone.LOOKUP_KEY, Phone.DISPLAY_NAME_PRIMARY, Phone.NUMBER, Phone.STARRED),
            null, null, "${Phone.SORT_KEY_PRIMARY} ASC")?.use { cursor ->
            while (cursor.moveToNext()) {
                val number = cursor.getString(3).orEmpty()
                people += Person(cursor.getLong(0), cursor.getString(1).orEmpty(),
                    cursor.getString(2).orEmpty().ifBlank { number }, number, cursor.getInt(4) != 0)
            }
        }
        if (callsAllowed) context.contentResolver.query(Calls.CONTENT_URI,
            arrayOf(Calls._ID, Calls.CACHED_NAME, Calls.NUMBER, Calls.DATE, Calls.DURATION, Calls.TYPE, Calls.NUMBER_PRESENTATION),
            "${Calls.TYPE} != ?", arrayOf(Calls.VOICEMAIL_TYPE.toString()), "${Calls.DATE} DESC, ${Calls._ID} DESC")?.use { cursor ->
            while (cursor.moveToNext()) {
                val number = cursor.getString(2).orEmpty()
                calls += RecentCall(cursor.getLong(0), cursor.getString(1).orEmpty(), number,
                    cursor.getLong(3), cursor.getLong(4), cursor.getInt(5), cursor.getInt(6),
                    numberKeys.getOrPut(number) { recentNumberKey(number, country) })
            }
        }
        DialerData(people.distinctBy { it.id to it.number }, calls, contactsAllowed, callsAllowed)
    }
    suspend fun setStarred(person: Person) = withContext(Dispatchers.IO) {
        context.contentResolver.update(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, person.id),
            ContentValues().apply { put(ContactsContract.Contacts.STARRED, if (person.starred) 0 else 1) }, null, null)
    }
    suspend fun deleteCalls(calls: List<RecentCall>) = withContext(Dispatchers.IO) {
        calls.map { it.id }.distinct().chunked(200).forEach { ids ->
            context.contentResolver.delete(Calls.CONTENT_URI,
                "${Calls._ID} IN (${ids.joinToString(",") { "?" }})", ids.map { it.toString() }.toTypedArray())
        }
    }
    internal suspend fun blockState(number: String): NumberBlockState = withContext(Dispatchers.IO) {
        val available = number.isNotBlank() && BlockedNumberContract.canCurrentUserBlockNumbers(context)
        NumberBlockState(available && BlockedNumberContract.isBlocked(context, number), available)
    }
    internal suspend fun setBlocked(number: String, blocked: Boolean): NumberBlockState = withContext(Dispatchers.IO) {
        check(number.isNotBlank() && BlockedNumberContract.canCurrentUserBlockNumbers(context))
        if (blocked) {
            if (!BlockedNumberContract.isBlocked(context, number)) {
                checkNotNull(context.contentResolver.insert(BlockedNumbers.CONTENT_URI,
                    ContentValues().apply { put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number) }))
            }
        } else {
            BlockedNumberContract.unblock(context, number)
        }
        blockState(number).also { check(it.available && it.blocked == blocked) }
    }
}
