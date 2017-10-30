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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.panel

import android.app.Activity.RESULT_OK
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.DatePickerActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.DateOnly
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_summary.*
import org.jetbrains.anko.startActivityForResult
import java.util.*

class PanelSummarySection: Fragment(),
  TreeitemPanel.Section,
  AdapterView.OnItemSelectedListener {

  private var loaded = false
  override var treeitem: Treeitem? = null
  override lateinit var panel: TreeitemPanel

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater!!.inflate(R.layout.activity_treeitem_panel_section_summary, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    loaded = true

    showTreeitem(treeitem)
  }
//
//  override fun onSaveInstanceState(outState: Bundle?) {
//    super.onSaveInstanceState(outState)
//    outState?.putParcelable(TreeitemPanel.PANEL, panel)
//  }

  override fun onItemSelected(adapter: AdapterView<*>?, view: View?, pos: Int, id: Long) {
    val done = when(pos) {
      0 -> false
      else -> true
    }

    if (done != treeitem?.is_done) {
      panel.setTreeitem(Treeitem.IS_DONE to done)
    }
  }

  override fun onNothingSelected(p0: AdapterView<*>?) { }

  override fun showTreeitem(treeitem: Treeitem?) {
    this.treeitem = treeitem

    if (!loaded) { return }

    panel.field(panel_textView_item_type, panel_textView_item_type_label)
    panel.field(panel_editText_description, panel_editText_description_label)
    panel.field(panel_checkbox_is_on_hold, panel_textView_is_on_hold_label)
    panel.field(panel_editText_description, panel_editText_description_label)
    panel.field(panel_button_delay_until, panel_editText_delay_until_label) {
      it.setOnClickListener {
        updateDate(REQ_PICK_DELAY_UNITL, { treeitem?.delay_until })
      }
    }
    panel.field(panel_button_deadline, panel_editText_deadline_label) {
      it.setOnClickListener {
        updateDate(REQ_PICK_PROMISE_BY, { treeitem?.promise_by })
      }
    }
    panel.field(panel_button_milestone_date, panel_textView_milestone_date_label) {
      it.setOnClickListener {
        updateDate(REQ_PICK_DATE, { treeitem?.date }, allowClear = false)
      }
    }

    panel_spinner_status?.apply {
      adapter = ArrayAdapter.createFromResource(
        activity,
        R.array.treeitem_status,
        android.R.layout.simple_spinner_dropdown_item
      )
      onItemSelectedListener = this@PanelSummarySection
      setSelection(when (treeitem?.is_done) {
        true -> 1
        else -> 0
      })
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (Pair(resultCode, requestCode)) {
      RESULT_OK to REQ_PICK_DELAY_UNITL -> updateDate(data, Treeitem.DELAY_UNITL)
      RESULT_OK to REQ_PICK_PROMISE_BY -> updateDate(data, Treeitem.PROMISE_BY)
      RESULT_OK to REQ_PICK_DATE -> updateDate(data, Treeitem.DATE)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  fun updateDate(req: Int, get: () -> Date?, allowClear: Boolean = true) {
    val date = Calendar.getInstance().apply { get()?.let { time = it } }
    startActivityForResult<DatePickerActivity>(req,
      DatePickerActivity.DATE to date,
      DatePickerActivity.ALLOW_CLEAR to allowClear
    )
  }

  fun updateDate(data: Intent?, set: String)
    = (data?.getSerializableExtra(DatePickerActivity.DATE) as Date?)?.let {
      panel.setTreeitem(set to DateOnly().apply { time = it.time })
    } ?: panel.setTreeitem(mapOf(set to (null as Date?)))

  companion object {
    val REQ_PICK_DELAY_UNITL = 1
    val REQ_PICK_PROMISE_BY = 2
    val REQ_PICK_DATE = 3
  }
}

