/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2022 Kazimierz Pogoda
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

import com.xemantic.osc.ktor.readOscString
import com.xemantic.osc.ktor.writeOscString
import com.xemantic.osc.ktor.writeOscTypeTag
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import kotlin.reflect.KType
import kotlin.reflect.typeOf

const val COMMA_BYTE: Byte = ','.code.toByte()

data class OscPeer(
  val host: String,
  val port: Int,
  val transport: String
)

class OscRouter {

  private val converterMap = CopyOnWriteMap<KType, OscMessage.Converter<*>>(
    initialMap = OscMessage.DEFAULT_CONVERTERS
  )

  private val addressRouteMap = CopyOnWriteMap<String, Route<*>>()

  private val addressMatcherRoutes = LinkedIterable<Route<*>>()

  fun convert(
    type: KType,
    converter: OscMessage.Converter<*>,
  ) {
    converterMap.put(type, converter)
  }

  fun <T> route(
    type: KType,
    address: String,
    addressMatcher: ((address: String) -> Boolean)?,
    converter: OscMessage.Converter<T>?,
    action: ((OscMessage<T>) -> Unit)? = null
  ) {
    val routeConverter = converter
      ?: (converterMap.map[type] as OscMessage.Converter<T>?
        ?: throw IllegalArgumentException("No converter for type: $type"))
    val route = Route(
      address,
      addressMatcher,
      routeConverter,
      action
    )
    if (addressMatcher == null) {
      addressRouteMap.put(address, route)
    } else {
      addressMatcherRoutes.addOrReplace(route) { existing ->
        existing.address == route.address
      }
    }
  }

  internal fun unroute(address: String): Boolean {
    val addressRouteRemoved = (addressRouteMap.remove(address) != null)
    val addressMatcherRouteRemoved = (addressMatcherRoutes.remove { route ->
      route.address == address
    } != null)
    return (addressRouteRemoved || addressMatcherRouteRemoved)
  }

  internal fun getRoute(address: String): Route<Any>? = (
      addressRouteMap.map[address]
          ?: addressMatcherRoutes.find { it.addressMatcher!!(address) }
      ) as Route<Any>?

}

class OscInput(
  block: OscInput.() -> Unit = {}
) {

  private val logger = KotlinLogging.logger {}

  val router = OscRouter()

  // block needs to be executed after all the properties are created
  init {
    block(this)
  }

  inline fun <reified T> convert(
    converter: OscMessage.Converter<T>
  ) {
    router.convert(typeOf<T>(), converter)
  }

  inline fun <reified T> route(
    address: String,
    noinline addressMatcher: ((address: String) -> Boolean)? = null,
    converter: OscMessage.Converter<T>? = null,
    noinline action: ((OscMessage<T>) -> Unit)? = null
  ) {
    router.route(
      typeOf<T>(),
      address,
      addressMatcher,
      converter,
      action,
    )
  }

  /**
   * This method is not intended to be used directly
   */


  fun unroute(address: String) {
    if (!router.unroute(address)) {
      logger.warn {
        "No such address to unroute: $address"
      }
    }
  }

  private val _messages = MutableSharedFlow<OscMessage<*>>() // private mutable shared flow
  val messages = _messages.asSharedFlow() // publicly exposed as read-only shared flow

//  inline fun <reified T> flow(address: String): Flow<T> {
//    route<T>(address)
//    return messageFlow
//      .filter { it.address == address }
//      .map { it.value as T }
//  }

  suspend fun handle(peer: OscPeer, input: Input) {
    val address = input.readOscString()
    if (address == "[bundle]") { // TODO check with protocol
      throw UnsupportedOperationException("bundle still not implemented")
    }

    logger.trace {
      "OSC IN, peer: $peer -> $address"
    }

    val route = router.getRoute(address)

    if (route != null) {
      handleRoute(
        peer,
        address,
        input,
        route
      )
    } else {
      logger.debug {
        "Unmatched OSC message, peer: $peer, address: $address, typeTag: ${input.readOscString()}"
      }
    }
  }

  private suspend fun <T> handleRoute(
    peer: OscPeer,
    address: String,
    input: Input,
    route: Route<T>
  ) {
    val converter = route.converter
    input.discardUntilDelimiter(COMMA_BYTE)

    val typeTag = input.readOscString().removePrefix(",")
    if (converter.typeTag != null && converter.typeTag != typeTag) {
      logger.error {
        "Invalid typeTag, expected: ${converter.typeTag}, was: $typeTag"
      }
    } else {

      val reader = OscMessage.Reader(typeTag, input)
      val value = converter.decode(reader)
      val message = OscMessage(
        peer = peer,
        address = address,
        value = value
      )

      if (route.action != null) {
        val action = route.action
        action(message)
      }

      _messages.emit(message)
    }
  }

}

@OptIn(ExperimentalStdlibApi::class)
class OscOutput(
  private val peer: OscPeer,
  private val transport: Transport
) : AutoCloseable {

  private val logger = KotlinLogging.logger {}

  val router = OscRouter()

  inline fun <reified T> convert(
    converter: OscMessage.Converter<T>
  ) {
    router.convert(typeOf<T>(), converter)
  }

  inline fun <reified T> route(
    address: String,
    noinline addressMatcher: ((address: String) -> Boolean)? = null,
    converter: OscMessage.Converter<T>? = null,
  ) {
    router.route(
      typeOf<T>(),
      address,
      addressMatcher,
      converter,
      null
    )
  }

  fun sendBundle(packet: OscBundle) {

  }

  /**
   * @throws IllegalArgumentException if no converter was registered for the [address].
   */
  suspend fun send(address: String, value: Any) {

    logger.trace {
      "OSC OUT, peer: $peer -> $address"
    }

    val route = router.getRoute(address)

    if (route != null) {
      val converter = route.converter
      transport.send {
        writeOscString(address)
        if (converter.typeTag != null) {
          writeOscTypeTag(converter.typeTag)
          val writer = OscMessage.Writer(value, this)
          route.converter.encode(writer)
        }
      }
    } else {
      logger.warn {
        "Nor route defined for address: $address, cannot send message to $peer"
      }
    }
  }

  override fun close() {
    //clear all the lists
    TODO("Not yet implemented")
    // TODO do we need auto closeable here?
  }

  interface Transport {

    suspend fun send(block: (Output.() -> Unit))

  }

}

interface OscPacket {
  val peer: OscPeer
}

data class OscBundle(
  override val peer: OscPeer,
  val packets: List<OscPacket> // TODO list of packets or list of messages?
) : OscPacket

data class OscMessage<T>(
  override val peer: OscPeer,
  val address: String,
  val value: T,
) : OscPacket {

  class Reader(
    val typeTag: String,
    val input: Input
  ) {
    fun int(): Int = input.readInt()
    fun float(): Float = input.readFloat()
    fun double(): Double = input.readDouble()
    fun string(): String = input.readOscString()
  }

  class Writer<T>(
    val value: T,
    val output: Output
  ) {
    fun typeTag(tag: String) = output.writeOscTypeTag(tag)
    fun int(x: Int) = output.writeInt(x)
    fun float(x: Float) = output.writeFloat(x)
    fun double(x: Double) = output.writeDouble(x)
    fun string(x: String) = output.writeOscString(x)
  }

  class Converter<T>(
    val typeTag: String? = null,
    val decode: Reader.() -> T,
    val encode: Writer<T>.() -> Unit
  ) {

    class Error(message: String) : RuntimeException(message)

    companion object {

      val INT = Converter(
        typeTag = "i",
        decode = { int() },
        encode = { int(value) }
      )

      val FLOAT = Converter(
        typeTag = "f",
        decode = { float() },
        encode = { float(value) }
      )

      val DOUBLE = Converter(
        typeTag = "d",
        decode = { double() },
        encode = { double(value) }
      )

      val STRING = Converter(
        typeTag = "s",
        decode = { string() },
        encode = { string(value) }
      )

      val BOOLEAN = Converter(
        decode = {
          when (typeTag) {
            "T" -> true
            "F" -> false
            else -> throw IllegalStateException(
              "Invalid typeTag for boolean: $typeTag"
            )
          }
        },
        encode = {
          typeTag(
            if (value) "T"
            else "F"
          )
        }
      )

      val LIST_OF_INTS = listConverter(
        elementTypeTag = 'i',
        elementReader = { int() },
        elementWriter = { int(value) }
      )

      val LIST_OF_FLOATS = listConverter(
        elementTypeTag = 'f',
        elementReader = { float() },
        elementWriter = { float(value) }
      )

      val LIST_OF_DOUBLES = listConverter(
        elementTypeTag = 'd',
        elementReader = { float() },
        elementWriter = { float(value) }
      )

      val LIST_OF_STRINGS = listConverter(
        elementTypeTag = 's',
        elementReader = { string() },
        elementWriter = { string(value) }
      )

      private inline fun <reified T> listConverter(
        elementTypeTag: Char,
        crossinline elementReader: Reader.() -> T,
        crossinline elementWriter: Writer<T>.() -> Unit
      ): Converter<List<T>> {
        return Converter(
          decode = {
            if (typeTag.any { it != elementTypeTag }) {
              throw Error(
                "Cannot decode List<${typeOf<T>()}>, typeTag must consists " +
                    "of '$elementTypeTag' characters only, but was: $typeTag"
              )
            }
            typeTag.map {
              elementReader(this)
            }
          },
          encode = {
            typeTag(CharArray(value.size) { elementTypeTag }.concatToString())
            value.forEach {
              elementWriter(Writer(it, output))
            }
          }
        )
      }

    }

  }

  companion object {

    val DEFAULT_CONVERTERS = mapOf(
      typeOf<Int>() to Converter.INT,
      typeOf<Float>() to Converter.FLOAT,
      typeOf<Double>() to Converter.DOUBLE,
      typeOf<String>() to Converter.STRING,
      typeOf<Boolean>() to Converter.BOOLEAN,
      typeOf<List<Int>>() to Converter.LIST_OF_INTS,
      typeOf<List<Float>>() to Converter.LIST_OF_FLOATS,
      typeOf<List<Double>>() to Converter.LIST_OF_DOUBLES,
      typeOf<List<String>>() to Converter.LIST_OF_STRINGS
    )
  }

}

internal class Route<T>(
  val address: String,
  val addressMatcher: ((address: String) -> Boolean)?,
  val converter: OscMessage.Converter<T>,
  val action: ((OscMessage<T>) -> Unit)?
)
