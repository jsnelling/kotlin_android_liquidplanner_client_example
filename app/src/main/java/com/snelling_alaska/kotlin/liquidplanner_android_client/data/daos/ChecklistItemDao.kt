package com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos

import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Api
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.ChecklistItem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.toRequestBody

object ChecklistItemDao {
  fun loadChecklistItems(
    workspace: Workspace,
    treeitem: Treeitem,
    then: (List<ChecklistItem>?) -> Unit
  ) {
    Api.shared
      .get<ChecklistItem>(checklistItemUrl(workspace, treeitem.id))
      .fetchA(then)
  }

  fun createChecklistItem(
    workspace: Workspace,
    treeitem: Treeitem,
    title: String,
    then: (ChecklistItem?) -> Unit
  ) {
    Api.shared
      .post<ChecklistItem>(checklistItemUrl(workspace, treeitem.id),
        mapOf( "checklist_item" to mapOf(
          ChecklistItem.NAME to title
        )).toRequestBody(true))
      .fetch(then)
  }

  fun updateChecklistItem(
    workspace: Workspace,
    checklistItem: ChecklistItem,
    update: Map<String, Any?>,
    then: (ChecklistItem?) -> Unit
  ) {
    Api.shared
      .put<ChecklistItem>(checklistItemUrl(workspace, checklistItem.item_id, checklistItem.id),
        mapOf( "checklist_item" to update).toRequestBody(true))
      .fetch(then)
  }

  fun deleteChecklistItem(workspace: Workspace, checklistItem: ChecklistItem, then: () -> Unit) {
    Api.shared
      .delete<ChecklistItem>(checklistItemUrl(workspace, checklistItem.item_id, checklistItem.id))
      .fetch { then() }
  }

  fun checklistItemUrl(workspace: Workspace, tiId: Int, clId: Int? = null)
    = "workspaces/${ workspace.id }/treeitems/${ tiId }/checklist_items${
        clId?.let { "/$it" } ?: "" }"
}
