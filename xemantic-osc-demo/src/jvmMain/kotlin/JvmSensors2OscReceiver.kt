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

import UdpOscTransport
import com.xemantic.osc.OscMessage
import com.xemantic.osc.OscInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

data class Vector2(
  val x: Double,
  val y: Double
)

/**
 * Sensors2OSC app
 */
fun main() {
  val logger = KotlinLogging.logger {}
  val locations = mutableMapOf<String, List<Float>>()
  val input = OscInput {
    route<List<Float>>("/location") {
      logger.debug { "Received: $it" }
      locations[it.address] = it.value
    }
    // so you can specify an immediate action as convenient lambda
    // for the rest we will do common logger shortcut
    val logAction: (message: OscMessage<*>) -> Unit  = { message ->
      logger.debug { "Received: $message" }
    }
    route<List<Float>>("/accelerometer", action = logAction)
    route<List<Float>>("/magneticfield", action = logAction)
    route<List<Float>>("/orientation", action = logAction)
    route<List<Float>>("/gyroscope", action = logAction)
    route<Float>("/light", action = logAction)
    route<Float>("/pressure", action = logAction)
    route<Float>("/proximity", action = logAction)
    route<List<Float>>("/gravity", action = logAction)
    route<Vector2>(
      address = "/touch*",
      addressMatcher = { address -> address.startsWith("/touch") },
      converter = OscMessage.Converter(
        typeTag = "ff",
        decode = {
          Vector2(
            float().toDouble(),
            float().toDouble()
          )
        },
        encode = {
          float(value.x.toFloat())
          float(value.y.toFloat())
        },
      ),
      action = logAction
    )
    val lastNote = arrayOfNulls<Int>(200)
    route<Int>(
      address = "/Note*",
      addressMatcher = { address -> address.startsWith("/Note") },
    ) { message ->
      val noteIndex = message.address.substringAfter("/Note").toInt()
      lastNote[noteIndex] = message.value
    }
    route<Int>(
      address = "/Velocity*",
      addressMatcher = { address -> address.startsWith("/Velocity") },
    ) { message ->
      val noteIndex = message.address.substringAfter("/Velocity").toInt()
      logger.debug {
        "From ${message.peer}, note: ${lastNote[noteIndex]}, velocity: ${message.value}"
      }
    }
  }
  val oscTransport = UdpOscTransport(
    input = input,
    dispatcher = Dispatchers.IO,
    port = 40001
  )
  runBlocking(Dispatchers.IO) {
    launch {
      oscTransport.start()
    }
    launch {
      input.messages.collect {
        logger.debug { "Message received: $it" }
      }
    }
  }
}
