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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.treeitems_list

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem

class TreeitemFragment : Fragment() {
  private var mListener: OnListFragmentInteractionListener? = null
  private var adapter: MyTreeitemRecyclerViewAdapter? = null

  var treeitems: List<Treeitem>? = null
    set(items) {
      field = items
      items?.let { tis ->
        adapter?.update(tis)
      }
    }

  var outdentFirst: Boolean
    get() { return adapter?.outdentFirst ?: false }
    set(v) { adapter?.let { it.outdentFirst = v } }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater!!.inflate(R.layout.fragment_treeitem_list, container, false)

    if (view is RecyclerView) {
      val context = view.getContext()
      view.layoutManager = LinearLayoutManager(context)

      val items = treeitems ?: listOf()

      adapter = MyTreeitemRecyclerViewAdapter(items, mListener)
      view.adapter = adapter
    }
    return view
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
    fun onListFragmentInteraction(item: Treeitem)
    fun onListFragmentLongClick(item: Treeitem)
  }

  companion object {
    val ARG_TREEITEM_ROOT = "treeitem-root"
  }
}
