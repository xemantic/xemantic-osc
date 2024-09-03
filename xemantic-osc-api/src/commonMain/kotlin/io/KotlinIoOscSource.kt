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
import com.xemantic.osc.OscTimeTag
import com.xemantic.osc.oscPadding
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
public fun Source.readOscChar(): Char = readInt().toChar()

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
public fun Source.readOscTimeTag(): OscTimeTag = OscTimeTag(
  seconds = readUInt(),
  fraction = readUInt()
)

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
