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
import com.xemantic.osc.ableton.AbletonNote
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import javax.sound.midi.MidiChannel
import javax.sound.midi.ShortMessage
import javax.sound.midi.ShortMessage.NOTE_OFF
import javax.sound.midi.ShortMessage.NOTE_ON
import kotlin.test.Test

@Suppress("MemberVisibilityCanBePrivate")
class JavaMidiAbletonNotesTest {

  // given
  val peer = OscPeer(
    hostname = "localhost",
    port = 10042,
    transport = "test"
  )

  val startNote = AbletonNote(
    key = 42,
    velocity = 43,
    polyphonyIndex = 3,
    peer = peer
  )

  val endNote = AbletonNote(
    key = 42,
    velocity = 0,
    polyphonyIndex = 3,
    peer = peer
  )

  @Test
  fun `should convert AbletonNote to MIDI ShortMessage`() {
    // when
    val startMessage = startNote.toMidiShortMessage()
    val endMessage = endNote.toMidiShortMessage()

    // then
    with(startMessage) {
      command shouldBe NOTE_ON
      channel shouldBe 0
      data1 shouldBe 42
      data2 shouldBe 43
    }
    with(endMessage) {
      command shouldBe NOTE_OFF
      channel shouldBe 0
      data1 shouldBe 42
      data2 shouldBe 0
    }
  }

  @Test
  fun `should play AbletonNotes Flow on MIDI Receiver`() = runTest {
    // given
    val receiver = TestReceiver()
    val notes = MutableSharedFlow<AbletonNote>()
    val playJob = launch { notes.playOn(receiver) }
    advanceUntilIdle()

    // when
    notes.emit(startNote)
    notes.emit(endNote)

    // then
    receiver.messages shouldHaveSize 2
    receiver.messages[0] shouldBe instanceOf<ShortMessage>()
    val message1 = receiver.messages[0] as ShortMessage
    with(message1) {
      command shouldBe NOTE_ON
      channel shouldBe 0
      data1 shouldBe 42
      data2 shouldBe 43
    }
    receiver.messages[1] shouldBe instanceOf<ShortMessage>()
    val message2 = receiver.messages[1] as ShortMessage
    with(message2) {
      command shouldBe NOTE_OFF
      channel shouldBe 0
      data1 shouldBe 42
      data2 shouldBe 0
    }

    // cleanup
    playJob.cancelAndJoin()
  }

  @Test
  fun `should play AbletonNotes Flow on MIDI Channel`() = runTest {
    // given
    val channel = mockk<MidiChannel>()
    every { channel.noteOn(any(), any()) } just runs
    every { channel.noteOff(any()) } just runs
    val notes = MutableSharedFlow<AbletonNote>()
    val playJob = launch {
      notes.playOn(channel)
    }
    advanceUntilIdle()

    // when
    notes.emit(startNote)
    notes.emit(endNote)

    // then
    verifySequence {
      channel.noteOn(42, 43)
      channel.noteOff(42)
    }
    confirmVerified(channel)

    // cleanup
    playJob.cancelAndJoin()
  }

}
