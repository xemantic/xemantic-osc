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

package com.xemantic.osc

import com.xemantic.osc.test.toBytes
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import kotlin.test.Test

private const val COMMA = ','.code.toByte()

private const val F = 'f'.code.toByte()


class OscProtocolRulesTest {

  @Test
  fun shouldWriteTypeTagPaddedWithZeroes() {
    toBytes {
      typeTag("f")
    } shouldBe byteArrayOf(COMMA, F, 0, 0)
    toBytes {
      typeTag("ff")
    } shouldBe byteArrayOf(COMMA, F, F, 0)
    toBytes {
      typeTag("fff")
    } shouldBe byteArrayOf(COMMA, F, F, F, 0, 0, 0, 0)
    toBytes {
      typeTag("ffff")
    } shouldBe byteArrayOf(COMMA, F, F, F, F, 0, 0, 0)
  }

  @Test
  fun shouldThrowExceptionOnEmptyTypeTag() {
    shouldThrowWithMessage<IllegalArgumentException>(
      "typeTag cannot be blank"
    ) {
      toBytes { typeTag("") }
    }
  }

  @Test
  fun shouldThrowExceptionOnBlankTypeTag() {
    shouldThrowWithMessage<IllegalArgumentException>(
      "typeTag cannot be blank"
    ) {
      toBytes { typeTag(" \t") }
    }
  }

  @Test
  fun shouldWriteOscStrings() {

    toBytes { string("OSC") } shouldBe byteArrayOf(
      'O'.code.toByte(),
      'S'.code.toByte(),
      'C'.code.toByte(),
      0
    )

    toBytes { string("data") } shouldBe byteArrayOf(
      'd'.code.toByte(),
      'a'.code.toByte(),
      't'.code.toByte(),
      'a'.code.toByte(),
      0,
      0,
      0,
      0
    )

  }

}