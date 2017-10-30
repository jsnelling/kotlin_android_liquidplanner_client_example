package com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos

import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel

@PaperParcel
data class Client(
  val id: Int,
  val name: String
): Parcelable {
  override fun describeContents() = 0

  override fun writeToParcel(data: Parcel, flags: Int) {
    PaperParcelClient.writeToParcel(this, data, flags)
  }

  companion object {
    @JvmField val CREATOR = PaperParcelClient.CREATOR
  }
}

//  "alerts",
//  "assignments",
//  "contract_value",
//  "created_at",
//  "created_by",
//  "description",
//  "done_on",
//  "earliest_finish",
//  "earliest_start",
//  "effective_is_on_hold",
//  "expected_finish",
//  "expected_start",
//  "external_reference",
//  "has_note",
//  "high_effort_remaining",
//  "id",
//  "is_done",
//  "is_on_hold",
//  "latest_finish",
//  "low_effort_remaining",
//  "manual_alert",
//  "name",
//  "occurrences",
//  "p98_finish",
//  "space_id",
//  "started_on",
//  "type",
//  "updated_at",
//  "updated_by",
//  "work"
