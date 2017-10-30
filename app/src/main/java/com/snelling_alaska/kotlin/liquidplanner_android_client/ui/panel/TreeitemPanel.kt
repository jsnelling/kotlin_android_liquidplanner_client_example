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

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.TreeitemDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import kotlinx.android.synthetic.main.activity_treeitem_panel.*
import org.jetbrains.anko.*

class TreeitemPanel: AppCompatActivity,
  TabHost.OnTabChangeListener,
  View.OnFocusChangeListener,
  Parcelable {

  constructor() : super()

  private var treeitem: Treeitem? = null
    set(value) {
      field = value
      showTreeitem()
    }

  private val fields = PanelFields<Treeitem>(
    PanelAllowedFields.treeitemFields,
    { _, update -> setTreeitem(update) },
    null,
    this
  )

  lateinit var sectionHeader: PanelHeaderSection

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_treeitem_panel)

    contentView?.onFocusChangeListener = this

    sectionHeader = fragmentManager.findFragmentById(R.id.section_header) as PanelHeaderSection
    sectionHeader.panel = this

    intent?.let {
      if (it.hasExtra(TREEITEM)) {
        val ti: Treeitem = it.getParcelableExtra(TREEITEM)
        treeitem = ti
        Current.space?.let { space ->
          TreeitemDao.loadLuggage(space, ti) {
            treeitem = it
          }
        }
      }
    }

    setupTabs()
  }

  //------------------------------------------------------------------------------------------------

  fun showTreeitem() {
    fields.model = treeitem
    sectionHeader.showTreeitem(treeitem)
    currentSection?.let {
      if (it is Section) { it.showTreeitem(treeitem) }
    }
  }

  //------------------------------------------------------------------------------------------------

  fun allowSection(id: Int) = when(id) {
    R.id.section_checklist -> allowChecklists()
    else -> true
  }

  fun allowChecklists() = when (treeitem?.type) {
    "Task" -> true
    else -> false
  }

  //------------------------------------------------------------------------------------------------

  val sections = mapOf(
    "Summary" to R.id.section_summary,
    "People" to R.id.section_people,
    "Planning" to R.id.section_planning,
    "Checklist" to R.id.section_checklist,
    "Comments" to R.id.section_comments,
    "Notes" to R.id.section_notes,
    "Documents" to R.id.section_documents,
    "Links" to R.id.section_links
  )

  val sectionsRev = sections.entries.associate { (k, v) -> v to k }

  val tabs = mutableMapOf<String, Int>()

  protected fun setupTabs() {
    tabHost.apply {
      setup()

      var first: Int? = Current.getPref<String>(LAST_TAB)?.let { sections[it] }
      var tabI = 0

      sections.forEach { (label, id) ->
        if (allowSection(id)) {
          if (first == null) { first = id }
          addTab(newTabSpec(id.toString()).apply {
            setContent(id)
            setIndicator(label)
          })
          tabs.set(label, tabI)
          tabI++
        } else {
          findViewById<View>(id).visibility = View.GONE
        }
      }

      first?.let { onTabChanged(it.toString()) }

      setOnTabChangedListener(this@TreeitemPanel)
    }
  }

  override fun onTabChanged(tabId: String?) {
    tabId?.let {
      sectionsRev[it.toInt()]?.let {
        Current.setPref(LAST_TAB, it)
        if (tabHost.currentTabTag != tabId) {
          tabs[it]?.let { tabHost.currentTab = it }
        }
      }
    }
    when (tabId?.toInt()) {
      R.id.section_summary ->
        showSection(R.id.section_summary_fragment) { PanelSummarySection() }

      R.id.section_people ->
        showSection(R.id.section_people_fragment) { PanelAssignmentsSection() }

      R.id.section_planning ->
        showSection(R.id.section_planning_fragment) { PanelPlanningSection() }

      R.id.section_checklist ->
        showSection(R.id.section_checklist_fragment) { PanelChecklistItemsSection()}

      R.id.section_comments ->
        showSection(R.id.section_comments_fragment) { PanelCommentsSection() }

      R.id.section_notes ->
        showSection(R.id.section_notes_fragment) { PanelNotesSection() }

      else -> {}
    }
  }

  var currentTabId: Int? = null
  var currentSection: Fragment? = null

  fun showSection(id: Int, getSection:() -> Fragment) {
    val currentSection = getSection()

    fragmentManager
      .beginTransaction()
      .apply {
        currentTabId?.let {
          fragmentManager.findFragmentById(it)?.let {
            remove(it)
          }
        }
      }
      .replace(id, currentSection)
      .commit()

    currentTabId = id

    if (currentSection is Section) {
      currentSection.panel = this
      currentSection.showTreeitem(treeitem)
    }

    this.currentSection = currentSection
  }

  //------------------------------------------------------------------------------------------------

  override fun onFocusChange(view: View?, focused: Boolean) {
    if (view != null && view == contentView && focused) {
      (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(view.windowToken, 0)
    }
  }

  //------------------------------------------------------------------------------------------------

  fun setTreeitem(update: Map<String, Any?>) {
    Current.space?.let { space ->
      treeitem?.let { ti ->
        progress { done ->
          TreeitemDao.updateTreeitem(space, ti, update) {
            it?.let { newTi ->
              if (newTi.parent_id != ti.parent_id ||
                  newTi.package_id != ti.package_id ||
                  newTi.client_id != ti.client_id) {
                TreeitemDao.loadLuggage(space, ti) {
                  treeitem = it
                  done()
                }
              } else {
                treeitem = it.copy(
                  package_ti = ti.package_ti,
                  parent = ti.parent
                )
                done()
              }
            }
          }
        }
      }
    }
  }

  fun setTreeitem(update: Pair<String, Any?>) = setTreeitem(mapOf(update))

  fun progress(cb: (() -> Unit) -> Unit) {
    progressBarContainer.apply {
      alpha = 0.0f
      visibility = View.VISIBLE
      animate().alpha(1.0f)
    }

    cb({
      progressBarContainer.animate() .alpha(0.0f)
        .setUpdateListener { u -> if (!u.isRunning) {
          progressBarContainer.visibility = View.GONE
        } }
    })
  }

  //------------------------------------------------------------------------------------------------

  override fun onBackPressed() {
    setResult(Activity.RESULT_OK, intentFor<TreeitemPanel>(TREEITEM to treeitem))
    super.onBackPressed()
  }

  //------------------------------------------------------------------------------------------------

  interface Section {
    fun showTreeitem(treeitem: Treeitem?)

    var treeitem: Treeitem?
    var panel: TreeitemPanel
  }

  //------------------------------------------------------------------------------------------------

  fun <K : View> field(
    cell: K,
    label: TextView,
    type: PanelFields.FieldType? = null,
    get: (() -> Any?)? = null,
    set: String? = null,
    readOnly: (() -> Boolean)? = null,
    clearable: (() -> Boolean)? = null,
    icon: View? = null,
    then: ((field: K) -> Unit)? = null
  ) = fields.field(
    treeitem,
    PanelFields.Field(cell, label, type, get, set, readOnly, clearable, icon, then)
  )

  fun editTextUpdater(text: EditText, get: () -> String?, set: String)
    = fields.editTextUpdater(text, get, set)

  fun checkBoxUpdater(check: CheckBox, get: () -> Boolean?, set: String)
    = fields.checkBoxUpdater(check, get, set)

  //------------------------------------------------------------------------------------------------
  // Parcelable
  override fun describeContents(): Int = 0

  override fun writeToParcel(out: Parcel, flags: Int) {
    out.writeParcelable(treeitem, flags)
  }

  constructor(data: Parcel) : super() {
    treeitem = data.readParcelable(classLoader)
  }


  //------------------------------------------------------------------------------------------------

  companion object {
    val TREEITEM = "TREEITEM"
    val PANEL = "PANEL"

    val LAST_TAB = "TREEITEM_PANEL_LAST_TAB"

    val CREATOR = object: Parcelable.Creator<TreeitemPanel> {
      override fun createFromParcel(data: Parcel): TreeitemPanel = TreeitemPanel(data)
      override fun newArray(len: Int): Array<TreeitemPanel> = Array(len) { TreeitemPanel() }
    }

    fun showFrom(activity: AppCompatActivity, treeitem: Treeitem) {
      activity.apply {
        startActivity<TreeitemPanel>(TREEITEM to treeitem)
      }
    }
  }
}