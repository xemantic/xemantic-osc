/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2024 Kazimierz Pogoda
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

package com.xemantic.osc.type

import com.xemantic.osc.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public class OscDecodersBuilder {
  @PublishedApi
  internal val decoders: MutableMap<KType, OscDecoder<*>> = mutableMapOf()
  public inline fun <reified T> decoder(
    typeTag: String? = null,
    noinline decode: OscReader.() -> T
  ) {
    decoders[typeOf<T>()] = OscDecoder(typeTag, decode)
  }
}

public fun oscDecoders(
  block: OscDecodersBuilder.() -> Unit
): Map<KType, OscDecoder<*>> =
  OscDecodersBuilder().apply(block).decoders

public inline fun <reified T> OscReader.listOf(
  elementTypeTag: Char,
  crossinline elementDecoder: OscReader.() -> T
): List<T> {
  val typeTag = typeTag()
  if (typeTag.any { it != elementTypeTag }) {
    throw OscInputException(
      "Cannot decode List<${typeOf<T>()}>, typeTag must consists " +
          "of '$elementTypeTag' characters only, but was: $typeTag"
    )
  }
  return typeTag.map { elementDecoder(this) }.toList()
}

public val DEFAULT_OSC_DECODERS: Map<KType, OscDecoder<*>> = oscDecoders {
  decoder<Int>("i") { int() }
  decoder<Float>("f") { float() }
  decoder<String>("s") { string() }
  decoder<ByteArray>("b") { blob() }
  decoder<Long>("h") { long() }
  decoder<OscTimeTag>("t") { timeTag() }
  decoder<Double>("d") { double() }
  decoder<Char>("c") { char() }
  decoder<Boolean> { typeTagToBoolean(typeTag()[0]) }
  decoder<List<Int>> { listOf('i') { int() } }
  decoder<List<Float>> { listOf('f') { float() } }
  decoder<List<String>> { listOf('s') { string() } }
  decoder<List<ByteArray>> { listOf('b') { blob() } }
  decoder<List<Long>> { listOf('h') { long() } }
  decoder<List<OscTimeTag>> { listOf('t') { timeTag() } }
  decoder<List<Double>> { listOf('d') { double() } }
  decoder<List<Char>> { listOf('c') { char() } }
  decoder<List<Boolean>> { typeTag().map { typeTagToBoolean(it) } }
  decoder<List<Any>> { readByTypeTag(typeTag().toList()) }
}

private fun OscReader.readByTypeTag(
  typeTag: List<Char>
): List<Any> {
  var nextIndex = 0
  return typeTag.mapIndexedNotNull { index, char ->
    if (index >= nextIndex) {
      if (char == '[') {
        nextIndex = index + typeTag.drop(index).indexOf(']') + 1
        readByTypeTag(
          typeTag.slice((index + 1)..<nextIndex - 1)
        )
      } else {
        readByTypeTag(char)
      }
    } else {
      null
    }
  }
}

public fun OscReader.readByTypeTag(typeTag: Char): Any = when (typeTag) {
  'i' -> int()
  'f' -> float()
  's' -> string()
  'b' -> blob()
  'h' -> long()
  't' -> timeTag()
  'd' -> double()
  'c' -> char()
  'm' -> midiMessage()
  'r' -> color()
  'T' -> true
  'F' -> false
  else -> throw OscInputException(
    "Unsupported OSC type tag: $typeTag"
  )
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T> defaultOscDecoder(): OscDecoder<T> =
  (DEFAULT_OSC_DECODERS[typeOf<T>()]
    ?: throw IllegalArgumentException(
      "No encoder for specified type: ${typeOf<T>()}"
    )
  ) as OscDecoder<T>

private fun typeTagToBoolean(
  typeTag: Char
): Boolean = when (typeTag) {
  'T' -> true
  'F' -> false
  else -> throw OscInputException(
    "Invalid typeTag for Boolean: $typeTag"
  )
}

