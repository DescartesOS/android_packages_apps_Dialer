/*
 * Copyright (C) 2026 Lunaris AOSP
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
 * limitations under the License.
 */

package com.android.dialer.expressive

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONException

class QuickResponseStore(context: Context) {
    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun load(): List<String> {
        val encoded = try {
            preferences.getString(KEY_RESPONSES, null)
        } catch (_: ClassCastException) {
            null
        } ?: return emptyList()

        return try {
            val responses = ArrayList<String>()
            val json = JSONArray(encoded)
            for (index in 0 until json.length()) {
                val response = (json.opt(index) as? String)?.trim().orEmpty()
                if (response.isNotEmpty() && response !in responses) {
                    responses.add(response)
                }
            }
            responses
        } catch (_: JSONException) {
            emptyList()
        }
    }

    fun save(responses: List<String>): Boolean {
        val normalized = normalize(responses)
        val json = JSONArray()
        normalized.forEach { response -> json.put(response) }
        return preferences.edit().putString(KEY_RESPONSES, json.toString()).commit()
    }

    fun add(response: String): Boolean {
        val normalized = response.trim()
        if (normalized.isEmpty()) return false

        val responses = load().toMutableList()
        if (normalized in responses) return false
        responses.add(normalized)
        return save(responses)
    }

    fun remove(response: String): Boolean {
        val normalized = response.trim()
        if (normalized.isEmpty()) return false

        val responses = load().toMutableList()
        if (!responses.remove(normalized)) return false
        return save(responses)
    }

    private fun normalize(responses: List<String>): List<String> {
        val normalized = ArrayList<String>(responses.size)
        responses.forEach { response ->
            val value = response.trim()
            if (value.isNotEmpty() && value !in normalized) normalized.add(value)
        }
        return normalized
    }

    private companion object {
        const val KEY_RESPONSES = "custom_quick_responses"
    }
}
