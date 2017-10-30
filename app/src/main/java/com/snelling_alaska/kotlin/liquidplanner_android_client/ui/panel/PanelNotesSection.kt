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

import android.app.Fragment
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dao.NoteDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Note
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Treeitem
import com.snelling_alaska.kotlin.liquidplanner_android_client.util.let
import kotlinx.android.synthetic.main.activity_treeitem_panel_section_notes.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class PanelNotesSection : Fragment(),
  TreeitemPanel.Section {

  /*
    TODO Notes add support for picking foreground color
    TODO Notes add support for picking background color
    TODO Notes add support for toggling super/sub/quote
   */

  private var loaded = false
  override var treeitem: Treeitem? = null
  override lateinit var panel: TreeitemPanel

  private var editing: Boolean = false
    set(value) {
      field = value
      updateEditing()
    }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
    savedInstanceState?.let {
      if (it.containsKey(SAVE_PANEL)) {
        panel = it.getParcelable(SAVE_PANEL)
      }
    }

    return inflater!!.inflate(R.layout.activity_treeitem_panel_section_notes, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    outState?.putParcelable(SAVE_PANEL, panel)
    super.onSaveInstanceState(outState)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    loaded = true

    button_editNotes.onClick {
      if (editing) { saveNote() }
      else { editing = !editing }
    }

    setupIcons()

    showTreeitem(treeitem)
  }

  //------------------------------------------------------------------------------------------------

  override fun showTreeitem(treeitem: Treeitem?) {
    this.treeitem = treeitem

    editing = false
  }

  private fun updateEditing() {
    if (!loaded) { return }

    if (editing) {
      button_editNotes.setImageDrawable(ContextCompat
        .getDrawable(activity, android.R.drawable.ic_menu_save))
      button_bar.visibility = View.VISIBLE
      webView.visibility = View.GONE
      richView.visibility = View.VISIBLE
      treeitem?.note?.let {
        richView.html = it.note
      }
    } else {
      button_editNotes
        .setImageDrawable(ContextCompat
          .getDrawable(activity, android.R.drawable.ic_menu_edit))
      button_bar.visibility = View.GONE
      webView.visibility = View.VISIBLE
      richView.visibility = View.GONE
      treeitem?.note?.let {
        webView.loadData(it.note, "text/html", null)
      }
    }
  }

  private fun saveNote() {
    let(Current.space, treeitem) { space, treeitem ->
      panel.progress { done ->
        NoteDao.updateNote(space, treeitem, treeitem.note, Note.NOTE to richView.html) {
          done()
          treeitem.note = it
          editing = false
        }
      }
    }
  }

  //------------------------------------------------------------------------------------------------

  fun setupIcons() {
    val font = Typeface.createFromAsset(activity.assets,
      "fonts/fontawesome-webfont.ttf")
    mapOf(
      button_undo to R.string.icon_undo,
      button_redo to R.string.icon_redo,
      button_bold to R.string.icon_bold,
      button_italic to R.string.icon_italic,
      button_subscript to R.string.icon_subscript,
      button_superscript to R.string.icon_superscript,
      button_strikethrough to R.string.icon_strikethrough,
      button_underline to R.string.icon_underline,
      button_h1 to null,
      button_h2 to null,
      button_h3 to null,
      button_h4 to null,
      button_h5 to null,
      button_h6 to null,
      button_text_color to null,
      button_bg_color to R.string.icon_square,
      button_indent to R.string.icon_indent,
      button_outdent to R.string.icon_indent,
      button_align_left to R.string.icon_align_left,
      button_align_center to R.string.icon_align_center,
      button_align_right to R.string.icon_align_right,
      button_bullets to R.string.icon_bullets,
      button_numbers to R.string.icon_numbers,
      button_blockquote to R.string.icon_blockquote,
      button_insert_image to R.string.icon_image,
      button_insert_link to R.string.icon_link
    ).forEach { (elem: Button, icon) ->
      elem.typeface = font
      icon?.let { elem.text = activity.resources.getString(it) }
      elem.setTextColor(ContextCompat.getColor(activity, android.R.color.white))
      elem.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent))
      elem.setOnClickListener { handleIconClick(elem.id) }
    }

    button_text_color.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_light))
    button_bg_color.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_light))
  }

  fun handleIconClick(id: Int) = when(id) {
    R.id.button_undo -> richView.undo()
    R.id.button_redo -> richView.redo()
    R.id.button_bold -> richView.setBold()
    R.id.button_italic -> richView.setItalic()
    R.id.button_subscript -> richView.setSubscript()
    R.id.button_superscript -> richView.setSuperscript()
    R.id.button_strikethrough -> richView.setStrikeThrough()
    R.id.button_underline -> richView.setUnderline()
    R.id.button_h1 -> richView.setHeading(1)
    R.id.button_h2 -> richView.setHeading(2)
    R.id.button_h3 -> richView.setHeading(3)
    R.id.button_h4 -> richView.setHeading(4)
    R.id.button_h5 -> richView.setHeading(5)
    R.id.button_h6 -> richView.setHeading(6)
    R.id.button_text_color -> handleSetTextColor()
    R.id.button_bg_color -> handleSetBackgroundColor()
    R.id.button_indent -> richView.setIndent()
    R.id.button_outdent -> richView.setOutdent()
    R.id.button_align_left -> richView.setAlignLeft()
    R.id.button_align_center -> richView.setAlignCenter()
    R.id.button_align_right -> richView.setAlignRight()
    R.id.button_bullets -> richView.setBullets()
    R.id.button_numbers -> richView.setNumbers()
    R.id.button_blockquote -> richView.setBlockquote()
    R.id.button_insert_image -> handleInsertImage()
    R.id.button_insert_link -> handleInsertLink()
    else -> {}
  }

  fun handleSetTextColor() {

  }

  fun handleSetBackgroundColor() {

  }

  fun handleInsertImage() {

  }

  fun handleInsertLink() {

  }

  //------------------------------------------------------------------------------------------------

  companion object {
    val SAVE_PANEL = "SAVE_PANEL"
  }
}