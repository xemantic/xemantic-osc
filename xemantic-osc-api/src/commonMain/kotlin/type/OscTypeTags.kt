/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2024 Kazimierz Pogoda
 *
 * This file is part of xemantic-osc.
 *
 * xemantic-osc is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * xemantic-osc is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with xemantic-osc.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.osc.type

import com.xemantic.osc.OscException

public object OscImpulse

/**
 * The OSC type tag associated with this boolean value (`T` or `F`).
 */
public val Boolean.oscTypeTag: String get() = if (this) "T" else "F"

/**
 * A string containing a sequence of OSC type tags for all the elements of this list.
 * Note: null elements are allowed, and they might be represented as a single `N` character.
 *
 * @return the OSC type tag string.
 * @throws OscException if any element of the list cannot be represented in OSC protocol.
 */
public fun List<*>.oscTypeTags(): String = joinToString(separator = "") {
  it?.oscTypeTag ?: "N"
}

/**
 * The OSC Type Tag string corresponding with the type of this object.
 *
 * @throws OscException if this object's type cannot be represented in OSC protocol.
 */
public val Any.oscTypeTag: String get() = when (this) {
  is Int -> "i"
  is Float -> "f"
  is String -> "s"
  is ByteArray -> "b"
  is Boolean -> oscTypeTag
  is OscImpulse -> "I"
  is OscTimeTag -> "t"
  is Long -> "h"
  is Double -> "d"
  is Char -> "c"
  is OscColor -> "r"
  is OscMidiMessage -> "m"
  is List<*> -> "[${oscTypeTags()}]"
  else -> throw OscException(
    "Type unsupported in OSC: ${this::class}"
  )
}

public fun String.toBooleanOscTypeTag(): Boolean = when (this) {
  "T" -> true
  "F" -> false
  else -> throw OscException( // should never happen
    "Invalid typeTag for representing Boolean: $this"
  )
}
