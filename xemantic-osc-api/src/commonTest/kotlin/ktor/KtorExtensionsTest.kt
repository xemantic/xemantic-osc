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

package com.xemantic.osc.ktor

import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlin.test.Test

const val F = 'f'.code.toByte()

class KtorExtensionsTest {

  @Test
  fun writeOscTypeTagWithPadding() {
    // when
    val floatTag = toBytes { writeOscTypeTag("f") }
    val vec2Tag = toBytes { writeOscTypeTag("ff") }
    val vec3Tag = toBytes { writeOscTypeTag("fff") }
    val vec4Tag = toBytes { writeOscTypeTag("ffff") }

    // then
    floatTag shouldBe byteArrayOf(COMMA, F, 0, 0)
    vec2Tag shouldBe byteArrayOf(COMMA, F, F, 0)
    vec3Tag shouldBe byteArrayOf(COMMA, F, F, F, 0, 0, 0, 0)
    vec4Tag shouldBe byteArrayOf(COMMA, F, F, F, F, 0, 0, 0)
  }

}

fun toBytes(
  output: Output.() -> Unit
): ByteArray = buildPacket {
  output()
}.readBytes()
