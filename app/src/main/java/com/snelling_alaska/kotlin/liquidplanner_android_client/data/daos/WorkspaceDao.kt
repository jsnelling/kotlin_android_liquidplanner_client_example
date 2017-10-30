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

import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.*
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.Parallel

object WorkspaceDao {
  fun loadWorkspaceLuggage(workspace: Workspace, then: (Workspace) -> Unit) {
    Parallel()
      .step { done -> loadMembers(workspace)    { done() } }
      .step { done -> loadTeams(workspace)      { done() } }
      .step { done -> loadTimers(workspace)     { done() } }
      .step { done -> loadActivities(workspace) { done() } }
      .step { done -> loadClients(workspace)    { done() } }
      .then { then(workspace) }
  }

  fun loadComments(workspace: Workspace, then: (Comments?) -> Unit)
    = CommentDao.loadComments(workspace) {
      workspace.comments = it
      then(it)
    }

  fun loadMembers(workspace: Workspace, then: (List<Member>?) -> Unit)
    = MemberDao.loadMembers(workspace) {
      workspace.members = it
      then(it)
    }

  fun loadTeams(workspace: Workspace, then: (List<Team>?) -> Unit)
    = TeamDao.loadTeams(workspace) {
    workspace.teams = it
    then(it)
  }

  fun loadTimers(workspace: Workspace, then: (List<Timer>?) -> Unit)
    = TimerDao.getMyTimers(workspace) {
      workspace.timers = it
      then(it)
    }

  fun loadActivities(workspace: Workspace, then: (List<Activity>?) -> Unit)
    = ActivityDao.getActivities(workspace) {
      workspace.activities = it
      then(it)
    }

  fun loadClients(workspace: Workspace, then: (List<Client>?) -> Unit)
    = ClientDao.getClients(workspace) {
    workspace.clients = it
    then(it)
  }

}
