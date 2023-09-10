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

package com.xemantic.osc.collections

import io.kotest.matchers.maps.beEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test

class CopyOnWriteMapTest {

  @Test
  fun shouldCreateCopyOnWriteMap() {
    val map = CopyOnWriteMap<String, String>()
    map shouldNotBe null
    map shouldBe instanceOf<MutableMap<String, String>>()
    map should beEmpty()
    map["foo"] = "bar"
    map["foo"] shouldBe "bar"
    map shouldHaveSize 1
    map["foo"] = "buzz"
    map["foo"] shouldBe "buzz"
    map shouldHaveSize 1
  }

  @Test
  fun shouldCreateCopyOnWriteMapFromKeyValuePairs() {
    // when
    val map = CopyOnWriteMap(
      "foo" to "bar"
    )

    // then
    map shouldHaveSize 1
    map["foo"] shouldBe "bar"
  }

  @Test
  fun shouldCreateCopyOnWriteMapFromExistingMap() {
    // when
    val map = CopyOnWriteMap(
      mapOf(
        "foo" to "bar"
      )
    )

    // then
    map shouldHaveSize 1
    map["foo"] shouldBe "bar"
  }

  @Test
  fun shouldClearMap() {
    // given
    val map = CopyOnWriteMap(
      "foo" to "bar"
    )

    // when
    map.clear()

    // then
    map should beEmpty()
  }

  @Test
  fun shouldRemoveElement() {
    // given
    val map = CopyOnWriteMap(
      "foo" to "bar"
    )

    // when
    val oldValue = map.remove("foo")

    // then
    map should beEmpty()
    oldValue shouldBe "bar"
  }

  @Test
  fun shouldGetOrPutElement() {
    // given
    val map = CopyOnWriteMap(
      "foo1" to "bar1"
    )

    // when
    val value = map.getOrPut("foo2") { "bar2" }

    // then
    map shouldHaveSize 2
    map["foo1"] shouldBe "bar1"
    map["foo2"] shouldBe "bar2"
    value shouldBe "bar2"
  }

}
