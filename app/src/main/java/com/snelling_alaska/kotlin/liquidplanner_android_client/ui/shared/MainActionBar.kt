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

import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.CommentsListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.LoginActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.ProjectsListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.TimersListActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_shared_main_actions.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlinx.android.synthetic.main.nav_header_home_screen.*
import org.jetbrains.anko.startActivity

class MainActionBar(
  val activity: AppCompatActivity,
  private val drawerLayout: DrawerLayout,
  val navigationView: NavigationView,
  toolbar: Toolbar
) : NavigationView.OnNavigationItemSelectedListener,
    ViewTreeObserver.OnGlobalLayoutListener {

  enum class HomeView {
    TimersView,
    CommentsView,
    ProjectsView,
  }

  val toggle: ActionBarDrawerToggle

  private var spaceIdList: MutableMap<Int, Workspace> = mutableMapOf()
  private var spacesMenu: SubMenu

  init {
    activity.setSupportActionBar(toolbar)

    toggle = ActionBarDrawerToggle(
      activity, drawerLayout, toolbar,
      R.string.navigation_drawer_open,
      R.string.navigation_drawer_close
    )

    drawerLayout.addDrawerListener(toggle)
    toggle.syncState()

    spacesMenu = navigationView.menu.addSubMenu("Workspaces")

    navigationView.setNavigationItemSelectedListener(this)

    navigationView.viewTreeObserver.addOnGlobalLayoutListener(this)

    Current.onChange(this)
  }

  fun onStop() {
    Current.offChange(this)
  }

  fun onBackPressed(): Boolean {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START)
      return true
    } else {
      return false
    }
  }

  //------------------------------------------------------------------------------------------------

  override fun onGlobalLayout() {
    navigationView.viewTreeObserver.removeOnGlobalLayoutListener(this)

    displayCurrentUserInformation()
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    // Handle navigation view item clicks here.
    when (item.itemId) {
      R.id.nav_projects -> startActivity(activity, HomeView.ProjectsView)
      R.id.nav_comments -> startActivity(activity, HomeView.CommentsView)
      R.id.nav_timers   -> startActivity(activity, HomeView.TimersView)
      else -> {
        spaceIdList[item.itemId]?.let {
          activity.login_progress.visibility = View.VISIBLE
          Current.space = it
        }
      }
    }

    drawerLayout.closeDrawer(GravityCompat.START)
    return true
  }

  //------------------------------------------------------------------------------------------------

  fun onCreateOptionsMenu(menu: Menu): Boolean {
    activity.menuInflater.inflate(R.menu.home_screen, menu)
    return true
  }

  fun onPrepareOptionsMenu(menu: Menu?): Boolean {
    return false
  }

  fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_settings -> return true
      R.id.action_logout -> {
        Current.logOut()
        return true
      }
      else -> return false
    }
  }

  //------------------------------------------------------------------------------------------------

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun handleCurrentChange(event: Current.CurrentChange) {
    activity.login_progress.visibility = View.GONE
    Current.loggedIn { itIs ->
      if (itIs) {
        displayCurrentUserInformation()
      } else {
        LoginActivity.showFrom(activity)
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun handleCurrentWillChange(event: Current.CurrentChanging) {
    activity.login_progress.visibility = View.VISIBLE
  }

  fun displayCurrentUserInformation() {
    Current.user?.let { user ->
      Picasso.with(activity.applicationContext)
        .load(user.avatar_url)
        .resize(activity.userAvatarView.width, activity.userAvatarView.height)
        .into(activity.userAvatarView)

      activity.userFullName.text = "${user.first_name} ${user.last_name}"
      activity.userEmailAddress.text = user.email

      Current.space?.let { activity.title = it.name }

      spacesMenu.clear()

      user.workspaces.forEach {
        val id = View.generateViewId()
        spaceIdList[id] = it
        spacesMenu.add(0, id, 0, it.name)
      }

      drawerLayout.invalidate()
    }
  }

  //------------------------------------------------------------------------------------------------

  companion object {
    val LAST_SAVED_VIEW = "LAST_SAVED_VIEW"

    private fun tryStartActivity(activity: AppCompatActivity, homeView: HomeView)
      = when (homeView) {
        HomeView.CommentsView -> {
          activity.startActivity<CommentsListActivity>()
          true
        }
        HomeView.ProjectsView -> {
          activity.startActivity<ProjectsListActivity>()
          true
        }
        HomeView.TimersView -> {
          activity.startActivity<TimersListActivity>()
          true
        }
      }

    fun startActivity(activity: AppCompatActivity, homeView: HomeView): Boolean {
      if (tryStartActivity(activity, homeView)) {
        Current.setPref(LAST_SAVED_VIEW, homeView.toString())
        return true
      }
      return false
    }

    fun startActivity(activity: AppCompatActivity, name: String?) = try {
      name?.let {
        startActivity(activity, HomeView.valueOf(it))
        true
      } ?: false
    } catch (e: IllegalArgumentException)  {
      false
    }
  }

}