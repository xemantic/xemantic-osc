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
package com.xemantic.osc.demo

import UdpOsc
import com.xemantic.osc.OscInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

fun main() {
  val logger = KotlinLogging.logger {}
  UdpOsc(
    input = OscInput(),
    dispatcher = Dispatchers.IO,
    port = 40002
  ).use { osc ->
    runBlocking {
      launch {
        osc.start()
      }
      val output = osc.output(host = "localhost", port = 40001) {
        route<Int>(
          address = "/Note*",
          addressMatcher = { address -> address.startsWith("/Note") }
        )
        route<Int>(
          address = "/Velocity*",
          addressMatcher = { address -> address.startsWith("/Velocity") }
        )
      }
      output.send("/Note10", 42)
      output.send("/Velocity10", 112)
      osc.close()
    }
  }
}
