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

import kotlin.system.exitProcess

fun main(args: Array<String>) {

  println(
    """
    Usage: java -jar xemantic-osc-ableton-tools-VERSION.jar tool parameters
    tools:
      - playAbletonNotesOnMidiDevice
      - playAbletonNotesOnMidiSynthesizer      
      - forwardMidiFileToRemoteOsc
      - forwardMidiDeviceToRemoteOsc

    """.trimIndent()
  )

  if (args.isEmpty()) {
    println("Error: tool not specified")
    exitProcess(1)
  }

  val tool = args[0]
  val params = args.copyOfRange(1, args.size)

  when (tool) {
    "playAbletonNotesOnMidiDevice" -> playAbletonNotesOnMidiDevice(params)
    "playAbletonNotesOnMidiSynthesizer" -> playAbletonNotesOnMidiSynthesizer(params)
    "forwardMidiFileToRemoteOsc" -> forwardMidiFileToRemoteOsc(params)
    "forwardMidiDeviceToRemoteOsc" -> forwardMidiDeviceToRemoteOsc(params)
    else -> {
      println("Error: unknown tool: $tool")
      exitProcess(2)
    }
  }

}
