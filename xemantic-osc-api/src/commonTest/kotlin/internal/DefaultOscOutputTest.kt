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

package com.xemantic.osc.internal

import com.xemantic.osc.OscPeer
import com.xemantic.osc.convert.DEFAULT_OSC_ENCODERS
import com.xemantic.osc.convert.oscEncoders
import com.xemantic.osc.oscillator4FrequencyBytes
import com.xemantic.osc.route
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.reflect.typeOf
import kotlin.test.Test

class DefaultOscOutputTest {

  // given
  private val peer = OscPeer("localhost", 12345, "udp")
  data class Foo(val value: String)
  data class Bar(val value: String)

  @Test
  fun shouldExtendEncoders() {
    // given
    val output = DefaultOscOutput(peer) { /* no need to capture packet */ }
    output.encoders shouldBe DEFAULT_OSC_ENCODERS

    // when
    output.encoders[typeOf<Foo>()] = {
      // intentionally does nothing
    }
    // then
    output.encoders.size shouldBe DEFAULT_OSC_ENCODERS.size + 1

    // when
    output.encoders += oscEncoders {
      encoder<Bar> { string(it.value) }
    }
    // then
    output.encoders.size shouldBe DEFAULT_OSC_ENCODERS.size + 2
  }

  @Test
  fun shouldHaveZeroRoutesInitially() {
    // given
    val output = DefaultOscOutput(peer) { /* no need to capture packet */ }

    // then
    output.routes should beEmpty()
  }

  @Test
  fun shouldAddRoute() {
    // given
    val output = DefaultOscOutput(peer) { /* no need to capture packet */ }

    // when
    output.route<String>("/foo")

    // then
    output.routes should haveSize(1)
    output.routes[0].address shouldBe "/foo"
    output.routes[0].type shouldBe typeOf<String>()
  }

  @Test
  fun shouldUnrouteRoute() {
    // given
    val output = DefaultOscOutput(peer) { /* no need to capture packet */ }
    output.route<String>("/foo")

    // when
    output.unroute("/foo")

    // then
    output.routes should beEmpty()
  }

  @Test
  fun shouldSendOutPacket() = runTest {
    // given
    lateinit var bytes: ByteArray
    val output = DefaultOscOutput(peer) {
      bytes = it.readBytes()
    }
    output.route<Float>("/oscillator/4/frequency")

    // when
    output.send("/oscillator/4/frequency", 440.0f)
    advanceUntilIdle()

    // then
    bytes shouldBe oscillator4FrequencyBytes
  }

}
