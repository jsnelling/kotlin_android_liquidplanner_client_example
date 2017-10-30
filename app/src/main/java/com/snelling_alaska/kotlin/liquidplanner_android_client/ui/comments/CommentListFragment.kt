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

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu

import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Comment

open class CommentListFragment : Fragment() {
  private var mColumnCount = 1
  protected var mListener: OnListFragmentInteractionListener? = null
  private var viewAdapter: CommentListViewAdapter? = null
  var comments: List<Comment>? = null
    set(list) {
      field = list
      list?.let { comments ->
        viewAdapter?.refresh(comments)
      }
    }

  override fun onCreate(savedInstanceState:Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      if (it.containsKey(COMMENTS)) {
        it.getParcelable<Comment.Collection>(COMMENTS)?.let {
          comments = it.comments
        }
      }
    }
  }

  override fun onCreateView(inflater:LayoutInflater?,
                            container:ViewGroup?,
                            savedInstanceState:Bundle?): View? {
    val view = inflater!!.inflate(R.layout.fragment_comment_list, container, false)

    // Set the adapter
    if (view is RecyclerView) {
      val context = view.getContext()

      if (mColumnCount <= 1) {
        view.layoutManager = LinearLayoutManager(context)
      }
      else {
        view.layoutManager = GridLayoutManager(context, mColumnCount)
      }

      var list = comments ?: listOf()

      viewAdapter = CommentListViewAdapter(context, list, mListener) { _, anchor ->
        var popup = PopupMenu(activity, anchor)
        popup.menuInflater.inflate(R.menu.comment_list_fragment_options_menu, popup.menu)
        popup
      }

      view.adapter = viewAdapter
    }

    return view
  }


  override fun onAttach(context:Context?) {
    super.onAttach(context)
    if (mListener != null) {

    } else if (context is OnListFragmentInteractionListener) {
      mListener = context
    } else {
      throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }

  interface OnListFragmentInteractionListener {
    fun onCommentReplyAll(comment: Comment)
    fun onCommentReply(comment: Comment)
    fun onCommentEdit(comment: Comment)
    fun onCommentDelete(comment: Comment)
  }

  companion object {
    private val COMMENTS = "comments"

    fun newInstance(comments: List<Comment>?): CommentListFragment {
      val fragment = CommentListFragment()
      val args = Bundle()
      comments?.let { args.putParcelable(COMMENTS, Comment.Collection(it)) }
      fragment.arguments = args
      return fragment
    }
  }
}
