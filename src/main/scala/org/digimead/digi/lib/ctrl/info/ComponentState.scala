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

package org.digimead.digi.lib.ctrl.info

import org.digimead.digi.lib.ctrl.declaration.DState
import org.digimead.digi.lib.log.Logging

import android.os.Parcelable
import android.os.Parcel

case class ComponentState(val componentPackage: String,
  val executableState: List[ExecutableState],
  val state: DState.Value,
  val reason: Option[String],
  val execPath: String,
  val dataPath: String,
  val enabled: Boolean,
  val serviceState: DState.Value,
  val serviceBusy: Boolean) extends Parcelable {
  def this(in: Parcel) = this(componentPackage = in.readString,
    executableState =
      in.readParcelableArray(classOf[ExecutableState].getClassLoader) match {
        case null =>
          Nil
        case p =>
          p.map(_.asInstanceOf[ExecutableState]).toList
      },
    state = DState(in.readInt),
    reason = in.readString match {
      case empty if empty == "" => None
      case reason => Some(reason)
    },
    execPath = in.readString,
    dataPath = in.readString,
    enabled = (in.readByte == 1),
    serviceState = DState(in.readInt),
    serviceBusy = (in.readByte == 1))
  def writeToParcel(out: Parcel, flags: Int) {
    if (ComponentState.log.isTraceExtraEnabled)
      ComponentState.log.trace("writeToParcel ComponentState with flags " + flags)
    out.writeString(componentPackage)
    out.writeParcelableArray(executableState.toArray, 0)
    out.writeInt(state.id)
    out.writeString(reason.getOrElse(""))
    out.writeString(execPath)
    out.writeString(dataPath)
    out.writeByte(if (enabled) 1 else 0)
    out.writeInt(serviceState.id)
    out.writeByte(if (serviceBusy) 1 else 0)
  }
  def describeContents() = 0
}

object ComponentState extends Logging {
  final val CREATOR: Parcelable.Creator[ComponentState] = new Parcelable.Creator[ComponentState]() {
    def createFromParcel(in: Parcel): ComponentState = try {
      if (log.isTraceExtraEnabled)
        log.trace("createFromParcel new ComponentState")
      new ComponentState(in)
    } catch {
      case e =>
        log.error(e.getMessage, e)
        null
    }
    def newArray(size: Int): Array[ComponentState] = new Array[ComponentState](size)
  }
}
