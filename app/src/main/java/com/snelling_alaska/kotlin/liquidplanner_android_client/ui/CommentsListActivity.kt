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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui

import android.app.Fragment
import android.content.Intent
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.CommentDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.WorkspaceDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Comment
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.comments.CommentListFragment
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.comments.CreateCommentActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.MainActionsBaseActivity
import org.jetbrains.anko.startActivityForResult

class CommentsListActivity : MainActionsBaseActivity(),
                             CommentListFragment.OnListFragmentInteractionListener {

  private lateinit var commentsListFragment: CommentListFragment

  override fun createFragment(): Fragment {
    commentsListFragment = CommentListFragment.newInstance(Current.space?.comments)
    return commentsListFragment
  }

  override fun onStart() {
    super.onStart()

    reloadComments()
  }

  override fun onCurrentChange(event: Current.CurrentChange) {
    super.onCurrentChange(event)

    reloadComments()
  }

  protected fun reloadComments(then: (() -> Unit)? = null) {
    Current.space?.let {
      WorkspaceDao.loadComments(it) {
        commentsListFragment.comments = it
        if (then != null) { then() }
      }
    }
  }

  //----------------------------------------------------------------------------

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
    progress { done -> CommentDao.deleteComment(comment) { reloadComments(done) } }
  }

  //----------------------------------------------------------------------------

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      EDIT_COMMENT_REQ -> updateComment(data)
      REPLY_COMMENT_REQ -> createComment(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  protected fun updateComment(data: Intent?) {
    data?.getParcelableExtra<Comment>(CreateCommentActivity.COMMENT)?.let { comment ->
      progress { done -> CommentDao.updateComment(comment) {
        reloadComments(done)
      } }
    }
  }

  protected fun createComment(data: Intent?) {
    data?.getParcelableExtra<Comment>(CreateCommentActivity.COMMENT)?.let { comment ->
      progress { done -> CommentDao.createComment(comment) { reloadComments(done) } }
    }
  }

  //----------------------------------------------------------------------------

  companion object {
    val EDIT_COMMENT_REQ = 1
    val REPLY_COMMENT_REQ = 2
  }
}
