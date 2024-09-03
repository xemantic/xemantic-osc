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

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OscMidiMessageTest {

  @Test
  fun shouldCreateOscMidiMessageFromComponents() {
    OscMidiMessage(
      portId = 1u,
      statusByte = 2u,
      data1 = 3u, data2 = 4u
    ).apply {
      portId shouldBe 1u
      statusByte shouldBe 2u
      data1 shouldBe 3u
      data2 shouldBe 4u
    }
  }

  @Test
  fun shouldCreateOscMidiMessageFromInt() {
    OscMidiMessage(0x01020304u).apply {
      portId shouldBe 1u
      statusByte shouldBe 2u
      data1 shouldBe 3u
      data2 shouldBe 4u
    }
  }

  @Test
  fun midiMessagesShouldBeEqual() {
    OscMidiMessage(0x01020304u) shouldBeEqual OscMidiMessage(1u, 2u, 3u, 4u)
  }

}
