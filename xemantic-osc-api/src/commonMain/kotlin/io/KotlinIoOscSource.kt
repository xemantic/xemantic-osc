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
import com.xemantic.osc.oscPadding
import com.xemantic.osc.type.OscColor
import com.xemantic.osc.type.OscMidiMessage
import kotlinx.io.*

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
public inline fun Source.readOscTimeTag(): OscTimeTag =
  OscTimeTag(readULong())

/**
 * Removes 4 bytes from this source, interpreting them as [OscColor] according to OSC protocol rules.
 *
 * @return the OSC RGBA color.
 */
public inline fun Source.readOscColor(): OscColor =
  OscColor(readUInt())

/**
 * Removes 4 bytes from this source, interpreting them as [OscMidiMessage] according to OSC protocol rules.
 *
 * @return the OSC MIDI message.
 */
public inline fun Source.readOscMidiMessage(): OscMidiMessage =
  OscMidiMessage(readUInt())

/**
 * Removes 4 bytes from this source, interpreting the first
 * byte as a byte value according to OSC protocol rules.
 *
 * @return the byte.
 */
public fun Source.readOscByte(): Byte {
  val value = readByte()
  skip(3)
  return value
}

/**
 * Removes 4 bytes from this source, interpreting the
 * first byte as an unsigned byte according to OSC protocol rules.
 *
 * @return the unsigned byte.
 */
public fun Source.readOscUByte(): UByte {
  val value = readUByte()
  skip(3)
  return value
}

/**
 * Removes 4 bytes from this source, interpreting the first
 * 2 bytes as a short value according to OSC protocol rules.
 *
 * @return the short.
 */
public fun Source.readOscShort(): Short {
  val value = readShort()
  skip(2)
  return value
}

/**
 * Removes 4 bytes from this source, interpreting the first
 * 2 bytes as an unsigned short value according to OSC protocol rules.
 *
 * @return the unsigned short.
 */
public fun Source.readOscUShort(): UShort {
  val value = readUShort()
  skip(2)
  return value
}

/**
 * Creates a [Source] from supplied bytes.
 * Useful for testing.
 *
 * @param bytes the sequence of bytes.
 */
public fun Source(
  vararg bytes: Byte,
): Source = Buffer().apply {
  write(bytes)
}

/**
 * Creates a [Source] from supplied chars.
 * Useful for testing.
 *
 * @param chars the sequence of characters.
 */
public fun Source(
  vararg chars: Char
): Source = Buffer().apply {
  write(
    chars.map {
      it.code.toByte()
    }.toByteArray()
  )
}
