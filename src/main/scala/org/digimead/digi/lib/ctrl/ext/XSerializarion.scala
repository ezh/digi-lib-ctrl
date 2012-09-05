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

package org.digimead.digi.lib.ctrl.ext

import scala.collection.JavaConversions._
import org.digimead.digi.lib.log.Logging

object XSerializarion extends Logging {
  def parcelToList(o: android.os.Parcelable, flags: Int = 0): java.util.List[Byte] =
    parcelToArray(o).toList
  def parcelToArray(o: android.os.Parcelable, flags: Int = 0): Array[Byte] = {
    val parcel = android.os.Parcel.obtain
    o.writeToParcel(parcel, flags)
    val result = parcel.marshall
    parcel.recycle()
    result
  }
  def unparcelFromList[T <: android.os.Parcelable](s: java.util.List[Byte], loader: ClassLoader = null)(implicit m: scala.reflect.Manifest[T]): Option[T] =
    if (s == null) None else unparcelFromArray[T](s.toList.toArray, loader)
  def unparcelFromArray[T <: android.os.Parcelable](s: Array[Byte], loader: ClassLoader = null)(implicit m: scala.reflect.Manifest[T]): Option[T] = try {
    if (s == null) return None
    assert(m.erasure.getName != "java.lang.Object")
    val p = android.os.Parcel.obtain()
    p.unmarshall(s, 0, s.length)
    p.setDataPosition(0)
    val c = if (loader == null) Class.forName(m.erasure.getName) else Class.forName(m.erasure.getName, true, loader)
    val f = c.getField("CREATOR")
    val creator = f.get(null).asInstanceOf[android.os.Parcelable.Creator[T]]
    val result = Option(creator.createFromParcel(p))
    p.recycle()
    result
  } catch {
    case e =>
      log.error("unparcel '" + m.erasure.getName + "' error", e)
      None
  }
}
