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

package com.snelling_alaska.kotlin.liquidplanner_android_client.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.AccountDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.daos.WorkspaceDao
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Account
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Member
import com.snelling_alaska.kotlin.liquidplanner_android_client.data.dtos.Workspace
import com.orhanobut.hawk.Hawk
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

object Current {
  enum class Status {
    LoggedOut,
    LoggingIn,
    LoggedIn,
  }

  enum class Changes {
    Status,
    Account,
    Workspace,
  }

  enum class Host(val url: String) {
    Production("https://app.liquidplanner.com"),
    Dogfood("https://dogfood.liquidplannerlab.com"),
    Other(""),
  }

  var status = Status.LoggedOut
  var loginResultWaiting: MutableList<(Boolean) -> Unit> = mutableListOf()

  var user: Account? = null
    set(value) {
      val old = field
      field = value
      notifyChange(Changes.Account, old, value)
    }

  val member: Member?
    get() = user?.let { space?.membersById?.get(it.id) }

  var space: Workspace? = null
    set(value) {
      val old = field
      field = value
      onSpaceChanged(value, old)
    }

  var host: Host = Host.Production
    set(value) {
      field = value
      when (host) {
        Host.Production -> hostUrl = host.url
        Host.Dogfood    -> hostUrl = host.url
        Host.Other      -> {}
      }
    }

  lateinit var hostUrl: String

  var avatars: MutableMap<Member, Bitmap> = mutableMapOf()

  /* state change handlers */

  fun onSpaceChanged(newSpace: Workspace?, oldSpace: Workspace?) {
    fun notify() = notifyChange(Changes.Workspace, oldSpace, newSpace)

    notifyWillChange(Changes.Workspace, oldSpace)

    avatars.clear()

    newSpace?.let { WorkspaceDao.loadWorkspaceLuggage(it) {
      notify()
    } } ?: notify()
  }

  /* Login Status */

  fun loggedIn(cb: (Boolean) -> Unit) {
    when (status) {
      Status.LoggedOut -> cb(false)
      Status.LoggedIn -> cb(true)
      Status.LoggingIn -> loginResultWaiting.add(cb)
    }
  }

  fun <T>logIn(username :String, password: String, then: (success: Boolean) -> T) {
    logOut()
    status = Status.LoggedIn


    Api.shared.login(username, password)

    fun failHandler() {
      Log.w(TAG, "login failed")
      status = Status.LoggedOut

      loginResultWaiting.apply { forEach { it(false) } }.clear()

      then(false)
    }

    try {
      AccountDao.getAccount { user ->
        status = Status.LoggedIn

        Current.user = user

        // Jump back to last space if applicable.
        user.last_workspace_id.let { id -> user.getWorkspaceById(id)?.let { Current.space = it } }

        loginResultWaiting.apply { forEach { it(true) } }.clear()

        Hawk.put(STORE_EMAIL, username)
        Hawk.put(STORE_PASSWORD, password)
        Hawk.put(STORE_HOST_URL, Current.hostUrl)

        then(true)
      }
    }
    catch (fail: AccountDao.LoginFailedException) { failHandler() }
    catch (fail: Api.RequestFailure) { failHandler() }

  }

  fun logOut() {
    status = Status.LoggedOut
    Hawk.delete(STORE_EMAIL)
    Hawk.delete(STORE_PASSWORD)
    user = null
    space = null
  }

  /* Preferences Store */

  fun initStore(context: Context) {
    Hawk.init(context).build();

    if (Hawk.contains(STORE_EMAIL)) {
      val url = Hawk.get<String>(STORE_HOST_URL)
      if (url != null) {
        when (url) {
          Host.Production.url -> Current.host = Host.Production
          Host.Dogfood.url -> Current.host = Host.Dogfood
          else -> {
            Current.host = Host.Other
            Current.hostUrl = url
          }
        }
      } else {
        Current.host = Host.Production
      }
      logIn(Hawk.get(STORE_EMAIL), Hawk.get(STORE_PASSWORD)) {}
    }
  }

  fun <T> getPref(key: String): T? = Hawk.get(key)

  fun <T> setPref(key: String, value: T?) = value?.let { Hawk.put(key, it) } ?: Hawk.delete(key)


  /* Change handlers */

  fun onChange(listener: Any) = EventBus.getDefault().register(listener)

  fun offChange(listener: Any) = EventBus.getDefault().unregister(listener)

  private fun notifyWillChange(change: Changes, from: Any?)
    = EventBus.getDefault().post(CurrentChanging(change, from))

  private fun notifyChange(change: Changes, from: Any?, to: Any?)
    = EventBus.getDefault().post(CurrentChange(change, from, to))

  data class CurrentChanging(val change: Changes, val from: Any?)
  data class CurrentChange(val change: Changes, val from: Any?, val to: Any?)

  /* avatar cache */
  fun avatar(context: Context, member: Member, cb: (Bitmap) -> Unit) {
    avatars[member]?.let { cb(it) } ?: doAsync {
      val download = Picasso.with(context)
        .load(Api.fullUrl(member.avatar_url))
        .resize(125,125)
        .get()
      avatars[member] = download
      uiThread { cb(download) }
    }
  }

  private val STORE_EMAIL = "store_email"
  private val STORE_PASSWORD = "store_password"
  private val STORE_HOST_URL = "store_host"

  private val TAG = "Current"
}

