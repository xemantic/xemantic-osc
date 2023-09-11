/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright  2023 Kazimierz Pogoda
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

import com.xemantic.osc.test.defaultOscDecoder
import com.xemantic.osc.test.defaultOscEncoder
import com.xemantic.osc.test.readOsc
import com.xemantic.osc.test.toBytes
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * See [Osc Message Examples](https://opensoundcontrol.stanford.edu/spec-1_0-examples.html).
 */
class OscSpecificationTest {

  // given
  private val oscillator4FrequencyBytes = ubyteArrayOf(
    0x2fu, 0x6fu, 0x73u, 0x63u,
    0x69u, 0x6cu, 0x6cu, 0x61u,
    0x74u, 0x6fu, 0x72u, 0x2fu,
    0x34u, 0x2fu, 0x66u, 0x72u,
    0x65u, 0x71u, 0x75u, 0x65u,
    0x6eu, 0x63u, 0x79u, 0x00u,
    0x2cu, 0x66u, 0x00u, 0x00u,
    0x43u, 0xdcu, 0x00u, 0x00u
  ).asByteArray()

  private val fooBytes = ubyteArrayOf(
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

  data class Foo(
    val firstInt: Int = 1000,
    val secondInt: Int = -1,
    val string: String = "hello",
    val firstFloat: Float = 1.234f,
    val secondFloat: Float = 5.678f
  )

  @Suppress("SpellCheckingInspection")
  private val fooTag = "iisff"

  private val fooDecoder: OscDecoder<Foo> = OscDecoder(fooTag) {
    Foo(
      firstInt = int(),
      secondInt = int(),
      string = string(),
      firstFloat = float(),
      secondFloat = float()
    )
  }

  private val fooEncoder: OscEncoder<Foo> = { foo ->
    typeTag(fooTag)
    int(foo.firstInt)
    int(foo.secondInt)
    string(foo.string)
    float(foo.firstFloat)
    float(foo.secondFloat)
  }

  @Test
  fun shouldWriteOscMessageAsOscillatorFrequencyBytes() {
    toBytes {
      string("/oscillator/4/frequency")
      defaultOscEncoder<Float>()(this, 440.0f)
    } shouldBe oscillator4FrequencyBytes
  }

  @Test
  fun shouldReadOscillatorFrequencyBytesAsOscMessage() {
    // given
    val decoder = defaultOscDecoder<Float>()

    // when
    val (address, value) = oscillator4FrequencyBytes.readOsc {
      val address = string()
      val value = decoder.decode(this)
      address to value
    }

    // then
    address shouldBe "/oscillator/4/frequency"
    value shouldBe 440.0f
  }

  @Test
  fun shouldWriteOscMessageAsFooBytes() {
    toBytes {
      string("/foo")
      fooEncoder(this, Foo())
    } shouldBe fooBytes
  }

  @Test
  fun shouldReadFooBytesAsOscMessage() {
    // when
    val (address, value) = fooBytes.readOsc {
      val address = string()
      val value = fooDecoder.decode(this)
      address to value
    }

    // then
    /*
      We have to check properties one by one instead of comparing
      the whole OscMessage, because in case of JavaScript the floating point
      numbers are represented by 64bit double by default instead of
      32bit float, which is causing rounding differences.
     */
    address shouldBe "/foo"
    with (value) {
      firstInt shouldBe 1000
      secondInt shouldBe -1
      string shouldBe "hello"
      firstFloat shouldBe 1.234f.plusOrMinus(0.0000001f)
      secondFloat shouldBe 5.678f.plusOrMinus(0.0000001f)
    }

  }

}
