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

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import java.util.LinkedList
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem
import javax.sound.midi.Synthesizer
import kotlin.concurrent.thread

class Closer {

  private val closeables = LinkedList<AutoCloseable>()

  init {
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
      closeables.reversed().forEach {
        it.close()
      }
    })
  }

  fun <T : AutoCloseable> closeOnExit(closeable: T): T {
    closeables.add(closeable)
    return closeable
  }

  fun onExit(block: () -> Unit) {
    closeables.add(AutoCloseable { block() })
  }

}

internal fun udpSocket(
  hostname: String? = null,
  port: Int? = null
): Pair<SelectorManager, BoundDatagramSocket> {
  val selectorManager = SelectorManager(Dispatchers.IO)
  val socket = aSocket(selectorManager).udp().let {
    if (port == null && hostname == null) {
      it.bind()
    } else {
      it.bind(InetSocketAddress(hostname ?: "::", port ?: 0))
    }
  }
  return selectorManager to socket
}

internal fun listMidiDevices(
  deviceInfos: Array<MidiDevice.Info>
): String = """
  MIDI devices:

  |no|name|vendor|description|version|max receivers|max transmitters|
  |--|----|------|-----------|-------|-------------|----------------|
""".trimIndent() + "\n" +
    deviceInfos.mapIndexed { index, info ->
      val device = MidiSystem.getMidiDevice(info)
      "|$index|${info.name}|${info.vendor}" +
          "|${info.description}|${info.version}" +
          "|${formatMax(device.maxReceivers)}" +
          "|${formatMax(device.maxTransmitters)}|"
    }.joinToString("\n")

private fun formatMax(count: Int) = if (count == -1) "∞" else count

internal fun Synthesizer.listInstruments(): String = """
  MIDI synthesizer instruments:

""".trimIndent() + "\n" +
      defaultSoundbank.instruments.mapIndexed {  index, instrument ->
        "* $index - ${instrument.name}"
      }.joinToString("\n")
