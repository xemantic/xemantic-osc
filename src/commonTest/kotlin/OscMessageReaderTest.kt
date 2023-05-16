/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2022 Kazimierz Pogoda
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

@file:OptIn(ExperimentalUnsignedTypes::class)
package com.xemantic.osc

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlin.test.Test

class OscMessageReaderTest {

  @Test
  fun shouldReadBasicTypes() {
    reader(66u, 40u, 0u, 0u) { float() } shouldBe 42.0f
    reader(64u, 69u, 0u, 0u, 0u, 0u, 0u, 0u) { double() } shouldBe 42.0
    reader('a'.code.toUByte(), 0u, 0u, 0u) { string() } shouldBe "a"
  }

  @Test
  fun shouldThrowExceptionWhenReadingFromEmptyStream() {
    shouldThrowWithMessage<EOFException>(
      "Premature end of stream: expected 4 bytes"
    ) {
      reader { int() }
    }
  }

  @Test
  fun shouldGiveAccessReadBasicTypes() {
    reader(66u, 40u, 0u, 0u) { float() } shouldBe 42.0f
    reader(64u, 69u, 0u, 0u, 0u, 0u, 0u, 0u) { double() } shouldBe 42.0
    reader('a'.code.toUByte(), 0u, 0u, 0u) { string() } shouldBe "a"
  }

}

private fun reader(
  vararg bytes: UByte,
  block: Osc.Message.Reader.() -> Any
): Any = block(
  Osc.Message.Reader(
    "N/A",
    ByteReadPacket(bytes.toByteArray())
  )
)
