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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.timers

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View

import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Timer
import kotlinx.android.synthetic.main.component_timer.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class TimerView @JvmOverloads constructor (
  context: Context,
  attributeSet: AttributeSet,
  defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {
  var timer: Timer? = null
    set(value) {
      val changed = field != value
      field = value
      updateTimer();
      if (changed) {
        value?.let {
          run = true
          tick(it)
        }
      }
    }

  private var run = true

  init {
    LayoutInflater.from(context)
      .inflate(R.layout.component_timer, this, true)

    timer?.let { tick(it) }
  }

  private fun tick(watchTimer: Timer) {
    doAsync {
      while (timer == watchTimer && run) {
        Thread.sleep(10000)
        uiThread {
          timer?.let { updateTimeReadout(it) }
        }
      }
    }
  }

  fun setPlayButtonHandler(listener: ((View) -> (Unit)))
    = imageButtonStartStop.setOnClickListener(listener)

  fun setOptionButtonHandler(listener: ((View) -> (Unit)))
    = imageButtonOptions.setOnClickListener(listener)

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    run = false
  }

  private fun updateTimer() {
    timer?.let { timer ->
      updateTimeReadout(timer)
      updateTimerAppearance(timer)
    } ?: showEmptyTimer()
  }

  private fun showEmptyTimer() {
    imageButtonStartStop.setImageResource(android.R.drawable.ic_media_play)
    imageButtonStartStop.setBackgroundResource(R.drawable.layout_timer_play_bg)
    setBackgroundResource(R.drawable.layout_timer_pill_play_bg)
    textViewTimerValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
    textViewTimerValue.text = "Start"
    imageButtonOptions.visibility = View.GONE
  }

  private fun updateTimerAppearance(timer: Timer) {
    if (timer.running) {
      imageButtonStartStop.setImageResource(android.R.drawable.ic_media_pause)
      imageButtonStartStop.setBackgroundResource(R.drawable.layout_timer_pause_bg)
      setBackgroundResource(R.drawable.layout_timer_pill_pause_bg)
      textViewTimerValue.setTextColor(ContextCompat.getColor(context, android.R.color.white))
      imageButtonOptions.setBackgroundResource(R.color.colorAccent)
    } else {
      imageButtonStartStop.setImageResource(android.R.drawable.ic_media_play)
      imageButtonStartStop.setBackgroundResource(R.drawable.layout_timer_play_bg)
      setBackgroundResource(R.drawable.layout_timer_pill_play_bg)
      textViewTimerValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
      imageButtonOptions.setBackgroundResource(android.R.color.transparent)
    }
    imageButtonOptions.visibility = View.VISIBLE
  }

  private fun updateTimeReadout(timer: Timer) {
    Current.space?.let {
      val time = it.totalTimerTime(timer)

      val hr = time.toInt()
      val min = (60 * (time - hr)).toInt()
      val label = "${ if (hr < 10) { "0" } else { "" } }${ hr }:${ if (min < 10) { "0" } else { "" } }${ min }"

      textViewTimerValue.text = label
    }
  }

}
