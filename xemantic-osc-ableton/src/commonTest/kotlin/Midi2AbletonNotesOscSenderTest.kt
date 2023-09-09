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

package com.xemantic.osc.ableton

import com.xemantic.osc.OscPeer
import com.xemantic.osc.test.TestOscOutput
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.reflect.typeOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class Midi2AbletonNotesOscSenderTest {

  @BeforeTest
  fun setUpLogging() {
    platformTestLogging.start()
  }

  @AfterTest
  fun tearDownLogging() {
    platformTestLogging.stop()
  }

  // given
  private val peer = OscPeer(
    hostname = "localhost",
    port = 10042,
    transport = "test"
  )

  @Test
  fun shouldRouteOscOutputForAbletonNotes() {
    // given
    val output = TestOscOutput(peer)

    // when
    Midi2AbletonNotesOscSender(output)

    // then
    output.routes shouldHaveSize 2
    with(output.routes[0]) {
      address shouldBe "/Note*"
      type shouldBe typeOf<Int>()
    }
    with(output.routes[1]) {
      address shouldBe "/Velocity*"
      type shouldBe typeOf<Int>()
    }
  }

  @Test
  fun shouldUnrouteOscOutputForAbletonNotes() {
    // given
    val output = TestOscOutput(peer)
    val sender = Midi2AbletonNotesOscSender(output)

    // when
    sender.unrouteAbletonNotes()

    // then
    output.routes should beEmpty()
  }

  @Test
  fun shouldRouteOscOutputForAbletonNotesWithAddressBase() {
    // given
    val output = TestOscOutput(peer)

    // when
    Midi2AbletonNotesOscSender(
      output,
      addressBase = "/foo"
    )

    // then
    output.routes shouldHaveSize 2
    with(output.routes[0]) {
      address shouldBe "/foo/Note*"
      type shouldBe typeOf<Int>()
    }
    with(output.routes[1]) {
      address shouldBe "/foo/Velocity*"
      type shouldBe typeOf<Int>()
    }
  }

  @Test
  fun shouldUnrouteOscOutputForAbletonNotesWithAddressBase() {
    // given
    val output = TestOscOutput(peer)
    val sender = Midi2AbletonNotesOscSender(
      output,
      addressBase = "/foo"
    )

    // when
    sender.unrouteAbletonNotes()

    // then
    output.routes should beEmpty()
  }

  @Test
  fun shouldTransformMidiToAbletonNotes() = runTest {
    // given
    val output = TestOscOutput(peer)
    val sender = Midi2AbletonNotesOscSender(output)

    // when
    with(sender) {
      noteOn(42, 100)
      noteOn(43, 101)
      noteOff(42, 0)
      noteOff(43, 0)
      noteOn(42, 103)
      noteOff(42, 0)
    }

    output.messages shouldBe listOf(
      "/Note1" to 42,
      "/Velocity1" to 100,
      "/Note2" to 43,
      "/Velocity2" to 101,
      "/Note1" to 42,
      "/Velocity1" to 0,
      "/Note2" to  43,
      "/Velocity2" to 0,
      "/Note1" to 42,
      "/Velocity1" to 103,
      "/Note1" to 42,
      "/Velocity1" to 0
    )
  }

  @Test
  fun shouldTransformMidi2AbletonNotesWithAddressBase() = runTest {
    // given
    val output = TestOscOutput(peer)
    val sender = Midi2AbletonNotesOscSender(
      output,
      addressBase = "/foo"
    )

    // when
    with (sender) {
      noteOn(42, 100)
      noteOff(42, 0)
    }

    output.messages shouldBe listOf(
      "/foo/Note1" to 42,
      "/foo/Velocity1" to 100,
      "/foo/Note1" to 42,
      "/foo/Velocity1" to 0,
    )

  }

}
