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

trait FCPaste {
  this: FileChooser =>
  private lazy val paste = new WeakReference(extContent.map(l => l.findViewById(XResource.getId(l.getContext,
    "filechooser_paste")).asInstanceOf[Button]).getOrElse(null))

  def initializePaste() = paste.get.foreach {
    paste =>
      log.debug("FCPaste::initializePaste")
      paste.setVisibility(View.GONE)
      paste.setOnClickListener(new View.OnClickListener() {
        override def onClick(v: View) {
          /*        for (val fileToMove <- cutFiles)
          FileSystemUtils.rename(mRoot, fileToMove)
        new CopyFilesTask(FileChooserActivity.this, copiedFiles, cutFiles, mRoot).execute()*/
        }
      })
  }
}