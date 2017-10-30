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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import kotlinx.android.synthetic.main.activity_shared_picker_list.*

abstract class SharedPickerBase: Activity() {
  abstract fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_shared_picker_list)

    val recycler = recycler_view

    val context = recycler.context
    recycler.layoutManager = LinearLayoutManager(context)

    recycler.adapter = getAdapter()
  }

  fun getReturnIntent() = getReturnIntent(intent)

  companion object {
    val EXTRAS = "EXTRAS"

    fun getReturnIntent(inIntent: Intent) = Intent().apply {
      if (inIntent.hasExtra(EXTRAS)) {
        putExtras(inIntent.getBundleExtra(EXTRAS))
      }
    }

  }
}
