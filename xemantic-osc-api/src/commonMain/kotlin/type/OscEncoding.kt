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

import com.xemantic.osc.OscException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public typealias OscEncoder<T> = OscWriter.(value: T) -> Unit

public inline fun <reified T> oscEncoder(): OscEncoder<T> =
  DEFAULT_OSC_ENCODERS.resolve(typeOf<T>())

public val DEFAULT_OSC_ENCODERS: Map<KType, OscEncoder<*>> = oscEncoders {
  encoder<Int> { typeTag("i"); int(it) }
  encoder<Float> { typeTag("f"); float(it) }
  encoder<String> { typeTag("s"); string(it) }
  encoder<ByteArray> { typeTag("b"); blob(it) }
  encoder<Boolean> { typeTag(it.oscTypeTag) }
  encoder<OscImpulse> { typeTag("I") }
  encoder<OscTimeTag> { typeTag("t"); timeTag(it) }
  encoder<Long> { typeTag("h"); long(it) }
  encoder<Double> { typeTag("d"); double(it) }
  encoder<Char> { typeTag("c"); char(it) }
  encoder<OscColor> { typeTag("r"); color(it) }
  encoder<OscMidiMessage> { typeTag("m"); midiMessage(it) }
  encoder<List<*>> { typeTag(it.oscTypeTags()); writeList(it) }
}

public class OscEncodersBuilder {
  @PublishedApi
  internal val encoders: MutableMap<KType, OscEncoder<*>> = mutableMapOf()
  public inline fun <reified T> encoder(
    noinline encoder: OscEncoder<T>
  ) {
    encoders[typeOf<T>()] = encoder
  }
}

public fun oscEncoders(
  block: OscEncodersBuilder.() -> Unit
): Map<KType, OscEncoder<*>> =
  OscEncodersBuilder().apply(block).encoders

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T> Map<KType, OscEncoder<*>>.resolve(
  type: KType,
): OscEncoder<T> = (this[type] ?: throw IllegalArgumentException(
  "No OscEncoder for type: $type"
)) as OscEncoder<T>

private fun OscWriter.writeList(list: List<*>) {
  list
    .filter { it != null && it !is Boolean }
    .forEach {
      when (it) {
        is Int -> int(it)
        is Float -> float(it)
        is String -> string(it)
        is ByteArray -> blob(it)
        is Long -> long(it)
        is Double -> double(it)
        is Char -> char(it)
        is OscTimeTag -> timeTag(it)
        is OscMidiMessage -> midiMessage(it)
        is OscColor -> color(it)
        is List<*> -> writeList(it)
        else -> throw OscException(
          "Unsupported type: ${it!!::class} in input list"
        )
      }
    }
}
