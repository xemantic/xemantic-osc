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
package com.xemantic.osc.demo

/*
fun main() {

  val osc = Osc(
    UpdOsc(port = 40001),
    TcpOsc(port = 40002),
    OscQuery(port = 8080)
  )

  runBlocking {
    osc.flow<Int>(pattern("/Note([0-9]){,2}")).

    }
    osc.flow(pattern("/Note([0-9]){,2}"))
      .map { _, message ->
      }
      .stateIn(scope = this)
      .withLatestFrom {

      }
  }

  val noteFlow =

  osc.flow(pattern("/Velocity([0-9]){,2}")) { address, note, message ->

  }.withLatestFrom(noteFlow) {

  }.collect {
    println("note")
  }

}
*/