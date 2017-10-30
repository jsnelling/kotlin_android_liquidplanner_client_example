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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.TimerDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Timer
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.TrackProgress
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.LogProgressActivity.Companion.TIMER
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.MainActionsBaseActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.timers.TimerListFragment
import kotlinx.android.synthetic.main.activity_shared_main_actions.*
import org.jetbrains.anko.startActivityForResult

class TimersListActivity : MainActionsBaseActivity(),
  TimerListFragment.OnListFragmentInteractionListener {

  private lateinit var timersFragement: TimerListFragment
  private val searchId = View.generateViewId()

  override fun createFragment(): Fragment {
    timersFragement = TimerListFragment.newInstance()
    return timersFragement
  }

  override fun onCurrentChange(event: Current.CurrentChange) {
    super.onCurrentChange(event)

    timersFragement.refresh()
  }

  //----------------------------------------------------------------------------

  protected fun spaceProgress(cb: (Workspace) -> Unit) {
    Current.space?.let {
      login_progress.visibility = View.VISIBLE
      cb(it)
    }
  }

  protected fun updateTimers(timers: List<Timer>?) {
    Current.space?.let {
      it.timers = timers
      timersFragement.refresh()
      login_progress.visibility = View.GONE
    }
  }

  override fun onListFragmentInteraction(timer: Timer) {
    android.util.Log.v("TIMER", timer.toString())
  }

  override fun onTimerToggleClick(timer: Timer) {
    spaceProgress {
      TimerDao.toggleTimer(it, timer) {
        updateTimers(it)
      }
    }
  }

  override fun onTimerUseClick(timer: Timer) {
    Current.space?.let {
      startActivityForResult<LogProgressActivity>(
        LOG_PROGRESS_REQUEST_CODE,
        LogProgressActivity.TIMER to timer
      )
    }
  }

  override fun onTimerClearClick(timer: Timer) {
    spaceProgress {
      TimerDao.clearTimer(it, timer) {
        updateTimers(it)
      }
    }
  }

  //----------------------------------------------------------------------------

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val res = super.onCreateOptionsMenu(menu)

    menu.add(Menu.NONE, searchId, 0, R.string.search_for_treeitem)

    return res
  }

  override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
    searchId -> startSearch()
    else -> super.onOptionsItemSelected(item)
  }

  //----------------------------------------------------------------------------

  protected fun startSearch(): Boolean {
    startActivityForResult<SearchTreeitemsActivity>(SEARCH_REQUEST_CODE)
    return true
  }

  protected fun handleSearchResult(data: Intent?) {
    if (data?.hasExtra(SEARCH_RESULT) == true) {
      val item = data.getParcelableExtra<Treeitem>(SEARCH_RESULT)
      spaceProgress {
        TimerDao.startTimer(it, item.id) {
          updateTimers(it)
        }
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    = when (requestCode) {
      SEARCH_REQUEST_CODE       -> handleSearchResult(data)
      LOG_PROGRESS_REQUEST_CODE -> handleLogProgressResult(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }

  //----------------------------------------------------------------------------

  protected fun handleLogProgressResult(data: Intent?) {
    data?.let {
      val progress = it.getParcelableExtra<TrackProgress>(TRACK_PROGRESS_RESULT)
      val timer = it.getParcelableExtra<Timer>(TIMER)
      spaceProgress {
        TimerDao.useTimer(it, timer, progress) {
          updateTimers(it)
        }
      }
    }
  }

  //----------------------------------------------------------------------------

  companion object {
    val SEARCH_REQUEST_CODE = 1
    val SEARCH_RESULT = SearchTreeitemsActivity.SEARCH_RESULT
    val LOG_PROGRESS_REQUEST_CODE = 2
    val TRACK_PROGRESS_RESULT = LogProgressActivity.TRACK_PROGRESS_RESULT
  }

}