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

import com.xemantic.osc.OscInput
import com.xemantic.osc.OscMessage
import com.xemantic.osc.OscPeer
import kotlinx.coroutines.flow.*
import com.xemantic.osc.route
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Routes `/Note*` and `/Velocity*` addresses for incoming
 * OSC messages describing [AbletonNote]s sent out by
 * [Ableton's Max for Live OSC plugin](https://www.ableton.com/en/packs/connection-kit/).
 *
 * Note: The associated [OscInput] has to be passed
 * [kotlinx.coroutines.CoroutineScope] associated
 * with a single-threaded [kotlin.coroutines.CoroutineContext]
 * to guarantee that received [OscMessage]s are not reordered:
 *
 * ```
 * val scope = CoroutineScope(
 *   newSingleThreadContext("note-listener")
 * )
 * val input = OscInput(scope) {
 *   routeAbletonNotes()
 * }
 * ```
 *
 * Note: `runBlocking` as well as `runTest` used in testing
 * coroutine code has such a guarantee:
 * ```
 * runBlocking {
 *   oscInput {
 *     routeAbletonNotes()
 *   }
 * }
 * ```
 *
 * In this example a special extension function `CoroutineScope.oscInput`
 * is used to associate [OscInput] automatically with current
 * [kotlinx.coroutines.CoroutineScope]
 *
 * @param addressBase the OSC address base starting with `/`,
 *          e.g. `/ableton`, defaults to empty string.
 * @see unrouteAbletonNotes
 */
public fun OscInput.routeAbletonNotes(
  addressBase: String = ""
) {

  val noteAddressBase = "$addressBase/Note"
  val velocityAddressBase = "$addressBase/Velocity"

  logger.info {
    "Routing incoming Ableton OSC notes to " +
        "$noteAddressBase* and $velocityAddressBase*"
  }

  route<Int>(
    address = "$noteAddressBase*",
    matchAddress = { it.startsWith(noteAddressBase) },
  )

  route<Int>(
    address = "$velocityAddressBase*",
    matchAddress = { it.startsWith(velocityAddressBase) },
  )

}

/**
 * Transforms the flow of [OscMessage]s to the flow
 * of [AbletonNote]s.
 *
 * Note: this [Flow] should be [Flow.collect]ed only on a
 * single-threaded coroutine, because non-concurrent map
 * is used internally to correlate received `/Note*` and
 * `/Velocity*` messages.
 *
 * @param addressBase the OSC address base starting with `/`,
 *          e.g. `/ableton`, defaults to empty string.
 * @return the [Flow] of [AbletonNote]s.
 */
public fun Flow<OscMessage<*>>.toAbletonNotes(
  addressBase: String = "",
): Flow<AbletonNote> {
  val peerNotesMap = mutableMapOf<OscPeer, MutableMap<Int, Int>>()
  val noteAddressBase = "$addressBase/Note"
  val velocityAddressBase = "$addressBase/Velocity"
  return transform { message ->
    if (message.address.startsWith(noteAddressBase)) {
      val note = message.value as Int
      val polyphonyIndex = message.address.removePrefix(
        noteAddressBase
      ).toInt()
      val polyphonyNotesMap = peerNotesMap.getOrPut(message.peer) {
        mutableMapOf()
      }
      polyphonyNotesMap[polyphonyIndex] = note
    } else if (message.address.startsWith(velocityAddressBase)) {
      val polyphonyIndex = message.address.removePrefix(
        velocityAddressBase
      ).toInt()
      val polyphonyNotesMap = peerNotesMap[message.peer]
      if (polyphonyNotesMap != null) {
        val key = polyphonyNotesMap.remove(polyphonyIndex)
        if (key != null) {
          val abletonNote = AbletonNote(
            peer = message.peer,
            key = key,
            velocity = message.value as Int,
            polyphonyIndex = polyphonyIndex
          )
          logger.trace { "Received: $abletonNote" }
          emit(abletonNote)
        } else {
          logger.warn {
            "Ignoring: received ${message.address}=${message.value}, " +
                "but no matching prior note key received"
          }
        }
      } else {
        logger.warn {
          "Ignoring: received ${message.address}=${message.value}, " +
              "but no matching prior note received from this peer: ${message.peer}"
        }
      }
    }
  }
}

/**
 * Unroutes `/Note*` and `/Velocity*` OSC addresses.
 *
 * @param addressBase the OSC address base starting with `/`,
 *          e.g. `/ableton`, defaults to empty string.
 */
public fun OscInput.unrouteAbletonNotes(
  addressBase: String = ""
) {

  val noteAddressBase = "$addressBase/Note"
  val velocityAddressBase = "$addressBase/Velocity"

  logger.info {
    "Unrouting incoming Ableton OSC notes to " +
        "$noteAddressBase* and $velocityAddressBase*"
  }

  unroute(
    "$noteAddressBase*",
    "$velocityAddressBase*"
  )

}
