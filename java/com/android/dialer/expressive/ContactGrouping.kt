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
 * limitations under the License.
 */

package com.android.dialer.expressive

internal data class ContactNameGroup(val name: String, val people: List<Person>)

internal fun groupContactsByDisplayName(contacts: List<Person>): List<ContactNameGroup> {
    val groups = mutableListOf<Pair<String, MutableList<Person>>>()
    val namedGroupIndices = mutableMapOf<String, Int>()
    contacts.forEach { contact ->
        val name = contact.name.trim()
        val index = if (name.isBlank()) null else namedGroupIndices[name]
        if (index == null) {
            if (name.isNotBlank()) namedGroupIndices[name] = groups.size
            groups.add(name to mutableListOf(contact))
        } else {
            groups[index].second.add(contact)
        }
    }
    return groups.map { (name, people) -> ContactNameGroup(name, people.distinctBy { it.number }) }
}
