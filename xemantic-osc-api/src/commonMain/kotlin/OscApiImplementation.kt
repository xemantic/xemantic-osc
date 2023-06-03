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

import kotlin.reflect.KType

@PublishedApi
internal class OscRouter {

  private val converterMap = CopyOnWriteMap<KType, OscMessage.Converter<*>>(
    initialMap = OscMessage.DEFAULT_CONVERTERS
  )

  private val addressRouteMap = CopyOnWriteMap<String, Route<*>>()

  private val addressMatcherRoutes = LinkedIterable<Route<*>>()

  fun converter(
    type: KType,
    converter: OscMessage.Converter<*>,
  ) {
    converterMap.put(type, converter)
  }

  fun converters(
    converters: Map<KType, OscMessage.Converter<*>>
  ) {
    converterMap.putAll(converters)
  }

  @PublishedApi
  internal fun <T> route(
    type: KType,
    address: String,
    addressMatcher: ((address: String) -> Boolean)? = null,
    converter: OscMessage.Converter<T>? = null,
    action: ((OscMessage<T>) -> Unit)? = null // TODO consider action as suspended function
  ) {
    val routeConverter = converter
      ?: (converterMap.map[type] as OscMessage.Converter<T>?
        ?: throw IllegalArgumentException("No converter for type: $type"))
    val route = Route(
      address,
      addressMatcher,
      routeConverter,
      action
    )
    if (addressMatcher == null) {
      addressRouteMap.put(address, route)
    } else {
      addressMatcherRoutes.addOrReplace(route) { existing ->
        existing.address == route.address
      }
    }
  }

  internal fun unroute(address: String): Boolean {
    val addressRouteRemoved = (addressRouteMap.remove(address) != null)
    val addressMatcherRouteRemoved = (addressMatcherRoutes.remove { route ->
      route.address == address
    } != null)
    return (addressRouteRemoved || addressMatcherRouteRemoved)
  }

  internal fun getRoute(address: String): Route<Any>? = (
      addressRouteMap.map[address]
        ?: addressMatcherRoutes.find { it.addressMatcher!!(address) }
      ) as Route<Any>?

}
