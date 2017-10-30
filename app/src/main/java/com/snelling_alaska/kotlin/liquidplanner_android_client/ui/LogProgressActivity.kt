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

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Activity
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Member
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Timer
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.TrackProgress
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.ActivityListActivity
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.MemberListActivity
import kotlinx.android.synthetic.main.activity_log_progress.*
import org.jetbrains.anko.startActivityForResult

class LogProgressActivity : AppCompatActivity() {
  private var person: Member? = null
    set(value) {
      field = value
      button_person.text = value?.user_name
    }

  private var activity: Activity? = null
    set(value) {
      field = value
      button_activity.text = value?.name
    }

  private var timeChanged = false
  private var time: Double? = null
    set(value) {
      field = value
      timeChanged = true
      editText_time.text = Editable.Factory.getInstance()
        .newEditable(value?.toString() ?: "")
    }

  private var comment: String? = null
    set(value) {
      field = value
      editText_comment.text = Editable.Factory.getInstance()
        .newEditable(value ?: "")
    }

  private var timer: Timer? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_log_progress)
    setTitle(R.string.log_progress)

    intent?.let {
      if (it.hasExtra(TIMER)) {
        val timer = it.getParcelableExtra<Timer>(TIMER)
        this.timer = timer

        Current.space?.let {
          time = it.totalTimerTime(timer)
          timeChanged = false
          editText_time.isEnabled = false
        }

        button_person.isEnabled = false
      }

      if (it.hasExtra(PERSON)) { person = it.getParcelableExtra(PERSON) }
      if (it.hasExtra(ACTIVITY)) { activity = it.getParcelableExtra(ACTIVITY) }
    } ?: run {
      activity = null
    }

    if (person == null) { person = Current.member }

    comment = null

    button_person.setOnClickListener {
      startActivityForResult<MemberListActivity>(SELECT_PERSON) }
    button_activity.setOnClickListener {
      startActivityForResult<ActivityListActivity>(SELECT_ACTIVITY) }

    editText_comment.setOnFocusChangeListener { _, b ->
      if (!b) { comment = editText_comment.text.toString() }
    }

    editText_time.setOnFocusChangeListener { _, b ->
      if (!b) { time = editText_time.text.toString().toDouble() }
    }

    button_log_progress.setOnClickListener { submit() }
  }

  fun submit() {
    if (activity == null) {
      AlertDialog.Builder(this)
        .setTitle("Activity Required")
        .setMessage("Please select and activity")
        .setNeutralButton("OK") { d, _ -> d.dismiss() }
        .create()
        .show()
      return
    }

    val ret = Intent()
    val progress = TrackProgress(
      person = person,
      comment = comment,
      activity = requireNotNull(activity),
      time = time
    )
    ret.putExtra(TRACK_PROGRESS_RESULT, progress)
    timer?.let { ret.putExtra(TIMER, it) }
    setResult(android.app.Activity.RESULT_OK, ret)
    finish()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      SELECT_PERSON -> handleMemberSelected(data)
      SELECT_ACTIVITY -> handleActivitySelected(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  protected fun handleMemberSelected(data: Intent?)
    = data?.let { person = data.getParcelableExtra(MemberListActivity.SELECTED_MEMBER) }

  protected fun handleActivitySelected(data: Intent?)
    = data?.let { activity = data.getParcelableExtra(ActivityListActivity.SELECTED_ACTIVITY) }

  companion object {
    val TIMER = "TIMER"
    val PERSON = "PERSON"
    val ACTIVITY = "ACTIVITY"
    val TRACK_PROGRESS_RESULT = "TRACK_PROGRESS_RESULT"

    val SELECT_PERSON = 1
    val SELECT_ACTIVITY = 2
  }
}
