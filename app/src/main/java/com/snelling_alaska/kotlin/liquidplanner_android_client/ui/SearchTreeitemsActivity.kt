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

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.TreeitemDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.MainActionsBaseActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.treeitems_list.TreeitemFragment
import kotlinx.android.synthetic.main.activity_shared_main_actions.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivityForResult

class SearchTreeitemsActivity : MainActionsBaseActivity(),
  TreeitemFragment.OnListFragmentInteractionListener,
  SearchView.OnQueryTextListener {

  private lateinit var searchResultsFragment: TreeitemFragment
  private var searching = false
  private var rootFolder: Treeitem? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    search_bar.visibility = View.VISIBLE

    search_bar.setOnQueryTextListener(this)

    if (intent.hasExtra(TreeitemFragment.ARG_TREEITEM_ROOT)) {
      rootFolder = intent.getParcelableExtra(TreeitemFragment.ARG_TREEITEM_ROOT)
    }
  }

  override fun onStart() {
    super.onStart()

    Current.space?.let {
      login_progress.visibility = View.VISIBLE
      TreeitemDao.getChildrenOfTreeitem(it, rootFolder) { tis ->
        searchResultsFragment.treeitems = rootFolder?.let {
          tis?.let { tiList -> listOf(it) + tiList }
        } ?: tis

        searchResultsFragment.outdentFirst = parent != null
        login_progress.visibility = View.GONE
      }
    }
  }

  override fun onQueryTextChange(p0: String?) = true

  override fun onQueryTextSubmit(text: String?): Boolean {
    if (searching) { return true }
    searching = true

    doAsync {
      Thread.sleep(2000)
      if (searching) {
        searching = false
        Log.w(TAG, "search timeout")
      }
    }

    text?.let { searchStr ->
      Current.space?.let {
        TreeitemDao.quickSearchForTreeitems(it, searchStr) {
          searching = false
          searchResultsFragment.treeitems = it
            ?.filter { it.isLeaf() }
            ?.distinctBy { it.id }
        }
      }
    }
    return true
  }

  override fun createFragment(): Fragment {
    searchResultsFragment = TreeitemFragment()

    return searchResultsFragment
  }

  //----------------------------------------------------------------------------

  override fun onListFragmentInteraction(item: Treeitem) {
    if (item == rootFolder) {
    } else if (item.isLeaf()) {
      val result = Intent()
      result.putExtra(SEARCH_RESULT, item)
      setResult(Activity.RESULT_OK, result)
      finish()
    } else {
      startActivityForResult<SearchTreeitemsActivity>(
        SEARCH_SUB_SCREEN,
        TreeitemFragment.ARG_TREEITEM_ROOT to item
      )
    }
  }

  override fun onListFragmentLongClick(item: Treeitem) {
    if (item == rootFolder) {
    } else {
      val result = Intent()
      result.putExtra(SEARCH_RESULT, item)
      setResult(Activity.RESULT_OK, result)
      finish()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    = when (Pair(resultCode, requestCode)) {
      Activity.RESULT_OK to SEARCH_SUB_SCREEN  -> {
        // pass it on up the tree
        setResult(resultCode, data)
        finish()
      }
      else -> super.onActivityResult(requestCode, resultCode, data)
    }

  //----------------------------------------------------------------------------

  companion object {
    val TAG = "SearchTreeitemsActivity"

    val SEARCH_RESULT = "SEARCH_RESULT"
    val SEARCH_SUB_SCREEN = 1
  }
}