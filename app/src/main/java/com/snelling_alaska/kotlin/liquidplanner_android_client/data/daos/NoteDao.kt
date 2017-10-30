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

package com.snelling_alaska.kotlin.liquidplanner_android_client.data.dao

import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Api
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Note
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.toRequestBody

object NoteDao {
  fun updateNote(
    space: Workspace,
    treeitem: Treeitem,
    note: Note?,
    updates: Map<String, Any>,
    then: (Note?) -> Unit
  ) {
    val update = mapOf(
      "note" to updates
    ).toRequestBody()

    Api.shared
      .let {
        if (note == null) {
          it.post(noteUrl(space, treeitem.id), update)
        } else {
          it.put<Note>(noteUrl(space, treeitem.id), update)
        }
      }
      .fetch(then)
  }

  fun updateNote(
    space: Workspace,
    treeitem: Treeitem,
    note: Note?,
    updates: Pair<String, Any>,
    then: ((Note?) -> Unit)
  ) = updateNote(space, treeitem, note, mapOf(updates), then)

  fun noteUrl(workspace: Workspace, tiId: Int)
    = "workspaces/${ workspace.id }/treeitems/${ tiId }/note"
}

