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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.comments

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Comment
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import kotlinx.android.synthetic.main.activity_create_comment.*
import org.jetbrains.anko.intentFor

class CreateCommentActivity : AppCompatActivity() {
  private var treeitem: Treeitem? = null
  private var comment: Comment? = null
  private var editing: Boolean = false
  private var replyAll: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_comment)

    intent?.let {
      editing = it.getBooleanExtra(EDITING, false)
      replyAll = it.getBooleanExtra(REPLY_ALL, false)
      if (it.hasExtra(COMMENT))  { comment = it.getParcelableExtra(COMMENT) }
      if (it.hasExtra(TREEITEM)) { treeitem = it.getParcelableExtra(TREEITEM) }
    }

    buttonPost.setOnClickListener { handlePostComment() }
  }

  override fun onStart() {
    super.onStart()

    Current.member?.let {
      Current.avatar(applicationContext, it) {
        imageViewAvatar.setImageBitmap(it)
      }

      populateComment()
    }
  }

  fun populateComment() {
    val comment = comment
    val text = if (comment != null) {
      if (editing) {
        comment.plain_text
      } else {
        if (replyAll) {
          comment.at_member_ids?.map {
            Current.space?.membersById?.get(it)?.let { "@${ it.user_name }" }
          }?.filterNotNull()?.joinToString(separator = " ") ?: ""
        } else {
          Current.space?.membersById?.get(comment.created_by)?.let { "@${ it.user_name }" }
        }
      }
    } else {
      ""
    }
    editTextComment.text = Editable.Factory.getInstance().newEditable("${text} ")
  }

  fun handlePostComment() {
    val exitingComment = comment
    val comment = if (exitingComment != null) {
      exitingComment.comment = editTextComment.text.toString()
      exitingComment
    } else {
      Comment(
        comment = editTextComment.text.toString(),
        item_id = comment?.item_id ?: treeitem?.id ?: throw Exception("can't set item_id"),
        space_id = Current.space?.id ?: throw Exception("Missing space")
      )
    }

    val intent = intentFor<CreateCommentActivity>(COMMENT to comment)

    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  companion object {
    val TREEITEM = "TREEITEM"
    val COMMENT = "COMMENT"
    val EDITING = "EDITING"
    val REPLY_ALL = "REPLY_ALL"
  }
}
