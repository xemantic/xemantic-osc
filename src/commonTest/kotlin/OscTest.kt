/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2022 Kazimierz Pogoda
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

import io.kotest.matchers.collections.containExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.io.use
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OscTest {

  @Test
  fun writeTypeTagWithPadding() {
    // when
    val floatTag = toBytes { writeTypeTag("f") }
    val vec2Tag = toBytes { writeTypeTag("ff") }
    val vec3Tag = toBytes { writeTypeTag("fff") }
    val vec4Tag = toBytes { writeTypeTag("ffff") }

    // then
    floatTag shouldBe byteArrayOf(COMMA_BYTE, FLOAT_BYTE, 0, 0)
    vec2Tag shouldBe byteArrayOf(COMMA_BYTE, FLOAT_BYTE, FLOAT_BYTE, 0)
    vec3Tag shouldBe byteArrayOf(COMMA_BYTE, FLOAT_BYTE, FLOAT_BYTE, FLOAT_BYTE, 0, 0, 0, 0)
    vec4Tag shouldBe byteArrayOf(
      COMMA_BYTE,
      FLOAT_BYTE,
      FLOAT_BYTE,
      FLOAT_BYTE,
      FLOAT_BYTE,
      0,
      0,
      0
    )
  }

  @Test
  fun shouldSendAndReceiveOscMessages() {

    osc {
      conversion<Double>("/entity/double")
      conversion<Float>("/entity/float")
    }.use { osc ->

      // technically we could use the same osc port for input and output
      // but let's make it more similar to actual use case
      osc {}.use { clientOsc ->
        clientOsc.output {
          port = osc.port
          conversion<Double>("/entity/double")
          conversion<Float>("/entity/float")
        }.use { output ->

          runTest {
            val receivedMessages = mutableListOf<Osc.Message<*>>()

            val job = launch {
              osc.messageFlow.collect {
                receivedMessages.add(it)
                if (receivedMessages.size == 2) {
                  coroutineContext.job.cancel()
                }
              }
            }

            output.send("/entity/double", 42.0)
            output.send("/entity/float", 4242.0f)

            job.join()

            receivedMessages shouldHaveSize 2
            // slight packet reordering might happen
            receivedMessages.map { it.value } shouldBe containExactlyInAnyOrder(
              42.0,
              4242.0f
            )
          }
        }
      }
    }
  }

}

fun toBytes(
  output: Output.() -> Unit
): ByteArray = buildPacket {
  output()
}.readBytes()

const val FLOAT_BYTE = 'f'.code.toByte()
