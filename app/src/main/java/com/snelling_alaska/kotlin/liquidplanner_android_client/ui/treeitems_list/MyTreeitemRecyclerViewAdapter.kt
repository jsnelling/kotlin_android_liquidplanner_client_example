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

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.treeitems_list.TreeitemFragment.OnListFragmentInteractionListener
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.Icons
import kotlinx.android.synthetic.main.fragment_treeitem.view.*

class MyTreeitemRecyclerViewAdapter(
  private var mValues: List<Treeitem>,
  private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<MyTreeitemRecyclerViewAdapter.ViewHolder>() {

  var outdentFirst: Boolean = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.fragment_treeitem, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val treeitem = mValues[position]

    holder.mItem = treeitem
    holder.mTreeitemName.text = treeitem.name

    holder.mTreeitemIcon.apply {
      setImageResource(treeitemIcon(treeitem))

      val params = layoutParams as ConstraintLayout.LayoutParams
      params.leftMargin = if (outdentFirst && position > 0) { 50 } else { 0 }
      layoutParams = params
    }

    holder.mView.setOnClickListener {
      holder.mItem?.let {
        mListener?.onListFragmentInteraction(it)
      }
    }

    holder.mView.isLongClickable = true
    holder.mView.setOnLongClickListener {
      holder.mItem?.let {
        mListener?.onListFragmentLongClick(it)
      }
      true
    }

  }

  fun update(items: List<Treeitem>) {
    mValues = items
    notifyDataSetChanged()
  }

  private fun treeitemIcon(treeitem: Treeitem) = Icons.treeitemIcon(treeitem.type)

  override fun getItemCount(): Int {
    return mValues.size
  }

  inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
    val mTreeitemIcon: ImageView
    val mTreeitemName: TextView
    var mItem: Treeitem? = null

    init {
      mTreeitemIcon = mView.treeitem_icon
      mTreeitemName = mView.treeitem_name
    }

    override fun toString(): String {
      return super.toString() + " '" + mTreeitemName.text + "'"
    }
  }
}
