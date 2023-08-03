/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2023 Kazimierz Pogoda
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

package com.xemantic.osc.protocol

public const val COMMA: Byte = ','.code.toByte()

public fun oscPadding(size: Int): Int = 4 - ((size) % 4)

// TODO unit test for this
public data class OscTimeTag(
  val seconds: Int,
  var fraction: Int
) {

  val asMilliseconds: Long get() {
    // Convert from NTP epoch to Unix epoch
    val epochSeconds = seconds.toLong() - 2208988800L
    // NTP timestamps are represented using a timestamp format of seconds and fractions of a second.
    // The fractions part represents binary fractions of a second, so it needs to be converted to milliseconds.
    val milliseconds = (fraction.toLong() * 1000L) / 0x100000000L
    return epochSeconds * 1000L + milliseconds
  }

  public companion object {
    public fun now(): OscTimeTag {
      return OscTimeTag(0, 0) // TODO it should use kotlinx-date
    }
  }

}
