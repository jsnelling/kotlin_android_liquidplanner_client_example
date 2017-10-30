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
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import kotlinx.android.synthetic.main.activity_date_picker.*
import org.jetbrains.anko.intentFor
import java.util.*

class DatePickerActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_date_picker)
  }

  override fun onStart() {
    super.onStart()

    intent?.let {
      if (it.hasExtra(DATE)) {
        val date = it.getSerializableExtra(DATE) as Calendar
        datePicker.init(
          date.get(Calendar.YEAR),
          date.get(Calendar.MONTH),
          date.get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, dayOfMonth ->
          val newDate = Calendar.getInstance().apply{
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
          }

          val intent = SharedPickerBase.getReturnIntent(it)
          intent.putExtra(DATE, newDate.time)

          setResult(Activity.RESULT_OK, intent)
          finish()
        }
      }

      if (it.hasExtra(ALLOW_CLEAR)) {
        buttonClear.visibility = if (it.getBooleanExtra(ALLOW_CLEAR, true)) {
          View.VISIBLE
        } else {
          View.GONE
        }
      }
    }

    buttonToday.setOnClickListener {
      Calendar.getInstance().apply{
        datePicker.updateDate(
          get(Calendar.YEAR),
          get(Calendar.MONTH),
          get(Calendar.DAY_OF_MONTH)
        )
      }
    }

    buttonClear.setOnClickListener {
      var date: Date? = null
      val intent = intentFor<DatePickerActivity>(DATE to date)

      setResult(Activity.RESULT_OK, intent)
      finish()
    }
  }

  companion object {
    val DATE = "DATE"
    val ALLOW_CLEAR = "ALLOW_CLEAR"
  }
}
