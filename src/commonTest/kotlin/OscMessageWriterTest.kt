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

package com.xemantic.osc

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlin.test.Test

class OscMessageWriterTest {

  @Test
  fun shouldThrowExceptionOnEmptyTypeTag() {
    shouldThrowWithMessage<IllegalArgumentException>(
      "tag cannot be blank"
    ) {
      written { typeTag("") }
    }
  }

  @Test
  fun shouldThrowExceptionOnBlankTypeTag() {
    shouldThrowWithMessage<IllegalArgumentException>(
      "tag cannot be blank"
    ) {
      written { typeTag(" ") }
    }
  }

  @Test
  fun shouldWriteTypeTagsOfVariousSizesWith4ByteAlignment() {
    written {
      typeTag("f")
    } shouldBe byteArrayOf(COMMA_BYTE, FLOAT_BYTE, 0, 0)
    written {
      typeTag("ff")
    } shouldBe byteArrayOf(COMMA_BYTE, FLOAT_BYTE, FLOAT_BYTE, 0)
    written {
      typeTag("fff")
    } shouldBe byteArrayOf(COMMA_BYTE, FLOAT_BYTE, FLOAT_BYTE, FLOAT_BYTE, 0, 0, 0, 0)
    written {
      typeTag("ffff")
    } shouldBe byteArrayOf(
      COMMA_BYTE,
      FLOAT_BYTE,
      FLOAT_BYTE,
      FLOAT_BYTE,
      FLOAT_BYTE,
      0,
      0,
      0
    )
  }

  @Test
  fun shouldWriteString() {
    written {
      string("a")
    } shouldBe byteArrayOf('a'.code.toByte(), 0, 0, 0)
  }

}

private fun written(
  value: Any? = null,
  block: Osc.Message.Writer<*>.() -> Unit
): ByteArray = buildPacket {
  val oscWriter = Osc.Message.Writer(value, this)
  block(oscWriter)
}.readBytes()
