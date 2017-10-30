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

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Comment
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Member

import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.comments.CommentListFragment.OnListFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_comment_list_item.view.*

class CommentListViewAdapter(
  private val context: Context,
  private var mValues:List<Comment>,
  private val mListener:OnListFragmentInteractionListener?,
  private val optionClick: (Comment, ImageView) -> PopupMenu
):RecyclerView.Adapter<CommentListViewAdapter.ViewHolder>() {

  fun refresh(items: List<Comment>) {
    mValues = items
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.fragment_comment_list_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.apply {
      mItem = mValues[position]
      mItem?.let { item ->
        Current.space?.let { space ->
          (space.membersById?.get(item.created_by) ?: space.membersById?.get(Member.UNASSINGED_ID))
            ?.let { author ->
              commentTextView?.text = item.plain_text
              item.created_at?.let { dateTextView?.text = DateUtils.getRelativeTimeSpanString(it.time) }
              authorTextView?.text = author.user_name
              Current.avatar(context, author) { avatarImageView?.setImageBitmap(it) }

              val ti = item.treeitem
              if (ti != null && !(position > 0 && mValues[position - 1].treeitem == ti)) {
                treeitemHeader?.visibility = View.VISIBLE
                textViewTreeitemName?.text = ti.name
                textViewTreeitemBreadcrumb?.text = arrayOf(
                  ti.package_crumbs.let {
                    if (it?.isNotEmpty() == true) {
                      it.joinToString(separator = " > ", limit = 3, truncated = "...")
                    } else { null }
                  },
                  ti.parent_crumbs.let {
                    if (it?.isNotEmpty() == true) {
                      it.joinToString(separator = " > ", limit = 3, truncated = "...")
                    } else { null }
                  }
                )
                  .filterNotNull()
                  .joinToString(" | ")
                textViewTreeitemOwners?.text = ti.assignments
                  ?.map { space.getOwner(it).name }
                  ?.joinToString(", ")
                  ?: ""
              } else {
                treeitemHeader?.visibility = View.GONE
              }

              treeitemSpacer?.visibility = if (position > 0) { View.VISIBLE } else { View.GONE }

              mView.isLongClickable = true
              mView.setOnLongClickListener {
                avatarImageView?.let { showTimerOptions(item, author, it) }
                true
              }
            }
        }
      }
    }
  }

  private fun showTimerOptions(comment: Comment, author: Member, button: ImageView) {
    val menu = optionClick(comment, button)

    if (author != Current.member) {
      menu.menu.findItem(R.id.comment_option_edit)?.let { it.isVisible = false }
      menu.menu.findItem(R.id.comment_option_delete)?.let { it.isVisible = false }
    }

    menu.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.comment_option_reply_all -> mListener?.onCommentReplyAll(comment)
        R.id.comment_option_reply     -> mListener?.onCommentReply(comment)
        R.id.comment_option_edit      -> mListener?.onCommentEdit(comment)
        R.id.comment_option_delete    -> mListener?.onCommentDelete(comment)
      }
      true
    }

    menu.show()
  }

  override fun getItemCount():Int {
    return mValues.size
  }

  inner class ViewHolder( val mView:View):RecyclerView.ViewHolder(mView) {
    var avatarImageView: ImageView? = null
    var authorTextView: TextView? = null
    var dateTextView: TextView? = null
    var commentTextView: TextView? = null
    var treeitemHeader: View? = null
    var treeitemSpacer: View? = null
    var textViewTreeitemName: TextView? = null
    var textViewTreeitemBreadcrumb: TextView? = null
    var textViewTreeitemOwners: TextView? = null

    var mItem: Comment? = null

    init {
      avatarImageView = mView.avatarImageView
      authorTextView = mView.authorTextView
      dateTextView = mView.dateTextView
      commentTextView = mView.commentTextView
      treeitemHeader = mView.treeitem_header
      treeitemSpacer = mView.treeitem_spacer
      textViewTreeitemName = mView.textViewTreeitemName
      textViewTreeitemBreadcrumb = mView.textViewTreeitemBreadcrumb
      textViewTreeitemOwners = mView.textViewTreeitemOwners
    }

    override fun toString():String = "${ super.toString() } '${ commentTextView?.text }'"
  }
}
