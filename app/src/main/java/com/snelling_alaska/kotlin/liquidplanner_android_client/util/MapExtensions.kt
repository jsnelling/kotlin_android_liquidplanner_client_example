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

import android.util.Log
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Api
import com.squareup.moshi.Types
import okhttp3.RequestBody

fun <K, V> Map<K, V>.toJson(serializeNulls: Boolean = false): String {
  val types = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)

  val adapter = Api.moshi.adapter<Map<K, V>>(types)

  return if (serializeNulls) {
    adapter.serializeNulls()
  } else {
    adapter
  }.toJson(this.toMap())
}

fun <K, V> Map<K, V>.toRequestBody(serializeNulls: Boolean = false): RequestBody {
  return RequestBody.create(Api.JSON, this.toJson(serializeNulls).apply {
    Log.v("JSON BODY", this)
  })
}

