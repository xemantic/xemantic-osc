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
import javax.sound.midi.MidiSystem
import javax.sound.midi.Synthesizer

fun main(args: Array<String>) = ListMidiSynthesizerInstruments().main(args)

class ListMidiSynthesizerInstruments : CliktCommand(
  help = "Lists instruments available on the default JVM MIDI synthesizer.",
  printHelpOnEmptyArgs = true
) {

  private val closer: Closer = Closer()

  private val synthesizer: Synthesizer = closer.closeOnExit(
    MidiSystem.getSynthesizer()
  )

  override fun commandHelpEpilog(context: Context): String =
    synthesizer.listInstruments()

  override fun run() { /* nothing to do here */ }

}
