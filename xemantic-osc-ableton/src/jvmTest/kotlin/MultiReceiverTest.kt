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
package com.xemantic.osc.ableton.midi

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf
import javax.sound.midi.ShortMessage
import javax.sound.midi.ShortMessage.*
import kotlin.test.Test

class MultiReceiverTest {

  @Test
  fun `should create multi MIDI Receiver from List of Receivers and varargs`() {
    MultiReceiver(listOf(TestReceiver(), TestReceiver())) shouldNotBe null
    MultiReceiver(TestReceiver(), TestReceiver()) shouldNotBe null
  }

  @Test
  fun `should send MIDI Message to multiple receivers`() {
    // given
    val receiver1 = TestReceiver()
    val receiver2 = TestReceiver()
    val multiReceiver = MultiReceiver(receiver1, receiver2)

    // when
    multiReceiver.send(
      ShortMessage(NOTE_ON, 42, 0),
      -1
    )

    // then
    receiver1.messages shouldHaveSize 1
    receiver1.messages[0] shouldBe instanceOf<ShortMessage>()
    val message1 = receiver1.messages[0] as ShortMessage
    message1.command shouldBe NOTE_ON
    message1.data1 shouldBe 42
    message1.data2 shouldBe 0

    receiver2.messages shouldHaveSize 1
    receiver2.messages[0] shouldBe instanceOf<ShortMessage>()
    val message2 = receiver2.messages[0] as ShortMessage
    message2.command shouldBe NOTE_ON
    message2.data1 shouldBe 42
    message2.data2 shouldBe 0
  }

  @Test
  fun `should close all the receivers`() {
    // given
    val receiver1 = TestReceiver()
    val receiver2 = TestReceiver()
    val multiReceiver = MultiReceiver(receiver1, receiver2)

    // when
    multiReceiver.close()

    // then
    receiver1.closed shouldBe true
    receiver2.closed shouldBe true
  }

}
