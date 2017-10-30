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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.app.Activity
import android.content.Intent
import com.snelling_alaska.kotlin.liquidplanner_android_client.R
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.Current

import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
  private var active = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    setupActionBar()
    populateAutoComplete()

    password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
      if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
        attemptLogin()
        return@OnEditorActionListener true
      }
      false
    })

    email_sign_in_button.setOnClickListener { attemptLogin() }

    radio_login.setOnCheckedChangeListener { radioGroup, i ->
      when (i) {
        R.id.radio_login_prod -> {
          Current.host = Current.Host.Production
          textFieldHost.visibility = View.GONE
        }
        R.id.radio_login_dogfood -> {
          Current.host = Current.Host.Dogfood
          textFieldHost.visibility = View.GONE
        }
        R.id.radio_login_other -> {
          Current.host = Current.Host.Other
          textFieldHost.visibility = View.VISIBLE
        }
      }

      textFieldHost.textChangedListener {
        onTextChanged { char, _, _, _ ->
          Current.hostUrl = char.toString()
        }
      }
    }

    when (Current.host) {
      Current.Host.Production -> radio_login_prod.isChecked = true
      Current.Host.Dogfood    -> radio_login_dogfood.isChecked = true
      Current.Host.Other      -> radio_login_other.isChecked = true
    }
  }

  private fun populateAutoComplete() {
    if (!mayRequestContacts()) {
      return
    }

    loaderManager.initLoader(0, null, this)
  }

  private fun mayRequestContacts(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true
    }
    if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
      return true
    }
    if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
      Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
        .setAction(android.R.string.ok,
          { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
    } else {
      requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
    }
    return false
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                          grantResults: IntArray) {
    if (requestCode == REQUEST_READ_CONTACTS) {
      if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        populateAutoComplete()
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private fun setupActionBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      // Show the Up button in the action bar.
      supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
  }

  private fun attemptLogin() {
    if (active) {
      return
    }

    email.error = null
    password.error = null

    val emailStr = email.text.toString()
    val passwordStr = password.text.toString()

    var cancel = false
    var focusView: View? = null

    if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
      password.error = getString(R.string.error_invalid_password)
      focusView = password
      cancel = true
    }

    if (TextUtils.isEmpty(emailStr)) {
      email.error = getString(R.string.error_field_required)
      focusView = email
      cancel = true
    } else if (!isEmailValid(emailStr)) {
      email.error = getString(R.string.error_invalid_email)
      focusView = email
      cancel = true
    }

    if (cancel) {
      focusView?.requestFocus()
    } else {
      showProgress(true)

      active = true
      Current.logIn(emailStr, passwordStr) { loggedIn ->
        active = false
        showProgress(false)

        if (loggedIn) {
          finish()
        } else {
          password.error = getString(R.string.error_incorrect_password)
          password.requestFocus()
        }
      }
    }
  }

  private fun isEmailValid(email: String): Boolean {
    return email.contains("@")
  }

  private fun isPasswordValid(password: String): Boolean {
    return password.length > 4
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
  private fun showProgress(show: Boolean) {
    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
    // for very easy animations. If available, use these APIs to fade-in
    // the progress spinner.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
      val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

      login_form.visibility = if (show) View.GONE else View.VISIBLE
      login_form.animate()
        .setDuration(shortAnimTime)
        .alpha((if (show) 0 else 1).toFloat())
        .setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            login_form.visibility = if (show) View.GONE else View.VISIBLE
          }
        })

      login_progress.visibility = if (show) View.VISIBLE else View.GONE
      login_progress.animate()
        .setDuration(shortAnimTime)
        .alpha((if (show) 1 else 0).toFloat())
        .setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
          }
        })
    } else {
      // The ViewPropertyAnimator APIs are not available, so simply show
      // and hide the relevant UI components.
      login_progress.visibility = if (show) View.VISIBLE else View.GONE
      login_form.visibility = if (show) View.GONE else View.VISIBLE
    }
  }

  override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
    return CursorLoader(this,
      // Retrieve data rows for the device user's 'profile' contact.
      Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

      // Select only email addresses.
      ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
      .CONTENT_ITEM_TYPE),

      // Show primary email addresses first. Note that there won't be
      // a primary email address if the user hasn't specified one.
      ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
  }

  override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
    val emails = ArrayList<String>()
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
      emails.add(cursor.getString(ProfileQuery.ADDRESS))
      cursor.moveToNext()
    }

    addEmailsToAutoComplete(emails)
  }

  override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

  }

  private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
    val adapter = ArrayAdapter(this@LoginActivity,
      android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

    email.setAdapter(adapter)
  }

  object ProfileQuery {
    val PROJECTION = arrayOf(
      ContactsContract.CommonDataKinds.Email.ADDRESS,
      ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
    val ADDRESS = 0
    val IS_PRIMARY = 1
  }

  companion object {
    private val REQUEST_READ_CONTACTS = 0


    fun showFrom(source: Activity) {
      val intent = Intent(source, LoginActivity::class.java)
      source.startActivity(intent)
    }
  }
}
