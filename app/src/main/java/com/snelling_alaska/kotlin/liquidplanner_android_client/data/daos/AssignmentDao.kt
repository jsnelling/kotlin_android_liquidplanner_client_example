package com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos

import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Api
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Assignment
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.toRequestBody

object AssignmentDao {
  fun createAssignment(
    workspace: Workspace,
    treeitem: Treeitem,
    create: Map<String, Any?>,
    then: (List<Assignment>?) -> Unit
  ) {
    Api.shared
      .post<Treeitem>(assignmentUrl(workspace, treeitem.id), create.toRequestBody(true))
      .fetch { it?.let { then(it.assignments) } }
  }

  fun updateAssignment(
    workspace: Workspace,
    assignment: Assignment,
    update: Map<String, Any?>,
    then: (List<Assignment>?) -> Unit
  ) {
    Api.shared
      .post<Treeitem>(assignmentUrl(workspace, assignment.treeitem_id),
        mapOf("assignment_id" to assignment.id).plus(update).toRequestBody(true)
      )
      .fetch { it?.let { then(it.assignments) } }
  }

  fun destroyAssignment(
    workspace: Workspace,
    assignment: Assignment,
    then: () -> Unit
  ) {
    Api.shared
      .delete<Assignment>(assignmentUrl(workspace, assignment.treeitem_id, assignment.id))
      .fetch { then() }
  }

  fun assignmentUrl(workspace: Workspace, tiId: Int, assignmentId: Int)
    = assignmentUrl(workspace, tiId, "assignments/${ assignmentId }")

  fun assignmentUrl(workspace: Workspace, tiId: Int, path: String = "update_assignment")
    = "workspaces/${ workspace.id }/treeitems/${ tiId }/${ path }"
}