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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Activity
import kotlinx.android.synthetic.main.activity_shared_picker_list_item.view.*

class ActivityListActivity : SharedPickerBase() {
  var adapter: ActivityListAdapter? = null

  override fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
    val items = Current.space?.activities ?: listOf()

    return ActivityListAdapter(items, this)
      .apply { adapter = this } as RecyclerView.Adapter<RecyclerView.ViewHolder>
  }

  fun onListFragmentInteraction(activity: Activity) {
    val result = getReturnIntent()
    result.putExtra(SELECTED_ACTIVITY, activity)
    setResult(RESULT_OK, result)
    finish()
  }

  //------------------------------------------------------------------------------------------------

  inner class ActivityListAdapter(
    var items: List<Activity>? = listOf(),
    var listener: ActivityListActivity
  ): RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount() = items?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.activity_shared_picker_list_item, parent, false)
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.apply {
        item = items?.get(position)
        item?.let { item ->
          textView.text = item.name

          mView.setOnClickListener { listener.onListFragmentInteraction(item) }
        }
      }
    }
  }

  inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
    var item: Activity? = null
    val textView: TextView

    init {
      textView = mView.text_view
      mView.image_view.visibility = View.GONE
    }
  }

  companion object {
    val SELECTED_ACTIVITY = "SELECTED_ACTIVITY"
  }
}