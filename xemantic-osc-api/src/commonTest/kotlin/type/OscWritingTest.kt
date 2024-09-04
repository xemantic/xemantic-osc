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

import com.xemantic.osc.ZERO
import com.xemantic.osc.writeOscToBytes
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OscWritingTest {

  @Test
  fun shouldWriteString() {
    writeOscToBytes {
      string("OSC")
    } shouldBe com.xemantic.osc.byteArrayOf('O', 'S', 'C', ZERO)
  }

  @Test
  fun shouldWriteInt() {
    writeOscToBytes {
      int(42)
    } shouldBe byteArrayOf(0, 0, 0, 42)
  }

  @Test
  fun shouldWriteFloat() {
    writeOscToBytes {
      float(42.0f)
    } shouldBe byteArrayOf(66, 40, 0, 0)
  }

  @Test
  fun shouldWriteDouble() {
    writeOscToBytes {
      double(42.0)
    } shouldBe byteArrayOf(64, 69, 0, 0, 0, 0, 0, 0)
  }

  @Test
  fun shouldWriteLong() {
    writeOscToBytes {
      long(42)
    } shouldBe byteArrayOf(0, 0, 0, 0, 0, 0, 0, 42)
  }

  @Test
  fun shouldWriteChar() {
    writeOscToBytes {
      char('a')
    } shouldBe com.xemantic.osc.byteArrayOf(ZERO, ZERO, ZERO, 'a')
  }

  @Test
  fun shouldWriteBlob() {
    writeOscToBytes {
      blob(byteArrayOf(42))
    } shouldBe byteArrayOf(0, 0, 0, 1, 42, 0, 0, 0)
  }

  @Test
  fun shouldWriteTimeTag() {
    writeOscToBytes {
      timeTag(OscTimeTag.IMMEDIATE)
    } shouldBe byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1)
  }

  @Test
  fun shouldWriteColor() {
    writeOscToBytes {
      color(OscColor(1u, 2u, 3u, 4u))
    } shouldBe byteArrayOf(1, 2, 3, 4)
  }

  @Test
  fun shouldWriteMidiMessage() {
    writeOscToBytes {
      midiMessage(OscMidiMessage(1u, 2u, 3u, 4u))
    } shouldBe byteArrayOf(1, 2, 3, 4)
  }

  @Test
  fun shouldWriteTypeTagPaddedWithZeroes() {

    writeOscToBytes {
      typeTag("f")
    } shouldBe com.xemantic.osc.byteArrayOf(',', 'f', ZERO, ZERO)

    writeOscToBytes {
      typeTag("fi")
    } shouldBe com.xemantic.osc.byteArrayOf(',', 'f', 'i', ZERO)

    writeOscToBytes {
      typeTag("fis")
    } shouldBe com.xemantic.osc.byteArrayOf(',', 'f', 'i', 's', ZERO, ZERO, ZERO, ZERO)

    writeOscToBytes {
      typeTag("fisc")
    } shouldBe com.xemantic.osc.byteArrayOf(',', 'f', 'i', 's', 'c', ZERO, ZERO, ZERO)

  }

  @Test
  fun shouldThrowExceptionOnEmptyTypeTag() {
    shouldThrowWithMessage<IllegalArgumentException>(
      "typeTag cannot be blank"
    ) {
      writeOscToBytes { typeTag("") }
    }
  }

  @Test
  fun shouldThrowExceptionOnBlankTypeTag() {
    shouldThrowWithMessage<IllegalArgumentException>(
      "typeTag cannot be blank"
    ) {
      writeOscToBytes { typeTag(" \t") }
    }
  }

}