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
import com.xemantic.osc.byteArrayOf
import com.xemantic.osc.writeOscToBytes
import io.kotest.matchers.shouldBe
import kotlin.test.Test

private val Char.b get() = code.toByte()

class OscEncodingTest {

  @Test
  fun shouldEncodeString() {
    writeOscToBytes {
      oscEncoder<String>()("foo")
    } shouldBe byteArrayOf(',', 's', ZERO, ZERO, 'f', 'o', 'o', ZERO)
  }

  @Test
  fun shouldEncodeInt() {
    writeOscToBytes {
      oscEncoder<Int>()(42)
    } shouldBe byteArrayOf(','.b, 'i'.b, 0, 0, 0, 0, 0, 42)
  }

  @Test
  fun shouldEncodeFloat() {
    writeOscToBytes {
      oscEncoder<Float>()(42.0f)
    } shouldBe byteArrayOf(','.b, 'f'.b, 0, 0, 0x42, 0x28, 0x00, 0x00)
  }

  @Test
  fun shouldEncodeByteArray() {
    writeOscToBytes {
      oscEncoder<ByteArray>()(byteArrayOf(0x01, 0x02, 0x03, 0x04))
    } shouldBe byteArrayOf(','.b, 'b'.b, 0, 0, 0, 0, 0, 4, 1, 2, 3, 4)
  }

  @Test
  fun shouldEncodeBooleanTrue() {
    writeOscToBytes {
      oscEncoder<Boolean>()(true)
    } shouldBe byteArrayOf(','.b, 'T'.b, 0, 0)
  }

  @Test
  fun shouldEncodeBooleanFalse() {
    writeOscToBytes {
      oscEncoder<Boolean>()(false)
    } shouldBe byteArrayOf(','.b, 'F'.b, 0, 0)
  }

  @Test
  fun shouldEncodeOscImpulse() {
    writeOscToBytes {
      oscEncoder<OscImpulse>()(OscImpulse)
    } shouldBe byteArrayOf(','.b, 'I'.b, 0, 0)
  }

  @Test
  fun shouldEncodeOscTimeTag() {
    writeOscToBytes {
      oscEncoder<OscTimeTag>()(OscTimeTag.IMMEDIATE)
    } shouldBe byteArrayOf(','.b, 't'.b, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
  }

  @Test
  fun shouldEncodeLong() {
    writeOscToBytes {
      oscEncoder<Long>()(42L)
    } shouldBe byteArrayOf(','.b, 'h'.b, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42)
  }

  @Test
  fun shouldEncodeDouble() {
    writeOscToBytes {
      oscEncoder<Double>()(42.0)
    } shouldBe byteArrayOf(','.b, 'd'.b, 0, 0, 0x40, 0x45, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
  }

  @Test
  fun shouldEncodeChar() {
    writeOscToBytes {
      oscEncoder<Char>()('A')
    } shouldBe byteArrayOf(','.b, 'c'.b, 0, 0, 0, 0, 0, 'A'.b)
  }

  @Test
  fun shouldEncodeColor() {
    writeOscToBytes {
      oscEncoder<OscColor>()(OscColor(1u, 2u, 3u, 4u))
    } shouldBe byteArrayOf(','.b, 'r'.b, 0, 0, 1, 2, 3, 4)
  }

  @Test
  fun shouldEncodeMidiMessage() {
    writeOscToBytes {
      oscEncoder<OscMidiMessage>()(OscMidiMessage(1u, 2u, 3u, 4u))
    } shouldBe byteArrayOf(','.b, 'm'.b, 0, 0, 1, 2, 3, 4)
  }

  @Test
  fun shouldEncodeListOfIntegers() {
    writeOscToBytes {
      oscEncoder<List<*>>()(listOf(1, 2))
    } shouldBe byteArrayOf(','.b, 105, 105, 0, 0, 0, 0, 1, 0, 0, 0, 2)
  }

  @Test
  fun shouldEncodeNestedLists() {
    writeOscToBytes {
      oscEncoder<List<*>>()(listOf(1, listOf(2, 3), 4))
    } shouldBe byteArrayOf(','.b, 'i'.b, '['.b, 'i'.b, 'i'.b, ']'.b, 'i'.b, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4)
  }

  @Test
  fun shouldEncodeListsContainingNull() {
    writeOscToBytes {
      oscEncoder<List<*>>()(listOf(1, null, 2))
    } shouldBe byteArrayOf(','.b, 'i'.b, 'N'.b, 'i'.b, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2)
  }

}
