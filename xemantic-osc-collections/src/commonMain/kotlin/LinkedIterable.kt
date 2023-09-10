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

public class LinkedIterable<T> : Iterable<T>{

  private class Node<T>(
    var value: T,
    @kotlin.concurrent.Volatile
    var next: Node<T>?
  )

  @kotlin.concurrent.Volatile
  private var first: Node<T>? = null

  override fun iterator(): Iterator<T> = object : Iterator<T> {

    private var head: Node<T>? = first

    override fun hasNext(): Boolean = (head != null)

    override fun next(): T {
      if (head != null) {
        val value = head!!.value
        head = head!!.next
        return value
      } else {
        throw NoSuchElementException("No more elements in iterator")
      }
    }

  }

  public fun addOrReplace(value: T, predicate: (T) -> Boolean) {
    if (first == null) {
      first = Node(value, next = null)
    } else {
      var head = first
      while (head != null) {
        if (predicate(head.value)) {
          head.value = value
          break
        } else if (head.next == null) { // last element
          head.next = Node(value, next = null)
          break
        }
        head = head.next
      }
    }
  }

  public fun remove(predicate: (T) -> Boolean): T? {
    var head = first
    var previous: Node<T>? = null
    while (head != null) {
      if (predicate(head.value)) {
        val removedValue = head.value
        if (previous == null) {
          first = head.next
        } else {
          previous.next = head.next
        }
        return removedValue
      }
      previous = head
      head = head.next
    }
    return null
  }

}
