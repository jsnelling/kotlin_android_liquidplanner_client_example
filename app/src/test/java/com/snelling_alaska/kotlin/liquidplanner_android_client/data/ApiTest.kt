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

package com.snelling_alaska.kotlin.liquidplanner_android_client.data

import com.snelling_alaska.kotlin.liquidplanner_android_client.util.DateOnly
import com.squareup.moshi.*
import org.junit.Test

import org.junit.Assert.*
import java.util.*

    class ApiTest {
      @Test fun testNameAnnotation() {
        data class Example (
          @field:Json(name = "apple") val foo: String
        )

        val moshi = Moshi.Builder()
          .build()
    val adapter = moshi.adapter(Example::class.java)
    val res = adapter.toJson(Example("hello world"))

    assertEquals("""{"apple":"hello world"}""", res)

    val (parsed, _) = Api.shared.parse<Example>("""
      { "apple": "success" }
    """)

    assertEquals("success", parsed?.foo)
  }

  @Test fun serializeShortDate() {
    val date = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2017)
      set(Calendar.MONTH, 7)
      set(Calendar.DAY_OF_MONTH, 25)
      set(Calendar.HOUR, 11)
      set(Calendar.MINUTE, 15)
      set(Calendar.SECOND, 22)
    }.time

    assertEquals(Api.dateAdapter.toJson(
      DateOnly().apply { time = date.time }),
      "\"2017-08-25\""
    )
  }

  @Test fun serializeDate() {
    val date = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2017)
      set(Calendar.MONTH, 7)
      set(Calendar.DAY_OF_MONTH, 25)
      set(Calendar.HOUR, 11)
      set(Calendar.MINUTE, 15)
      set(Calendar.SECOND, 22)
    }.time

    assertEquals("\"2017-08-25T11:15:22-0700\"", Api.dateAdapter.toJson(date))
  }

  @Test fun serializeNull() {
    val date: Date? = null
    assertEquals("null", Api.dateAdapter.toJson(date))
  }
}
