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

public actual val Any.oscTypeTag: String get() = when (this) {
  is Number -> if (isInteger(this)) "i" else "d"
  is String -> "s"
  is ByteArray -> "b"
  is Boolean -> oscTypeTag
  is OscImpulse -> "I"
  is OscTimeTag -> "t"
  is Long -> "h"
  is Char -> "c"
  is OscColor -> "r"
  is OscMidiMessage -> "m"
  is List<*> -> "[${oscTypeTags()}]"
  else -> throw OscException(
    "Type unsupported in OSC: ${this::class}"
  )
}

@Suppress("NOTHING_TO_INLINE")
private inline fun isInteger(
  @Suppress("UNUSED_PARAMETER") o: Any
): Boolean {
  return js("Number.isInteger(o)") as Boolean
}
