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
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OscDecodingTest {

  @Test
  fun shouldDecodeString() {
    oscDecoder<String>().decode(OscReader {
      typeTag("s")
      string("foo")
    }) shouldBe "foo"
  }

  @Test
  fun shouldDecodeInt() {
    oscDecoder<Int>().decode(OscReader {
      typeTag("i")
      int(42)
    }) shouldBe 42
  }

  @Test
  fun shouldDecodeFloat() {
    oscDecoder<Float>().decode(OscReader {
      typeTag("f")
      float(3.0f)
    }) shouldBe 3.0f
  }

  @Test
  fun shouldDecodeBlob() {
    oscDecoder<ByteArray>().decode(OscReader {
      typeTag("b")
      blob(byteArrayOf(0x01, 0x02, 0x03))
    }) shouldBe byteArrayOf(0x01, 0x02, 0x03)
  }

  @Test
  fun shouldDecodeBooleanTrue() {
    oscDecoder<Boolean>().decode(OscReader {
      typeTag("T")
    }) shouldBe true
  }

  @Test
  fun shouldDecodeBooleanFalse() {
    oscDecoder<Boolean>().decode(OscReader {
      typeTag("F")
    }) shouldBe false
  }

  @Test
  fun shouldDecodeImpulse() {
    oscDecoder<OscImpulse>().decode(OscReader {
      typeTag("I")
    }) shouldBe OscImpulse
  }

  @Test
  fun shouldDecodeTimeTag() {
    val now = OscTimeTag.now()
    oscDecoder<OscTimeTag>().decode(OscReader {
      typeTag("t")
      timeTag(now)
    }) shouldBe now
  }

  @Test
  fun shouldDecodeLong() {
    oscDecoder<Long>().decode(OscReader {
      typeTag("h")
      long(9876543210L)
    }) shouldBe 9876543210L
  }

  @Test
  fun shouldDecodeDouble() {
    oscDecoder<Double>().decode(OscReader {
      typeTag("d")
      double(2.718281828)
    }) shouldBe 2.718281828
  }

  @Test
  fun shouldDecodeChar() {
    oscDecoder<Char>().decode(OscReader {
      typeTag("c")
      char('A')
    }) shouldBe 'A'
  }

  @Test
  fun shouldDecodeColor() {
    val expectedColor = OscColor(255u, 0u, 0u, 255u)
    oscDecoder<OscColor>().decode(OscReader {
      typeTag("r")
      color(expectedColor)
    }) shouldBe expectedColor
  }

  @Test
  fun shouldDecodeMidiMessage() {
    // Note On message
    val expectedMidiMessage = OscMidiMessage(0x90u, 0x45u, 0x60u, 0x00u)
    oscDecoder<OscMidiMessage>().decode(OscReader {
      typeTag("m")
      midiMessage(expectedMidiMessage)
    }) shouldBe expectedMidiMessage
  }

  @Test
  fun shouldDecodeList() {
    oscDecoder<List<*>>().decode(OscReader {
      typeTag("is[d[TN]]")
      int(1)
      string("foo")
      double(1.234)
    }) shouldBe listOf(1, "foo", listOf(1.234, listOf(true, null)))
  }

  @Test
  fun shouldDecodeNestedLists() {
    oscDecoder<List<*>>().decode(OscReader {
      typeTag("[i[i[i]]]")
      int(1)
      int(2)
      int(3)
    }) shouldBe listOf(listOf(1, listOf(2, listOf(3))))
  }

  @Test
  fun shouldDecodeListContainingOnlyNull() {
    oscDecoder<List<*>>().decode(OscReader {
      typeTag("N")
    }) shouldBe listOf(null)
  }

  @Test
  fun shouldNotDecodeUnexpectedTagType() {
    val input = OscReader {
      typeTag("i")
      string("foo")
    }
    shouldThrowWithMessage<OscInputException>(
      "Expected typeTag: 's', but was: 'i'"
    ) {
      oscDecoder<String>().decode(input)
    }
  }

}
