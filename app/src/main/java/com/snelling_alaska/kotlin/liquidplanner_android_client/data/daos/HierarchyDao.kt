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
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Hierarchy
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace

object HierarchyDao {
  fun loadHierarchy(workspace: Workspace, then:(Hierarchy?) -> Unit) {
    val req = Api.shared.urlBuilder(TreeitemDao.treeitemUrl(workspace))
      .addQueryParameter("flat", "true")
      .addQueryParameter("leaves", "false")

    Api.shared
      .get<Hierarchy>(req)
      .fetchA { tis ->
        if (tis != null) {
          val items = mutableMapOf<Int, Hierarchy>()

          tis.forEach { ti ->
            items.put(ti.id, ti)
            ti.package_id?.let {
              items.get(it)?.let {
                val pkg = ti.copy(isPackagedVersion = true)
                it.children.add(pkg)
                pkg.parent = it
                pkg.depth = it.depth + 1

              }
            }
            ti.parent_id?.let {
              items.get(it)?.let {
                it.children.add(ti)
                ti.parent = it
                ti.depth = it.depth + 1
              }
            }
          }

          then(items.get(tis[0].id)?.apply { expanded = true })
        }
      }
  }
}