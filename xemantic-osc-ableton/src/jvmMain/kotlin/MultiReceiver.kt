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

import javax.sound.midi.MidiMessage
import javax.sound.midi.Receiver

public class MultiReceiver(
  private val receivers: List<Receiver>
) : Receiver {

  public constructor(
    vararg receivers: Receiver
  ) : this(receivers.toList())

  override fun send(
    message: MidiMessage,
    timeStamp: Long
  ) {
    receivers.forEach {
      it.send(message, timeStamp)
    }
  }

  override fun close() {
    receivers.forEach {
      it.close()
    }
  }

}