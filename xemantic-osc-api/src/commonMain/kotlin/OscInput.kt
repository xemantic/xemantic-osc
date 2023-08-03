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
import com.xemantic.osc.convert.DEFAULT_OSC_DECODERS
import com.xemantic.osc.protocol.readOscMessage
import com.xemantic.osc.protocol.readOscString
import com.xemantic.osc.protocol.readOscTimeTag
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KType

internal class DefaultOscInput(
  dispatcher: CoroutineContext,
  block: OscInput.() -> Unit = {}
) : OscInput {

  private val logger = KotlinLogging.logger {}

  private val router = OscRouter<InputRoute<*>>()

  private val job = Job()
  private val scope = CoroutineScope(dispatcher + job)

  override val decoders: MutableMap<KType, OscDecoder<*>> = CopyOnWriteMap(
    initialMap = DEFAULT_OSC_DECODERS
  )

  override var discardLateBundles: Boolean = false

  override var acceptableLateness: Long = 1000L



  // block needs to be executed after all the properties are created
  init {
    block(this)
  }

  override fun <T> route(
    type: KType,
    address: String,
    matchAddress: AddressMatcher?,
    decoder: OscDecoder<T>?,
    action: OscAction<T>?
  ) : OscInput.Route<T> {
    val route = InputRoute(
      address,
      matchAddress,
      decoder ?: decoders.resolve(type),
      action
    )
    router.addRoute(route)
    return route
  }

  override fun unroute(vararg addresses: String) {
    router.unroute(addresses)
  }

  // private mutable shared flow
  private val _messages = MutableSharedFlow<OscMessage<*>>()
  // publicly exposed as read-only shared flow
  override val messages: Flow<OscMessage<*>> = _messages.asSharedFlow()

//  inline fun <reified T> flow(address: String): Flow<T> {
//    route<T>(address)
//    return messageFlow
//      .filter { it.address == address }
//      .map { it.value as T }
//  }

  override suspend fun handle(peer: OscPeer, input: Input) {
    try {
      doHandle(peer, input)
    } catch (e : Exception) {
      logger.error(e) { "Error while reading OSC packet" }
    }
  }

  override fun close() {
    job.cancel()
  }

  private suspend fun doHandle(peer: OscPeer, input: Input) {
    when (
      val header = input.readOscString()
    ) {
      "#bundle" -> {
        logger.trace { "IN Bundle, peer: $peer" }
        readOscBundle(peer, input)
        logger.trace { "IN Bundle, decoded" }
      }
      else -> {
        logger.trace { "IN Message, peer: $peer -> $header" }
        val route = router.getRoute(address = header)
        if (route != null) {
          val message = input.readOscMessage(
            peer,
            address = header,
            route.decoder
          )
          logger.trace { "IN Message, decoded: $message" }
          (route as InputRoute<Any?>).onMessageReceived(message)
          _messages.emit(message)
        } else {
          logger.warn {
            val typeTag = input.readOscString()
            val content = input.readOscString()
            "Unmatched OSC Message: $peer -> $header [typeTag: $typeTag, asString: $content]"
          }
        }
      }
    }
  }

  private fun readOscBundle(
    peer: OscPeer,
    input: Input
  ): OscBundle {
    val timeTag = input.readOscTimeTag()
    scope.launch {
      //delay(timeTag.asMilliseconds)
    }
//    // read elements
//    val packets = mutableListOf<OscPacket>()
//    while (input.hasBytes(1)) {
//      val elementSize = input.readInt()
//      val packet = readOscPacket(peer, input)
//      if (packet != null) {
//        packets.add(packet)
//      }
//    }
//    return OscBundle(
//      peer,
//      timeTag,
//      packets
//    )
////    val count = input.readInt()
////    val size = input.readInt()
////    readOscPacket(peer, input)
    throw UnsupportedOperationException(
      "bundle still not implemented"
    )
  }

}

// TODO move to some general file
@Suppress("UNCHECKED_CAST")
internal fun <T> Map<KType, OscDecoder<*>>.resolve(
  type: KType,
): OscDecoder<T> = (this[type] ?: throw IllegalArgumentException(
  "No OscDecoder for type: $type"
)) as OscDecoder<T>

@Suppress("UNCHECKED_CAST")
internal fun <T> Map<KType, OscEncoder<*>>.resolve(
  type: KType,
): OscEncoder<T> = (this[type] ?: throw IllegalArgumentException(
  "No OscEncoder for type: $type"
)) as OscEncoder<T>
