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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import kotlinx.android.synthetic.main.activity_shared_main_actions.*
import kotlinx.android.synthetic.main.naviagtion_view_main_action_bar.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class MainActionsBaseActivity : AppCompatActivity() {

  private lateinit var actionBar: MainActionBar
  private var fragment: Fragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_shared_main_actions)

    loadFragment()

    actionBar = MainActionBar(this, drawer_layout, nav_view, toolbar)
  }

  private fun loadFragment() {
    fragment = createFragment()

    fragmentManager.beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .commit()
  }

  abstract fun createFragment(): Fragment

  override fun onStart() {
    if (fragment == null) {
      loadFragment()
    }

    super.onStart()

    Current.onChange(this)
  }

  override fun onStop() {
    super.onStop()

    actionBar.onStop()

    Current.offChange(this)
  }

  //------------------------------------------------------------------------------------------------

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun handleCurrentChange(event: Current.CurrentChange) {
    onCurrentChange(event);
  }

  protected open fun onCurrentChange(event: Current.CurrentChange) {}

  //------------------------------------------------------------------------------------------------

  protected inline fun progress(then: (() -> Unit) -> Unit) {
    login_progress.visibility = View.VISIBLE
    then({ login_progress.visibility = View.GONE })
  }

  //------------------------------------------------------------------------------------------------

  override fun onSaveInstanceState(outState: Bundle?) {
    fragmentManager.beginTransaction()
      .remove(fragment)
      .commit()

    fragment = null

    super.onSaveInstanceState(outState)
  }

  override fun onBackPressed() {
    if (!actionBar.onBackPressed()) {
      super.onBackPressed()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    return actionBar.onCreateOptionsMenu(menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
    actionBar.onPrepareOptionsMenu(menu)
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return actionBar.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
  }
}