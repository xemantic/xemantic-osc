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

import com.xemantic.osc.*
import com.xemantic.osc.type.OscColor
import com.xemantic.osc.type.OscMidiMessage
import com.xemantic.osc.type.OscTimeTag
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KotlinIoOscSinkTest {

  @Test
  fun shouldWriteOscPadding() {
    writeToBytes { writeOscPadding(0) } shouldBe byteArrayOf()
    writeToBytes { writeOscPadding(1) } shouldBe byteArrayOf(0, 0, 0)
    writeToBytes { writeOscPadding(2) } shouldBe byteArrayOf(0, 0)
    writeToBytes { writeOscPadding(3) } shouldBe byteArrayOf(0)
    writeToBytes { writeOscPadding(4) } shouldBe byteArrayOf()
  }

  /**
   * Some test cases taken from OSC protocol specification:
   *
   * https://opensoundcontrol.stanford.edu/spec-1_0-examples.html
   */
  @Test
  fun shouldWriteOscString() {

    writeToBytes {
      writeOscString("")
    } shouldBe byteArrayOf(0, 0, 0, 0)

    writeToBytes {
      writeOscString("OSC")
    } shouldBe byteArrayOf(
      'O', 'S', 'C', ZERO
    )

    writeToBytes {
      writeOscString("data")
    } shouldBe byteArrayOf(
      'd', 'a', 't', 'a', ZERO, ZERO, ZERO, ZERO
    )

    writeToBytes {
      writeOscString("data+")
    } shouldBe byteArrayOf(
      'd', 'a', 't', 'a', '+', ZERO, ZERO, ZERO
    )

  }

  @Test
  fun shouldWriteOscChar() {
    writeToBytes {
      writeOscChar('a')
    } shouldBe byteArrayOf(
      ZERO, ZERO, ZERO, 'a'
    )
  }

  @Test
  fun shouldWriteOscCharWithPolishDiacritics() {
    writeToBytes {
      writeOscChar('ą')
    } shouldBe byteArrayOf(
      0, 0, 1, 5
    )
  }

  @Test
  fun shouldWriteMandarinOscChar() {
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
      0, 0, 0, 1, 97, 0, 0, 0
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b'))
    } shouldBe byteArrayOf(
      0, 0, 0, 2, 97, 98, 0, 0
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b', 'c'))
    } shouldBe byteArrayOf(
      0, 0, 0, 3, 97, 98, 99, 0
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b', 'c', 'd'))
    } shouldBe byteArrayOf(
      0, 0, 0, 4, 97, 98, 99, 100
    )

    writeToBytes {
      writeOscBlob(byteArrayOf('a', 'b', 'c', 'd', 'e'))
    } shouldBe byteArrayOf(
      0, 0, 0, 5, 97, 98, 99, 100, 101, 0, 0, 0
    )

  }

  @Test
  fun shouldWriteOscTimeTag() {
    writeToBytes {
      writeOscTimeTag(OscTimeTag(1u, 2u))
    } shouldBe byteArrayOf(0, 0, 0, 1, 0, 0, 0, 2)
  }

  @Test
  fun shouldWriteImmediateOscTimeTag() {
    writeToBytes {
      writeOscTimeTag(OscTimeTag.IMMEDIATE)
    } shouldBe byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1)
  }

  @Test
  fun shouldWriteOscColor() {
    writeToBytes {
      writeOscColor(OscColor(1u, 2u, 3u, 4u))
    } shouldBe byteArrayOf(1, 2, 3, 4)
  }

  @Test
  fun shouldWriteOscMidiMessage() {
    writeToBytes {
      writeOscMidiMessage(OscMidiMessage(1u, 2u, 3u, 4u))
    } shouldBe byteArrayOf(1, 2, 3, 4)
  }

}
