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
data class Timer (
  var id: Int,
  var item_id: Int,
  var person_id: Int,
  var running: Boolean,
  var running_time: Double,
  var total_time: Double?,
  var treeitem: Treeitem? = null
): Parcelable {
  companion object {
    @JvmField val CREATOR = PaperParcelTimer.CREATOR
  }

  override fun describeContents() = 0

  override fun writeToParcel(data: Parcel, flags: Int) {
    PaperParcelTimer.writeToParcel(this, data, flags)
  }
}
