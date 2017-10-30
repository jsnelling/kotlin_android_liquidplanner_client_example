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

import android.util.Log
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Hierarchy
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.DateOnly
import com.squareup.moshi.*
import okhttp3.*
import java.util.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.nio.charset.Charset
import java.text.ParseException
import java.util.concurrent.TimeUnit

object Api {
  val JSON = MediaType.parse("application/json; charset=utf-8")

  val dateAdapter = object: JsonAdapter<Date>() {

    override fun toJson(writer: JsonWriter?, value: Date?) {
      writer?.value(value?.let {
        if (it is DateOnly) {
          DateOnly.asDate(it)
        } else {
          DateOnly.asDateTime(it)
        }
      })
    }

    override fun fromJson(reader: JsonReader?): Date?
      = if (reader?.hasNext() == true) {
        val peek =  reader.peek()
        if (peek == JsonReader.Token.STRING) {
          var date = reader.nextString()
          try {
            DateOnly.formatter.parse(date)
          } catch (ex: ParseException) {
            DateOnly.dateOnlyFormatter.parse(date)
          }
        } else if (peek == JsonReader.Token.NULL) {
          reader.nextNull<Date>()
          null
        } else {
          null
        }
      } else {
        null
      }
  }

  val moshi: Moshi = Moshi.Builder()
    .add(Date::class.java, dateAdapter)
    .add(DateOnly::class.java, dateAdapter)
    .add(TimeZone::class.java, object: JsonAdapter<TimeZone>() {
      override fun toJson(writer: JsonWriter?, value: TimeZone?) {
        writer?.value(value.toString())
      }
      override fun fromJson(reader: JsonReader?) = TimeZone.getTimeZone(reader?.nextString())
    })
    .add(Hierarchy.Adapter())
    .build()



  interface JSONable {
    fun toJSON(): String
  }

  class Connection {
    private var username: String? = null
    private var password: String? = null

    private val client = OkHttpClient.Builder()
      .readTimeout(10, TimeUnit.SECONDS)
      .build()

    fun login(username: String, password: String) {
      this.username = username
      this.password = password
    }

    fun execute(request: okhttp3.Request): okhttp3.Response = client.newCall(request).execute()

    class UrlParseException : Exception()

    fun urlBuilder(url: String): HttpUrl.Builder {
      HttpUrl.parse(fullUrl(url))?.let {
        return HttpUrl.Builder()
          .scheme(it.scheme())
          .host(it.host())
          .apply { it.pathSegments().forEach { addPathSegment(it) } }
      } ?: throw UrlParseException()
    }

    fun <Model>_fetch(klass: Class<Model>, builder: HttpUrl.Builder, build: (okhttp3.Request.Builder) -> Unit): Request<Model> {
      return _fetch(klass, builder.build(), build)
    }

    fun <Model>_fetch(klass: Class<Model>, url: String, build: (okhttp3.Request.Builder) -> Unit): Request<Model> {
      HttpUrl.parse(fullUrl(url))?.let {
        return _fetch(klass, it, build)
      } ?: throw UrlParseException()
    }

    fun <Model>_fetch(klass: Class<Model>, url: HttpUrl, build: (okhttp3.Request.Builder) -> Unit): Request<Model> {
      val request = okhttp3.Request.Builder()
        .url(url)
        .apply {
          if (username != null && password != null) {
            header("Authorization", Credentials.basic(username, password))
          }
        }
        .apply(build)
        .build()

      return Request(this, klass, request)
    }

    fun <Model> adapter(klass: Class<Model>) = moshi.adapter<Model>(klass)
    fun <Model> listAdapter(klass: Class<Model>): JsonAdapter<List<Model>> {
      val listType = Types.newParameterizedType(List::class.java, klass)
      return moshi.adapter<List<Model>>(listType)
    }

    fun <Model> parse(klass: Class<Model>, jsonStr: String): Pair<Model?, List<Model>?> {
      val json = jsonStr.trim()
      return when (json[0]) {
        '[' -> Pair(null, listAdapter(klass).fromJson(json))
        else -> Pair(adapter(klass).fromJson(json), null)
      }
    }

    inline fun <reified Model> parse(json: String) = parse(Model::class.java, json)

    inline fun <reified Model>fetch(url: String, noinline build: (okhttp3.Request.Builder) -> Unit)
      = _fetch(Model::class.java, url, build)

    //--------------------------------------------------------------------------------------

    inline fun <reified Model> get(builder: HttpUrl.Builder) =
      _fetch(Model::class.java, builder) { it.get() }

    inline fun <reified Model> get(url: String) = fetch<Model>(url) { it.get() }

    //--------------------------------------------------------------------------------------

    inline fun <reified Model> post(url: String, body: RequestBody?) =
      _fetch(Model::class.java, url) { it.post(body ?: RequestBody.create(JSON, "")) }

    inline fun <reified Model> post(url: String, json: JSONObject) =
      _fetch(Model::class.java, url) { it.post(RequestBody.create(JSON, json.toString())) }

    inline fun <reified Model> post(url: String, json: JSONable?) =
      _fetch(Model::class.java, url) { it.post(RequestBody.create(JSON, json?.toJSON() ?: "")) }

    //--------------------------------------------------------------------------------------

    inline fun <reified Model> put(builder: HttpUrl.Builder, body: RequestBody?) =
      _fetch(Model::class.java, builder) { it.put(body ?: RequestBody.create(JSON, "")) }

    inline fun <reified Model> put(url: String, body: RequestBody?) =
      _fetch(Model::class.java, url) { it.put(body ?: RequestBody.create(JSON, "")) }

    //--------------------------------------------------------------------------------------

    inline fun <reified Model>delete(url: String) =
      _fetch(Model::class.java, url) { it.delete() }
  }

  data class RequestFailure(val request: okhttp3.Request): Exception()

  data class Request<out Model>(
    private val connection: Connection,
    private val klass: Class<Model>,
    private val request: okhttp3.Request
  ) {
    fun fetch(then: (Model?) -> Unit) = fetch { m, _ -> then(m) }
    fun fetchA(then: (List<Model>?) -> Unit) = fetch { _, m -> then(m) }

    private fun fetch(then: (Model?, List<Model>?) -> Unit) {
      doAsync {
        Log.v(TAG, "start request: ${request.url()}")
        connection.execute(request).let {
          if (!it.isSuccessful) {
            Log.e(TAG, "request failed: ${request.url()}")
            Log.e(TAG, "result: ${it.body()?.source()?.readString(Charset.defaultCharset())}")
            throw RequestFailure(request)
          }

          val result = it.body()?.string()

          Log.v(TAG, "result: ${request.url()} ${result}")

          result?.let {
            Log.v(TAG, "parsing request: ${request.url()}")
            val res = connection.parse(klass, result)
            Log.v(TAG, "finished parsing request: ${request.url()}")
            uiThread {
              then(res.first, res.second)
            }
          }
        }
      }
    }
  }


  fun fullUrl(url: String): String {
    if (url.startsWith("http")) {
      return url
    }
    if (url.startsWith("/")) {
      return "${Current.hostUrl}${url}"
    }
    return "${Current.hostUrl}/api/${url}"
  }


  val shared = Connection()
  private val TAG = "API"
}


