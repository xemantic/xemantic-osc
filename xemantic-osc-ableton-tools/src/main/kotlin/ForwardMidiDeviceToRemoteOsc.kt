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

import com.xemantic.osc.ableton.MidiDeviceToAbletonOscNotesForwarder
import com.xemantic.osc.ableton.midi.listMidiDevices
import kotlinx.coroutines.runBlocking
import javax.sound.midi.*
import kotlin.system.exitProcess

fun main(args: Array<String>) = forwardMidiDeviceToRemoteOsc(args)

fun forwardMidiDeviceToRemoteOsc(args: Array<String>) {

  println(
    "Usage: forwardMidiDeviceToRemoteOscPlayers midi_device_no host port [host port..]"
  )

  val deviceInfos = MidiSystem.getMidiDeviceInfo()
  println(listMidiDevices(deviceInfos))

  if (args.size < 3) {
    println("Error: not enough arguments")
    exitProcess(3)
  }

  val midiDeviceNo = args[0].toInt()
  val deviceInfo = deviceInfos[midiDeviceNo]
  val midiDevice = MidiSystem.getMidiDevice(deviceInfo)
  val hosts = args.copyOfRange(1, args.size).asHosts()

  val forwarder = MidiDeviceToAbletonOscNotesForwarder(
    midiDevice,
    hosts
  )

  runBlocking {
    forwarder.start()
  }

}
