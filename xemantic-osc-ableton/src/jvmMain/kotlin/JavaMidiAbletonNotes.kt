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

import com.xemantic.osc.ableton.AbletonNote
import kotlinx.coroutines.flow.Flow
import javax.sound.midi.*

/**
 * Converts [AbletonNote] to [ShortMessage].
 *
 * @return the Java MIDI [ShortMessage] instance.
 */
public fun AbletonNote.toMidiShortMessage(): ShortMessage =
  if (velocity != 0) {
    ShortMessage(
      ShortMessage.NOTE_ON,
      key,
      velocity
    )
  } else {
    ShortMessage(
      ShortMessage.NOTE_OFF,
      key,
      0
    )
  }

/**
 * Plays [AbletonNote]s from this [Flow] on MIDI [Receiver].
 *
 * Note: this function calls [Flow.collect] and because the
 * stream of notes is potentially infinite. It is blocking.
 * In order to cancel playing in coroutine:
 *
 * ```
 * val receiver: Receiver = ...
 * val notes: Flow<AbletonNote> = ...
 * val job = launch {
 *   notes.playOn(receiver)
 * }
 * // ...
 * job.cancelAndJoin()
 * ```
 *
 * @param receiver the Java MIDI receiver.
 */
public suspend fun Flow<AbletonNote>.playOn(
  receiver: Receiver
) {
  collect { note ->
    receiver.send(note.toMidiShortMessage(), -1)
  }
}

/**
 * Plays [AbletonNote]s from this [Flow] on [MidiChannel].
 *
 * Note: this function calls [Flow.collect] and because the
 * stream of notes is potentially infinite. It is blocking.
 * In order to cancel playing in coroutine:
 *
 * ```
 * val channel: MidiChannel = ...
 * val notes: Flow<AbletonNote> = ...
 * val job = launch {
 *   notes.playOn(channel)
 * }
 * // ...
 * job.cancelAndJoin()
 * ```
 *
 * @param channel the Java MIDI channel.
 */
public suspend fun Flow<AbletonNote>.playOn(
  channel: MidiChannel
) {
  collect { note ->
    if (note.velocity != 0) {
      channel.noteOn(note.key, note.velocity)
    } else {
      channel.noteOff(note.key)
    }
  }
}
