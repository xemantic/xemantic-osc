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

/**
 * Returns padding of given amount of bytes to 4-byte chunks
 * required by OSC protocol.
 *
 * @param size the amount of bytes to pad.
 * @return the osc padding.
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
public inline fun oscPadding(
  size: Long
): Long = (4L - (size % 4L)) % 4L

/**
 * Returns padding of given amount of bytes to 4-byte chunks
 * required by OSC protocol.
 *
 * @param size the amount of bytes to pad.
 * @return the osc padding.
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
public inline fun oscPadding(
  size: Int
): Int = (4 - (size % 4)) % 4
