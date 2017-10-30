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
import com.squareup.moshi.Json
import paperparcel.PaperParcel
import java.util.*

@PaperParcel
data class Treeitem(
  val id: Int,
  val space_id: Int,
  var name: String,
  val type: String,
  val is_done: Boolean,
  val is_on_hold: Boolean,
  val description: String?,
  val client_id: Int?,
  val client: Treeitem?,
  val package_id: Int?,
  @field:Json(name="package") val package_ti: Treeitem?,
  var delay_until: Date?,
  var promise_by: Date?,
  var date: Date?,
  val project_id: Int?,
  val parent: Treeitem?,
  val parent_id: Int?,
  val max_effort: Float?,
  val activity_id: Int?,
  val manual_alert: String?,
  val started_on: Date?,
  val has_note: Boolean,
  var note: Note?,
  var assignments: List<Assignment>?,
  var checklist_items: List<ChecklistItem>?,
  var comments: List<Comment>?,
  val children: List<Treeitem>?,
  val package_crumbs: List<String>?,
  val parent_crumbs: List<String>?
): Parcelable {
  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    PaperParcelTreeitem.writeToParcel(this, dest, flags)
  }

  enum class Type {
    Root,
    Inbox,
    Task,
    Event,
    PartialDayEvent,
    Milestone,
    Package,
    BacklogPackage,
    Project,
    Folder,
    Client;

    fun isA(type: String?) = toString() == type
    fun isA(ti: Treeitem?) = toString() == ti?.type
  }

  fun isA(vararg types: Type): Boolean = types.contains(Type.valueOf(this.type))

  fun isEvent() = isA(Type.Event, Type.PartialDayEvent)

  fun isMilestone() = Type.valueOf(type) == Type.Milestone

  fun isLeaf() = isEvent() || isMilestone() || Type.valueOf(type) == Type.Task

  fun isContainer() = !isLeaf()

  companion object {
    @JvmField val CREATOR = PaperParcelTreeitem.CREATOR

    val IS_DONE = "is_done"
    val NAME = "name"
    val DESCRIPTION = "description"
    val DELAY_UNITL = "delay_until"
    val PROMISE_BY = "promise_by"
    val DATE = "date"
    val IS_ON_HOLD = "is_on_hold"
    val PACKAGE_ID = "package_id"
    val PARENT_ID = "parent_id"
    val CLIENT_ID = "client_id"
    val MAX_EFFORT = "max_effort"
    val ACTIVITY_ID = "activity_id"
    val MANUAL_ALERT = "manual_alert"
    val STARTED_ON = "started_on"
  }
}


//  var activity_id",
//  var alerts",
//  var assignments",
//  var billable_expenses",
//  var client_id",
//  var client_name",
//  var created_at",
//  var created_by",
//  var custom_field_values",
//  var done_on",
//  var earliest_finish",
//  var earliest_start",
//  var effective_is_on_hold",
//  var expected_finish",
//  var expected_start",
//  var external_reference",
//  var global_package_priority",
//  var global_priority",
//  var high_effort_remaining",
//  var is_estimated",
//  var is_packaged_version",
//  var is_shared",
//  var item_email",
//  var latest_finish",
//  var low_effort_remaining",
//  var nonbillable_expenses",
//  var occurrences",
//  var p98_finish",
//  var package_delay_until",
//  var package_ids",
//  var package_promise_by",
//  var parent_delay_until",
//  var parent_ids",
//  var parent_promise_by",
//  var project_id",
//  var total_expenses",
//  var updated_at",
//  var updated_by",
//  var work",
//  var work_in_order"
