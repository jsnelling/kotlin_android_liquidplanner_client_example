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

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu

import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Timer

class TimerListFragment : Fragment() {
  private var mColumnCount = 1
  private var mListener: OnListFragmentInteractionListener? = null
  private var listViewAdapter: TimerListViewAdapter? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (arguments != null) {
    }
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater!!.inflate(R.layout.fragment_timer_list, container, false)

    // Set the adapter
    if (view is RecyclerView) {
      val context = view.getContext()
      if (mColumnCount <= 1) {
        view.layoutManager = LinearLayoutManager(context)
      } else {
        view.layoutManager = GridLayoutManager(context, mColumnCount)
      }
      val items = Current.space?.timers ?: listOf()

      listViewAdapter = TimerListViewAdapter(items, mListener) { _, button ->
        var popup = PopupMenu(activity, button)
        popup.menuInflater.inflate(R.menu.timer_list_fragment_options_menu, popup.menu)
        popup
      }

      view.adapter = listViewAdapter
    }
    return view
  }

  fun refresh() {
    Current.space?.timers?.let { timers ->
      listViewAdapter?.updateValues(timers)
    }
  }


  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is OnListFragmentInteractionListener) {
      mListener = context
    } else {
      throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }

  interface OnListFragmentInteractionListener {
    fun onListFragmentInteraction(timer: Timer)
    fun onTimerToggleClick(timer: Timer)
    fun onTimerUseClick(timer: Timer)
    fun onTimerClearClick(timer: Timer)
  }

  companion object {
    fun newInstance(): TimerListFragment {
      val fragment = TimerListFragment()
      val args = Bundle()
      fragment.arguments = args
      return fragment
    }
  }
}
