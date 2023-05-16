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

package com.xemantic.osc.ktor

import io.ktor.utils.io.core.*

const val COMMA = ','.code.toByte()

// TODO it seems to belong to implementation more
fun Input.readOscString(): String = buildPacket {
  readUntilDelimiter(0, this)
  val padding = 4 - ((size) % 4)
  discard(padding)
}.readText()

fun Output.writeOscTypeTag(tag: String) {
  require(tag.isNotBlank()) {
    "tag cannot be blank"
  }
  writeByte(COMMA)
  writeText(tag)
  val padding = 4 - ((tag.length + 1) % 4)
  writeZeros(count = padding)
}

fun Output.writeOscString(value: String) {
  writeText(value)
  val padding = 4 - ((value.length) % 4)
  writeZeros(padding)
}

fun Output.writeZeros(
  count: Int
) = fill(times = count.toLong(), 0)
