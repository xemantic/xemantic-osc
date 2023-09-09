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

import com.xemantic.osc.*
import com.xemantic.osc.test.emitFrom
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.reflect.typeOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AbletonOscInputTest {

  // given
  private val testScope = CoroutineScope(Dispatchers.Default)

  @BeforeTest
  fun setUp() {
    platformTestLogging.start()
  }

  @AfterTest
  fun tearDown() {
    testScope.cancel()
    platformTestLogging.stop()
  }

  // given
  private val peer = OscPeer(
    hostname = "localhost",
    port = 10042,
    transport = "test"
  )

  @Test
  fun shouldRouteAbletonNotes() {
    // when
    val input = OscInput(testScope) {
      routeAbletonNotes()
    }

    // then
    input.routes shouldHaveSize 2
    with(input.routes[0]) {
      address shouldBe "/Note*"
      type shouldBe typeOf<Int>()
      typeTag shouldBe "i"
    }
    with(input.routes[1]) {
      address shouldBe "/Velocity*"
      type shouldBe typeOf<Int>()
      typeTag shouldBe "i"
    }
  }

  @Test
  fun shouldUnrouteAbletonOscNotes() {
    // given
    val input = OscInput(testScope) {
      routeAbletonNotes()
    }

    // when
    input.unrouteAbletonNotes()

    // then
    input.routes should beEmpty()
  }

  @Test
  fun shouldRouteAbletonOscNotesWithAddressBase() {
    // when
    val input = OscInput(testScope) {
      routeAbletonNotes(addressBase = "/foo")
    }

    // then
    input.routes shouldHaveSize 2
    with(input.routes[0]) {
      address shouldBe "/foo/Note*"
      type shouldBe typeOf<Int>()
      typeTag shouldBe "i"
    }
    with(input.routes[1]) {
      address shouldBe "/foo/Velocity*"
      type shouldBe typeOf<Int>()
      typeTag shouldBe "i"
    }
  }

  @Test
  fun shouldUnrouteAbletonOscNotesWithAddressBase() {
    // given
    val input = OscInput(testScope) {
      routeAbletonNotes(addressBase = "/foo")
    }

    // when
    input.unrouteAbletonNotes(addressBase = "/foo")

    // then
    input.routes should beEmpty()
  }

  @Test
  fun shouldThrowExceptionWhenUnroutingNonRoutedAbletonNotes() {
    // given
    val input = OscInput(testScope) {
      routeAbletonNotes(addressBase = "/foo")
    }

    shouldThrowWithMessage<IllegalStateException>(
      "Cannot unroute non-routed address: /Note*"
    ) {
      // when
      input.unrouteAbletonNotes()
    }
  }

  @Test
  fun shouldReceiveAbletonOscNotes() = runTest {
    // given
    val packets = MutableSharedFlow<OscDataPacket>()
    val input = oscInput {
      routeAbletonNotes()
    }

    val plug = input.connect(packets)

    val notes = async {
      input.messages.toAbletonNotes().take(6).toList()
    }
    advanceUntilIdle()

    // when
    packets.emitFrom(peer) {
      message("/Note1", 42)
      message("/Velocity1", 100)
      message("/Note2", 43)
      message("/Velocity2", 101)
      message("/Note1", 42)
      message("/Velocity1", 0)
      message("/Note2",  43)
      message("/Velocity2",  0)
      message("/Note1", 42)
      message("/Velocity1", 103)
      message("/Note1", 42)
      message("/Velocity1", 0)
    }

    // then
    notes.await() shouldBe listOf(
      AbletonNote(key = 42, velocity = 100, polyphonyIndex = 1, peer),
      AbletonNote(key = 43, velocity = 101, polyphonyIndex = 2, peer),
      AbletonNote(key = 42, velocity = 0, polyphonyIndex = 1, peer),
      AbletonNote(key = 43, velocity = 0, polyphonyIndex = 2, peer),
      AbletonNote(key = 42, velocity = 103, polyphonyIndex = 1, peer),
      AbletonNote(key = 42, velocity = 0, polyphonyIndex = 1, peer)
    )
    plug.cancelAndJoin()
  }

  @Test
  fun shouldReceiveAbletonOscNotesRoutedWithAddressBase() = runTest {
    // given
    val addressBase = "/foo"
    val packets = MutableSharedFlow<OscDataPacket>()
    val input = oscInput {
      routeAbletonNotes(addressBase)
    }

    val plug = input.connect(packets)

    val notes = async {
      input.messages.toAbletonNotes(addressBase).take(2).toList()
    }
    advanceUntilIdle()

    // when
    packets.emitFrom(peer) {
      message("/foo/Note1", 42)
      message("/foo/Velocity1", 100)
      message("/foo/Note1", 42)
      message("/foo/Velocity1", 0)
    }

    // then
    notes.await() shouldBe listOf(
      AbletonNote(key = 42, velocity = 100, polyphonyIndex = 1, peer),
      AbletonNote(key = 42, velocity = 0, polyphonyIndex = 1, peer)
    )
    plug.cancelAndJoin()
  }

}
