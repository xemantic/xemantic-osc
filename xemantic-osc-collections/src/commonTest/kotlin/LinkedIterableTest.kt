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

import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class LinkedIterableTest {

  @Test
  fun shouldCreateLinkedIterableInstance() {
    val iterable = LinkedIterable<String>()
    iterable shouldNotBe null
    iterable.count() shouldBe 0
  }

  @Test
  fun shouldAddOneElements() {
    // given
    val iterable = LinkedIterable<String>()

    // when
    iterable.addOrReplace("foo") { true }

    // then
    iterable.count() shouldBe 1
    iterable shouldContainInOrder listOf("foo")
  }

  @Test
  fun shouldAddMultipleSameElementsWithoutReplace() {
    // given
    val iterable = LinkedIterable<String>()

    // when
    iterable.addOrReplace("foo") { false }
    iterable.addOrReplace("foo") { false }

    // then
    iterable.count() shouldBe 2
    iterable shouldContainInOrder listOf("foo", "foo")
  }

  @Test
  fun shouldAddOnlyOneSameElementWhenReplaceIsAlwaysTrue() {
    // given
    val iterable = LinkedIterable<String>()

    // when
    iterable.addOrReplace("foo") { true }
    iterable.addOrReplace("foo") { true }

    // then
    iterable.count() shouldBe 1
    iterable shouldContainInOrder listOf("foo")
  }

  @Test
  fun shouldReplaceExistingElement() {
    // given
    val iterable = LinkedIterable<String>()

    // when
    iterable.addOrReplace("foo") { false }
    iterable.addOrReplace("bar") { it == "foo" }

    // then
    iterable.count() shouldBe 1
    iterable shouldContainInOrder listOf("bar")
  }

  @Test
  fun shouldRemoveFirstElement() {
    // given
    val iterable = LinkedIterable<String>()
    iterable.addOrReplace("foo") { false }
    iterable.addOrReplace("bar") { false }
    iterable.addOrReplace("buzz") { false }

    // when
    val removed = iterable.remove { it == "foo" }

    // then
    iterable shouldContainInOrder listOf("bar", "buzz")
    removed shouldBe "foo"
  }

  @Test
  fun shouldRemoveMiddleElement() {
    // given
    val iterable = LinkedIterable<String>()
    iterable.addOrReplace("foo") { false }
    iterable.addOrReplace("bar") { false }
    iterable.addOrReplace("buzz") { false }

    // when
    val removed = iterable.remove { it == "bar" }

    iterable shouldContainInOrder listOf("foo", "buzz")
    removed shouldBe "bar"
  }

  @Test
  fun shouldRemoveLastElement() {
    // given
    val iterable = LinkedIterable<String>()
    iterable.addOrReplace("foo") { false }
    iterable.addOrReplace("bar") { false }
    iterable.addOrReplace("buzz") { false }

    // when
    val removed = iterable.remove { it == "buzz" }

    iterable shouldContainInOrder listOf("foo", "bar")
    removed shouldBe "buzz"
  }

}
