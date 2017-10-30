package com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos

import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel
import java.util.*

@PaperParcel
data class ChecklistItem(
  val id: Int? = null,
  val name: String,
  val owner_id: Int = ALL_OWNERS_ID,
  val completed: Boolean,
  val completed_at: Date?,
  val completed_by: Int?,
  val sort_order: Int,
  val item_id: Int
): Parcelable {

  override fun describeContents() = 0

  override fun writeToParcel(data: Parcel, flags: Int) {
    PaperParcelChecklistItem.writeToParcel(this, data, flags)
  }

  companion object {
    val ALL_OWNERS_ID = 0

    val NAME = "name"
    val OWNER_ID = "owner_id"
    val COMPLETED = "completed"

    @JvmField val CREATOR = PaperParcelChecklistItem.CREATOR
  }
}

//"completed": false,
//"created_at": "2017-10-04T11:41:37+00:00",
//"created_by": 2579,
//"id": 138403,
//"space_id": 1159,
//"type": "ChecklistItem",
//"updated_at": "2017-10-04T11:41:37+00:00",
//"updated_by": 2579
