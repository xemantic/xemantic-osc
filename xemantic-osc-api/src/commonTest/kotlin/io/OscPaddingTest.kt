/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2024 Kazimierz Pogoda
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

package com.xemantic.osc.io

import kotlin.test.Test
import io.kotest.matchers.shouldBe

class OscPaddingTest {

  @Test
  fun shouldReturnZeroPaddingForMultipleOfFour_Int() {
    oscPadding(4) shouldBe 0
    oscPadding(8) shouldBe 0
    oscPadding(12) shouldBe 0
    oscPadding(16) shouldBe 0
  }

  @Test
  fun shouldReturnCorrectPaddingForNonMultipleOfFour_Int() {
    oscPadding(1) shouldBe 3
    oscPadding(2) shouldBe 2
    oscPadding(3) shouldBe 1
    oscPadding(5) shouldBe 3
    oscPadding(7) shouldBe 1
    oscPadding(10) shouldBe 2
  }

  @Test
  fun shouldReturnZeroPaddingForMultipleOfFour_Long() {
    oscPadding(4L) shouldBe 0L
    oscPadding(8L) shouldBe 0L
    oscPadding(12L) shouldBe 0L
    oscPadding(16L) shouldBe 0L
  }

  @Test
  fun shouldReturnCorrectPaddingForNonMultipleOfFour_Long() {
    oscPadding(1L) shouldBe 3L
    oscPadding(2L) shouldBe 2L
    oscPadding(3L) shouldBe 1L
    oscPadding(5L) shouldBe 3L
    oscPadding(7L) shouldBe 1L
    oscPadding(10L) shouldBe 2L
  }

  @Test
  fun shouldHandleEdgeCases_Int() {
    oscPadding(0) shouldBe 0
    oscPadding(Int.MAX_VALUE) shouldBe 1
  }

  @Test
  fun shouldHandleEdgeCases_Long() {
    oscPadding(0L) shouldBe 0L
    oscPadding(Long.MAX_VALUE) shouldBe 1L
  }

}
