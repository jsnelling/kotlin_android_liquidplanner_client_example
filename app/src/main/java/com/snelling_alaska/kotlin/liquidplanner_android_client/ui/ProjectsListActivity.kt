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
import android.os.Bundle
import android.view.View
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.TreeitemDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.panel.TreeitemPanel
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.MainActionsBaseActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.treeitems_list.TreeitemFragment
import kotlinx.android.synthetic.main.activity_shared_main_actions.*
import org.jetbrains.anko.startActivity

class ProjectsListActivity : MainActionsBaseActivity(),
  TreeitemFragment.OnListFragmentInteractionListener {

  private lateinit var projectsListFragment: TreeitemFragment
  private var parent: Treeitem? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent.hasExtra(TreeitemFragment.ARG_TREEITEM_ROOT)) {
      parent = intent.getParcelableExtra(TreeitemFragment.ARG_TREEITEM_ROOT)
    }
  }

  override fun onStart() {
    super.onStart()

    loadParent()
  }

  override fun createFragment(): Fragment {
    projectsListFragment = TreeitemFragment()

    return projectsListFragment
  }

  override fun onCurrentChange(event: Current.CurrentChange) {
    super.onCurrentChange(event)

    loadParent()
  }

  fun loadParent() {
    Current.space?.let {
      login_progress.visibility = View.VISIBLE
      TreeitemDao.getChildrenOfTreeitem(it, parent) { tis ->
        projectsListFragment.treeitems = parent?.let {
          tis?.let { tiList -> listOf(it) + tiList }
        } ?: tis

        projectsListFragment.outdentFirst = parent != null
        login_progress.visibility = View.GONE
      }
    }
  }

  //----------------------------------------------------------------------------

  override fun onListFragmentInteraction(item: Treeitem) {
    if (item == parent || item.isLeaf()) {
      TreeitemPanel.showFrom(this, item)
    } else {
      startActivity<ProjectsListActivity>(TreeitemFragment.ARG_TREEITEM_ROOT to item)
    }
  }

  override fun onListFragmentLongClick(item: Treeitem) {
    TreeitemPanel.showFrom(this, item)
  }
}