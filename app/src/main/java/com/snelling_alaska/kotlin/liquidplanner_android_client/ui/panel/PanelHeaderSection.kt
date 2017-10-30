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
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.TimerDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Timer
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.TrackProgress
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.LogProgressActivity
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_header.*
import org.jetbrains.anko.startActivityForResult

class PanelHeaderSection : Fragment(),
  TreeitemPanel.Section {

  override var treeitem: Treeitem? = null
  override lateinit var panel: TreeitemPanel

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val view = inflater!!.inflate(R.layout.activity_treeitem_panel_section_header, container, false)

    return view
  }

  override fun showTreeitem(treeitem: Treeitem?) {
    this.treeitem = treeitem

    panel.editTextUpdater(
      panel_editText_name,
      { treeitem?.name },
      Treeitem.NAME
    )

    panel.checkBoxUpdater(panel_checkbox_is_done, { treeitem?.is_done }, Treeitem.IS_DONE)

    panel_textView_crumb.text = tempBreadcrumb()

    Current.space?.let {
      panel_timerView.apply {
        timer = it.timersByItemId?.get(treeitem?.id)
        setPlayButtonHandler { handleTimerPlayPause() }
        setOptionButtonHandler { handleTimerOptions() }
      }
    }
  }

  fun tempBreadcrumb() = listOf(
    treeitem?.client?.name,
    listOf(
      treeitem?.parent?.name
    ).filterNotNull().joinToString(" > "),
    treeitem?.package_ti?.name
  ).filterNotNull().joinToString(" | ")

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    = when(Pair(resultCode, requestCode)) {
      RESULT_OK to LOG_PROGRESS_REQUEST_CODE -> handlUseTimerAndProgress(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }

  //------------------------------------------------------------------------------------------------

  protected fun handleTimerPlayPause() {
    Current.space?.let { space ->
      panel_timerView.timer
        ?.let { timer -> TimerDao.toggleTimer(space, timer) { reloadTimers(space, it) } }
        ?: treeitem?.let { TimerDao.startTimer(space, it.id) { reloadTimers(space, it) } }
    }
  }

  protected fun handleTimerUse() {
    Current.space?.let { _ ->
      panel_timerView.timer?.let { timer ->
        startActivityForResult<LogProgressActivity>(
          LOG_PROGRESS_REQUEST_CODE,
          LogProgressActivity.TIMER to timer
        )
      }
    }
  }

  protected fun handlUseTimerAndProgress(data: Intent?) {
    data?.let {
      val progress = it.getParcelableExtra<TrackProgress>(LogProgressActivity.TRACK_PROGRESS_RESULT)
      Current.space?.let { space ->
        panel_timerView.timer?.let { timer ->
          TimerDao.useTimer(space, timer, progress) { reloadTimers(space, it) }
        }
      }
    }
  }

  protected fun handleTimerClear() {
    Current.space?.let { space ->
      panel_timerView.timer?.let { timer ->
        TimerDao.clearTimer(space, timer) { reloadTimers(space, it) }
      }
    }
  }

  protected fun handleTimerOptions() {
    val menu = PopupMenu(activity, panel_timerView)
    menu.inflate(R.menu.timer_list_fragment_options_menu)
    menu.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.timer_option_use -> handleTimerUse()
        R.id.timer_option_clear -> handleTimerClear()
      }
      true
    }
    menu.show()
  }

  private fun reloadTimers(space: Workspace, timers: List<Timer>?) {
    space.timers = timers
    panel_timerView.timer = space.timersByItemId?.get(treeitem?.id)
  }

  companion object {
    val LOG_PROGRESS_REQUEST_CODE = 1
  }
}
