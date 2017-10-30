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

package com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos

import java.util.*

interface MemberOrTeamish {
  val id: Int
  val name: String
}

data class MemberOrTeam(val member: Member?, val team: Team?): MemberOrTeamish {
  override val id: Int
    get() = requireNotNull(member?.id ?: team?.id)
  override val name: String
    get() = requireNotNull(member?.name ?: team?.name)
}

data class Workspace(
  var company_name: String,
  var created_at: Date,
  var created_by: Int,
  var default_units: Int,
  var description: String,
  var hours_per_day: Float,
  var id: Int,
  var inbox_id: Int,
  var item_email: String,
  var name: String,
  var owner_id: Int,
  var root_id: Int,
  var type: String,
  var updated_at: Date,
  var updated_by: Int,
  var week_start: Int
) {

  var members: List<Member>? = null
    set(value) {
      field = value
      membersById = value?.associateBy { it.id } ?: mapOf()
    }
  var membersById: Map<Int, Member>? = null

  var teams: List<Team>? = null
    set(value) {
      field = value
      teamsById = value?.associateBy { it.id } ?: mapOf()
    }
  var teamsById: Map<Int, Team>? = null

  var comments: List<Comment>? = null

  var timers: List<Timer>? = null
    set(value) {
      field = value
      timerFetchTime = Calendar.getInstance().time
      timersByItemId = value?.associateBy { it.item_id } ?: mapOf()
    }
  var timerFetchTime: Date? = null
  var timersByItemId: Map<Int, Timer>? = null

  var activities: List<Activity>? = null
    set(value) {
      field = value
      activitiesById = value?.associateBy { it.id } ?: mapOf()
    }
  var activitiesById: Map<Int, Activity>? = null

  var clients: List<Client>? = null


  fun totalTimerTime(timer: Timer): Double {
    val now = Calendar.getInstance().time
    val total = timer.total_time ?: 0.0
    val running = timer.running_time

    return if (timer.running && timerFetchTime != null) {
      val elapsed = 1.0 * (now.time - timerFetchTime!!.time) / 1000 / 60 / 60
      total + running + elapsed
    } else {
      total + running
    }
  }

  fun getMemberOrTeam(assignment: Assignment): MemberOrTeam = MemberOrTeam(
    assignment.person_id?.let { membersById?.get(it) },
    assignment.team_id?.let { teamsById?.get(it) }
  )

  fun getOwner(assignment: Assignment): MemberOrTeamish
    = assignment.person_id?.let { membersById?.get(it) as? MemberOrTeamish }
    ?: assignment.team_id?.let { teamsById?.get(it) as? MemberOrTeamish }
    ?: throw Exception("todo unknown person or team")


}