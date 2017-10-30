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
import android.content.Context
import android.content.Intent
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.CommentDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Comment
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.comments.CommentListFragment
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.comments.CreateCommentActivity
import org.jetbrains.anko.startActivityForResult

class PanelCommentsSection : CommentListFragment(),
  CommentListFragment.OnListFragmentInteractionListener,
  TreeitemPanel.Section {

  override fun onAttach(context: Context?) {
    mListener = this
    super.onAttach(context)
  }

  override fun showTreeitem(treeitem: Treeitem?) {
    this.treeitem = treeitem

    panel.progress { done -> reloadComments { done() } }
  }

  override lateinit var panel: TreeitemPanel
  override var treeitem: Treeitem? = null

  //------------------------------------------------------------------------------------------------

  override fun onCommentEdit(comment: Comment) {
    startActivityForResult<CreateCommentActivity>(
      EDIT_COMMENT_REQ,
      CreateCommentActivity.COMMENT to comment,
      CreateCommentActivity.EDITING to true
    )
  }

  override fun onCommentReply(comment: Comment) {
    startActivityForResult<CreateCommentActivity>(
      REPLY_COMMENT_REQ,
      CreateCommentActivity.COMMENT to comment
    )
  }

  override fun onCommentReplyAll(comment: Comment) {
    startActivityForResult<CreateCommentActivity>(
      REPLY_COMMENT_REQ,
      CreateCommentActivity.COMMENT to comment,
      CreateCommentActivity.REPLY_ALL to true
    )
  }

  override fun onCommentDelete(comment: Comment) {
    panel.progress { done ->
      CommentDao.deleteComment(comment) { reloadComments(done) }
    }
  }

  //------------------------------------------------------------------------------------------------

  fun reloadComments(done: () -> Unit) {
    treeitem?.let { treeitem ->
      CommentDao.loadComments(treeitem) {
        treeitem.comments = it
        comments = it
        done()
      }
    } ?: throw Exception("Missing treeitem")
  }

  fun handleEditComment(data: Intent?) {
    data?.let {
      panel.progress { done ->
        CommentDao.updateComment(data.getParcelableExtra(CreateCommentActivity.COMMENT)) {
          reloadComments(done)
        }
      }
    }
  }

  fun handleReplyComment(data: Intent?) {
    data?.let {
      panel.progress { done ->
        CommentDao.createComment(data.getParcelableExtra(CreateCommentActivity.COMMENT)) {
          reloadComments(done)
        }
      }
    }
  }

  //------------------------------------------------------------------------------------------------

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (Pair(resultCode, requestCode)) {
      RESULT_OK to EDIT_COMMENT_REQ -> handleEditComment(data)
      RESULT_OK to REPLY_COMMENT_REQ -> handleReplyComment(data)
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  //------------------------------------------------------------------------------------------------

  companion object {
    val EDIT_COMMENT_REQ = 1
    val REPLY_COMMENT_REQ = 2
  }
}