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

package com.xemantic.osc.type

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OscColorTest {

  @Test
  fun shouldCreateOscColorFromComponents() {
    OscColor(r = 1u, g = 2u, b = 3u, a = 4u).apply {
      r shouldBe 1u
      g shouldBe 2u
      b shouldBe 3u
      a shouldBe 4u
    }
  }

  @Test
  fun shouldCreateOscColorFromInt() {
    OscColor(0x01020304u).apply {
      r shouldBe 1u
      g shouldBe 2u
      b shouldBe 3u
      a shouldBe 4u
    }
  }

  @Test
  fun colorsShouldBeEqual() {
    OscColor(0x01020304u) shouldBeEqual OscColor(r = 1u, g = 2u, b = 3u, a = 4u)
  }

}
