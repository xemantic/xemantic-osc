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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import com.xemantic.osc.ableton.midi.playOn
import com.xemantic.osc.ableton.routeAbletonNotes
import com.xemantic.osc.ableton.toAbletonNotes
import com.xemantic.osc.oscInput
import com.xemantic.osc.udp.UdpOscTransport
import kotlinx.coroutines.runBlocking
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem

fun main(args: Array<String>) = PlayAbletonNotesOnMidiDevice().main(args)

@Suppress("MemberVisibilityCanBePrivate")
class PlayAbletonNotesOnMidiDevice : CliktCommand(
  help = "Receives notes sent by Ableton via OSC protocol " +
      "and plays them back on the local MIDI device.",
  printHelpOnEmptyArgs = true
) {

  val port: Int? by argument(
    help = "The UDP/OSC port to listen to MIDI notes, random port if not specified"
  ).int()

  val localhost: String? by option(
    help = "The local interface to bind to, if omitted, it will listen on all the interfaces"
  )
  val addressBase: String by option(
    help = "The OSC address base, e.g.: /notes"
  ).default("")

  private val deviceInfos: Array<MidiDevice.Info> = MidiSystem.getMidiDeviceInfo()

  val midiDevice: Int by option(
    help = "The MIDI synthesizer instrument index to use"
  ).int().restrictTo(deviceInfos.indices).default(0)

  override fun commandHelpEpilog(context: Context): String =
    listMidiDevices(deviceInfos)

  override fun run() {
    val closer = Closer()
    val deviceInfo = deviceInfos[midiDevice]
    val device = closer.closeOnExit(
      MidiSystem.getMidiDevice(deviceInfo)
    )

    val (selectorManager, socket) = udpSocket(localhost, port)
    closer.onExit {
      socket.close()
      selectorManager.close()
    }
    val transport = UdpOscTransport(socket)

    device.open()

    runBlocking {
      oscInput {
        routeAbletonNotes(addressBase)
        connect(transport)
      }.messages
        .toAbletonNotes(addressBase)
        .playOn(device.receiver)
    }

  }

}
