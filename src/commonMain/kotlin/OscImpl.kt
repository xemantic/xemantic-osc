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

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

internal class UdpOsc(builder: Osc.Builder) : Osc {

  private val logger = KotlinLogging.logger {}

  private val converters = builder.converters
  private val conversions = builder.buildConversions(converters)

  private val selectorManager = SelectorManager(Dispatchers.IO)
  private val socket = aSocket(selectorManager).udp().bind(
    InetSocketAddress(builder.hostname, builder.port)
  )

  override val hostname = (socket.localAddress as InetSocketAddress).hostname
  override val port = (socket.localAddress as InetSocketAddress).port

  private val _outputs = mutableListOf<Osc.Output>()

  // defensive copy prevents ConcurrentModificationException
  override val outputs: List<Osc.Output> get() = _outputs.toList()

  init {
    logger.info { "OSC port open: udp://${socket.localAddress}" }
  }

  override val messageFlow: Flow<Osc.Message<*>> = flow {
    logger.info {
      "Flowing OSC messages sent to udp://${socket.localAddress}"
    }
    while (true) {
      val input = socket.incoming.receive()
      // client address
      val remoteAddress = input.address as InetSocketAddress
      val message = input.packet.use { packet ->

        val address = packet.readOscString()

        val maybeConverter = conversions[address]
        if (maybeConverter == null) {
          logger.error { "No conversion registered for address: $address" }
          null
        } else {
          @Suppress("UNCHECKED_CAST")
          val converter = maybeConverter as Osc.Converter<Any>

          packet.discardUntilDelimiter(COMMA_BYTE)

          val typeTag = packet.readOscString()
          val padding = 4 - ((typeTag.length) % 4)
          packet.discard(padding)

          val value = converter.decode(typeTag, input.packet)

          logger.debug {
            "OSC Message IN: udp://$remoteAddress -> $address=$value"
          }

          Osc.Message(
            address = address,
            value = value,
            hostname = remoteAddress.hostname,
            port = remoteAddress.port
          )

        }

      }

      // emitting message only after closing the packet
      if (message != null) {
        emit(message)
      }

    }

  }

  override fun <V> valueFlow(address: String): Flow<V> = messageFlow
    .filter { it.address.startsWith(address) }
    .map {
      @Suppress("UNCHECKED_CAST")
      it.value as V
    }

  override fun output(
    build: Osc.Output.Builder.() -> Unit
  ): Osc.Output {
    val builder = Osc.Output.Builder()
    build(builder)
    return UdpOscOutput(
      builder.hostname,
      builder.port,
      builder.buildConversions(converters)
    )
  }

  override fun close() {
    outputs.forEach {
      it.close()
    }
    socket.close()
  }

  override fun toString() = "Osc[upd:${socket.localAddress}]"

  inner class UdpOscOutput(
    hostname: String,
    port: Int,
    private val conversions: Map<String, Osc.Converter<*>>
  ) : Osc.Output {

    private val socketAddress = InetSocketAddress(hostname, port)
    override val hostname = socketAddress.hostname
    override val port = socketAddress.port

    override suspend fun send(packet: Osc.Packet) {
      // it should be relatively easy to add
      // only OSC time tag and scheduling require some analysis
      throw NotImplementedError("OSC Packet is not supported")
    }

    override suspend fun <T> send(address: String, value: T) {
      @Suppress("UNCHECKED_CAST")
      val converter = requireNotNull(conversions[address]) {
        "No converter registered for address: $address"
      } as Osc.Converter<T>
      logger.debug {
        "OSC Message OUT: udp://$socketAddress$address=$value"
      }
      val packet = buildPacket {
        writeText(address)
        val padding = 4 - (address.length % 4)
        writeZeros(count = padding)
        converter.encode(value, this)
      }
      socket.send(Datagram(packet, socketAddress))
    }

    override fun close() {
      _outputs.remove(this)
    }

    override fun toString() = "Osc.Output[upd:${socket.localAddress}->udp:$socketAddress]"

  }

}
