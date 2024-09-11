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

package com.xemantic.osc.io

import com.xemantic.osc.OscInputException
import com.xemantic.osc.type.OscTimeTag
import com.xemantic.osc.type.OscColor
import com.xemantic.osc.type.OscMidiMessage
import kotlinx.io.*

/**
 * Removes bytes from this source interpreting them as OSC Type Tag.
 * _Note: the initial comma described in the protocol is already stripped in returned string._
 *
 * See [Osc Type Tag String specification](https://ccrma.stanford.edu/groups/osc/spec-1_0.html#osc-type-tag-string)
 *
 * @return the OSC Type Tag String without leading comma character.
 * @throws OscInputException if type tag doesn't exist or is malformed.
 */
public fun Source.readOscTypeTag(): String {
  val head = readOscString()
  if (head.isEmpty()) {
    throw OscInputException(
      "OSC type tag is empty"
    )
  }
  if (head[0] != ',') {
    throw OscInputException(
      "OSC type tag must start with ,"
    )
  }
  return head.substring(1)
}

/**
 * Removes bytes from this source interpreting them as OSC String.
 * The OSC String should be `0`-terminated and padded up to 4 bytes.
 *
 * @return the OSC String.
 * @throws OscInputException on unexpected input data.
 * @throws EOFException on unexpected input data.
 */
public fun Source.readOscString(): String {
  val terminatorIndex = indexOf(0.toByte())
  if (terminatorIndex == -1L) {
    throw OscInputException(
      "Cannot read OSC String, because byte sequence is not 0-terminated"
    )
  }
  val string = readString(terminatorIndex)
  if (terminatorIndex % 4L == 0L) {
    skip(4)
  } else {
    skip(oscPadding(terminatorIndex))
  }
  return string
}

/**
 * Removes 4 bytes from this source, interpreting them as OSC char.
 *
 * @return the OSC Char.
 * @throws EOFException on unexpected input data.
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
public inline fun Source.readOscChar(): Char = readInt().toChar()

/**
 * Removes data from this source interpreting them as OSC blob.
 * The first 4-bytes encode an [Int] describing the size of the blob.
 *
 * @return the OSC Blob.
 * @throws EOFException on unexpected input data.
 */
public fun Source.readOscBlob(): ByteArray {
  val size = readInt()
  val blob = readByteArray(size)
  skip(oscPadding(size).toLong())
  return blob
}

/**
 * Removes 8 bytes from this source, interpreting them as OSC time tag.
 *
 * @return the OSC Time Tag.
 * @throws EOFException on insufficient input data.
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
public inline fun Source.readOscTimeTag(): OscTimeTag =
  OscTimeTag(readULong())

/**
 * Removes 4 bytes from this source, interpreting them as [OscColor] according to OSC protocol rules.
 *
 * @return the OSC RGBA color.
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
public inline fun Source.readOscColor(): OscColor =
  OscColor(readUInt())

/**
 * Removes 4 bytes from this source, interpreting them as [OscMidiMessage] according to OSC protocol rules.
 *
 * @return the OSC MIDI message.
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
public inline fun Source.readOscMidiMessage(): OscMidiMessage =
  OscMidiMessage(readUInt())
