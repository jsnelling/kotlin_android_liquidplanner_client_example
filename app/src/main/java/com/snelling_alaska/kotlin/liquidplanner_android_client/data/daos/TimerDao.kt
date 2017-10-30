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
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Timer
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.TrackProgress
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.toRequestBody

object TimerDao {
  fun getMyTimers(workspace: Workspace, then: (List<Timer>?) -> Unit) {
    Api.shared
      .get<Timer>("workspaces/${workspace.id}/my_timers")
      .fetchA { it?.let { timers ->

        val treeitemIds = timers.map { it.item_id }.toList()

        TreeitemDao.findTreeitems(workspace, treeitemIds) { it?.let { items ->

          val itemsById: Map<Int, Treeitem> = items.associateBy { it.id }

          timers.forEach { timer ->
            itemsById[timer.item_id]?.let {
              timer.treeitem = it
            }
          }


          then(timers)

        } }

      } }
  }

  fun timerUrl(workspace: Workspace, itemId: Int)
    = "workspaces/${workspace.id}/tasks/${itemId}/timer"

  fun toggleTimer(workspace: Workspace, timer: Timer, then: (List<Timer>?) -> Unit)
    = when (timer.running) {
      true -> stopTimer(workspace, timer, then)
      false -> startTimer(workspace, timer, then)
    }

  fun startTimer(workspace: Workspace, itemId: Int, then: (List<Timer>?) -> Unit) {
    Api.shared
      .post<Timer>("${timerUrl(workspace, itemId)}/start", body = null)
      .fetch { getMyTimers(workspace, then) }
  }

  fun startTimer(workspace: Workspace, timer: Timer, then: (List<Timer>?) -> Unit)
    = startTimer(workspace, timer.item_id, then)


  fun stopTimer(workspace: Workspace, timer: Timer, then: (List<Timer>?) -> Unit) {
    Api.shared
      .post<Timer>("${timerUrl(workspace, timer.item_id)}/stop", body = null)
      .fetch { getMyTimers(workspace, then) }
  }

  fun useTimer(
    workspace: Workspace,
    timer: Timer,
    progress: TrackProgress?,
    then: (List<Timer>?) -> Unit
  ) {
    val progressData = mapOf(
      "activity_id" to progress?.activity?.id,
      "low" to progress?.low,
      "high" to progress?.high,
      "is_done" to progress?.is_done,
      "restart" to progress?.restart,
      "comment" to progress?.comment,
      "note" to progress?.note
    ).toRequestBody()

    Api.shared
      .post<Timer>("${timerUrl(workspace, timer.item_id)}/commit", progressData)
      .fetch { getMyTimers(workspace, then) }
  }

  fun clearTimer(workspace: Workspace, timer: Timer, then: (List<Timer>?) -> Unit) {
    Api.shared
      .post<Timer>("${timerUrl(workspace, timer.item_id)}/clear", body = null)
      .fetch { getMyTimers(workspace, then) }
  }
}
