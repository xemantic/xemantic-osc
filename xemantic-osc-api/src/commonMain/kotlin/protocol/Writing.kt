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

import com.xemantic.osc.OscEncoder
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*

public class OscWriter(
  public val output: Output
) {
  public inline fun typeTag(typeTag: String) {
    output.writeOscTypeTag(typeTag)
  }
  public inline fun int(x: Int) {
    output.writeInt(x)
  }
  public inline fun float(x: Float) {
    output.writeFloat(x)
  }
  public inline fun double(x: Double) {
    output.writeDouble(x)
  }
  public inline fun long(x: Long) {
    output.writeLong(x)
  }
  public inline fun char(x: Char) {
    output.writeInt(x.code)
  }
  public inline fun string(
    x: String,
    charset: Charset = Charsets.UTF_8
  ) {
    output.writeOscString(x, charset)
  }
  public inline fun blob(x: ByteArray) {
    output.writeOscBlob(x)
  }
  public inline fun timeTag(x: OscTimeTag) {
    output.writeOscTimeTag(x)
  }
}

public fun Output.writeOscTypeTag(typeTag: String) {
  require(typeTag.isNotBlank()) { "typeTag cannot be blank" }
  writeByte(COMMA)
  writeText(typeTag)
  writeZeros(oscPadding((typeTag.length + 1)))
}

public fun Output.writeOscString(
  string: String,
  charset: Charset = Charsets.UTF_8
) {
  val text = string.toByteArray(charset)
  writeFully(text)
  writeZeros(oscPadding(text.size))
}

public fun Output.writeOscBlob(blob: ByteArray) {
  writeInt(blob.size)
  writeFully(blob)
  writeZeros(oscPadding(blob.size))
}

public fun Output.writeOscTimeTag(timeTag: OscTimeTag) {
  writeInt(timeTag.seconds)
  writeInt(timeTag.fraction)
}

public fun Output.writeZeros(
  count: Int
) {
  fill(
    times = count.toLong(),
    value = 0
  )
}

public fun <T> Output.writeOscMessage(
  address: String,
  encoder: OscEncoder<T>,
  value: T
) {
  writeOscString(address)
  encoder(OscWriter(this), value)
}
