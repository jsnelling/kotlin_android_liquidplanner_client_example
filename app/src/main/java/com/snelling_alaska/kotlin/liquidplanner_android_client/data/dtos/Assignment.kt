package com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos

import android.os.Parcel
import android.os.Parcelable
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.Exclude
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.ExcludeField
import java.util.Date
import paperparcel.PaperParcel

@PaperParcel
@PaperParcel.Options(
  excludeAnnotations = arrayOf(Exclude::class, ExcludeField::class)
)
data class Assignment @JvmOverloads constructor(
  val id: Int,
  val treeitem_id: Int,
  val activity_id: Int?,
  val can_destroy: Boolean,
  val daily_limit: Float?,
  val expected_finish: Date?,
  val expected_start: Date?,
  val high_effort_remaining: Float?,
  val hours_logged: Float?,
  val is_done: Boolean,
  val is_owner: Boolean,
  val low_effort_remaining: Float?,
  val person_id: Int?,
  val space_id: Int,
  val team_id: Int?,
  @ExcludeField val estimates: List<Estimate>? = null,

  @ExcludeField var treeitem: Treeitem? = null
): Parcelable {
  val estimated: Boolean
    get() = estimates?.isNotEmpty() ?: false

  override fun describeContents() = 0

  override fun writeToParcel(data: Parcel, flags: Int) {
    PaperParcelAssignment.writeToParcel(this, data, flags)
  }

  companion object {
    @JvmField val CREATOR = PaperParcelAssignment.CREATOR

    val PERSON_ID = "person_id"
    val TEAM_ID = "team_id"
    val IS_DONE = "is_done"
    val ACTIVITY_ID = "activity_id"
    val DAILY_LIMIT = "daily_limit"
    val LOW = "low_effort_remaining"
    val HIGH = "high_effort_remaining"
  }
}

//"position": 0,
//"type": "Assignment"
