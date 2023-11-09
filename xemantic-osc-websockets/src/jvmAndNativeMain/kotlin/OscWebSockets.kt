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

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

//public fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

public fun Application.module() {
  install(WebSockets) {
//    pingPeriod = Duration.ofSeconds(15)
//    timeout = Duration.ofSeconds(15)
    maxFrameSize = Long.MAX_VALUE
    masking = false
  }
  routing {
    get("/") {
      call.respondText("Hello, world!")
    }

    webSocket("/echo") {
      send("Please enter your name")
      for (frame in incoming) {
        frame as? Frame.Binary ?: continue
        //val receivedText = frame.buffer.
//        if (receivedText.equals("bye", ignoreCase = true)) {
//          close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
//        } else {
//          send(Frame.Text("Hi, $receivedText!"))
//        }
      }
    }
  }
}