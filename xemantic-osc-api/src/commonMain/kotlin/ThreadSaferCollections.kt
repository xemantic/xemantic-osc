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

internal class LinkedIterable<T> : Iterable<T>{

  class Node<T>(var value: T, var next: Node<T>?)

  var first: Node<T>? = null

  override fun iterator(): Iterator<T> = object : Iterator<T> {
    var head = Node(
      value = null as T,
      next = first
    )
    override fun hasNext(): Boolean = (head.next != null)
    override fun next(): T {
      if (head.next != null) {
        head = head.next!!
        return head.value
      } else {
        throw NoSuchElementException()
      }
    }
  }

  fun addOrReplace(value: T, predicate: (T) -> Boolean) {
    var head = first
    while (head != null) {
      if (predicate(head.value)) {
        head.value = value
        break
      }
      head = head.next
    }
    first = Node(value, first)
  }

  fun remove(predicate: (T) -> Boolean): T? {
    var head = first
    var removed: T? = null
    while (head != null) {
      if (predicate(head.value)) {
        removed = head.value
        head.next = head.next?.next
        break
      }
      head = head.next
    }
    return removed
  }

}

internal class CopyOnWriteMap<K, V>(
  initialMap: Map<K, V> = emptyMap()
) {

  var map: Map<K, V> = initialMap
    private set

  fun put(key: K, value: V): V? {
    val newMap = map.toMutableMap()
    val oldValue = newMap.put(key, value)
    map = newMap
    return oldValue
  }

  fun remove(key: K): V? {
    val newMap = map.toMutableMap()
    val oldValue = newMap.remove(key)
    return if (oldValue != null) {
      map = newMap
      oldValue
    } else {
      null
    }
  }

}
