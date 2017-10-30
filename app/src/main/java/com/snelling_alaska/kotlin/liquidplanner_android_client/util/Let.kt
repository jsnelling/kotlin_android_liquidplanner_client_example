/*
 * Copyright 2017 Jon Snelling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.snelling_alaska.kotlin.liquidplanner_android_client.util

fun <A: Any, B: Any, Z: Any>
  let(a: A?, b: B?, z: (A, B) -> Z)
  = if (a != null && b != null) { z(a, b) } else { null }

fun <A: Any, B: Any, C, Any, Z: Any>
  let(a: A?, b: B?, c: C?, z: (A, B, C) -> Z)
  = if (a != null && b != null && c != null) { z(a, b, c) } else { null }

fun <A: Any, B: Any, C, Any, D: Any, Z: Any>
  let(a: A?, b: B?, c: C?, d: D?, z: (A, B, C, D) -> Z)
  = if (a != null && b != null && c != null && d != null) { z(a, b, c, d) } else { null }

fun <A: Any, B: Any, C, Any, D: Any, E: Any, Z: Any>
  let(a: A?, b: B?, c: C?, d: D?, e: E?, z: (A, B, C, D, E) -> Z)
  = if (a != null && b != null && c != null && d != null &&
  e != null) { z(a, b, c, d, e) } else { null }
