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

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface Osc : Closeable {

  abstract class ConversionBuilder {

    abstract val establishedConverters: Map<KType, Osc.Converter<*>>

    inline fun <reified T> conversion(address: String) {
      conversion(address, typeOf<T>())
    }

    fun conversion(address: String, type: KType) {
      _conversions[address] = requireNotNull(establishedConverters[type]) {
        "No converter for type: $type"
      }
    }

    val conversions: Map<String, Osc.Converter<*>>
      get() = _conversions

    private val _conversions = mutableMapOf<String, Osc.Converter<*>>()

  }

  class Builder : ConversionBuilder() {

    var hostname: String = "0.0.0.0"

    var port: Int = 0

    var converters: Map<KType, Converter<*>> = converters {

      convert<Float>(
        encode = { x, output ->
          output.writeTypeTag("f")
          output.writeFloat(x)
        },
        decode = { _, input ->
          input.readFloat()
        }
      )

      convert<Boolean>(
        encode = { x, output ->
          output.writeText(if (x) "T" else "F")
        },
        decode = { tag, _ ->
          when (tag) {
            "T" -> true
            "F" -> false
            else -> throw IllegalStateException("Invalid typeTag for boolean: $tag")
          }
        }
      )

      convert<Double>(
        encode = { x, output ->
          output.writeTypeTag("f")
          output.writeFloat(x.toFloat())
        },
        decode = { _, input ->
          input.readFloat().toDouble()
        }
      )

      convert<Int>(
        encode = { x, output ->
          output.writeTypeTag("i")
          output.writeInt(x)
        },
        decode = { _, input ->
          input.readInt()
        }
      )

      convert<String>(
        encode = { x, output ->
          output.writeTypeTag("s")
          output.writeText(x)
          val padding = 4 - ((x.length) % 4)
          output.writeZeros(padding)
        },
        decode = { _, input ->
          input.readText()
        }
      )

    }

    override val establishedConverters: Map<KType, Converter<*>>
      get() = converters

  }

  val hostname: String

  val port: Int

  val messageFlow: Flow<Message<*>>

  fun <T> valueFlow(address: String): Flow<T>

  interface Output : Closeable {

    class Builder(
      converters: Map<KType, Converter<*>>
    ) : ConversionBuilder() {

      var hostname: String = "localhost"

      var port: Int = 0

      override val establishedConverters: Map<KType, Converter<*>> = converters

    }

    val hostname: String

    val port: Int

    suspend fun send(packet: Packet)

    /**
     * @throws IllegalArgumentException if no converter was registered for the [address].
     */
    suspend fun <T> send(address: String, value: T)

  }

  class Converter<T>(
    val encode: (x: T, output: io.ktor.utils.io.core.Output) -> Unit,
    val decode: (tag: String, input: Input) -> T
  )

  interface Packet

  data class Message<T>(
    val address: String,
    val value: T,
    val hostname: String,
    val port: Int
  ) : Packet

  data class Bundle(
    val packets: List<Packet>
  ) : Packet

  fun output(build: Output.Builder.() -> Unit): Output

  val outputs: List<Output>

}

fun osc(
  build: Osc.Builder.() -> Unit
): Osc = UdpOsc(
  Osc.Builder().also {
    build(it)
  }
)

fun converters(
  block: ConvertersBuilder.() -> Unit
): Map<KType, Osc.Converter<*>> {
  val builder = ConvertersBuilder()
  block(builder)
  return builder.converters.toMap()
}

class ConvertersBuilder {

  internal val converters = mutableListOf<Pair<KType, Osc.Converter<*>>>()

  inline fun <reified T> convert(
    noinline encode: (x: T, output: io.ktor.utils.io.core.Output) -> Unit,
    noinline decode: (tag: String, input: Input) -> T
  ) {
    convert(typeOf<T>(), encode, decode)
  }

  fun <T> convert(
    type: KType,
    encode: (x: T, output: io.ktor.utils.io.core.Output) -> Unit,
    decode: (tag: String, input: io.ktor.utils.io.core.Input) -> T
  ) {
    converters.add(
      Pair(type, Osc.Converter(encode, decode))
    )
  }

}

const val COMMA_BYTE = ','.code.toByte()

fun Output.writeTypeTag(tag: String) {
  writeByte(COMMA_BYTE)
  writeText(tag)
  val padding = 4 - ((tag.length + 1) % 4)
  writeZeros(count = padding)
}

internal fun Output.writeZeros(
  count: Int
) = fill(times = count.toLong(), 0)