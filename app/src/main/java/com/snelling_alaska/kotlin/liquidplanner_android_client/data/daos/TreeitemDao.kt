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

package com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos

import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Api
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.TrackProgress
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.toRequestBody

object TreeitemDao {
  fun findTreeitems(workspace: Workspace, ids: List<Int>, then: (List<Treeitem>?) -> Unit) {
    val url = Api.shared.urlBuilder("workspaces/${ workspace.id }/treeitems")
      .addQueryParameter("filter[]", "id=${ids.joinToString(",")}")

    Api.shared
      .get<Treeitem>(url)
      .fetchA(then)
  }

  fun getChildrenOfTreeitem(workspace: Workspace, item: Treeitem?, then: (List<Treeitem>?) -> Unit) {
    val url = Api.shared.urlBuilder(treeitemUrl(workspace, item?.id))
      .addQueryParameter("depth", "1")
      .addQueryParameter("flat", "true")
      .addQueryParameter("filter[]", "is_done is false")

    Api.shared
      .get<Treeitem>(url)
      .fetchA { it?.let{ then(it.drop(1)) } }
  }

  fun quickSearchForTreeitems(workspace: Workspace, text: String, then: (List<Treeitem>?) -> Unit) {
    val url = Api.shared.urlBuilder(treeitemUrl(workspace))
      .addQueryParameter("filter[]", "name contains ${text}")

    Api.shared
      .get<Treeitem>(url)
      .fetchA(then)
  }

  fun loadLuggage(workspace: Workspace, treeitem: Treeitem, then: (Treeitem?) -> Unit) {
    val url = Api.shared.urlBuilder(treeitemUrl(workspace, treeitem.id))
      .addQueryParameter("include",
        "comments,checklist_items,parent,package,client,note,estimates")

    Api.shared
      .get<Treeitem>(url)
      .fetch(then)
  }

  fun updateTreeitem(
    workspace: Workspace,
    treeitem: Treeitem,
    update: Map<String, Any?>,
    then: (Treeitem?) -> Unit
  ) {
    Api.shared
      .put<Treeitem>(treeitemUrl(workspace, treeitem.id),
        mapOf( "treeitem" to update).toRequestBody(true))
      .fetch(then)
  }

  fun trackTime(
    workspace: Workspace,
    treeitem: Treeitem,
    trackProgress: TrackProgress,
    then: (Treeitem?) -> Unit
  ) {
    Api.shared
      .post<Treeitem>(treeitemUrl(workspace, treeitem.id, "track_time"),
        mapOf(
          TrackProgress.MEMBER_ID to trackProgress.person?.id,
          TrackProgress.WORK to trackProgress.time,
          TrackProgress.ACTIVITY_ID to requireNotNull(trackProgress.activity).id,
          TrackProgress.LOW to trackProgress.low,
          TrackProgress.HIGH to trackProgress.high,
          TrackProgress.IS_DONE to trackProgress.is_done,
          TrackProgress.COMMENT to trackProgress.comment,
          TrackProgress.NOTE to trackProgress.note
        ).toRequestBody())
      .fetch(then)
  }

  fun treeitemUrl(workspace: Workspace, tiId: Int? = null, path: String? = null)
    = "workspaces/${ workspace.id }/treeitems${
        tiId?.let { "/${ it }"} ?: ""
       }${
        path?.let { "/${ it }"} ?: ""
       }"
}


