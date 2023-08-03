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

import com.xemantic.osc.protocol.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Represents the side of OSC communication, either the
 * sender or the receiver.
 *
 * Note: "OSC Peer" is not a part of the OSC protocol specification.
 * It is introduced in this API to provide more context regarding received
 * OSC messages to allow logic based on this information.
 */
public data class OscPeer(
  val hostname: String,
  val port: Int,
  /**
   * The transport protocol used by the OSC Peer to send an
   * [OscMessage]. For example `udp`, `tcp`, `websocket`.
   */
  val transport: String
)

/**
 * A marker interface describing OSC Packet.
 * Only 2 classes are derived from [OscPacket]:
 *
 * * [OscMessage]
 * * [OscBundle]
 *
 * @see OscMessage
 * @see OscBundle
 */
public interface OscPacket {

  /**
   * The sending peer.
   */
  public val peer: OscPeer

}

/**
 * Osc Bundle as defined in the protocol.
 */
public data class OscBundle(

  /**
   * The sending peer.
   */
  override val peer: OscPeer,

  /**
   * The OSC
   */
  val timeTag: OscTimeTag,

  /**
   * The list of _OSC Packets_.
   */
  val packets: List<OscPacket>
) : OscPacket {

//  public interface Builder {
//
//    public fun send(address: String, value: Any?)
//
//    public fun sendBundle(
//      timeTag: OscTimeTag = OscTimeTag.now(),
//      block: Builder.() -> Unit
//    )
//
//  }

}

public data class OscMessage<T>(

  /**
   * The sending peer.
   */
  override val peer: OscPeer,


  val address: String,
  val value: T,
) : OscPacket

@OptIn(ExperimentalStdlibApi::class)
public interface OscInput : AutoCloseable {

  public val decoders: MutableMap<KType, OscDecoder<*>>

  public fun <T> route(
    type: KType,
    address: String,
    matchAddress: AddressMatcher? = null,
    decoder: OscDecoder<T>? = null,
    action: OscAction<T>? = null
  ): Route<T>

  /**
   * Unroutes the [addresses].
   *
   * @throws IllegalArgumentException if no such address
   *           has been routed before.
   */
  public fun unroute(vararg addresses: String)

  public suspend fun handle(peer: OscPeer, input: Input)

  public val messages: Flow<OscMessage<*>>

  /**
   * Whether OSC Bundle should be discarded based on receiving
   * [OscBundle.timeTag] in the past.
   *
   * It defaults to `false` - late bundles are not discarded, but
   * handled immediately.
   *
   * This setting will work well only if the clocks of sending
   * and receiving computers are in sync, which might be
   * not the case. To control acceptable "late" means, an additional
   * [acceptableLateness] parameter can be used.
   *
   * @see OscBundle.timeTag
   * @see acceptableLateness
   */
  public var discardLateBundles: Boolean

  /**
   * An amount of time
   */
  // TODO maybe it should be rather Int?
  public var acceptableLateness: Long

  public interface Route<T> {

    public fun value(value: T): OscValue<T>

    public val messages: Flow<OscMessage<T>>

    public val values: Flow<T>

    // TODO add metadata support
    //public fun publish(): OscInputRoute<T>

  }

}

public inline fun <reified T> OscInput.route(
  address: String,
  noinline matchAddress: AddressMatcher? = null,
  noinline decoder: OscDecoder<T>? = null,
  noinline action: OscAction<T>? = null
): OscInput.Route<T> = route(
  typeOf<T>(),
  address,
  matchAddress,
  decoder,
  action
)

//public inline fun <reified T> OscInput.value(
//  address: String,
//  noinline addressMatcher: AddressMatcher? = null,
//  noinline decoder: OscDecoder<T>? = null,
//  initialValue: T
//): OscValue<T> {
//  var _value: T = initialValue
//  val value = object : OscValue<T> {
//    override val value: T get() = _value
//  }
//  route(
//    typeOf<T>(),
//    address,
//    addressMatcher,
//    decoder
//  ) {
//    _value = it.value
//  }
//  return value
//}

/**
 * Creates [OscInput] instance.
 *
 * Usage:
 * ```
 * val input = OscInput {
 *   route<String>("/ping") {
 *     println("$it")
 *   }
 * }
 * ```
 *
 * @param block a convenience lambda to configure
 *          newly created `OscInput`.
 */
public fun OscInput(
  dispatcher: CoroutineContext = platformDispatcher,
  block: OscInput.() -> Unit = {}
): OscInput = DefaultOscInput(dispatcher, block)

@OptIn(ExperimentalStdlibApi::class)
public interface OscOutput : AutoCloseable {

  public val peer: OscPeer

  public val encoders: MutableMap<KType, OscEncoder<*>>

  public fun <T> route(
    type: KType,
    address: String,
    matchAddress: AddressMatcher? = null,
    encoder: OscEncoder<T>? = null
  )

  /**
   * Unroutes the [addresses].
   *
   * @throws IllegalArgumentException if no such address
   *           has been routed before.
   */
  public fun unroute(vararg addresses: String)

  public fun send(address: String, value: Any?)

//  public fun sendBundle(
//    timeTag: OscTimeTag = OscTimeTag.now(),
//    block: OscBundle.Builder.() -> Unit
//  )

  /**
   * @throws IllegalArgumentException if no route was registered for the [address].
   */
  public suspend fun suspendedSend(address: String, value: Any?)

//  /**
//   * @param timeTag the _OSC time tag_.
//   * @param block the builder of _OSC Bundle_.
//   * @throws IllegalArgumentException if no converter was registered
//   *          for one of the addresses provided to the builder.
//   */
//  public suspend fun suspendedSendBundle(
//    timeTag: OscTimeTag = OscTimeTag.now(),
//    block: OscBundle.Builder.() -> Unit
//  )

}

public inline fun <reified T> OscOutput.route(
  address: String,
  noinline addressMatcher: AddressMatcher? = null,
  noinline encoder: OscEncoder<T>? = null
) {
  route(
    typeOf<T>(),
    address,
    addressMatcher,
    encoder
  )
}

public fun OscOutput(
  peer: OscPeer,
  sender: OscTransport.Sender,
  dispatcher: CoroutineContext
): OscOutput = DefaultOscOutput(
  peer,
  sender,
  dispatcher
)

@OptIn(ExperimentalStdlibApi::class)
public interface OscTransport : AutoCloseable {

  public val type: String

  public val peer: OscPeer

  // TODO does it need to be public?
  public val dispatcher: CoroutineContext

  public var input: OscInput?

  public fun output(
    peer: OscPeer,
    dispatcher: CoroutineContext = this.dispatcher,
    block: (OscOutput.() -> Unit) = {}
  ): OscOutput

  public fun output(
    hostname: String,
    port: Int,
    dispatcher: CoroutineContext = this.dispatcher,
    block: (OscOutput.() -> Unit) = {}
  ): OscOutput = output(
    OscPeer(
      hostname = hostname,
      port = port,
      transport = type
    ),
    dispatcher,
    block
  )

  /**
   * A special version of the [output] function useful in testing,
   * where output is pointing to the peer of supplied [OscTransport]
   * holding an [OscInput] instance.
   *
   * Usage:
   * ```
   * val input = oscInput {
   *   // ... configuration
   * }
   * val receivingTransport = UdpOscTransport(input)
   * val sendingTransport = UdpOscTransport()
   * val output = sendingOscTransport.output(receivingTransport) {
   *   route<Int>("/foo") { println("received $it") }
   * }
   * output.send("/foo", 42)
   * ```
   * @param transport the to direct output to.
   * @param block the [OscOutput] configuration block.
   */
  public fun output(
    transport: OscTransport,
    dispatcher: CoroutineContext = this.dispatcher,
    block: (OscOutput.() -> Unit) = {}
  ): OscOutput = output(
    transport.peer,
    dispatcher,
    block
  )

  public interface Sender {

    public suspend fun send(block: (Output.() -> Unit))

  }

}

public typealias OscDecoder<T> = OscReader.() -> T

public typealias OscEncoder<T> = OscWriter.(value: T) -> Unit

public typealias AddressMatcher = (String) -> Boolean

public typealias OscAction<T> = (suspend (value: T) -> Unit)

public open class OscException(message: String) : RuntimeException(message)

public class OscInputException(message: String) : OscException(message)

public class OscOutputException(message: String) : OscException(message)

public expect val platformDispatcher: CoroutineContext

public interface OscValue<T> {
  public val value: T
}
