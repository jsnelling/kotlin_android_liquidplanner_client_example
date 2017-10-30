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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.AssignmentDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.TreeitemDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.*
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.LogProgressActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.RemainingEffortActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.ActivityListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.MemberListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.SharedPickerBase
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.let
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_assignments.*
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_assignments_list_item.view.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivityForResult

class PanelAssignmentsSection : Fragment(),
  TreeitemPanel.Section,
  View.OnFocusChangeListener,
  TextView.OnEditorActionListener {

  private var adapter: AssignmentListAdapter? = null
  private var loaded = false
  override var treeitem: Treeitem? = null
  override lateinit var panel: TreeitemPanel

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
    savedInstanceState?.let {
      if (it.containsKey(SAVE_PANEL)) {
        panel = it.getParcelable(SAVE_PANEL)
      }
    }

    return inflater!!.inflate(R.layout.activity_treeitem_panel_section_assignments, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    outState?.putParcelable(SAVE_PANEL, panel)
    super.onSaveInstanceState(outState)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recycler = recycler_view

    val context = recycler.context
    recycler.layoutManager = LinearLayoutManager(context)

    adapter = AssignmentListAdapter(mutableListOf(), this)
    recycler.adapter = adapter

    loaded = true

    button_addItem.setOnClickListener { handleAddNew() }

    showTreeitem(treeitem)
  }

  override fun showTreeitem(treeitem: Treeitem?) {
    this.treeitem = treeitem

    if (!loaded) {
      return
    }

    let(adapter, treeitem) { adapter, treeitem ->
      adapter.items = treeitem.assignments
        ?.map { it.copy(treeitem = treeitem) }
        ?.toMutableList()
        ?: mutableListOf()
      adapter.notifyDataSetChanged()
    }
  }

  //------------------------------------------------------------------------------------------------

  override fun onEditorAction(view: TextView?, actionId: Int, KeyEvent: KeyEvent?): Boolean
    = when (actionId) {
    EditorInfo.IME_ACTION_DONE -> {
      handleAddNew()
      true
    }
    else -> false
  }

  override fun onFocusChange(view: View?, focused: Boolean) {
    if (view != null && view == this.view && focused) {
      hideKeyboard(view)
    }
  }

  private fun showKeyboard(view: View)
    = (activity.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
    .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

  private fun hideKeyboard(view: View)
    = (activity.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
    .hideSoftInputFromWindow(view.windowToken, 0)

  //------------------------------------------------------------------------------------------------

  fun updateAssignments() = let(Current.space, treeitem) { workspace, ti ->
    // TODO it'd be nice to avoid this extra call
    TreeitemDao.loadLuggage(workspace, ti) {
      showTreeitem(it)
    }
  }

  fun set(assignment: Assignment, update: Map<String, Any?>) {
    Current.space?.let { space ->
      panel.progress { done ->
        AssignmentDao.updateAssignment(space, assignment, update) {
          updateAssignments()
          done()
        }
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (Pair(resultCode, requestCode)) {
      RESULT_OK to REQ_SELECT_OWNER -> handleChangeOwner(data)
      RESULT_OK to REQ_SELECT_ACTIVITY -> handleChangeActivity(data)
      RESULT_OK to REQ_SELECT_NEW_OWNER -> handleAddNew(data)
      RESULT_OK to REQ_LOG_PROGRESS -> handleLogProgress(data)
      RESULT_OK to REQ_CHANGE_ESTIMATE -> handleChangeEstimate(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  fun handleAddNew() {
    startActivityForResult<MemberListActivity>(REQ_SELECT_NEW_OWNER, "" to "")
  }

  fun handleAddNew(data: Intent?) {
    val memberOrTeam = MemberListActivity.getSelection(data)
    panel.progress { done ->
      let(Current.space, treeitem, memberOrTeam) { space, treeitem, memberOrTeam ->
        AssignmentDao.createAssignment(space, treeitem, mapOf(
          Assignment.PERSON_ID to memberOrTeam.member?.id,
          Assignment.TEAM_ID to memberOrTeam.team?.id
        )) {
          updateAssignments()
          done()
        }
      }
    }
  }


  fun handleChangeOwner(assignment: Assignment, clear: Boolean) {
    if (clear) {
      if (assignment.person_id == Member.UNASSINGED_ID && assignment.can_destroy) {
        Current.space?.let { space ->
          panel.progress { done ->
            AssignmentDao.destroyAssignment(space, assignment) {
              updateAssignments()
              done()
            }
          }
        }
      } else {
        set(assignment, mapOf(
          Assignment.PERSON_ID to Member.UNASSINGED_ID,
          Assignment.TEAM_ID to null
        ))
      }
    } else {
      startActivityForResult<MemberListActivity>(
        REQ_SELECT_OWNER,
        SharedPickerBase.EXTRAS to Bundle().apply {
          putParcelable(ASSIGNMENT, assignment)
        }
      )
    }
  }

  fun handleChangeOwner(data: Intent?) = data?.let { data ->
    val assignment = data.getParcelableExtra<Assignment>(ASSIGNMENT)
    val memberOrTeam = MemberListActivity.getSelection(data)

    set(assignment, mapOf(
      Assignment.PERSON_ID to memberOrTeam?.member?.id,
      Assignment.TEAM_ID to memberOrTeam?.team?.id
    ))
  }

  fun handleLogProgress(assignment: Assignment) = Current.space?.let { space ->
    val person = space.getMemberOrTeam(assignment)?.member?.let { it }
    val activity = assignment.activity_id?.let { space.activitiesById?.get(it) }
    val intent = intentFor<LogProgressActivity>(
      PERSON to person,
      LogProgressActivity.ACTIVITY to activity
    )

    startActivityForResult(intent, REQ_LOG_PROGRESS)
  }

  fun handleChangeEstimate(assignment: Assignment) = Current.space?.let { space ->
    val person = space.getMemberOrTeam(assignment)?.member?.let { it }

    val (low, high, dailyLimit) = if (assignment.estimated) {
      Triple(
        assignment.low_effort_remaining,
        assignment.high_effort_remaining,
        assignment.daily_limit
      )
    } else {
      Triple(null, null, null)
    }

    var data = intentFor<RemainingEffortActivity>(
      RemainingEffortActivity.LOW to low,
      RemainingEffortActivity.HIGH to high,
      RemainingEffortActivity.DAILY_LIMIT to dailyLimit,
      SharedPickerBase.EXTRAS to bundleOf(
        ASSIGNMENT to assignment
      )
    )
    startActivityForResult(data, REQ_CHANGE_ESTIMATE)
  }

  fun handleChangeEstimate(data: Intent?) = let(Current.space, data) { space, data ->
    val result = data.getParcelableExtra<TrackProgress>(RemainingEffortActivity.ESTIMATE_RESULT)
    val assignment = data.getParcelableExtra<Assignment>(ASSIGNMENT)

    panel.progress { done ->
      AssignmentDao.updateAssignment(space, assignment, mapOf(
        Assignment.LOW to result.low,
        Assignment.HIGH to result.high,
        Assignment.DAILY_LIMIT to result.dailyLimit
      )) {
        updateAssignments()
        done()
      }
    }
  }


  fun handleLogProgress(data: Intent?) = data?.let { data ->
    val progress = data.getParcelableExtra<TrackProgress>(LogProgressActivity.TRACK_PROGRESS_RESULT)


    let(Current.space, treeitem) { workspace, treeitem ->
      panel.progress { done ->
        TreeitemDao.trackTime(workspace, treeitem, progress) {
          updateAssignments()
          done()
        }
      }
    }
  }

  fun handleChangeActivity(assignment: Assignment, clear: Boolean) {
    if (clear) {
      set(assignment, mapOf(Assignment.ACTIVITY_ID to null))
    } else {
      startActivityForResult<ActivityListActivity>(
        REQ_SELECT_ACTIVITY,
        SharedPickerBase.EXTRAS to Bundle().apply {
          putParcelable(ASSIGNMENT, assignment)
        }
      )
    }
  }

  fun handleChangeActivity(data: Intent?) = data?.let {
    val activity = it.getParcelableExtra<Activity>(ActivityListActivity.SELECTED_ACTIVITY)
    val assignment = it.getParcelableExtra<Assignment>(ASSIGNMENT)

    set(assignment, mapOf(Assignment.ACTIVITY_ID to activity.id))
  }

  //------------------------------------------------------------------------------------------------

  inner class AssignmentListAdapter(
    var items: MutableList<Assignment>? = mutableListOf(),
    var section: PanelAssignmentsSection
  ): RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount() = items?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(
          R.layout.activity_treeitem_panel_section_assignments_list_item,
          parent,
          false
        )
      return ViewHolder(view).apply {
        fields = PanelFields<Assignment>(
          PanelAllowedFields.assignmentFields,
          { a, u -> set(requireNotNull(a), u) },
          null,
          activity
        )
      }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.apply {
        item = items?.get(position)
        item?.let { item ->
          fields.model = item

          field(doneCheckbox, null)
          field(ownerButton, null) {
            it.setOnClickListener { clear -> handleChangeOwner(item, clear) }
          }
          field(loggedButton, null) {
            it.setOnClickListener { handleLogProgress(item) }
          }
          field(activityButton, null) {
            it.setOnClickListener { clear -> handleChangeActivity(item, clear) }
          }
          field(startELabel, null)
          field(finishELabel, null)
          field(estimateButton, null) {
            it.setOnClickListener { handleChangeEstimate(item) }
          }
        }
      }
    }
  }

  inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
    lateinit var fields: PanelFields<Assignment>
    var item: Assignment? = null
    val doneCheckbox: CheckBox = mView.checkBox_isDone
    val ownerButton: ClearableButtonView = mView.button_owner
    val loggedButton: Button = mView.button_logged
    val estimateButton: ClearableButtonView = mView.button_estimate
    val activityButton: ClearableButtonView = mView.button_activity
    val dragHandle: ImageView = mView.imageView_drag
    val startELabel: TextView = mView.textView_start_e
    val finishELabel: TextView = mView.textView_finish_e

    fun <K : View> field(
      cell: K,
      label: TextView?,
      type: PanelFields.FieldType? = null,
      get: (() -> Any?)? = null,
      set: String? = null,
      readOnly: (() -> Boolean)? = null,
      clearable: (() -> Boolean)? = null,
      icon: View? = null,
      then: ((field: K) -> Unit)? = null
    ) = fields.field(
      fields.model,
      PanelFields.Field(cell, label, type, get, set, readOnly, clearable, icon, then)
    )
  }

  //------------------------------------------------------------------------------------------------

  companion object {
    val SAVE_PANEL = "SAVE_PANEL"
    val ASSIGNMENT = "ASSIGNMENT"
    val PERSON = LogProgressActivity.PERSON

    val REQ_SELECT_OWNER = 1
    val REQ_SELECT_ACTIVITY = 2
    val REQ_SELECT_NEW_OWNER = 3
    val REQ_LOG_PROGRESS = 4
    val REQ_CHANGE_ESTIMATE = 5
  }

}