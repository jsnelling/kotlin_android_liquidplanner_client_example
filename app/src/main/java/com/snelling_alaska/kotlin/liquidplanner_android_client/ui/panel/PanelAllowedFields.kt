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

import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Assignment
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem.Type

private typealias Types = PanelFields.FieldType

object PanelAllowedFields {
  data class Accessor<Model>(
    val allowed: ((Model) -> Boolean),
    val type: Types,
    val get: ((Model) -> Any?)? = null,
    val set: String? = null,
    val readOnly: ((Model) -> Boolean)? = null,
    val clearable: ((Model) -> Boolean)? = null
  )

  val treeitemFields: Map<Int, Accessor<Treeitem>> = mapOf(
    R.id.treeitem_name to Accessor(
      { true },
      Types.Text,
      { ti -> ti.name },
      Treeitem.IS_DONE
    ),

    R.id.panel_textView_item_type to Accessor(
      { true },
      Types.Text,
      { ti -> ti.type }
    ),

    R.id.panel_checkbox_is_done to Accessor(
      { true },
      Types.Checkbox,
      { ti -> ti.is_done },
      Treeitem.IS_DONE
    ),

    R.id.panel_button_delay_until to Accessor(
      { ti -> !ti.isMilestone() && !ti.isEvent() },
      Types.Date,
      { ti -> ti.delay_until }
    ),

    R.id.panel_button_deadline to Accessor(
      { ti -> !ti.isMilestone() && !ti.isEvent() },
      Types.Date,
      { ti -> ti.promise_by }
    ),

    R.id.panel_checkbox_is_on_hold to Accessor(
      { true },
      Types.Checkbox,
      { ti -> ti.is_on_hold },
      Treeitem.IS_ON_HOLD
    ),

    R.id.panel_button_milestone_date to Accessor(
      { ti -> ti.isMilestone() },
      Types.Date,
      { ti -> ti.date }
    ),

    R.id.panel_editText_description to Accessor(
      { true },
      Types.Text,
      { ti -> ti.description },
      Treeitem.DESCRIPTION
    ),

    R.id.panel_button_package to Accessor(
      { ti -> !Type.Folder.isA(ti) },
      Types.Treeitem,
      { ti ->
        if (ti.package_ti != null) {
          ti.package_ti
        } else if (Type.Package.isA(ti.parent)) {
          ti.parent
        } else {
          null
        }
      },
      clearable = { ti -> ti.parent_id != null && ti.package_id != null }
    ),

    R.id.panel_button_project to Accessor(
      { ti -> !Type.Project.isA(ti.type) },
      Types.Text,
      { ti -> if (!Type.Package.isA(ti.parent)) {
        ti.parent_crumbs?.joinToString(separator = "  >  ")
      } else {
        null
      }
      },
      clearable = { ti -> ti.parent_id != null && ti.package_id != null }
    ),

    R.id.panel_button_client to Accessor(
      { ti -> !ti.isA(Type.Package, Type.Inbox) },
      Types.Treeitem,
      { ti -> ti.client },
      readOnly = { ti -> !Type.Project.isA(ti) }
    ),

    //------------------------------------------------------------------------------------------------

    R.id.panel_button_started_on to Accessor(
      { ti -> ti.isLeaf() },
      Types.Date,
      { ti -> ti.started_on },
      Treeitem.STARTED_ON
    ),

    R.id.panel_editText_max_effort to Accessor(
      { true },
      Types.Effort,
      { ti -> ti.max_effort },
      Treeitem.MAX_EFFORT
    ),

    R.id.panel_button_activity to Accessor(
      { ti -> ti.isLeaf() },
      Types.Model,
      { ti -> Current.space?.activitiesById?.get(ti.activity_id) }
    ),

    R.id.panel_editText_manual_alert to Accessor(
      { true },
      Types.Text,
      { ti -> ti.manual_alert },
      Treeitem.MANUAL_ALERT
    )
  )

  val assignmentFields: Map<Int, Accessor<Assignment>> = mapOf(
    R.id.checkBox_isDone to Accessor(
      { true },
      Types.Checkbox,
      { a -> a.is_done },
      Assignment.IS_DONE
    ),

    R.id.button_owner to Accessor(
      { true },
      Types.MemberOrTeam,
      { a -> Current.space?.getOwner(a) }
    ),

    R.id.button_logged to Accessor(
      { true },
      Types.Effort,
      { a -> a.hours_logged }
    ),

    R.id.button_activity to Accessor(
      { true },
      Types.Model,
      { a -> Current.space?.activitiesById?.get(a.activity_id) }
    ),

    R.id.textView_start_e to Accessor(
      { true },
      Types.Date,
      { a -> a.expected_start },
      readOnly = { true }
    ),

    R.id.textView_finish_e to Accessor(
      { true },
      Types.Date,
      { a -> a.expected_finish },
      readOnly = { true }
    ),

    R.id.button_estimate to Accessor(
      { a -> Type.Task.isA(a.treeitem) },
      Types.Text,
      { a ->
        if (a.estimated) { "${a.low_effort_remaining}h - ${a.high_effort_remaining}h" }
        else { null }
      }
    )
  )

}
