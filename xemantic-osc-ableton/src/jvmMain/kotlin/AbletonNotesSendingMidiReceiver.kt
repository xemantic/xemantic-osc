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

import com.xemantic.osc.ableton.Midi2AbletonNotesOscSender
import kotlinx.coroutines.*
import javax.sound.midi.MidiMessage
import javax.sound.midi.Receiver
import javax.sound.midi.ShortMessage
import javax.sound.midi.ShortMessage.NOTE_OFF
import javax.sound.midi.ShortMessage.NOTE_ON

public class AbletonNotesSendingMidiReceiver(
  private val sender: Midi2AbletonNotesOscSender,
  private val scope: CoroutineScope
) : Receiver {

  override fun send(message: MidiMessage, timeStamp: Long) {
    if (message is ShortMessage) {
      message.sendOut()
    }
  }

  private fun ShortMessage.sendOut() {
    when (command) {
      NOTE_ON -> scope.launch {
        sender.noteOn(data1, data2)
      }
      NOTE_OFF -> scope.launch {
        sender.noteOff(data1, data2)
      }
    }
  }

  override fun close() {
    /* nothing to close */
  }

}
