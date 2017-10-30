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

package com.snelling_alaska.kotlin.liquidplanner_android_client.util

import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem

object Icons {
  fun treeitemIcon(type: Treeitem.Type) = when(type) {
    Treeitem.Type.Root            -> R.mipmap.ic_ti_package
    Treeitem.Type.Task            -> R.mipmap.ic_ti_task
    Treeitem.Type.Event           -> R.mipmap.ic_ti_event
    Treeitem.Type.PartialDayEvent -> R.mipmap.ic_ti_event
    Treeitem.Type.Milestone       -> R.mipmap.ic_ti_milestone
    Treeitem.Type.Package         -> R.mipmap.ic_ti_package
    Treeitem.Type.Project         -> R.mipmap.ic_ti_project
    Treeitem.Type.Folder          -> R.mipmap.ic_ti_folder
    Treeitem.Type.BacklogPackage  -> R.mipmap.ic_ti_backlog_package
    Treeitem.Type.Inbox           -> R.mipmap.ic_ti_inbox
    else                          -> R.mipmap.ic_ti_task
  }

  fun treeitemIcon(type: String) = treeitemIcon(Treeitem.Type.valueOf(type))
}