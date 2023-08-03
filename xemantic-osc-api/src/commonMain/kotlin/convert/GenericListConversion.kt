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
import com.xemantic.osc.OscEncoder
import com.xemantic.osc.OscInputException
import com.xemantic.osc.protocol.OscTimeTag
import io.ktor.utils.io.charsets.*

public fun genericDecoder(
  charset: Charset = Charsets.UTF_8
): OscDecoder<List<Any>> = {
  val typeTag = typeTag()
  val list = mutableListOf<Any>()
  typeTag.forEach { char ->
    list.add(
      when (char) {
        'i' -> int()
        'f' -> float()
        's' -> string(charset)
        'b' -> blob()
        'h' -> long()
        't' -> timesTag()
        'd' -> double()
        'c' -> char
        'T' -> true
        'F' -> false
        else -> throw OscInputException(
          "Unsupported char: '$char' in OSC type tag: $typeTag"
        )
      }
    )
  }
  list
}

public fun genericEncoder(
  charset: Charset = Charsets.UTF_8
): OscEncoder<List<Any>> = { anys ->
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
      is String -> string(any, charset)
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
