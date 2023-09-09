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

package com.xemantic.osc.ableton

import com.xemantic.osc.OscOutput
import com.xemantic.osc.route
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * A service transforming MIDI notes into OSC notes, as if they were
 * produced by
 * [Ableton's Max for Live OSC plugin](https://www.ableton.com/en/packs/connection-kit/),
 * therefore it can be used for mimicking or simulating Ableton output.
 *
 * The MIDI input is detached from any particular implementation, so it
 * can be easily adapted to Java MIDI, or Web MIDI.
 *
 * Note: This service is not thread-safe. Internal mechanics simulating
 * Ableton-specific polyphony can only work if [noteOn], [noteOff]
 * events are always called in order, which is typically guaranteed
 * by using a single thread.
 *
 * @param output the OSC output.
 * @param addressBase the address base for `/Note*` and `/Velocity` routing.
 */
public class Midi2AbletonNotesOscSender(
  private val output: OscOutput,
  private val addressBase: String = ""
) {

  private val logger = KotlinLogging.logger {}

  init {

    val noteAddressBase = "$addressBase/Note"
    val velocityAddressBase = "$addressBase/Velocity"

    logger.info {
      "Routing outgoing Ableton OSC notes to " +
          "$noteAddressBase* and $velocityAddressBase*"
    }

    with (output) {

      route<Int>(
        address = "$noteAddressBase*",
        addressMatcher = {
          it.startsWith(noteAddressBase)
        }
      )

      route<Int>(
        address = "$velocityAddressBase*",
        addressMatcher = {
          it.startsWith(velocityAddressBase)
        }
      )

    }

  }

  private val polyphonyMap = mutableMapOf<Int, Int>()

  public suspend fun noteOn(key: Int, velocity: Int) {
    logger.trace { "noteOn: key=$key, velocity=$velocity" }
    val polyphony = polyphonyMap.size + 1
    polyphonyMap[key] = polyphony
    with(output) {
      send("$addressBase/Note$polyphony", key)
      send("$addressBase/Velocity$polyphony", velocity)
    }

  }

  public suspend fun noteOff(key: Int, velocity: Int) {
    logger.trace { "noteOff: key=$key, velocity=$velocity" }
    val polyphony = polyphonyMap.remove(key)
    if (polyphony != null) {
      with(output) {
        send("$addressBase/Note$polyphony", key)
        send("$addressBase/Velocity$polyphony", velocity)
      }
    } else {
      logger.error { "Cannot send out noteOff event, no prior noteOn for key: $key" }
    }
  }

  public fun unrouteAbletonNotes() {

    val noteAddressBase = "$addressBase/Note"
    val velocityAddressBase = "$addressBase/Velocity"

    logger.info {
      "Unrouting outgoing Ableton OSC notes to " +
          "$noteAddressBase* and $velocityAddressBase*"
    }

    output.unroute(
      "$noteAddressBase*",
      "$velocityAddressBase*"
    )

  }

}
