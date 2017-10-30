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
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Comment
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.toRequestBody

typealias Comments = List<Comment>
typealias CommentCb = (Comment?) -> Unit
typealias CommentsCb = (Comments?) -> Unit

object CommentDao {
  fun loadComments(workspace: Workspace, then: CommentsCb) {
    val url = Api.shared.urlBuilder("workspaces/${ workspace.id }/comments")
      .addQueryParameter("limit", "350")
      .addQueryParameter("include", "treeitem")

    Api.shared
      .get<Comment>(url)
      .fetchA(then)
  }

  fun loadComments(treeitem: Treeitem, then: CommentsCb)
    = loadComments(treeitem.id, treeitem.space_id, then)

  fun loadComments(treeitemId: Int, workspaceId: Int, then: CommentsCb) {
    Api.shared
      .get<Comment>("workspaces/${workspaceId}/treeitems/${treeitemId}/comments")
      .fetchA(then)
  }


  fun createComment(comment: Comment, then: CommentCb) {
    Api.shared
      .post<Comment>(
        "workspaces/${comment.space_id}/treeitems/${comment.item_id}/comments",
        mapOf(
          "comment" to mapOf<String, Any>(
            "comment" to comment.comment
          )
        ).toRequestBody()
      )
      .fetch(then)
  }

  fun updateComment(comment: Comment, then: CommentCb) {
    Api.shared
      .put<Comment>(
        "workspaces/${comment.space_id}/comments/${comment.id}",
        mapOf(
          "comment" to mapOf<String, Any>(
          "comment" to comment.comment
          )
        ).toRequestBody()
      )
      .fetch(then)
  }

  fun deleteComment(comment: Comment, then:() -> Unit) {
    Api.shared
      .delete<Comment>("workspaces/${comment.space_id}/comments/${comment.id}")
      .fetch { then() }
  }
}

