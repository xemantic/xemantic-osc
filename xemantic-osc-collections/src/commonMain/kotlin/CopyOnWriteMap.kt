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

import kotlin.concurrent.Volatile

/**
 * A version of [MutableMap] which does not throw exceptions in concurrent usage.
 * It is potentially slow for write operations, and only guarantees eventual
 * consistency, which is good enough for holding OSC protocol data.
 */
public class CopyOnWriteMap<K, V>(
  initialMap: Map<K, V> = emptyMap()
) : MutableMap<K, V>{

  public constructor(vararg pairs: Pair<K, V>) : this(pairs.toMap())

  @Volatile
  private var map: Map<K, V> = initialMap

  override val size: Int get() = map.size

  override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    get() = map.toMutableMap().entries

  override val keys: MutableSet<K>
    get() = map.keys.toMutableSet()

  override val values: MutableCollection<V>
    get() = map.values.toMutableList()

  override fun clear() {
    map = emptyMap()
  }

  override fun get(key: K): V? = map[key]

  override fun isEmpty(): Boolean = map.isEmpty()

  override fun remove(key: K): V? {
    val newMap = map.toMutableMap()
    val oldValue = newMap.remove(key)
    return if (oldValue != null) {
      map = newMap
      oldValue
    } else {
      null
    }
  }

  override fun putAll(from: Map<out K, V>) {
    val newMap = map.toMutableMap()
    newMap.putAll(from)
    map = newMap
  }

  override fun put(key: K, value: V): V? {
    val newMap = map.toMutableMap()
    val oldValue = newMap.put(key, value)
    map = newMap
    return oldValue
  }

  override fun containsValue(value: V): Boolean = map.containsValue(value)

  override fun containsKey(key: K): Boolean = map.containsKey(key)

}
