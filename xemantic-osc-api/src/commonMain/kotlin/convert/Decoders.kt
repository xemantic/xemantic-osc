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

package com.xemantic.osc.convert

import com.xemantic.osc.OscDecoder
import com.xemantic.osc.OscInputException
import com.xemantic.osc.protocol.OscTimeTag
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public class OscDecodersBuilder {
  @PublishedApi
  internal val decoders: MutableMap<KType, OscDecoder<*>> = mutableMapOf()
  public inline fun <reified T> decoder(
    noinline decoder: OscDecoder<T>
  ) {
    decoders[typeOf<T>()] = decoder
  }
}

public fun oscDecoders(
  block: OscDecodersBuilder.() -> Unit
): Map<KType, OscDecoder<*>> =
  OscDecodersBuilder().apply(block).decoders

private inline fun <reified T> listDecoder(
  elementTypeTag: Char,
  crossinline elementDecoder: OscDecoder<T>
): OscDecoder<List<T>> = {
  val typeTag = typeTag()
  if (typeTag.any { it != elementTypeTag }) {
    throw OscInputException(
      "Cannot decode List<${typeOf<T>()}>, typeTag must consists " +
          "of '$elementTypeTag' characters only, but was: $typeTag"
    )
  }
  typeTag.map {
    elementDecoder(this)
  }
}

public val DEFAULT_OSC_DECODERS: Map<KType, OscDecoder<*>> = oscDecoders {
  decoder<Int> { assertTypeTag("i"); int() }
  decoder<Float> { assertTypeTag("f"); float() }
  decoder<String> { assertTypeTag("s"); string() }
  decoder<ByteArray> { assertTypeTag("b"); blob() }
  decoder<Long> { assertTypeTag("h"); long() }
  decoder<OscTimeTag> { assertTypeTag("t"); timesTag() }
  decoder<Double> { assertTypeTag("d"); double() }
  decoder<Char> { assertTypeTag("c"); char() }
  decoder<Boolean> { typeTagToBoolean(typeTag()[0]) }
  decoder(listDecoder<Int>('i') { int() })
  decoder(listDecoder<Float>('f') { float() })
  decoder(listDecoder<String>('s') { string() })
  decoder(listDecoder<ByteArray>('b') { blob() })
  decoder(listDecoder<Long>('h') { long() })
  decoder(listDecoder<OscTimeTag>('t') { timesTag() })
  decoder(listDecoder<Double>('d') { double() })
  decoder(listDecoder<Char>('c') { char() })
  decoder<List<Boolean>> { typeTag().map { typeTagToBoolean(it) } }
}

private fun typeTagToBoolean(
  typeTag: Char
): Boolean = when (typeTag) {
  'T' -> true
  'F' -> false
  else -> throw OscInputException(
    "Invalid typeTag for Boolean: $typeTag"
  )
}
