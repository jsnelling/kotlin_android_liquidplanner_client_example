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
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Activity
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Client
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Hierarchy
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem.Type
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.ActivityListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.ClientListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.HierarchyListActivity
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_planning.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.startActivityForResult

class PanelPlanningSection : Fragment(),
  TreeitemPanel.Section {

  private var loaded = false
  override var treeitem: Treeitem? = null
  override lateinit var panel: TreeitemPanel

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
    savedInstanceState?.let {
      if (it.containsKey(SAVE_PANEL)) {
        panel = it.getParcelable(SAVE_PANEL)
      }
    }

    return inflater!!.inflate(R.layout.activity_treeitem_panel_section_planning, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    outState?.putParcelable(SAVE_PANEL, panel)
    super.onSaveInstanceState(outState)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    loaded = true

    showTreeitem(treeitem)
  }

  override fun showTreeitem(treeitem: Treeitem?) {
    this.treeitem = treeitem

    if (!loaded) { return }

    panel.field(panel_button_package, panel_textView_package_label,
      icon = panel_textView_package_icon) {
      it.setOnClickListener(handleChangePackage)
    }
    panel.field(panel_button_project, panel_textView_project_label,
      icon = panel_textView_project_icon) {
      it.setOnClickListener(handleChangeProject)
    }
    panel.field(panel_button_client, panel_textView_client_label,
      icon = panel_textView_client_icon) {
      it.setOnClickListener(handleChangeClient)
    }
    panel.field(panel_button_activity, panel_textView_activity_label) {
      it.setOnClickListener(handleChangeActivity)
    }
    panel.field(panel_button_started_on, panel_textView_started_on_label)
    panel.field(panel_editText_max_effort, panel_textView_max_effort_label)
    panel.field(panel_editText_manual_alert, panel_textView_manual_alert_label)

    toggleButton_itemDetails.onCheckedChange { buttonView, isChecked ->
      tableLayout_itemDetails.visibility = if (isChecked) { View.VISIBLE } else { View.GONE }
    }
  }

  //------------------------------------------------------------------------------------------------

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (Pair(resultCode, requestCode)) {
      RESULT_OK to REQ_PICK_PACKAGE -> handleChangePackage(data)
      RESULT_OK to REQ_PICK_PROJECT -> handleChangeProject(data)
      RESULT_OK to REQ_PICK_CLIENT -> handleChangeClient(data)
      RESULT_OK to REQ_PICK_ACTIVITY -> handleChangeActivity(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  private val handleChangePackage = { clear: Boolean ->
    if (clear) {
      handleChangePackage(null)
    } else {
      startActivityForResult<HierarchyListActivity>(
        REQ_PICK_PACKAGE,
        HierarchyListActivity.FILTER to object: HierarchyListActivity.HierarchyFilter() {
          override fun isVisible(item: Hierarchy) = true
          override fun isEnabled(item: Hierarchy) = item.isA(Type.Package)
          override fun pruneDisabled() = true
        })
    }
  }

  private fun handleChangePackage(data: Intent?) {
    val target = if (treeitem?.package_ti == null && Type.Package.isA(treeitem?.parent)) {
      Treeitem.PARENT_ID
    } else {
      Treeitem.PACKAGE_ID
    }

    data?.let {
      val ti: Hierarchy = it.getParcelableExtra(HierarchyListActivity.SELECTED_HIERARCHY)

      panel.setTreeitem(target to ti.id)
    } ?: panel.setTreeitem(target to null)
  }

  private val handleChangeProject = { clear: Boolean ->
    if (clear) {
      handleChangeProject(null)
    } else {
      startActivityForResult<HierarchyListActivity>(
        REQ_PICK_PROJECT,
        HierarchyListActivity.FILTER to object : HierarchyListActivity.HierarchyFilter() {
          override fun isVisible(item: Hierarchy) = true
          override fun isEnabled(item: Hierarchy) = !item.isA(Type.Package, Type.Inbox, Type.Root)
          override fun pruneDisabled() = true
        })
    }
  }

  private fun handleChangeProject(data: Intent?) {
    data?.let {
      val ti: Hierarchy = it.getParcelableExtra(HierarchyListActivity.SELECTED_HIERARCHY)

      panel.setTreeitem(Treeitem.PARENT_ID to ti.id)
    } ?: panel.setTreeitem(Treeitem.PARENT_ID to null)
  }

  private val handleChangeClient = { clear: Boolean ->
    if (clear) {
      handleChangeClient(null)
    } else {
      startActivityForResult<ClientListActivity>(REQ_PICK_CLIENT, "" to "")
    }
  }

  private fun handleChangeClient(data: Intent?) {
    data?.let {
      val ti: Client = it.getParcelableExtra(ClientListActivity.SELECTED_CLIENT)

      panel.setTreeitem(Treeitem.CLIENT_ID to ti.id)
    } ?: panel.setTreeitem(Treeitem.CLIENT_ID to null)
  }

  private val handleChangeActivity = { clear: Boolean ->
    if (clear) {
      handleChangeActivity(null)
    } else {
      startActivityForResult<ActivityListActivity>(REQ_PICK_ACTIVITY, "" to "")
    }
  }

  private fun handleChangeActivity(data: Intent?) {
    data?.let {
      val activity: Activity = it.getParcelableExtra(ActivityListActivity.SELECTED_ACTIVITY)

      panel.setTreeitem(Treeitem.ACTIVITY_ID to activity.id)
    } ?: panel.setTreeitem(Treeitem.ACTIVITY_ID to null)
  }

  //------------------------------------------------------------------------------------------------

  companion object {
    val SAVE_PANEL = "SAVE_PANEL"

    val REQ_PICK_PACKAGE = 1
    val REQ_PICK_CLIENT = 2
    val REQ_PICK_PROJECT = 3
    val REQ_PICK_ACTIVITY = 4
  }
}