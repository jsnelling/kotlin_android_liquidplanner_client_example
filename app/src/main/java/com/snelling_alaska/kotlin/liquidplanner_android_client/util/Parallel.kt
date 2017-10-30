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

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class Parallel {
  private val steps: MutableList<(() -> Unit) -> Unit> = mutableListOf()

  fun step(step: (() -> Unit) -> Unit): Parallel {
    steps.add(step)
    return this
  }

  fun then(cb: () -> Unit) {
    var i = steps.size
    steps.forEach { step ->
      doAsync {
        step({
          uiThread {
            i--
            if (i == 0) {
              cb()
            }
          }
        })
      }
    }
  }
}

