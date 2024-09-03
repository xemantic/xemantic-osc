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

import kotlinx.datetime.Clock

private const val NTP_EPOCH_OFFSET_SECONDS = 2208988800L

/**
 * OSC-timetag as described in the
 * [protocol specification](https://opensoundcontrol.stanford.edu/spec-1_0.html#timetags).
 */
public data class OscTimeTag(
  val seconds: UInt,  // Using Long to prevent overflow.
  val fraction: UInt  // 1/2^32 fractional seconds
) {

  public inline val immediate: Boolean
    get() = (seconds == 0u) && (fraction == 1u)

  /**
   * Converts the OSC Time Tag to Unix milliseconds.
   */
  val asMillis: Long get() {
    if (immediate) return Clock.System.now().toEpochMilliseconds()
    val unixSeconds = seconds.toLong() - NTP_EPOCH_OFFSET_SECONDS
    val millisFraction = (fraction.toLong() * 1000L) / 0x100000000L  // Convert NTP fraction to milliseconds
    return unixSeconds * 1000 + millisFraction
  }

  public companion object {

    public val IMMEDIATE: OscTimeTag = OscTimeTag(0u, 1u)

    /**
     * Creates an OSC Time Tag from the provided Unix milliseconds.
     */
    public fun fromMillis(millis: Long): OscTimeTag {
      val leftoverMillis = millis.toULong() % 1000u
      val fraction = ((leftoverMillis * 0x100000000u) / 1000u).toUInt()
      return OscTimeTag(
        seconds = ((millis / 1000) + NTP_EPOCH_OFFSET_SECONDS).toUInt(),
        fraction = fraction
      )
    }

    /**
     * Creates an OSC Time Tag for the current time.
     */
    public fun now(): OscTimeTag = fromMillis(
      Clock.System.now().toEpochMilliseconds()
    )

  }

}




