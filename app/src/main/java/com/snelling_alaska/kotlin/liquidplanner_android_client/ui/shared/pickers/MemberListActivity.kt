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

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Member
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.MemberOrTeam
import kotlinx.android.synthetic.main.activity_shared_picker_list_item.view.*

class MemberListActivity: SharedPickerBase() {
  var adapter: PersonListAdapter? = null

  override fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
    val items = Current.space?.members ?: listOf()

    return PersonListAdapter(items, this)
      .apply { adapter = this } as RecyclerView.Adapter<RecyclerView.ViewHolder>
  }

  fun onListFragmentInteraction(member: Member) {
    val result = getReturnIntent()
    result.putExtra(SELECTED_MEMBER, member)
    setResult(RESULT_OK, result)
    finish()
  }

  //------------------------------------------------------------------------------------------------

  inner class PersonListAdapter(
    var items: List<Member>? = listOf(),
    var listener: MemberListActivity
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
          textView.text = item.user_name
          Current.avatar(imageView.context, item) { imageView.setImageBitmap(it) }

          mView.setOnClickListener { listener.onListFragmentInteraction(item) }
        }
      }
    }
  }

  inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
    var item: Member? = null
    val imageView: ImageView
    val textView: TextView

    init {
      imageView = mView.image_view
      textView = mView.text_view
    }
  }

  companion object {
    val SELECTED_MEMBER = "SELECTED_MEMBER"
    val SELECTED_TEAM = "SELECTED_TEAM"

    fun getSelection(data: Intent?) = data?.let {
      if (it.hasExtra(SELECTED_MEMBER)) {
        MemberOrTeam(it.getParcelableExtra(SELECTED_MEMBER),null)
      } else {
        MemberOrTeam(null, it.getParcelableExtra(SELECTED_TEAM))
      }
    }
  }
}
