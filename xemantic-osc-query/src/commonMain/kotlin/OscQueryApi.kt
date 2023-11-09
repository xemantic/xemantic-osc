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

package com.xemantic.osc.query

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OscNode<T>(
  @SerialName("DESCRIPTION")
  val description: String,
  @SerialName("FULL_PATH")
  val fullPath: String,
  @SerialName("ACCESS")
  val access: String,
  @SerialName("CONTENTS")
  val contents: Map<String, OscNode<T>>?
//  @SerialName("VALUE")
//  val value: List<Any?>?,
//  @SerialName("RANGE")
//  val range: ClosedRange<T>
)

public data class OscValueRange(val min: Any?, val max: Any?)

//public fun <T> OscInput.Route<T>.publish(
//  note: OscNode<T>
//): OscInput.Route<T> {
//  return this
//}
