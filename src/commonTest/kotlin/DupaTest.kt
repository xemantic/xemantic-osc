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

package com.xemantic.osc/*
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


import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.test.Test

data class Vector2(
  val x: Double,
  val y: Double
)

data class Vector3(
  val x: Double,
  val y: Double,
  val z: Double
)

private fun checkVectorTypeTag(
  typeTag: String,
  size: Int
) {
  if (typeTag.length != size) {
    throw Osc.Message.Converter.Error(
      "Cannot decode Vector$size, invalid typeTag length, " +
          "expected: $size, was: ${typeTag.length}"
    )
  }
  if (typeTag.any { it != 'f' }) {
    throw Osc.Message.Converter.Error(
      "Cannot decode Vector$size, typeTag must consists " +
          "of 'f' characters only, but was: $typeTag"
    )
  }
}

val vector2Converter = Osc.Message.Converter(
  decode = {
    checkVectorTypeTag(
      typeTag = typeTag,
      size = 2
    )
    Vector2(
      float().toDouble(),
      float().toDouble()
    )
  },
  encode = {
    typeTag("f".repeat(2))
    float(value.x.toFloat())
    float(value.y.toFloat())
  }
)


class DupaTest {

  val logger = KotlinLogging.logger {  }
  @Test
  fun shouldDo() {
    val osc = osc {
      port = 12345
      converter(vector2Converter)
      (1..10).forEach {
        address<Vector2>("/touch$it")
      }
      address<List<Float>>("/location")
      address<List<Float>>("/accelerometer")
    }
    runBlocking {
      osc.messageFlow.collect {
        logger.debug { it.toString() }
      }
    }
  }

}
