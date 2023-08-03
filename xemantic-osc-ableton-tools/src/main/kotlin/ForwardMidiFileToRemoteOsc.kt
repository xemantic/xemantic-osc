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

package com.xemantic.osc.ableton.tools

import com.xemantic.osc.ableton.MidiSequenceToAbletonOscNotesForwarder
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.sound.midi.MidiSystem
import kotlin.system.exitProcess

fun main(args: Array<String>) = forwardMidiFileToRemoteOsc(args)

fun forwardMidiFileToRemoteOsc(args: Array<String>) {

  println(
    "Usage: forwardMidiFileToRemoteOsc file host port [host port..]"
  )

  if (args.size < 3) {
    println("Error: not enough arguments")
    exitProcess(3)
  }

  val file = args[0]
  val sequence = MidiSystem.getSequence(File(file))
  val hosts = args.copyOfRange(1, args.size).asHosts()

  val forwarder = MidiSequenceToAbletonOscNotesForwarder(
    sequence,
    hosts
  )

  runBlocking {
    forwarder.start()
  }

}
