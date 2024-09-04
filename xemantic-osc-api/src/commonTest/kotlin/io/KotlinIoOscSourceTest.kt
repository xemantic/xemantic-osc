/*
 * xemantic-osc - Kotlin idiomatic and multiplatform OSC protocol support
 * Copyright (C) 2024 Kazimierz Pogoda
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

package com.xemantic.osc.io

import com.xemantic.osc.OscInputException
import com.xemantic.osc.type.OscTimeTag
import com.xemantic.osc.ZERO
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import kotlinx.io.Buffer
import kotlinx.io.EOFException
import kotlin.test.Test

class KotlinIoOscSourceTest {

  @Test
  fun shouldReadEmptyOscString() {
    Source(0, 0, 0, 0).apply {
      readOscString() shouldBe ""
      exhausted() shouldBe true
    }
  }

  /**
   * Test case taken from OSC protocol specification:
   *
   * https://opensoundcontrol.stanford.edu/spec-1_0-examples.html
   */
  @Test
  fun shouldReadOscStringAccordingToSpecExample1() {
    Source('O', 'S', 'C', ZERO).apply {
      readOscString() shouldBe "OSC"
      exhausted() shouldBe true
    }
  }

  /**
   * Test case taken from OSC protocol specification:
   *
   * https://opensoundcontrol.stanford.edu/spec-1_0-examples.html
   */
  @Test
  fun shouldRead3CharacterOscString() {
    Source('d', 'a', 't', 'a', ZERO, ZERO, ZERO, ZERO).apply {
      readOscString() shouldBe  "data"
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldReadUtf8OscString() {
    Source(-59, -68, -61, -77, -59, -126, -60, -121, 0, 0, 0, 0).apply {
      readOscString() shouldBe "żółć"
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldNotReadOscStringFromEmptySource() {
    shouldThrowWithMessage<OscInputException>(
      "Cannot read OSC String, because byte sequence is not 0-terminated"
    ) {
      Buffer().readOscString()
    }
  }

  @Test
  fun shouldNotReadOscStringFromSingle0Source() {
    shouldThrowWithMessage<EOFException>(
      "Buffer exhausted before skipping 4 bytes."
    ) {
      Source(0).readOscString()
    }
  }

  @Test
  fun shouldNotReadOscStringFromDouble0Source() {
    shouldThrowWithMessage<EOFException>(
      "Buffer exhausted before skipping 4 bytes."
    ) {
      Source(0, 0).readOscString()
    }
  }

  @Test
  fun shouldNotReadOscStringFromTriple0Source() {
    shouldThrowWithMessage<EOFException>(
      "Buffer exhausted before skipping 4 bytes."
    ) {
      Source(0, 0).readOscString()
    }
  }

  @Test
  fun shouldNotReadOscStringFromNonTerminatedAndNonPaddedSource() {
    shouldThrowWithMessage<OscInputException>(
      "Cannot read OSC String, because byte sequence is not 0-terminated"
    ) {
      Source(91).readOscString()
    }
  }

  @Test
  fun shouldNotReadOscStringFromNonPadded4Bytes() {
    shouldThrowWithMessage<OscInputException>(
      "Cannot read OSC String, because byte sequence is not 0-terminated"
    ) {
      Source(1, 2, 3, 4).readOscString()
    }
  }

  @Test
  fun shouldNotReadOscStringFromTerminated4BytesButNotPaddedEnough() {
    shouldThrowWithMessage<EOFException>(
      "Buffer exhausted before skipping 4 bytes."
    ) {
      Source(1, 2, 3, 4, 0).readOscString()
    }
  }

  @Test
  fun shouldNotReadOscStringFromNonPaddedUnicodeSource() {
    shouldThrowWithMessage<EOFException>("Buffer exhausted before skipping 4 bytes.") {
      Source(-59, -68, -61, -77, -59, -126, -60, -121, 0, 0, 0).readOscString()
    }
  }

  @Test
  fun shouldReadSingleOscChar() {
    Source(ZERO, ZERO, ZERO, 'a').apply {
      readOscChar() shouldBe 'a'
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldReadPolishUtf8OscChar() {
    Source(0, 0, 1, 5).apply {
      readOscChar() shouldBe 'ą'
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldReadMandarinUtf8OscChar() {
    Source(0, 0, 79, 96).apply {
      readOscChar() shouldBe '你'
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldNotReadOscCharFromEmptySource() {
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 0, required: 4)"
    ) {
      Buffer().readOscChar()
    }
  }

  @Test
  fun shouldNotReadOscCharFrom1ByteLongSource() {
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 1, required: 4)"
    ) {
      Source(0).readOscChar()
    }
  }

  @Test
  fun shouldNotReadOscCharFrom2ByteLongSource() {
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 2, required: 4)"
    ) {
      Source(0, 1).readOscChar()
    }
  }

  @Test
  fun shouldNotReadOscCharFrom3ByteLongSource() {
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 3, required: 4)"
    ) {
      Source(0, 1, 2).readOscChar()
    }
  }

  @Test
  fun shouldReadOscBlob() {
    Source(0, 0, 0, 1, 1, 0, 0, 0).apply {
      readOscBlob() shouldBe byteArrayOf(1)
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldReadBiggerOscBlob() {
    Source(0, 0, 0, 2, 1, 2, 0, 0).apply {
      readOscBlob() shouldBe byteArrayOf(1, 2)
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldNotReadOscBlobFromEmptySource() {
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 0, required: 4)"
    ) {
      Buffer().readOscBlob()
    }
  }

  @Test
  fun shouldNotReadOscBlobIfSizeSpecificationIsTooShort() {
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 1, required: 4)"
    ) {
      Source(1).readOscBlob()
    }
  }

  @Test
  fun shouldNotReadOscBlobFromNonPaddedData() {
    shouldThrowWithMessage<EOFException>(
      "Buffer exhausted before skipping 3 bytes."
    ) {
      Source(0, 0, 0, 1, 1).readOscBlob()
    }
  }

  @Test
  fun shouldReadSpecificTimeTag() {
    Source(
      0, 0, 0, 1, 0, 0, 0, 2
    ).apply {
      readOscTimeTag() shouldBe OscTimeTag(1u, 2u)
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldReadImmediateTimeTag() {
    Source(
      0, 0, 0, 0, 0, 0, 0, 1
    ).apply {
      readOscTimeTag() shouldBe OscTimeTag.IMMEDIATE
      exhausted() shouldBe true
    }
  }

  @Test
  fun shouldNotReadOscTimeTagFromEmptySource() {
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 0, required: 8)"
    ) {
      Buffer().readOscTimeTag()
    }
  }

  @Test
  fun shouldNotReadOscTimeTagFromInsufficientSource() {
    // size not fully specified
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 1, required: 8)"
    ) {
      Source(1).readOscTimeTag()
    }
  }

  @Test
  fun shouldNotReadOscTimeTagFromInsufficientSecondIntSource() {
    // data not padded
    shouldThrowWithMessage<EOFException>(
      "Buffer doesn't contain required number of bytes (size: 5, required: 8)"
    ) {
      Source(0, 0, 0, 1, 1).readOscTimeTag()
    }
  }

}
