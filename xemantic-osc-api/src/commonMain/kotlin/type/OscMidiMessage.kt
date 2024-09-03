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

import kotlin.jvm.JvmInline

@JvmInline
public value class OscMidiMessage(
  public val message: UInt
) {

  public constructor(
    portId: UByte,
    statusByte: UByte,
    data1: UByte,
    data2: UByte
  ) : this(
    (portId.toUInt() shl 24) or
        (statusByte.toUInt() shl 16) or
        (data1.toUInt() shl 8) or
        data2.toUInt()
  )

  public inline val portId: UByte get() = ((message shr 24) and 0xFFu).toUByte()
  public inline val statusByte: UByte get() = ((message shr 16) and 0xFFu).toUByte()
  public inline val data1: UByte get() = ((message shr 8) and 0xFFu).toUByte()
  public inline val data2: UByte get() = (message and 0xFFu).toUByte()

}
