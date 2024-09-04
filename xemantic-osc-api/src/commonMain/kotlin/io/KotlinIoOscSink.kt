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

import com.xemantic.osc.type.OscColor
import com.xemantic.osc.type.OscMidiMessage
import com.xemantic.osc.type.OscTimeTag
import com.xemantic.osc.oscPadding
import kotlinx.io.*

/**
 * Writes padding of the data of given [size], according to
 * OSC protocol padding rules. The padding is aligned to 4-byte
 * chunks.
 *
 * @param size the size of the data to pad.
 */
public fun Sink.writeOscPadding(size: Int) {
  val padding = oscPadding(size)
  val bytes = ByteArray(padding) { 0 }
  write(bytes)
}

/**
 * Writes a string according to OSC protocol rules.
 * The written data is `0`-terminated and padded.
 *
 * @param string the string to write.
 * @see oscPadding
 * @see writeOscPadding
 */
public fun Sink.writeOscString(string: String) {
  val bytes = string.encodeToByteArray()
  write(bytes)
  writeByte(0)
  writeOscPadding(bytes.size + 1)
}

/**
 * Writes a char according to OSC protocol rules.
 * The written char is represented as 4 bytes.
 *
 * @param char the char to write.
 */
public inline fun Sink.writeOscChar(char: Char) {
  writeInt(char.code)
}

/**
 * Writes a [ByteArray] representing a BLOB according to OSC protocol rules.
 * The first written 4-byte [Int] represent the size of the following data
 * and the data is also padded.
 *
 * @param blob the blob to write.
 * @see oscPadding
 * @see writeOscPadding
 */
public fun Sink.writeOscBlob(blob: ByteArray) {
  writeInt(blob.size)
  write(blob)
  writeOscPadding(blob.size)
}

/**
 * Writes an [OscTimeTag] according to OSC protocol rules.
 *
 * @param timeTag a time tag.
 */
public inline fun Sink.writeOscTimeTag(timeTag: OscTimeTag) {
  writeULong(timeTag.timeTag)
}

/**
 * Writes an [OscColor] according to OSC protocol rules.
 *
 * @param color the color to write.
 */
public inline fun Sink.writeOscColor(color: OscColor) {
  writeUInt(color.rgba)
}

/**
 * Writes an [OscMidiMessage] according to OSC protocol rules.
 *
 * @param message the MIDI message to write.
 */
public inline fun Sink.writeOscMidiMessage(
  message: OscMidiMessage
) {
  writeUInt(message.message)
}

/**
 * Transforms a sequence of writes to given [Sink] into a [ByteArray].
 * Useful for testing.
 *
 * @param block the sequence of writes to the [Sink]
 * @return the byte array of data written to the [Sink].
 */
public fun writeToBytes(
  block: Sink.() -> Unit
): ByteArray = Buffer().apply(block).readByteArray()
