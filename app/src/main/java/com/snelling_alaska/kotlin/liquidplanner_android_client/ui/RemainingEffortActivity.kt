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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.EditText
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.TrackProgress
import com.snelling_alaska.kotlin.liquidplanner_android_client.ui.shared.pickers.SharedPickerBase
import kotlinx.android.synthetic.main.activity_remaining_effort.*

class RemainingEffortActivity: AppCompatActivity() {
  var lowEffort: Float? = null
    set(value) {
      field = value
      editText_lowEffort.text.update(value.toString())
    }

  var highEffort: Float? = null
    set(value) {
      field = value
      editText_highEffort.text.update(value.toString())
    }

  var dailyLimit: Float? = null
    set(value) {
      field = value
      editText_dailyLimit.text.update(value.toString())
    }

  var comment: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_remaining_effort)

    intent?.apply {
      lowEffort = getFloatExtra(LOW)
      highEffort = getFloatExtra(HIGH)
      dailyLimit = getFloatExtra(DAILY_LIMIT)
    }

    editText_lowEffort.onChange { lowEffort = it.text.toString().toFloat() }
    editText_highEffort.onChange { highEffort = it.text.toString().toFloat() }
    editText_dailyLimit.onChange { dailyLimit = it.text.toString().toFloat() }
    editText_comment.onChange { comment = it.text.toString() }

    button_save.setOnClickListener { submit() }
  }



  private fun submit() {
    currentFocus.clearFocus()
    val result = SharedPickerBase.getReturnIntent(intent)
    result.putExtra(ESTIMATE_RESULT, TrackProgress(
      comment = comment,
      low = lowEffort,
      high = highEffort,
      dailyLimit = dailyLimit
    ))
    setResult(Activity.RESULT_OK, result)
    finish()
  }

  //------------------------------------------------------------------------------------------------

  private fun EditText.onChange(then: (EditText) -> Unit)
    = setOnFocusChangeListener { _, focued -> if (!focued) then(this) }

  private fun Intent.getFloatExtra(name: String): Float? {
    if (hasExtra(name)) {
      val extra = getFloatExtra(name, Float.NaN)
      return if (extra.equals(Float.NaN)) { null } else { extra }
    }
    return null
  }

  private fun Editable.update(str: String) {
    clear()
    append(str)
  }

  //------------------------------------------------------------------------------------------------

  companion object {
    val LOW = "LOW"
    val HIGH = "HIGH"
    val DAILY_LIMIT = "DAILY_LIMIT"

    val ESTIMATE_RESULT = "ESTIMATE_RESULT"
  }
}