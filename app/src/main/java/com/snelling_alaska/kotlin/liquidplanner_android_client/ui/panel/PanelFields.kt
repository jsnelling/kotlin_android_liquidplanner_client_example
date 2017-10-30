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

package com.snelling_alaska.kotlin.liquidplanner_android_client.ui.panel

import android.content.Context
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.MemberOrTeamish
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.DateOnly
import java.util.*

class PanelFields<Model>(
  val fields: Map<Int, PanelAllowedFields.Accessor<Model>>,
  val set: (Model?, Map<String, Any?>) -> Unit,
  var model: Model?,
  val context: Context
)  {

  enum class FieldType {
    Text,
    Effort,
    Checkbox,
    Date,
    DateTime,
    Treeitem,
    MemberOrTeam,
    Model,
    NONE
  }

  data class Field<K : View>(
    val cell: K,
    val label: TextView?,
    var type: FieldType? = null,
    var get: (() -> Any?)? = null,
    var set: String? = null,
    var readOnly: (() -> Boolean)? = null,
    var clearable: (() -> Boolean)? = null,
    val icon: View? = null,
    val then: ((field: K) -> Unit)? = null
  )

  fun <K : View> field(model: Model?, field: Field<K>) {
    field.apply {
      model?.let { model ->
        fields.get(cell.id)?.let { acc ->
          if (acc.allowed(model)) {
            cell.visibility = View.VISIBLE
            label?.visibility = View.VISIBLE
            icon?.visibility = View.VISIBLE

            if (type == null) { type = acc.type }
            if (get == null) { get = { acc.get?.invoke(model) } }
            if (set == null) { set = acc.set }
            if (readOnly == null) { readOnly = acc.readOnly?.let { { it(model) } } }
            if (clearable == null) { clearable = acc.clearable?.let { { it(model) } } }

            setValue(field)

            if (field.readOnly?.let { !it() } != false) {
              handleChange(field)
              then?.invoke(cell)
            }
          } else {
            cell.visibility = View.GONE
            label?.visibility = View.GONE
            icon?.visibility = View.GONE
          }
        }
      }
    }
  }

  interface FieldModel {
    fun name(): String
  }

  private fun <K : View> stringFromGetter(field: Field<K>) = when (field.type) {
    FieldType.Text -> { field.get?.invoke() as? String }
    FieldType.Effort -> { field.get?.invoke()?.let { "${it}h" } }
    FieldType.Treeitem -> { (field.get?.invoke() as? Treeitem)?.let { it.name } }
    FieldType.Date -> { (field.get?.invoke() as? Date)?.let { DateOnly.asDate(it) } }
    FieldType.DateTime -> { (field.get?.invoke() as? Date)?.let { DateOnly.asDateTime(it) } }
    FieldType.Model -> { (field.get?.invoke() as? FieldModel)?.let { it.name() } }
    FieldType.MemberOrTeam -> { (field.get?.invoke() as? MemberOrTeamish)?.let { it.name } }
    else -> throw Exception("Unable to setup field ${field.type} -> $field")
  }

  private fun <K : View> setValue(field: Field<K>) = field.apply {
    if (type == FieldType.Checkbox && cell is CheckBox) {
      val set = set
      if (set != null) {
        checkBoxUpdater(cell, { get?.invoke() as? Boolean }, set)
      } else {
        cell.isChecked = get?.invoke() as? Boolean ?: false
      }
    } else if (cell is EditText) {
      cell.hint = "None"
      cell.text = Editable.Factory.getInstance().newEditable(stringFromGetter(field) ?: "")
    } else if (cell is TextView) {
      cell.hint = "None"
      cell.text = stringFromGetter(field)
    } else if (cell is Button) {
      cell.text = stringFromGetter(field) ?: "None"
    } else if (cell is ClearableButtonView) {
      clearableButtonUpdater(
        cell,
        stringFromGetter(field),
        readOnly = field.readOnly,
        allowClear = clearable
      )
    } else if (type == FieldType.NONE) {
    } else {
      throw Exception("Unable to setup field $type -> $field")
    }
    null
  }

  private fun <K : View> handleChange(field: Field<K>) = field.apply {
    set?.let { set ->
      if (type == FieldType.Effort && cell is EditText) {
        editTextUpdater(cell, { stringFromGetter(field) }, set) {
          str -> if (str.isEmpty()) { null } else { str.toFloat()  }
        }
      } else if (type == FieldType.Text && cell is EditText) {
        editTextUpdater(cell, { stringFromGetter(field) }, set)
      } else if (type == FieldType.Checkbox && cell is CheckBox) {
        checkBoxUpdater(cell, { field.get?.invoke() as? Boolean }, set)
      }
      null
    }
  }

  //------------------------------------------------------------------------------------------------

  fun <ST> editTextUpdater(
    editText: EditText,
    get: () -> String?,
    field: String,
    setValue: ((String) -> ST)? = null
  ) = editText.apply {
    val start = get() ?: ""
    text = Editable.Factory.getInstance().newEditable(start)
    setOnFocusChangeListener { _, focused ->
      if (!focused) {
        if (text.toString() != start) {
          val newValue = setValue?.invoke(text.toString()) ?: text.toString()
          set(model, mapOf(field to newValue))
        }
      }
    }
  }

  fun editTextUpdater(editText: EditText, get: () -> String?, field: String)
    = editTextUpdater<String>(editText, get, field, null)

  fun clearableButtonUpdater(
    view: ClearableButtonView,
    value: String?,
    readOnly: (() -> Boolean)? = null,
    defaultVal: String = "None",
    allowClear: (() -> Boolean)? = null
  ) {
    val isReadonly = readOnly?.invoke() == true
    val clearable = if (isReadonly) { false } else { allowClear?.invoke() ?: true }

    view.readonly = isReadonly
    value?.let {
      view.button.text = it
      view.clearButton.visibility = if (clearable) { View.VISIBLE } else { View.INVISIBLE }
    } ?: run {
      view.button.text = defaultVal
      view.clearButton.visibility = View.INVISIBLE
    }
  }

  fun checkBoxUpdater(checkBox: CheckBox, get: () -> Boolean?, field: String) = checkBox.apply {
    isChecked = get() ?: false
    setOnCheckedChangeListener { _, isChecked -> set(model, mapOf(field to isChecked)) }
  }
}

