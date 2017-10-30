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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.timers

import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Timer
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.timers.TimerListFragment.OnListFragmentInteractionListener
import kotlinx.android.synthetic.main.component_timer.view.*
import kotlinx.android.synthetic.main.fragment_timer_list_item.view.*

class TimerListViewAdapter(
  private var mValues: List<Timer>,
  private val mListener: OnListFragmentInteractionListener?,
  private val optionClick: (Timer, ImageButton) -> PopupMenu
) : RecyclerView.Adapter<TimerListViewAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.fragment_timer_list_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.apply {
      mItem = mValues[position]
      mItem?.let { item ->
        textViewTaskTitle.text = item.treeitem?.name
        textViewBreadCrumb.text = item.person_id.toString()
        timerView.timer = item

        mView.setOnClickListener         { mListener?.onListFragmentInteraction(item) }
        timerView.setPlayButtonHandler   { mListener?.onTimerToggleClick(item) }
        timerView.setOptionButtonHandler { showTimerOptions(item, timerView) }
      }
    }
  }

  private fun showTimerOptions(timer: Timer, timerView: TimerView) {
    val menu = optionClick(timer, timerView.imageButtonOptions)
    menu.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.timer_option_use -> mListener?.onTimerUseClick(timer)
        R.id.timer_option_clear -> mListener?.onTimerClearClick(timer)
      }
      true
    }

    menu.show()
  }

  fun updateValues(values: List<Timer>) {
    mValues = values
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int {
    return mValues.size
  }

  inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
    val textViewTaskTitle: TextView
    val textViewBreadCrumb: TextView
    val timerView: TimerView
    var mItem: Timer? = null

    init {
      textViewTaskTitle = mView.textViewTaskTitle
      textViewBreadCrumb = mView.textViewBreadCrumb
      timerView = mView.timerView
    }
  }
}
