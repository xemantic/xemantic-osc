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

package com.xemantic.osc

import com.xemantic.osc.collections.CopyOnWriteMap
import com.xemantic.osc.convert.DEFAULT_OSC_ENCODERS
import com.xemantic.osc.protocol.writeOscMessage
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KType

internal class DefaultOscOutput(
  override val peer: OscPeer,
  private val sender: OscTransport.Sender,
  dispatcher: CoroutineContext
) : OscOutput {

  private val logger = KotlinLogging.logger {}

  private val router = OscRouter<OutputRoute<*>>()

  private val job = Job()
  private val scope = CoroutineScope(dispatcher + job)

  override val encoders: MutableMap<KType, OscEncoder<*>> = CopyOnWriteMap(
    initialMap = DEFAULT_OSC_ENCODERS
  )

  override fun <T> route(
    type: KType,
    address: String,
    addressMatcher: AddressMatcher?,
    encoder: OscEncoder<T>?
  ) {
    val route = OutputRoute(
      address,
      addressMatcher,
      encoder ?: encoders.resolve(type)
    )
    router.addRoute(route)
  }

  override fun unroute(vararg addresses: String) {
    router.unroute(addresses)
  }

//  fun send(vararg bundle: Pair<String, Any>) {
//    val noRoutes = bundle.filter { (route, _) ->
//      router.getRoute(route) == null
//    }
//    if (noRoutes.isNotEmpty()) {
//      val message = noRoutes.joinToString(",\n") {
//          "No route defined for address: $it"
//      }
//      // TODO throw error
//    }

//      (it.first to router.getRoute(it.first)) to it.second
//    }
  //}

  /**
   * @throws IllegalArgumentException if no converter was registered for the [address].
   */
  override suspend fun suspendedSend(
    address: String,
    value: Any?
  ) {
    logger.trace { "OSC OUT, peer: $peer -> $address" }
    val route = router.getRoute(address)
    if (route != null) {
      sender.send {
        @Suppress("UNCHECKED_CAST")
        writeOscMessage(
          address,
          route.encoder as OscEncoder<Any?>,
          value
        )
      }
    } else {
      throw IllegalArgumentException(
        "No route defined for address: $address"
      )
    }
  }

  override fun send(
    address: String, value: Any?
  ) {
    scope.launch {
      suspendedSend(address, value)
    }
  }

  override fun close() {
    // TODO should it only cancel children?
    job.cancel()
  }

}
