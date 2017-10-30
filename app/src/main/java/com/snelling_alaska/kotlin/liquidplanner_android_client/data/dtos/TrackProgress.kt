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

@PaperParcel
data class TrackProgress(
  var activity: Activity? = null,
  var low: Float? = null,
  var high: Float? = null,
  var time: Double? = null,
  var dailyLimit: Float? = null,
  var is_done: Boolean? = null,
  var restart: Boolean? = null,
  var comment: String? = null,
  var note: String? = null,
  var person: Member? = null
): Parcelable {
  companion object {
    @JvmField val CREATOR = PaperParcelTrackProgress.CREATOR

    val MEMBER_ID = "member_id"
    val WORK = "work"
    val ACTIVITY_ID = "activity_id"
    val LOW = "low"
    val HIGH = "high"
    val IS_DONE = "is_done"
    val NOTE = "note"
    val COMMENT = "comment"
  }

  override fun describeContents() = 0

  override fun writeToParcel(data: Parcel, flags: Int) {
    PaperParcelTrackProgress.writeToParcel(this, data, flags)
  }
}

