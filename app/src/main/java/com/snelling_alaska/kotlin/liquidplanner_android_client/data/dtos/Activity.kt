package com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos

import android.os.Parcel
import android.os.Parcelable
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.panel.PanelFields
import paperparcel.PaperParcel

@PaperParcel
data class Activity(
  val id: Int,
  val billable: Boolean,
  val name: String,
  val shared: Boolean,
  val space_id: Int
): Parcelable,
  PanelFields.FieldModel {
  companion object {
    @JvmField val CREATOR = PaperParcelActivity.CREATOR
  }

  override fun describeContents() = 0

  override fun writeToParcel(data: Parcel, flags: Int) {
    PaperParcelActivity.writeToParcel(this, data, flags)
  }

  override fun name() = name
}

