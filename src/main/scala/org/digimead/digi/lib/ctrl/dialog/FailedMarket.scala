/**
 * Digi-Lib-Ctrl - common module for all android-digiNNN projects based on DigiControl
 *
 * Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.digimead.digi.lib.ctrl.dialog

import org.digimead.digi.lib.ctrl.ext.XResource
import org.digimead.digi.lib.aop.Loggable
import org.digimead.digi.lib.log.Logging

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface

object FailedMarket extends Logging {
  def getId(context: Context) = XResource.getId(context, "failed_market")
  @Loggable
  def createDialog(activity: Activity): Dialog = {
    new AlertDialog.Builder(activity).
      setTitle(XResource.getString(activity, "error_market_failed_title").
        getOrElse("Market failed")).
      setMessage(XResource.getString(activity, "error_market_failed_content").
        getOrElse("Market application not found on the device")).
      setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Loggable
        def onClick(dialog: DialogInterface, which: Int) {}
      }).
      create()
  }
}