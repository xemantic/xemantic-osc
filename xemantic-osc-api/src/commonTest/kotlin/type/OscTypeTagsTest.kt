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

import com.xemantic.osc.OscException
import kotlin.test.Test
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrowWithMessage

class OscTypeTagTest {

  @Test
  fun intOscTypeTagShouldBeI() {
    42.oscTypeTag shouldBe "i"
  }

  @Test
  fun floatOscTypeTagShouldBeF() {
    3.14f.oscTypeTag shouldBe "f"
  }

  @Test
  fun stringOscTypeTagShouldBeS() {
    "Hello".oscTypeTag shouldBe "s"
  }

  @Test
  fun blobOscTagShouldBeB() {
    byteArrayOf(0x01, 0x02).oscTypeTag shouldBe "b"
  }

  @Test
  fun booleanTrueOscTypeTagShouldBeT() {
    true.oscTypeTag shouldBe "T"
  }

  @Test
  fun booleanFalseOscTypeTagShouldBeF() {
    false.oscTypeTag shouldBe "F"
  }

  @Test
  fun oscImpulseOscTypeTagShouldBeI() {
    OscImpulse.oscTypeTag shouldBe "I"
  }

  @Test
  fun oscTimeTagOscTypeTagShouldBeT() {
    OscTimeTag.now().oscTypeTag shouldBe "t"
  }

  @Test
  fun longOscTypeTagShouldBeH() {
    123456789L.oscTypeTag shouldBe "h"
  }

  @Test
  fun doubleOscTypeTagShouldBeD() {
    2.71828.oscTypeTag shouldBe "d"
  }

  @Test
  fun charOscTypeTagShouldBeC() {
    'A'.oscTypeTag shouldBe "c"
  }

  @Test
  fun oscColorTypeTagShouldBeR() {
    OscColor(1u, 2u, 3u, 4u).oscTypeTag shouldBe "r"
  }

  @Test
  fun oscMidiMessageTypeTagShouldBeM() {
      OscMidiMessage(1u, 2u, 3u, 4u).oscTypeTag shouldBe "m"
  }

  @Test
  fun nestedListTypeTagsShouldBeCorrect() {
    listOf(42, "Hello", true, null, listOf(3.14f, listOf('A'))).oscTypeTags() shouldBe "isTN[f[c]]"
  }

  @Test
  fun unsupportedOscTypeShouldThrowException() {
    shouldThrowWithMessage<OscException>(
      "Type unsupported in OSC: class kotlin.Byte"
    ) {
      0.toByte().oscTypeTag
    }
  }

  @Test
  fun listWithUnsupportedOscTypeShouldThrowException() {
    shouldThrowWithMessage<OscException>(
      "Type unsupported in OSC: class kotlin.Any"
    ) {
      listOf(42, Any()).oscTypeTags()
    }
  }

  @Test
  fun tStringToBooleanOscTypeTagShouldReturnTrue() {
    "T".toBooleanOscTypeTag() shouldBe true
  }

  @Test
  fun fStringToBooleanOscTypeTagShouldReturnTrue() {
    "F".toBooleanOscTypeTag() shouldBe false
  }

  @Test
  fun unsupportedStringToBooleanOscTypeTagShouldThrowException() {
    shouldThrowWithMessage<OscException>(
      "Invalid typeTag for representing Boolean: U"
    ) {
      "U".toBooleanOscTypeTag() shouldBe false
    }
  }

}
