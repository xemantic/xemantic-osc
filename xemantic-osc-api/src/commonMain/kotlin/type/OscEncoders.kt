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

import com.xemantic.osc.OscEncoder
import com.xemantic.osc.OscInputException
import com.xemantic.osc.typeTag
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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

private inline fun <reified T> listEncoder(
  elementTypeTag: Char,
  crossinline elementEncoder: OscEncoder<T>
): OscEncoder<List<T>> = { list ->
  typeTag(CharArray(list.size) { elementTypeTag }.concatToString())
  list.forEach { elementEncoder(this, it) }
}

//https://www.cnmat.berkeley.edu/sites/default/files/attachments/2015_Dynamic_Message_Oriented_Middleware.pdf
// TODO externalize all the default encoders
public val intEncoder: OscEncoder<Int> = { typeTag("i"); int(it) }
public val Int.oscEncoder: OscEncoder<Int> get() = intEncoder

public val longEncoder: OscEncoder<Long> = { typeTag("h"); long(it) }

public val midiMessageEncoder: OscEncoder<OscMidiMessage> = {  }

public val DEFAULT_OSC_ENCODERS: Map<KType, OscEncoder<*>> = oscEncoders {
  encoder<Int> { typeTag("i"); int(it) }
  encoder<Float> { typeTag("f"); float(it) }
  encoder<String> { typeTag("s"); string(it) }
  encoder<ByteArray> { typeTag("b"); blob(it) }
  encoder<Long> { typeTag("h"); long(it) }
  encoder<OscTimeTag> { typeTag("t"); timeTag(it) }
  encoder<Double> { typeTag("d"); double(it) }
  encoder<Char> { typeTag("c"); char(it) }
  encoder<Boolean> { typeTag(it.typeTag) }
  encoder<OscMidiMessage> { typeTag("m"); midiMessage(it) }
  encoder<OscColor> { typeTag("c"); color(it) }
  encoder<List<Int>>(listEncoder('i') { int(it) })
  encoder<List<Float>>(listEncoder('f') { float(it) })
  encoder<List<String>>(listEncoder('s') { string(it) })
  encoder<List<ByteArray>>(listEncoder('b') { blob(it) })
  encoder<List<Long>>(listEncoder('h') { long(it) })
  encoder<List<OscTimeTag>>(listEncoder('t') { timeTag(it) })
  encoder<List<Double>>(listEncoder('d') { double(it) })
  encoder<List<Char>>(listEncoder('c') { char(it) })
  encoder<List<Boolean>> { booleans ->
    typeTag(booleans.joinToString("") { it.typeTag })
  }
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T> defaultOscEncoder(): OscEncoder<T> =
  (DEFAULT_OSC_ENCODERS[typeOf<T>()]
    ?: throw IllegalArgumentException(
      "No encoder for specified type: ${typeOf<T>()}"
    )
  ) as OscEncoder<T>

internal val Boolean.typeTag: String get() = if (this) "T" else "F"


public val genericEncoder: OscEncoder<List<Any>> = { anys ->
  val typeTag = anys.map { any ->
    when (any) {
      is Int -> 'i'
      is Float -> 'f'
      is String -> 's'
      is ByteArray -> 'b'
      is Long -> 'h'
      is OscTimeTag -> 't'
      is Double -> 'd'
      is Char -> 'c'
      is Boolean -> any.typeTag[0]
      else -> throw OscInputException(
        "Unsupported type: ${any::class} in input list"
      )
    }
  }.toCharArray().concatToString()
  typeTag(typeTag)
  anys.forEach { any ->
    when (any) {
      is Int -> int(any)
      is Float -> float(any)
      is String -> string(any)
      is ByteArray -> blob(any)
      is Long -> long(any)
      is Double -> double(any)
      is Char -> char(any)
      is Boolean -> any.typeTag // This is arbitrary
      else -> throw OscInputException(
        "Unsupported type: ${any::class} in input list"
      )
    }
  }
}
