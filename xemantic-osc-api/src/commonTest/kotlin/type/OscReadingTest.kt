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
import com.xemantic.osc.ZERO
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OscReadingTest {

  @Test
  fun shouldReadString() {
    OscReader('O', 'S', 'C', ZERO).string() shouldBe "OSC"
  }

  @Test
  fun shouldReadInt() {
    OscReader(0, 0, 0, 42).int() shouldBe 42
  }

  @Test
  fun shouldReadFloat() {
    OscReader(66, 40, 0, 0).float() shouldBe 42.0f
  }

  @Test
  fun shouldReadDouble() {
    OscReader(64, 69, 0, 0, 0, 0, 0, 0).double() shouldBe 42.0
  }

  @Test
  fun shouldReadLong() {
    OscReader(0, 0, 0, 0, 0, 0, 0, 42).long() shouldBe 42.toLong()
  }

  @Test
  fun shouldReadChar() {
    OscReader(ZERO, ZERO, ZERO, 'a').char() shouldBe 'a'
  }

  @Test
  fun shouldReadBlob() {
    OscReader(0, 0, 0, 1, 42, 0, 0, 0).blob() shouldBe byteArrayOf(42)
  }

  @Test
  fun shouldReadTimeTag() {
    OscReader(0, 0, 0, 0, 0, 0, 0, 1).timeTag() shouldBe OscTimeTag.IMMEDIATE
  }

  @Test
  fun shouldReadMidiMessage() {
    OscReader(1, 2, 3, 4).midiMessage() shouldBe OscMidiMessage(1u, 2u, 3u, 4u)
  }

  @Test
  fun shouldReadColor() {
    OscReader(1, 2, 3, 4).color() shouldBe OscColor(1u, 2u, 3u, 4u)
  }

  @Test
  fun shouldReadTypeTag() {
    OscReader(',', 'i', ZERO, ZERO).typeTag() shouldBe "i"
  }

  @Test
  fun shouldNotReadEmptyTypeTag() {
    shouldThrowWithMessage<OscInputException>(
      "Cannot read OSC String, because byte sequence is not 0-terminated"
    ) {
      OscReader {}.typeTag()
    }
  }

  @Test
  fun shouldNotReadTypeTagWhichDoesNotStartWithComma() {
    shouldThrowWithMessage<OscInputException>(
      "OSC type tag must start with ,"
    ) {
      OscReader('i', ZERO, ZERO, ZERO).typeTag()
    }
  }

  @Test
  fun shouldCreateOscReaderPopulatedByOscWriter() {
    OscReader { string("foo") }.string() shouldBe "foo"
  }

}
