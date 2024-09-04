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

import com.xemantic.osc.io.*
import kotlinx.io.*

/**
 * The writer of an OSC data stream.
 *
 * It adapts the [Sink] to only allow data types available
 * in the OSC protocol and make sequential write of values
 * concise.
 *
 * @param sink the data sink.
 */
public class OscWriter(
  @PublishedApi
  internal val sink: Sink
) {

  /**
   * Writes OSC String (Type Tag `s`).
   *
   * @param string the string to write.
   */
  public inline fun string(string: String) {
    sink.writeOscString(string)
  }

  /**
   * Writes integer (Type Tag `i`).
   *
   * @param value the integer value to write.
   */
  public inline fun int(value: Int) {
    sink.writeInt(value)
  }

  /**
   * Writes float (Type Tag `f`).
   *
   * @param value the float value to write.
   */
  public inline fun float(value: Float) {
    sink.writeFloat(value)
  }

  /**
   * Writes double (Type Tag `d`).
   *
   * @param value the double value to write.
   */
  public inline fun double(value: Double) {
    sink.writeDouble(value)
  }

  /**
   * Writes long (Type Tag `t`).
   *
   * @param value the long value to write.
   */
  public inline fun long(value: Long) {
    sink.writeLong(value)
  }

  /**
   * Writes char (Type Tag `c`).
   *
   * @param char the char value to write.
   */
  public inline fun char(char: Char) {
    sink.writeOscChar(char)
  }

  /**
   * Writes a blob (Type Tag `b`).
   *
   * @param blob the blob bytes to write.
   */
  public inline fun blob(blob: ByteArray) {
    sink.writeOscBlob(blob)
  }

  /**
   * Writes an OSC time tag (Type Tag `t`).
   *
   * @param timeTag the blob bytes to write.
   */
  public inline fun timeTag(timeTag: OscTimeTag) {
    sink.writeOscTimeTag(timeTag)
  }

  /**
   * Writes an OSC color (Type Tag `r`).
   *
   * @param color the color to write.
   */
  public inline fun color(color: OscColor) {
    sink.writeOscColor(color)
  }

  /**
   * Writes an OSC MIDI message (Type Tag `m`).
   *
   * @param message the color to write.
   */
  public inline fun midiMessage(message: OscMidiMessage) {
    sink.writeOscMidiMessage(message)
  }

}

/**
 * Writes OSC Type Tag.
 *
 * @param typeTag the type tag without leading comma.
 * @throws IllegalArgumentException if `typeTag` is blank or malformed.
 */
public fun OscWriter.typeTag(
  typeTag: String
) {
  require(typeTag.isNotBlank()) { "typeTag cannot be blank" }
  string(",$typeTag")
}
