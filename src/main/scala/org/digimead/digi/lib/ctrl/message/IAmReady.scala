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

package org.digimead.digi.lib.ctrl.message

import scala.annotation.implicitNotFound

import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.log.RichLogger

import android.os.Parcelable
import android.os.Parcel


case class IAmReady(val origin: Origin, val message: String,
  val ts: Long = System.currentTimeMillis)(implicit @transient val logger: RichLogger,
    @transient val dispatcher: Dispatcher) extends DMessage {
  if (logger != null)
    logger.infoWhere("IAmReady " + message + " ts#" + ts, Logging.Where.ALL)
  dispatcher.process(this)
  // parcelable interface
  def this(in: Parcel)(logger: RichLogger, dispatcher: Dispatcher) = this(origin = in.readParcelable[Origin](classOf[Origin].getClassLoader),
    message = in.readString, ts = in.readLong)(logger, dispatcher)
  def writeToParcel(out: Parcel, flags: Int) {
    if (IAmReady.log.isTraceExtraEnabled)
      IAmReady.log.trace("writeToParcel IAmReady with flags " + flags)
    out.writeParcelable(origin, flags)
    out.writeString(message)
    out.writeLong(ts)
  }
  def describeContents() = 0
}

object IAmReady extends Logging {
  final val CREATOR: Parcelable.Creator[IAmReady] = new Parcelable.Creator[IAmReady]() {
    def createFromParcel(in: Parcel): IAmReady = try {
      if (log.isTraceExtraEnabled)
        log.trace("createFromParcel new IAmReady")
      val dispatcher = new Dispatcher { def process(message: DMessage) {} }
      new IAmReady(in)(null, dispatcher)
    } catch {
      case e =>
        log.error(e.getMessage, e)
        null
    }
    def newArray(size: Int): Array[IAmReady] = new Array[IAmReady](size)
  }
}
