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

package com.xemantic.osc.ableton

import com.xemantic.osc.OscPeer

/**
 * A note received from Ableton via OSC protocol.
 */
public data class AbletonNote(
  val key: Int,
  val velocity: Int,
  /**
   * The index of polyphonic note as appended at the
   * end of `/Note*`, `/Velocity*` addresses in place of the
   * `*`. The [polyphonyIndex] starts with `1` and will
   * be always `1` for monophonic note stream.
   */
  val polyphonyIndex: Int,
  val peer: OscPeer
)
