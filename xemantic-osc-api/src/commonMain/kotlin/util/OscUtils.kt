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

package com.xemantic.osc.util

import com.xemantic.osc.OscDataPacket
import com.xemantic.osc.OscPeer
import com.xemantic.osc.type.OscReader
import com.xemantic.osc.type.OscWriter
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

/**
 * OSC data packet which can be immediately populated.
 * Useful for testng.
 *
 * @param peer the peer which sent this packet.
 */
public fun OscDataPacket(
  peer: OscPeer,
  block: OscWriter.() -> Unit
): OscDataPacket = OscDataPacket(
  peer,
  Buffer().apply { block(OscWriter(this)) }
)

/**
 * Transforms supplied sequence of OSC writes into byte array.
 * Useful for testng.
 *
 * @param block the sequence of writes.
 */
public fun writeOscToBytes(
  block: OscWriter.() -> Unit
): ByteArray = Buffer().apply {
  block(OscWriter(this))
}.readByteArray()

/**
 * Reads bytes as source of OSC data to decode.
 * Useful for testng.
 *
 * @param block the sequence of OSC reads.
 */
public fun <T> ByteArray.readBytesAsOsc(
  block: OscReader.() -> T
): T = block(
  OscReader(
    Buffer().apply { write(this@readBytesAsOsc) }
  )
)

/**
 * Char with code `0`.
 * Useful for testng.
 */
public const val ZERO: Char = 0.toChar()

/**
 * Creates a byte array out of supplied characters.
 * Useful for testng.
 */
public fun byteArrayOf(
  vararg chars: Char
): ByteArray = chars.map {
  it.code.toByte()
}.toByteArray()
