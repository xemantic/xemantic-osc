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

import io.kotest.matchers.longs.between
import io.kotest.matchers.shouldBe
import kotlinx.datetime.*
import kotlin.test.Test

class OscTimeTagTest {

  @Test
  fun shouldCreateZeroOscTimeTag() {
    // given
    val timeTag = OscTimeTag(0u, 0u)

    // when
    val instant = Instant.fromEpochMilliseconds(timeTag.asMillis)
    val date = instant.toLocalDateTime(TimeZone.UTC)

    // then
    timeTag.immediate shouldBe false
    date.year shouldBe 1900
    date.month shouldBe Month.JANUARY
    date.dayOfMonth shouldBe 1
    date.hour shouldBe 0
    date.minute shouldBe 0
    date.second shouldBe 0
  }

  @Test
  fun shouldCreateImmediateOscTimeTagForSecondEqualTo0AndFractionEqualTo1() {
    // given
    val timeTag = OscTimeTag(0u, 1u)

    // then
    timeTag.immediate shouldBe true
    val now = Clock.System.now().toEpochMilliseconds()
    timeTag.asMillis shouldBe between(now - 1L, now + 1L)
  }

  @Test
  fun shouldReturnImmediateOscTimeTagForImmediateConstant() {
    // given
    val timeTag = OscTimeTag.IMMEDIATE

    // then
    timeTag.immediate shouldBe true
    val now = Clock.System.now().toEpochMilliseconds()
    timeTag.asMillis shouldBe between(now - 1L, now + 1L)
  }

  @Test
  fun shouldConvertMillisToOscTimeTagAndBackAccurately() {
    // given
    val now = Clock.System.now().toEpochMilliseconds()

    // when
    val timeTag = OscTimeTag.fromMillis(now)

    // then
    timeTag.asMillis shouldBe between(now - 1L, now + 1L)
  }

}
