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

import com.xemantic.osc.OscDecoder
import com.xemantic.osc.OscInputException
import com.xemantic.osc.OscMessage
import com.xemantic.osc.OscPeer
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*

public class OscReader(
  public val input: Input
) {
  public fun typeTag(): String {
    val head = string()
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
  public fun assertTypeTag(typeTag: String) {
    val tag = typeTag()
    if (tag != typeTag) {
      throw OscInputException(
        "Expected typeTag: $typeTag, but was: $tag"
      )
    }
  }

  /**
   * Reads OSC integer type.
   *
   * @see Input.readInt
   */
  public inline fun int(): Int = input.readInt()

  /**
   * Reads OSC float type.
   *
   * @see Input.readFloat
   */
  public inline fun float(): Float = input.readFloat()
  public inline fun double(): Double = input.readDouble()
  public inline fun string(
    charset: Charset = Charsets.UTF_8
  ): String = input.readOscString(charset)
  public inline fun blob(): ByteArray = input.readOscBlob()
  public inline fun timesTag(): OscTimeTag = input.readOscTimeTag()
  public inline fun long(): Long = input.readLong()
  public inline fun char(): Char = input.readInt().toChar()
}

public fun Input.readOscString(
  charset: Charset = Charsets.UTF_8
): String = String(
  bytes = buildPacket {
    readUntilDelimiter(0, this)
    discard(oscPadding(size))
  }.readBytes(),
  charset = charset
)

// TODO blob tests
public fun Input.readOscBlob(): ByteArray {
  val size = readInt()
  val blob = readBytes(size)
  discard(oscPadding(size))
  return blob
}

public fun Input.readOscTimeTag(): OscTimeTag = OscTimeTag(
  seconds = readInt(),
  fraction = readInt()
)

public fun <T> Input.readOscMessage(
  peer: OscPeer,
  address: String,
  decoder: OscDecoder<T>,
): OscMessage<T> = OscMessage(
  peer,
  address,
  value = decoder(OscReader(this))
)
