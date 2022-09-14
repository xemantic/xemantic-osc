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

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OscTest {

  @Test
  fun shouldSendAndReceiveOscMessages() {
    // given
    osc {
      conversion<Double>("/entity/double")
      conversion<Float>("/entity/float")
    }.use { osc ->
      val receivedMessages = mutableListOf<Osc.Message<*>>()
      runTest {
        val job = launch {
          osc.messageFlow.collect {
            receivedMessages.add(it)
          }
        }
        delay(1)
        osc.output {
          port = osc.port
          conversion<Double>("/entity/double")
          conversion<Float>("/entity/float")
        }.use { output ->
          output.send("/entity/double", 42.0)
          output.send("/entity/float", 42.0)
        }
        delay(1)
        job.cancelAndJoin()

        receivedMessages shouldHaveSize 2
        receivedMessages[1].value shouldBe 42.0
        receivedMessages[2].value shouldBe 42.0
      }
    }
  }

}
