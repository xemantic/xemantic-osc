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
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.int
import com.xemantic.osc.ableton.Midi2AbletonNotesOscSender
import com.xemantic.osc.ableton.midi.AbletonNotesSendingReceiver
import com.xemantic.osc.ableton.midi.MultiReceiver
import com.xemantic.osc.udp.UdpOscTransport
import kotlinx.coroutines.*
import javax.sound.midi.*

fun main(args: Array<String>) = ForwardMidiDeviceToRemoteOsc().main(args)

@Suppress("MemberVisibilityCanBePrivate")
class ForwardMidiDeviceToRemoteOsc : CliktCommand(
  help = "Forwards MIDI notes from a local MIDI device to " +
      "remote hosts accepting Ableton OSC notes.",
  printHelpOnEmptyArgs = true
) {

  val midiDevice: Int by argument(
    help = "The MIDI device to connect to"
  ).int()

  val hostsWithPorts: List<Pair<String, Int>> by argument(
    name = "host:port",
    help = "The host and UDP port to connect to, at least one " +
        "is required, many can be specified"
  ).convert {
    val split = it.split(":")
    split[0] to split[1].toInt()
  }.multiple(required = true)

  private val closer = Closer()

  private val deviceInfos: Array<MidiDevice.Info> = MidiSystem.getMidiDeviceInfo()

  override fun commandHelpEpilog(context: Context): String =
    listMidiDevices(deviceInfos)

  override fun run() {

    val deviceInfo = deviceInfos[midiDevice]
    val midiDevice = closer.closeOnExit(
      MidiSystem.getMidiDevice(deviceInfo)
    )

    val (selectorManager, socket) = udpSocket()
    closer.onExit {
      socket.close()
      selectorManager.close()
    }

    val transport = UdpOscTransport(socket)

    val outputs = hostsWithPorts.map { (host, port) ->
      transport.output(host, port)
    }

    val receiver = MultiReceiver(
      outputs.mapIndexed { index, output ->
        val scope = CoroutineScope(newSingleThreadContext("note-sender$index"))
        closer.onExit { scope.cancel() }
        AbletonNotesSendingReceiver(
          Midi2AbletonNotesOscSender(output),
          scope
        )
      }
    )

    with(midiDevice) {
      open()
      transmitter.receiver = receiver
    }

    runBlocking {
      while (currentCoroutineContext().isActive) {
        delay(100)
      }
    }

  }

}
