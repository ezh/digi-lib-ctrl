/**
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

package org.digimead.digi.lib.ctrl.dialog.filechooser

import scala.ref.WeakReference

import org.digimead.digi.lib.ctrl.ext.XResource
import org.digimead.digi.lib.ctrl.dialog.FileChooser

import android.view.View
import android.widget.Button

trait FCOrder {
  this: FileChooser =>
  private lazy val order = new WeakReference(extContent.map(l => l.findViewById(XResource.getId(l.getContext,
    "filechooser_order")).asInstanceOf[Button]).getOrElse(null))

  def initializeOrder() = order.get.foreach {
    order =>
      log.debug("FCOrder::initializeOrder")
      order.setOnClickListener(new View.OnClickListener() { override def onClick(v: View) = {} /*showDialog(DIALOG_ORDER)*/ })
      order.setVisibility(View.GONE)
  }
}