/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2022 Kazimierz Pogoda
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

import kotlinx.coroutines.*
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AbstractOscTest {

  data class Foo(
    val bar: Double,
    val buzz: Int
  )

  val fooConverter = OscMessage.Converter(
    decode = {
      if (typeTag != "di") {
        throw OscMessage.Converter.Error("typeTag must be 'di', but was: '$typeTag'")
      }
      Foo(
        bar = double(),
        buzz = int()
      )
    },
    encode = {
      typeTag("di")
      double(value.bar)
      int(value.buzz)
    }
  )

//  private lateinit var receivingOsc: Osc
//
//  private lateinit var sendingOsc: Osc

  @Test
  fun shouldSendAndReceiveOscMessages() {
    val router = OscInput {
      route<Int>("/int")
      route<Float>("/float")
      route<Double>("/double")
      route<String>("/string")
      route<Boolean>("/boolean")
      route<List<String>>("/listOfStrings")
      //val noteAddress = Regex("/Note[0-1]{1}")
      //route<Int> { address -> address.matches(noteAddress) }
      //val velocityAddress = Regex("/Velocity[0-1]{1}")
//      route<Int>({ address -> address.matches(velocityAddress) }) {
//
//      }
    }
  }
}

//    receivingOsc = osc {
//      addMessageConverters(
//        fooConverter
//      )
//
//    }
//
//
//    sendingOsc.output {
//      port = receivingOsc.port
//      converter(listOfStringsConverter)
//      address<Int>("/int")
//      address<Float>("/float")
//      address<Double>("/double")
//      address<String>("/string")
//      address<Boolean>("/boolean")
//      address("/listOfStrings1", listOfStringsConverter)
//      address("/listOfStrings2", listOfStringsConverter)
//      address<List<String>>("/listOfStrings3")
//    }.use { output ->
//
//      runTest {
//        val receivedMessages = mutableListOf<Osc.Message<*>>()
//
//        val job = launch {
//          receivingOsc.messageFlow.collect {
//            receivedMessages.add(it)
//            if (receivedMessages.size == 6) {
//              coroutineContext.job.cancel()
//            }
//          }
//        }
//
//        output.send("/double", 42.0)
//        output.send("/float", 4242.0f)
//        output.send("/string", "foo")
//        output.send("/boolean", true)
//        output.send("/boolean", false)
//        output.send("/listOfStrings1", listOf("bar", "buzz"))
//
//        job.join()
//
//        receivedMessages shouldHaveSize 6
//        // slight packet reordering might happen
//        receivedMessages.map { it.value } shouldBe containExactlyInAnyOrder(
//          42.0,
//          4242.0f,
//          "foo",
//          true,
//          false,
//          listOf("bar", "buzz")
//        )
//      }
//    }
//  }
//
//  @Test
//  fun shouldDoSimpleListener() {
//    receivingOsc = osc {
//      route("/temperature") { temp: Double -> // implicity indicated converter type
//        println("temperature: $temp")
//      }
//      route<Int>("/level") { // explicitly indicated converter type
//        println("temperature: $it")
//      }
//    }
//  }
//
//  @Test
//  fun shouldAddRoutingAfterwards() {
//    receivingOsc = osc {
//
//    }
//    osc.route<Double>("/temperature") {
//      println("temperature: ${it}")
//    }
//  }
//
//  @Test
//  fun shouldFlow() {
//    receivingOsc = osc {
//      route<Int>("/level") { // explicitly indicated converter type
//        println("temperature: $it")
//      }
//    }
//    osc.flow<Double>("/temperature").collect {
//
//    }
//  }
//
//  @Test
//  fun shouldRouteAbletonNotes() {
//    receivingOsc = osc {
//      route<Int>(addressMatcher = { address ->
//        address.matches("/Note[0-1]")
//      }) { message ->
//        doSomething()
//      }
//      route<Int>(addressMatcher = { address ->
//        address.matches("/Velocity[0-1]")
//      }) { message ->
//        doSomething()
//      }
//    }
//    osc.
//  }
//
//  @Test
//  fun shouldRouteAbletonNotes2() {
//    receivingOsc = osc {
//
//    }
//    osc.listen<Int>(addressMatcher = { address ->
//      address.matches("/Note[0-1]")
//    })
//    osc.listen<Int>(addressMatcher = { address ->
//      address.matches("/Velocity[0-1]")
//    })
//    val lastNoteMap = mutableMapOf<Int, Int>()
//    osc.messageFlow.collect { message ->
//
//    }
//  }
//
//  @Test
//  fun shouldRouteAbletonNotes3() {
//    receivingOsc = osc {
//
//    }
//    val lastNoteMap = mutableMapOf<Int, Int>()
//    osc.flow<Int>(addressMatcher = { address ->
//      address.matches("/Note[0-1]")
//    }).collect {
//      //lastNoteMap =
//    }
//    osc.flow<Int>(addressMatcher = { address ->
//      address.matches("/Velocity[0-1]")
//    }).collect {
//
//    }
//  }


