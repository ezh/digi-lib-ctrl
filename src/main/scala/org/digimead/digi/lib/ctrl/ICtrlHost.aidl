/*
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

package org.digimead.digi.lib.ctrl;

import android.content.Intent;
import org.digimead.digi.lib.ctrl.info.ComponentState;

interface ICtrlHost {
  List<String> directories(in String componentPackage);
  void enable(in String componentPackage, in boolean flag);
  boolean reset(in String componentPackage);
  boolean start(in String componentPackage);
  ComponentState status(in String componentPackage);
  boolean stop(in String componentPackage);
  boolean disconnect(in String componentPackage, in int packageID, in int connectionID);
  List<String> interfaces(in String componentPackage);
  List<Intent> pending_connections(in String componentPackage);
  void update_shutdown_timer(in String componentPackage, in long remain_seconds);
}
