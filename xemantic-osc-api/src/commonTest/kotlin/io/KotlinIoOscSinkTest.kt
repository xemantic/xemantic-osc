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

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import com.xemantic.osc.byteArrayOf
import com.xemantic.osc.ZERO

class KotlinIoOscSinkTest {

  @Test
  fun shouldWriteOscPadding() {

    writeToBytes {} shouldBe byteArrayOf()

    writeToBytes {
      writeOscPadding(1)
    } shouldBe byteArrayOf(0, 0, 0)

    writeToBytes {
      writeOscPadding(2)
    } shouldBe byteArrayOf(0, 0)

    writeToBytes {
      writeOscPadding(3)
    } shouldBe byteArrayOf(0)

    writeToBytes {
      writeOscPadding(4)
    } shouldBe byteArrayOf()

  }

  @Test
  fun shouldWriteOscString() {

    writeToBytes {
      writeOscString("")
    } shouldBe byteArrayOf(0, 0, 0, 0)

    writeToBytes {
      writeOscString("a")
    } shouldBe byteArrayOf(
      'a', ZERO, ZERO, ZERO
    )

    writeToBytes {
      writeOscString("ab")
    } shouldBe byteArrayOf(
      'a', 'b', ZERO, ZERO
    )

    writeToBytes {
      writeOscString("abc")
    } shouldBe byteArrayOf(
      'a', 'b', 'c', ZERO
    )

    writeToBytes {
      writeOscString("abcd")
    } shouldBe byteArrayOf(
      'a', 'b', 'c', 'd', ZERO, ZERO, ZERO, ZERO
    )

    writeToBytes {
      writeOscString("abcde")
    } shouldBe byteArrayOf(
      'a', 'b', 'c', 'd', 'e', ZERO, ZERO, ZERO
    )

  }

  @Test
  fun shouldWriteOscChar() {

    writeToBytes {
      writeOscChar('a')
    } shouldBe byteArrayOf(
      ZERO, ZERO, ZERO, 'a'
    )

    writeToBytes {
      writeOscChar('ą')
    } shouldBe byteArrayOf(
      0, 0, 1, 5
    )

    writeToBytes {
      writeOscChar('你')
    } shouldBe byteArrayOf(
      0, 0, 79, 96
    )

  }

  @Test
  fun shouldWriteOscBlob() {

    writeToBytes {
      writeOscBlob(byteArrayOf('a'))
    } shouldBe byteArrayOf(
      97, 0, 0, 0
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b'))
    } shouldBe byteArrayOf(
      97, 98, 0, 0
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b', 'c'))
    } shouldBe byteArrayOf(
      97, 98, 99, 0
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b', 'c', 'd'))
    } shouldBe byteArrayOf(
      97, 98, 99, 100
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b', 'c', 'd', 'e'))
    } shouldBe byteArrayOf(
      97, 98, 99, 100, 101, 0, 0, 0
    )

  }

}
