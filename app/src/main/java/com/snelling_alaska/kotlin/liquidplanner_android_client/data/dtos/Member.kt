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
data class Member(
  val avatar_url: String,
  val company: String,
//  val created_at: Date,
  val created_by: Int,
  val disabled_workspaces_count: Int,
  val email: String,
  val first_name: String,
  override val id: Int,
  val last_name: String,
  val last_workspace_id: Int,
  val timezone: TimeZone,
  val type: String,
//  val updated_at: Date,
  val updated_by: Int,
  val user_name: String
): Parcelable, MemberOrTeamish {
  override val name: String get() = user_name

  companion object {
    @JvmField val CREATOR = PaperParcelMember.CREATOR

    val UNASSINGED_ID = 0
  }

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    PaperParcelMember.writeToParcel(this, dest, flags)
  }
}
