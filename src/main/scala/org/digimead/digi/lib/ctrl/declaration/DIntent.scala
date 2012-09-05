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

package org.digimead.digi.lib.ctrl.declaration

object DIntent {
  val DroneName = DConstant.prefix + "drone.name"
  val DronePackage = DConstant.prefix + "drone.package"
  val DroneStash = DConstant.prefix + "drone.stash"
  val SignRequest = DConstant.prefix + "signRequest"
  val SignResponse = DConstant.prefix + "signResponse"
  val Message = DConstant.prefix + "message"
  val Update = DConstant.prefix + "update"
  val UpdateInterfaceFilter = DConstant.prefix + "update_interface_filter" // args - source package
  val UpdateConnectionFilter = DConstant.prefix + "update_connection_filter" // args - source package
  val UpdateOption = DConstant.prefix + "update_option" // args - source package & option
  val Connection = DConstant.prefix + "connection"
  val HostActivity = DConstant.prefix + "host.activity"
  val HostService = DConstant.prefix + "host.service"
  val HostHistoryComplex = DConstant.prefix + "host.history.complex"
  val HostHistoryActivity = DConstant.prefix + "host.history.activity"
  val HostHistorySessions = DConstant.prefix + "host.history.sessions"
  val ComponentActivity = DConstant.prefix + "component.activity"
  val ComponentService = DConstant.prefix + "component.service"
  val Error = DConstant.prefix + "error"
  val FlushReport = DConstant.prefix + "flush.report"
}