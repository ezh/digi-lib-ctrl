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
import org.digimead.digi.lib.ctrl.message.Origin.richLoggerToOrigin

import android.os.Parcelable
import android.os.Parcel

case class IAmMumble(val origin: Origin, val message: String, @transient val onClickCallback: Option[() => Unit],
  val ts: Long = System.currentTimeMillis)(implicit @transient val logger: RichLogger,
    @transient val dispatcher: Dispatcher) extends DMessage {
  if (logger != null)
    logger.infoWhere("IAmMumble " + message + " ts#" + ts, Logging.Where.ALL)
  dispatcher.process(this)
  // parcelable interface
  def this(in: Parcel)(logger: RichLogger, dispatcher: Dispatcher) = this(origin = in.readParcelable[Origin](classOf[Origin].getClassLoader),
    message = in.readString, onClickCallback = None, ts = in.readLong)(logger, dispatcher)
  def writeToParcel(out: Parcel, flags: Int) {
    if (IAmMumble.log.isTraceExtraEnabled)
      IAmMumble.log.trace("writeToParcel IAmMumble with flags " + flags)
    out.writeParcelable(origin, flags)
    out.writeString(message)
    out.writeLong(ts)
  }
  def describeContents() = 0
}

object IAmMumble extends Logging {
  final val CREATOR: Parcelable.Creator[IAmMumble] = new Parcelable.Creator[IAmMumble]() {
    def createFromParcel(in: Parcel): IAmMumble = try {
      if (log.isTraceExtraEnabled)
        log.trace("createFromParcel new IAmMumble")
      val dispatcher = new Dispatcher { def process(message: DMessage) {} }
      new IAmMumble(in)(null, dispatcher)
    } catch {
      case e =>
        log.error(e.getMessage, e)
        null
    }
    def newArray(size: Int): Array[IAmMumble] = new Array[IAmMumble](size)
  }
  def apply(message: String)(implicit logger: RichLogger, dispatcher: Dispatcher) =
    new IAmMumble(logger, message, None)(logger, dispatcher)
  def apply(origin: Origin, message: String)(implicit logger: RichLogger, dispatcher: Dispatcher) =
    new IAmMumble(origin, message, None)(logger, dispatcher)
}
