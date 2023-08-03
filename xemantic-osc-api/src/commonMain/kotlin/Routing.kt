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

import com.xemantic.osc.collections.CopyOnWriteMap
import com.xemantic.osc.collections.LinkedIterable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlin.concurrent.Volatile
import kotlin.reflect.KType

/**
 * @param R route type.
 */
internal class OscRouter<R : Route> {

  private val addressRouteMap = CopyOnWriteMap<String, R>()

  private val addressMatcherRoutes = LinkedIterable<R>()

  fun addRoute(route: R) {
    addressRouteMap[route.address] = route
    if (route.addressMatcher != null) {
      addressMatcherRoutes.addOrReplace(route) { existing ->
        existing.address == route.address
      }
    }
  }

  fun getRoute(address: String): R? = (
      addressRouteMap[address]
        ?: addressMatcherRoutes.find { it.addressMatcher!!(address) }
      )

  fun unroute(addresses: Array<out String>) {
    addresses.forEach { address ->
      if (!unroute(address)) {
        throw IllegalStateException(
          "Cannot unroute non-routed address: $address"
        )
      }
    }
  }

  private fun unroute(address: String): Boolean {
    val addressRouteRemoved = (addressRouteMap.remove(address) != null)
    val addressMatcherRouteRemoved = (addressMatcherRoutes.remove { route ->
      route.address == address
    } != null)
    return (addressRouteRemoved || addressMatcherRouteRemoved)
  }

}

internal abstract class Route(
  val address: String,
  val addressMatcher: AddressMatcher?
)

internal class OutputRoute<T>(
  address: String,
  addressMatcher: AddressMatcher?,
  val encoder: OscEncoder<T>,
) : Route(
  address,
  addressMatcher
)

internal class InputRoute<T>(
  address: String,
  addressMatcher: AddressMatcher?,
  val decoder: OscDecoder<T>,
  val action: OscAction<T>?
) : Route(
  address,
  addressMatcher
), OscInput.Route<T> {

  @Volatile
  private var oscValue: DefaultOscValue<T>? = null

  override val messages: MutableSharedFlow<OscMessage<T>> = MutableSharedFlow()

  override val values = messages.map { it.value }

  override fun value(value: T): OscValue<T> {
    oscValue = DefaultOscValue(value)
    return oscValue!!
  }

  suspend fun onMessageReceived(message: OscMessage<T>) {
    val value = message.value
    oscValue?._value = value
    action?.invoke(value)
    messages.emit(message)
  }

}

internal class DefaultOscValue<T>(
  value: T // TODO check with MutableStateFlow
) : OscValue<T> {

  @Volatile
  internal var _value: T = value

  override val value: T get() = _value

}
