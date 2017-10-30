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

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.HierarchyDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Hierarchy
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.Icons
import kotlinx.android.synthetic.main.activity_shared_picker_list_item.view.*
import java.io.Serializable

class HierarchyListActivity : SharedPickerBase() {
  var adapter: HierarchyListAdapter? = null
  var filter: HierarchyFilter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    intent?.let {
      if (it.hasExtra(FILTER)) {
        filter = it.getSerializableExtra(FILTER) as HierarchyFilter
      }
    }
  }

  override fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Current.space?.let {
      HierarchyDao.loadHierarchy(it) { adapter?.hierarchy = it }
    }

    return HierarchyListAdapter(listOf(), this)
      .apply { adapter = this } as RecyclerView.Adapter<RecyclerView.ViewHolder>
  }

  fun onListFragmentInteraction(hierarchy: Hierarchy) {
    val result = getReturnIntent()
    result.putExtra(SELECTED_HIERARCHY, hierarchy)
    setResult(RESULT_OK, result)
    finish()
  }

  //------------------------------------------------------------------------------------------------

  abstract class HierarchyFilter: Serializable {
    abstract fun isVisible(item: Hierarchy): Boolean
    abstract fun isEnabled(item: Hierarchy): Boolean
    abstract fun pruneDisabled(): Boolean
  }

  //------------------------------------------------------------------------------------------------

  inner class HierarchyListAdapter(
    var items: List<Hierarchy>? = listOf(),
    var listener: HierarchyListActivity
  ): RecyclerView.Adapter<ViewHolder>() {
    var hierarchy: Hierarchy? = null
      set(value) {
        field = value
        refresh()
      }

    fun refresh() {
      val items = mutableListOf<Hierarchy>()
      val filter = filter
      var pruneDisabled = filter?.pruneDisabled() ?: false

      fun markEnabled(hierarchy: Hierarchy?) {
        var els = mutableListOf<String>()
        var el = hierarchy
        while (el != null) {
          els.add(el.name)
          if (el.parent != null && !el.parent!!.enabledChildren) {
            el.parent!!.enabledChildren = true
            el = el.parent
          } else {
            break
          }
        }
        android.util.Log.v("MEE", els.joinToString(" -> "))
      }

      fun recurse(hierarchy: Hierarchy) {
        val (visible, enabled) = if (filter != null) {
          Pair(filter.isVisible(hierarchy), filter.isEnabled(hierarchy))
        } else {
          Pair(true, true)
        }

        hierarchy.enabled = enabled
        if (pruneDisabled) {
          if (enabled) {
            markEnabled(hierarchy)
          }
        } else {
          hierarchy.enabledChildren = hierarchy.children.isNotEmpty()
        }

        if (visible && hierarchy.parent?.expanded != false) { items.add(hierarchy) }

        // is there a btetter thing to do here? perf wise?
        if (hierarchy.expanded || pruneDisabled) {
          hierarchy.children.forEach { recurse(it) }
        }
      }

      hierarchy?.let { recurse(it) }


      this.items = if (pruneDisabled) {
        items.filter { it.enabled || it.enabledChildren }
      } else {
        items
      }

      items.forEach { android.util.Log.v("HIER", "${pruneDisabled}: ${it.name} -> ${it.enabled} -> ${it.enabledChildren}") }

      notifyDataSetChanged()
    }

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

          icon.setImageResource(Icons.treeitemIcon(item.type))

          toggleWrapper.apply {
            val indent = 60 * item.depth
            setPadding(indent, 0, 0, 0)
            visibility = View.VISIBLE
            if (item.children.isEmpty() && !item.enabledChildren) {
              visibility = View.INVISIBLE
            } else {
              visibility = View.VISIBLE
              toggle.apply {
                rotation = if (item.expanded) { 90.0f } else { 0.0f }
                setOnClickListener {
                  item.expanded = !item.expanded
                  refresh()
                }
              }
            }

          }

          mView.apply {
            if (item.enabled) {
              setOnClickListener { listener.onListFragmentInteraction(item) }
              alpha = 1.0f
            } else {
              setOnClickListener(null)
              alpha = 0.5f
            }
          }
        }
      }
    }
  }

  inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
    var item: Hierarchy? = null
    val textView: TextView
    val icon: ImageView
    val toggle: ImageView
    val toggleWrapper: ConstraintLayout

    init {
      textView = mView.text_view
      icon = mView.image_view
      toggle = mView.toggle_button
      toggleWrapper = mView.toggle_button_wrapper
    }
  }

  companion object {
    val SELECTED_HIERARCHY = "SELECTED_HIERARCHY"
    val FILTER = "FILTER"
  }
}