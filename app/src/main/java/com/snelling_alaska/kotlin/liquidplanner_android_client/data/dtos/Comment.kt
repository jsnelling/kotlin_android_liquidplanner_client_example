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
import paperparcel.PaperParcel
import java.util.*

@PaperParcel
data class Comment(
  var at_member_ids: List<Int>? = null,
//  var at_person_ids: List<Int>,
  var comment: String,
  var created_at: Date? = null,
  var created_by: Int? = null,
  val id: Int? = null,
  val item_id: Int,
//  var member_id: Int,
//  var person_id: Int,
  var plain_text: String? = null,
//  var updated_at: Date,
//  var updated_by: Int,
  val space_id: Int,
  val treeitem: Treeitem? = null
): Parcelable {
  companion object {
    @JvmField val CREATOR = PaperParcelComment.CREATOR
  }

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    PaperParcelComment.writeToParcel(this, dest, flags)
  }

  @PaperParcel
  data class Collection(val comments: List<Comment>): Parcelable {
    companion object {
      @JvmField val CREATOR = PaperParcelComment_Collection.CREATOR
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
      PaperParcelComment_Collection.writeToParcel(this, dest, flags)
    }
  }
}
