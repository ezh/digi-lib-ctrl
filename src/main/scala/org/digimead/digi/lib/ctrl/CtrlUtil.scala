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

package org.digimead.digi.lib.ctrl

import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.aop.Loggable
import android.content.Context
import java.io.File
import android.os.Environment
import java.io.BufferedWriter
import java.io.FileWriter
import scala.collection.immutable.HashMap
import java.net.NetworkInterface

object CtrlUtil extends Logging {
  /** flag that prevent use of the external storage */
  @volatile private var externalStorageDisabled: Option[Boolean] = None
  // -rwx--x--x 711
  @Loggable
  def getDirectory(context: Context, name: String, forceInternal: Boolean,
    allRead: Option[Boolean], allWrite: Option[Boolean], allExecute: Option[Boolean]): Option[File] = {
    var directory: Option[File] = None
    var isExternal = true
    var isNew = false
    log.debug("get working directory, mode 'force internal': " + forceInternal)
    if (!forceInternal && externalStorageDisabled != Some(true)) {
      // try to use external storage
      try {
        directory = Option(Environment.getExternalStorageDirectory).flatMap(preBase => {
          val isMounted = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
          val baseAndroid = new File(preBase, "Android")
          val baseAndroidData = new File(baseAndroid, "data")
          val basePackage = new File(baseAndroidData, context.getPackageName)
          val baseFiles = new File(basePackage, "files")
          log.debug("try SD storage directory " + basePackage + ", SD storage is mounted: " + isMounted)
          if (isMounted) {
            var baseReady = true
            if (baseReady && !baseAndroid.exists) {
              if (!baseAndroid.mkdir) {
                log.error("mkdir '" + baseAndroid + "' failed")
                baseReady = false
              }
            }
            if (baseReady && !baseAndroidData.exists) {
              if (!baseAndroidData.mkdir) {
                log.error("mkdir '" + baseAndroidData + "' failed")
                baseReady = false
              }
            }
            if (baseReady && !basePackage.exists) {
              if (!basePackage.mkdir) {
                log.error("mkdir '" + basePackage + "' failed")
                baseReady = false
              }
            }
            if (baseReady && !baseFiles.exists) {
              if (!baseFiles.mkdir) {
                log.error("mkdir '" + baseFiles + "' failed")
                baseReady = false
              }
            }
            if (externalStorageDisabled == None) {
              try {
                log.debug("test external storage")
                val testFile = new File(baseFiles, "testExternalStorage.tmp")
                if (testFile.exists)
                  testFile.delete
                val testContent = (for (i <- 0 until 1024) yield i).mkString // about 2.9 kB
                val out = new BufferedWriter(new FileWriter(testFile))
                out.write(testContent)
                out.close()
                assert(testFile.length == testContent.length)
                val source = scala.io.Source.fromFile(testFile)
                if (source.getLines.mkString == testContent) {
                  log.debug("external storge test successful")
                  externalStorageDisabled = Some(false)
                } else {
                  log.debug("external storge test failed")
                  externalStorageDisabled = Some(true)
                }
                source.close
                testFile.delete()
              } catch {
                case e =>
                  log.debug("external storge test failed, " + e.getMessage)
                  externalStorageDisabled = Some(true)
              }
            }
            if (baseReady)
              Some(new File(baseFiles, name))
            else
              None
          } else
            None
        })
        if (directory == None)
          log.warn("external storage " + Option(Environment.getExternalStorageDirectory) + " unavailable")
        directory.foreach(dir => {
          if (!dir.exists) {
            log.warn("directory " + dir + " does not exists, creating")
            if (dir.mkdir)
              isNew = true
            else {
              log.error("mkdir '" + dir + "' failed")
              directory = None
            }
          }
        })
      } catch {
        case e =>
          log.debug(e.getMessage, e)
          directory = None
      }
    }
    if (directory == None) {
      // try to use internal storage
      isExternal = false
      try {
        directory = Option(context.getFilesDir()).flatMap(base => Some(new File(base, name)))
        directory.foreach(dir => {
          if (!dir.exists)
            if (dir.mkdir)
              isNew = true
            else {
              log.error("mkdir '" + dir + "' failed")
              directory = None
            }
        })
      } catch {
        case e =>
          log.debug(e.getMessage, e)
          directory = None
      }
    }
    if (directory != None && isNew && !isExternal) {
      allRead match {
        case Some(true) => directory.get.setReadable(true, false)
        case Some(false) => directory.get.setReadable(true, true)
        case None => directory.get.setReadable(false, false)
      }
      allWrite match {
        case Some(true) => directory.get.setWritable(true, false)
        case Some(false) => directory.get.setWritable(true, true)
        case None => directory.get.setWritable(false, false)
      }
      allExecute match {
        case Some(true) => directory.get.setExecutable(true, false)
        case Some(false) => directory.get.setExecutable(true, true)
        case None => directory.get.setExecutable(false, false)
      }
    }
    directory
  }
  @Loggable
  def listInterfaces(): Seq[String] = {
    var interfaces = HashMap[String, Seq[String]]()
    try {
      val nie = NetworkInterface.getNetworkInterfaces()
      while (nie.hasMoreElements) {
        val ni = nie.nextElement
        val name = ni.getName()
        if (name != "lo") {
          interfaces = interfaces.updated(name, Seq())
          val iae = ni.getInetAddresses
          while (iae.hasMoreElements) {
            val ia = iae.nextElement
            val address = ia.getHostAddress
            // skip ipv6
            if (address.matches("""\d+\.\d+\.\d+.\d+""") && !address.endsWith("127.0.0.1"))
              interfaces = interfaces.updated(name, interfaces(name) :+ address)
          }
        }
      }
    } catch {
      case e =>
        // suspect permission error at one of interfaces ;-)
        log.warn("NetworkInterface.getNetworkInterfaces() failed with " + e +
          (if (e.getMessage() != null) " " + e.getMessage))
    }
    // convert hash interface -> address to string interface:address
    interfaces.keys.map(i => {
      if (interfaces(i).isEmpty) Seq(i + ":0.0.0.0") else interfaces(i).map(s => i + ":" + s)
    }).flatten.toSeq
  }
  @Loggable
  def checkInterfaceInUse(interface: String, aclMask: String): Boolean = try {
    log.debug("check interface " + interface + " against " + aclMask)
    def check(acl: String, str: String): Boolean =
      str.matches(acl.replaceAll("""\*""", ".+"))
    val Array(acl0: String, acl1: String, acl2: String, acl3: String, acl4: String) = aclMask.split("[:.]")
    val Array(i0: String, i1: String, i2: String, i3: String, i4: String) = interface.split("[:.]")
    check(acl0, i0) & check(acl1, i1) & check(acl2, i2) & check(acl3, i3) & check(acl4, i4)
  } catch {
    case e =>
      log.error(e.getMessage, e)
      false
  }
}
