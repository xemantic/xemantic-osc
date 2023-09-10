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

import com.xemantic.osc.OscPeer
import com.xemantic.osc.ableton.Midi2AbletonNotesOscSender
import com.xemantic.osc.test.TestOscOutput
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import javax.sound.midi.ShortMessage
import javax.sound.midi.ShortMessage.NOTE_OFF
import javax.sound.midi.ShortMessage.NOTE_ON
import kotlin.test.Test

class AbletonNotesSendingReceiverTest {

  // given
  private val peer = OscPeer(
    hostname = "localhost",
    port = 10042,
    transport = "test"
  )

  @Test
  fun `should send MIDI Receiver messages to OscOutput`() = runTest {
    // given
    val output = TestOscOutput(peer)
    val receiver = AbletonNotesSendingReceiver(
      // note: sender could be also mocked, but TestOscOutput makes assertions cleaner
      sender = Midi2AbletonNotesOscSender(output),
      scope = this
    )

    // when
    with (receiver) {
      send(ShortMessage(NOTE_ON, 42, 100), -1)
      send(ShortMessage(NOTE_OFF, 42, 0), -1)
    }
    advanceUntilIdle()

    output.messages shouldBe listOf(
      "/Note1" to 42,
      "/Velocity1" to 100,
      "/Note1" to 42,
      "/Velocity1" to 0,
    )
  }

}
