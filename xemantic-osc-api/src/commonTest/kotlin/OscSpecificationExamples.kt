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

package com.xemantic.osc

/**
 * See [Osc Message Examples](https://opensoundcontrol.stanford.edu/spec-1_0-examples.html).
 */
val oscillator4FrequencyBytes = ubyteArrayOf(
  0x2fu, 0x6fu, 0x73u, 0x63u,
  0x69u, 0x6cu, 0x6cu, 0x61u,
  0x74u, 0x6fu, 0x72u, 0x2fu,
  0x34u, 0x2fu, 0x66u, 0x72u,
  0x65u, 0x71u, 0x75u, 0x65u,
  0x6eu, 0x63u, 0x79u, 0x00u,
  0x2cu, 0x66u, 0x00u, 0x00u,
  0x43u, 0xdcu, 0x00u, 0x00u
).asByteArray()

/**
 * See [Osc Message Examples](https://opensoundcontrol.stanford.edu/spec-1_0-examples.html).
 */
val fooBytes = ubyteArrayOf(
  0x2fu, 0x66u, 0x6fu, 0x6fu,
  0x00u, 0x00u, 0x00u, 0x00u,
  0x2cu, 0x69u, 0x69u, 0x73u,
  0x66u, 0x66u, 0x00u, 0x00u,
  0x00u, 0x00u, 0x03u, 0xe8u,
  0xffu, 0xffu, 0xffu, 0xffu,
  0x68u, 0x65u, 0x6cu, 0x6cu,
  0x6fu, 0x00u, 0x00u, 0x00u,
  0x3fu, 0x9du, 0xf3u, 0xb6u,
  0x40u, 0xb5u, 0xb2u, 0x2du
).asByteArray()
