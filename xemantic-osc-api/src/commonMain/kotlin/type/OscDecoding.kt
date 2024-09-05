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

public val DEFAULT_OSC_DECODERS: Map<KType, OscDecoder<*>> = oscDecoders {
  decoder<Int>("i") { int() }
  decoder<Float>("f") { float() }
  decoder<String>("s") { string() }
  decoder<ByteArray>("b") { blob() }
  decoder<Boolean> { tag -> tag.toBooleanOscTypeTag() }
  decoder<OscImpulse>("I") { OscImpulse }
  decoder<OscTimeTag>("t") { timeTag() }
  decoder<Long>("h") { long() }
  decoder<Double>("d") { double() }
  decoder<Char>("c") { char() }
  decoder<OscColor>("r") { color() }
  decoder<OscMidiMessage>("m") { midiMessage() }
  decoder<List<*>> { tag -> readByTypeTag(tag.toCharArray()) }
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T> oscDecoder(): OscDecoder<T> =
  (DEFAULT_OSC_DECODERS[typeOf<T>()]
    ?: throw IllegalArgumentException(
      "No encoder for specified type: ${typeOf<T>()}"
    )
  ) as OscDecoder<T>

public fun oscDecoders(
  block: OscDecodersBuilder.() -> Unit
): Map<KType, OscDecoder<*>> =
  OscDecodersBuilder().apply(block).decoders

public class OscDecoder<T>(
  public val typeTag: String? = null,
  private val block: OscReader.(typeTag: String) -> T
) {

  public fun decode(reader: OscReader): T {
    val tag = reader.typeTag()
    if (typeTag != null && tag != typeTag) {
      throw OscInputException(
        "Expected typeTag: '$typeTag', but was: '$tag'"
      )
    }
    return block(reader, tag)
  }

}

public class OscDecodersBuilder {

  @PublishedApi
  internal val decoders: MutableMap<KType, OscDecoder<*>> = mutableMapOf()

  public inline fun <reified T> decoder(
    typeTag: String? = null,
    noinline decode: OscReader.(typeTag: String) -> T
  ) {
    decoders[typeOf<T>()] = OscDecoder(typeTag, decode)
  }

}

private fun OscReader.readByTypeTag(
  typeTag: CharArray,
  startIndex: Int = 0,
  endIndex: Int = typeTag.size
): List<*> = buildList {
  var index = startIndex

  while (index < endIndex) {
    val typeTagChar = typeTag[index]
    if (typeTagChar != '[') {
      // Normal type tag, decode as a single value
      add(readByTypeTag(typeTagChar))
    } else {
      // Found an opening bracket '[', find the corresponding closing bracket ']'
      val subEnd = findClosingBracketIndex(typeTag, index)

      // Recursively read the nested list inside the brackets
      add(readByTypeTag(typeTag, index + 1, subEnd))

      // Move the index to the character after the closing ']'
      index = subEnd
    }
    index++
  }
}

private fun findClosingBracketIndex(
  typeTag: CharArray,
  startIndex: Int
): Int {
  var openBracketCount = 1
  for (index in (startIndex + 1) until typeTag.size) {
    when (typeTag[index]) {
      '[' -> openBracketCount++
      ']' -> openBracketCount--
    }
    if (openBracketCount == 0) return index
  }
  throw OscInputException(
    "Mismatched brackets in OSC type tag: ${typeTag.joinToString()}"
  )
}

private fun OscReader.readByTypeTag(
  typeTag: Char
): Any? = when (typeTag) {
  'i' -> int()
  'f' -> float()
  's' -> string()
  'b' -> blob()
  'T' -> true
  'F' -> false
  'N' -> null
  'I' -> OscImpulse
  't' -> timeTag()
  'h' -> long()
  'd' -> double()
  'c' -> char()
  'r' -> color()
  'm' -> midiMessage()
  else -> throw OscInputException(
    "Unsupported OSC type tag: $typeTag"
  )
}
