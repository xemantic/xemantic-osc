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

import com.xemantic.osc.OscOutput
import com.xemantic.osc.OscPeer
import com.xemantic.osc.OscInput
import com.xemantic.osc.OscTransport
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import mu.KotlinLogging

class UdpOscTransport(
  override val input: OscInput = OscInput(),
  dispatcher: CoroutineDispatcher,
  localAddress: InetSocketAddress? = null,
  configure: SocketOptions.UDPSocketOptions.() -> Unit = {}
) : OscTransport {

  constructor(
    input: OscInput  = OscInput(),
    dispatcher: CoroutineDispatcher,
    host: String = "0.0.0.0", // TODO is is something like default socket when null?
    port: Int = 0,
    configure: SocketOptions.UDPSocketOptions.() -> Unit = {}
  ) : this(
    input,
    dispatcher,
    InetSocketAddress(host, port),
    configure
  )

  override val type: String = "udp"

  private val logger = KotlinLogging.logger {}

  private val selectorManager: SelectorManager = SelectorManager(dispatcher)

  private val socket: BoundDatagramSocket = aSocket(
    selectorManager
  ).udp().bind(localAddress, configure)

  override val peer: OscPeer = OscPeer(
    (socket.localAddress as InetSocketAddress).hostname,
    (socket.localAddress as InetSocketAddress).port,
    transport = "udp"
  )

  override suspend fun start() {
    logger.info { "Starting OSC: $peer" }
    try {
      while (currentCoroutineContext().isActive) {
        val datagram = socket.incoming.receive()
        datagram.packet.use { packet ->
          input.handle(peer, packet)
        }
      }
    } catch (e: CancellationException) {
      logger.info { "OSC coroutine cancelled: $peer" }
    } finally {
      selectorManager.close()
      socket.close()
    }

  }

  override fun output(
    peer: OscPeer,
    block: (OscOutput.() -> Unit)
  ): OscOutput {
    val remoteAddress = InetSocketAddress(
      hostname = peer.hostname,
      port = peer.port
    )
    val output = OscOutput(
      peer = peer,
      sender = UdpOscTransportSender(remoteAddress)
    )
    block(output)
    return output
  }

  inner class UdpOscTransportSender(
    private val remoteAddress: SocketAddress
  ) : OscTransport.Sender {

    override suspend fun send(block: Output.() -> Unit) {
      socket.send(
        Datagram(
          buildPacket {
            block(this)
          },
          remoteAddress
        )
      )
    }

  }

}

//  // TODO it should be in common
//  private val outputEventFlow = MutableSharedFlow<OutputEvent>(
//    extraBufferCapacity = 1000 // this value is arbitrary
//  )



//  private val _outputs = mutableListOf<Osc.Output>()
//
//  // defensive copy prevents ConcurrentModificationException
//  override val outputs: List<Osc.Output> get() = _outputs.toList()
//
//  private val senderJob = coroutineScope.launch {
//    outputEventFlow.collect { event ->
//      try {
//
//        logger.trace {
//          "OSC Message OUT: udp:${event.socketAddress}${event.address}=${event.value}"
//        }
//
//        val converter = event.converter
//
//        socket.send(
//          Datagram(
//            buildPacket {
//              val writer = Osc.Message.Writer(event.value, this)
//              writer.string(event.address)
//              if (converter.typeTag != null) {
//                writer.typeTag(converter.typeTag)
//              }
//              converter.encode(writer)
//            },
//            event.socketAddress
//          )
//        )
//      } catch (e : Exception) {
//        logger.error(e) {
//          "Could not send OSC packet"
//        }
//      }
//    }
//  }
//
//  init {
//    logger.info { "OSC port open: udp:${socket.localAddress}" }
//  }
//
//  override val messageFlow = flow {
//    logger.info {
//      "Flowing OSC messages sent to udp:${socket.localAddress}"
//    }
//
//    }
//  }.shareIn(
//    coroutineScope,
//    SharingStarted.WhileSubscribed()
//  )
//
//  override fun <V> valueFlow(address: String) =
//    messageFlow
//      .filter { it.address.startsWith(address) }
//      .map {
//        @Suppress("UNCHECKED_CAST")
//        it.value as V
//      }
//
//  // inline see buildPackage
//  override fun output(
//    build: Osc.Output.Builder.() -> Unit
//  ): Osc.Output {
////    contract {
////      callsInPlace(block, InvocationKind.EXACTLY_ONCE)
////    }
//    val builder = Osc.Output.Builder()
//    build(builder)
//    return UdpOscOutput(builder)
//  }
//
//  fun close() {
//    senderJob.cancel()
//    outputs.forEach {
//      it.close()
//    }
//    coroutineScope.cancel()
//    socket.close()
//  }
//
//  override fun toString() = "UdpOsc[${socket.localAddress}]"
//
//  inner class UdpOscOutput(
//    builder: Osc.Output.Builder
//  ) : Osc.Output {
//
//    private val logger = KotlinLogging.logger(
//      UdpOscOutput::class.qualifiedName!!
//    )
//
//    private val socketAddress = InetSocketAddress(
//      builder.hostname,
//      builder.port
//    )
//
//    override val hostname = socketAddress.hostname
//    override val port = socketAddress.port
//
//    init {
//      logger.logConverters("OUTPUT", builder.converterMap)
//    }
//    private val conversionMap = builder.conversions.toMap()
//
//    fun send(packet: Osc.Packet) {
//      // it should be relatively easy to add
//      // only OSC time tag and scheduling require some analysis
//      throw NotImplementedError("OSC Packet is not supported")
//    }
//
//    fun <T> send(address: String, value: T) {
//      val converter = conversionMap[address]
//        ?: throw IllegalArgumentException(
//          "No Osc.Message.Converter for address: $address"
//        )
//      outputEventFlow.tryEmit(
//        OutputEvent(
//          converter = converter as Osc.Message.Converter<Any>,
//          socketAddress = socketAddress,
//          address = address,
//          value = value as Any
//        )
//      )
//    }
//
//    override fun close() {
//      _outputs.remove(this)
//    }
//
//    override fun toString() =
//      "Osc.Output[upd:${socket.localAddress}->udp:$socketAddress]"
//
//  }
//
//}
//
//fun KLogger.logConverters(
//  category: String,
//  converters: Map<KType, Osc.Message.Converter<*>>
//) {
//  //if (isDebugEnabled) {
//  debug { "$category converters registered for types:" }
//  converters.forEach {
//    debug { "  * ${it.key}" }
//  }
//  //}
//}
//
//private class OutputEvent(
//  val value: Any,
//  val address: String,
//  val socketAddress: SocketAddress,
//  val converter: Osc.Message.Converter<Any>,
//)

//expect fun platformUdpOscTransport(): UdpOscTransport
