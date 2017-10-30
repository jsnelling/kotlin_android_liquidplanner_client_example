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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.MainActionBar
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.content_home_screen.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

class LandingScreenActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_home_screen)
  }

  override fun onStart() {
    super.onStart()

    Current.onChange(this)
    gotoLastActivityOrLogin()
  }

  override fun onStop() {
    super.onStop()

    Current.offChange(this)
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun handleCurrentChange(event: Current.CurrentChange) {
    gotoLastActivityOrLogin()
  }

  fun gotoLastActivityOrLogin() {
    Current.loggedIn { itIs ->
      if (itIs) {
        spinner_home_screen.visibility = View.GONE
        fragment_container.visibility = View.VISIBLE

        val lastView = Current.getPref<String>(MainActionBar.LAST_SAVED_VIEW)

        MainActionBar.startActivity(this, lastView) ||
          MainActionBar.startActivity(this, MainActionBar.HomeView.CommentsView)
      } else {
        startActivity<LoginActivity>()
      }
      /*
        TODO what happens if something times out? We need to catch that case and then show an oops,
        something went wrong click here to retry, or send them back to the login screen with a
        similar message.
       */
    }
  }
}
