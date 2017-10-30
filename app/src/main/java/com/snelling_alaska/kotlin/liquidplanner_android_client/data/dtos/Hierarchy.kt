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

package com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos

import android.os.Parcel
import android.os.Parcelable
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.ExcludeField
import com.squareup.moshi.*
import paperparcel.PaperParcel

@PaperParcel
@PaperParcel.Options(
  excludeAnnotations = arrayOf(ExcludeField::class)
)
data class Hierarchy(
  val id: Int,
  val type: String,
  val name: String,
  val parent_id: Int?,
  val package_id: Int?,
  var depth: Int = 0,
  val isPackagedVersion: Boolean = false
): Parcelable {
  var expanded: Boolean = false
  var enabled: Boolean = true
  var enabledChildren: Boolean = false
  @ExcludeField var parent: Hierarchy? = null
  @ExcludeField var children: MutableList<Hierarchy> = mutableListOf()

  override fun describeContents(): Int = 0
  override fun writeToParcel(data: Parcel, flags: Int) {
    PaperParcelHierarchy.writeToParcel(this, data, flags)
  }

  fun isA(vararg types: Treeitem.Type): Boolean = types.contains(Treeitem.Type.valueOf(this.type))

  // TODO: A PR to squareup/moshi with a @Json(skip = true) would be good
  class Adapter : JsonAdapter<Hierarchy>() {
    @ToJson override fun toJson(writer: JsonWriter?, value: Hierarchy?) {
      throw NotImplementedError()
    }

    @FromJson override fun fromJson(reader: JsonReader?): Hierarchy? = reader?.let {
      var id: Int? = null
      var type: String? = null
      var name: String? = null
      var parentId: Int? = null
      var packageId: Int? = null
      reader.beginObject()
      while (reader.hasNext()) {
        when (reader.nextName()) {
          "id" -> id = reader.nextInt()
          "name" -> name = reader.nextString()
          "type" -> type = reader.nextString()
          "parent_id" -> parentId = reader.nextInt()
          "package_id" -> packageId = reader.nextInt()
          else -> reader.skipValue()
        }
      }
      reader.endObject()
      Hierarchy(requireNotNull(id), requireNotNull(type), requireNotNull(name), parentId, packageId)
    }
  }

  companion object {
    @JvmField val CREATOR = PaperParcelHierarchy.CREATOR
  }
}

