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

import com.xemantic.osc.OscInputException
import com.xemantic.osc.io.*
import kotlinx.io.*

/**
 * The reader of an OSC data stream.
 *
 * It adapts the [Source] to only allow data types available
 * in the OSC protocol and make sequential reads of values
 * concise.
 *
 * @param source the source to read from.
 */
public class OscReader(
  @PublishedApi
  internal val source: Source
) {

  /**
   * Reads an OSC signed integer (Type Tag `i`).
   *
   * @return the int value.
   */
  public inline fun int(): Int = source.readInt()

  /**
   * Reads an OSC float (Type Tag `f`).
   *
   * @return the float value.
   */
  public inline fun float(): Float = source.readFloat()

  /**
   * Reads an OSC String (Type Tag `s`).
   *
   * @return the string value.
   */
  public inline fun string(): String = source.readOscString()

  /**
   * Reads an OSC blob as bytes (Type Tag `b`).
   *
   * @return the blob.
   */
  public inline fun blob(): ByteArray = source.readOscBlob()

  /**
   * Reads an OSC long (Type Tag `h`- 64 bit big-endian two’s complement integer).
   *
   * @return the Long value.
   */
  public inline fun long(): Long = source.readLong()

  /**
   * Reads an OSC Time Tag (Type Tag `t`).
   *
   * @return the Osc Time Tag value.
   */
  public inline fun timeTag(): OscTimeTag = source.readOscTimeTag()

  /**
   * Reads an OSC double (Type Tag `d`).
   *
   * @return the Double value.
   */
  public inline fun double(): Double = source.readDouble()

  /**
   * Reads an OSC char (Type Tag `c`).
   *
   * @return the Char value.
   */
  public inline fun char(): Char = source.readOscChar()

  /**
   * Reads an OSC color (Type Tag `r`).
   *
   * @return the color value.
   */
  public inline fun color(): OscColor = source.readOscColor()

  /**
   * Reads an OSC MIDI message (Type Tag `m`).
   *
   * @return the MIDI message value.
   */
  public inline fun midiMessage(): OscMidiMessage = source.readOscMidiMessage()

}

/**
 * Reads OSC Type Tag.
 * _Note: the initial comma described in the protocol is already stripped._
 *
 * See [Osc Type Tag String specification](https://ccrma.stanford.edu/groups/osc/spec-1_0.html#osc-type-tag-string)
 *
 * @return the OSC Type Tag String without leading comma character.
 * @throws OscInputException if type tag doesn't exist or is malformed.
 */
public fun OscReader.typeTag(): String {
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

/**
 * Creates OSC reader populated with data from specified characters converted to bytes.
 * Useful for testing.
 *
 * @param chars the characters to source the reading from.
 */
public fun OscReader(vararg chars: Char): OscReader = OscReader(
  source = Buffer().apply {
    write(chars.map { it.code.toByte() }.toByteArray())
  }
)

/**
 * Creates OSC reader populated with data from specified bytes.
 * Useful for testing.
 *
 * @param bytes the bytes to source the reading from.
 */
public fun OscReader(vararg bytes: Byte): OscReader = OscReader(
  source = Buffer().apply { write(bytes) }
)

/**
 * Creates OSC reader populated with data from supplied OSC writer.
 * Useful for testing.
 *
 * @param block the sequence of writes to source the reading from.
 */
public fun OscReader(
  block: OscWriter.() -> Unit
): OscReader = OscReader(
  Buffer().apply {
    block(OscWriter(this))
  }
)
