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
import android.text.Editable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.ChecklistItemDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.ChecklistItem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Member
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.MemberListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.SharedPickerBase
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.let
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_checklist_items.*
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_checklist_items_list_item.view.*
import org.jetbrains.anko.startActivityForResult

class PanelChecklistItemsSection : Fragment(),
  TreeitemPanel.Section,
  View.OnFocusChangeListener,
  TextView.OnEditorActionListener {

  private var adapter: ChecklistItemListAdapter? = null
  private var loaded = false
  override var treeitem: Treeitem? = null
  override lateinit var panel: TreeitemPanel

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
    savedInstanceState?.let {
      if (it.containsKey(SAVE_PANEL)) {
        panel = it.getParcelable(SAVE_PANEL)
      }
    }

    return inflater!!.inflate(R.layout.activity_treeitem_panel_section_checklist_items, container, false)
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

    adapter = ChecklistItemListAdapter(mutableListOf(), this)
    recycler.adapter = adapter

    loaded = true

    editText_newTitle.apply {
      onFocusChangeListener = this@PanelChecklistItemsSection
      setOnEditorActionListener(this@PanelChecklistItemsSection)
    }

    button_addItem.setOnClickListener { handleAddNew() }

    showTreeitem(treeitem)
  }

  override fun showTreeitem(treeitem: Treeitem?) {
    this.treeitem = treeitem

    if (!loaded) { return }

    let(adapter, treeitem) { adapter, treeitem ->
      adapter.items = treeitem.checklist_items?.toMutableList()
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
    if (view != null && view == this.view && focused) { hideKeyboard(view) }
  }

  private fun showKeyboard(view: View)
    = (activity.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
      .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

  private fun hideKeyboard(view: View)
    = (activity.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
    .hideSoftInputFromWindow(view.windowToken, 0)

  //------------------------------------------------------------------------------------------------

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (Pair(resultCode, requestCode)) {
      RESULT_OK to REQ_SELECT_OWNER -> handleChangeOwner(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  fun handleAddNew() {
    let(Current.space, treeitem, editText_newTitle) { space, treeitem, newTitle ->
      val title = newTitle.text.toString()

      if (title.isNotEmpty()) {
        ChecklistItemDao.createChecklistItem(space, treeitem, title) { item ->
          editText_newTitle.text.clear()
          item?.let { adapter?.items?.add(it) }
          loadChecklists(space) { items ->
            let(items, item) { items, item ->
              recycler_view.layoutManager
                .scrollToPosition(items.indexOfFirst { it.id == item.id })
            }
          }
        }
      }

      editText_newTitle.apply {
        requestFocus()
        showKeyboard(this)
      }
    }
  }

  fun handleChangeName(checklistItem: ChecklistItem, name: String)
    = updateAndLoadChecklists(checklistItem, ChecklistItem.NAME to name)

  fun handleChangeOwner(checklistItem: ChecklistItem)
    = startActivityForResult<MemberListActivity>(
      REQ_SELECT_OWNER,
      SharedPickerBase.EXTRAS to Bundle().apply {
        putParcelable(CHECKLIST_ITEM, checklistItem)
      }
    )

  fun handleChangeOwner(data: Intent?) = data?.let {
    val member: Member = data.getParcelableExtra(MemberListActivity.SELECTED_MEMBER)
    val item: ChecklistItem = data.getParcelableExtra(CHECKLIST_ITEM)

    updateAndLoadChecklists(item, ChecklistItem.OWNER_ID to member.id)
  }

  fun handleChangeDone(item: ChecklistItem, done: Boolean)
    = updateAndLoadChecklists(item, ChecklistItem.COMPLETED to done)


  fun handleDelete(checklistItem: ChecklistItem) {
    Current.space?.let { space ->
      ChecklistItemDao.deleteChecklistItem(space, checklistItem) {
        adapter?.items?.remove(checklistItem)
        loadChecklists(space)
      }
    }
  }

  private fun updateAndLoadChecklists(item: ChecklistItem, update: Pair<String, Any>) {
    Current.space?.let { space ->
      ChecklistItemDao.updateChecklistItem(space, item, mapOf(update)) { loadChecklists(space) }
    }
  }

  private fun loadChecklists(workspace: Workspace, then: ((List<ChecklistItem>?) -> Unit)? = null)
    = let(treeitem, adapter) { treeitem, adapter ->
      ChecklistItemDao.loadChecklistItems(workspace, treeitem) {
        adapter.items = it?.toMutableList()
        treeitem.checklist_items = it
        adapter.notifyDataSetChanged()
        then?.invoke(it)
      }
    }

  //------------------------------------------------------------------------------------------------

  inner class ChecklistItemListAdapter(
    var items: MutableList<ChecklistItem>? = mutableListOf(),
    var section: PanelChecklistItemsSection
  ): RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount() = items?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(
          R.layout.activity_treeitem_panel_section_checklist_items_list_item,
          parent,
          false
        )
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.apply {
        item = items?.get(position)
        item?.let { item ->

          doneCheckbox.apply {
            isChecked = item.completed
            setOnCheckedChangeListener { _, checked -> section.handleChangeDone(item, checked) }
          }

          titleEditText.apply {
            text = Editable.Factory.getInstance().newEditable(item.name)

            setOnFocusChangeListener { _, focused ->
              if (!focused && text.toString() != item.name) {
                section.handleChangeName(item, text.toString())
              }
            }
          }

          ownerButton.apply {
            text = if (item.owner_id != ChecklistItem.ALL_OWNERS_ID) {
              Current.space?.membersById?.get(item.owner_id)?.name ?: "UNKNOWN"
            } else {
              "(All Owners)"
            }

            setOnClickListener { section.handleChangeOwner(item) }
          }

          deleteButton.setOnClickListener { section.handleDelete(item) }
        }
      }
    }
  }

  inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
    var item: ChecklistItem? = null
    val doneCheckbox = mView.checkBox_isDone
    val ownerButton = mView.button_owner
    val titleEditText = mView.editText_name
    val deleteButton = mView.imageButton_delete
  }

  //------------------------------------------------------------------------------------------------

  companion object {
    val SAVE_PANEL = "SAVE_PANEL"
    val CHECKLIST_ITEM = "CHECKLIST_ITEM"

    val REQ_SELECT_OWNER = 1
  }

}