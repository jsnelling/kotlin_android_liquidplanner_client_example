package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.panel

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import kotlinx.android.synthetic.main.component_clearable_button.view.*

private val xmlNS = "http://schemas.android.com/apk/res/android"

class ClearableButtonView @JvmOverloads constructor(
  context: Context,
  private val attributeSet: AttributeSet,
  defStyle: Int = 0
): ConstraintLayout(context, attributeSet, defStyle) {
  val button: Button get() = button_el
  val clearButton: ImageButton get() = clearButton_el
  var activeBackground: Drawable? = null

  var readonly: Boolean = false
    set(value) {
      field = value
      background = if (value) { null } else { activeBackground }
    }

  init {
    LayoutInflater.from(context)
      .inflate(R.layout.component_clearable_button, this, true)

    activeBackground = attributeSet.getResourceOr("background") { context.getDrawable(it) }

    button.text = attributeSet.getResourceOrString("text")
    button.hint = attributeSet.getResourceOrString("hint")

    background = activeBackground
  }

  fun setOnClickListener(handler: ((clear: Boolean) -> Unit)?) {
    if (handler != null) {
      button_el.setOnClickListener { handler(false) }
      clearButton_el.setOnClickListener { handler(true) }
    } else {
      button_el.setOnClickListener(null)
      clearButton_el.setOnClickListener(null)
    }
  }

  private fun <M> AttributeSet.getResourceOr(attr: String, then: (Int) -> M): M?
    = getAttributeResourceValue(xmlNS, attr, Int.MIN_VALUE).let {
      if (it == Int.MIN_VALUE) { null }
      else { then(it) }
    }

  private fun AttributeSet.getResourceOrString(attr: String)
    = getResourceOr(attr) { context.getString(it) }
    ?: getAttributeValue(xmlNS, attr)
}