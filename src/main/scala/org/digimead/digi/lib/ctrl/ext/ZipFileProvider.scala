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

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import org.digimead.digi.lib.log.Logging
import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import org.digimead.digi.lib.util.FileUtil

abstract class ZipFileProvider extends ContentProvider with ContentProvider.PipeDataWriter[ZipFileProvider.ZipHandler] with Logging {
  def onCreate() = true
  def query(uri: Uri, projection: Array[String], selection: String, selectionArgs: Array[String], sortOrder: String): Cursor =
    throw new UnsupportedOperationException("Not supported by this provider")
  def insert(uri: Uri, values: ContentValues): Uri =
    throw new UnsupportedOperationException("Not supported by this provider")
  def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int =
    throw new UnsupportedOperationException("Not supported by this provider")
  def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]) =
    throw new UnsupportedOperationException("Not supported by this provider")
  def getType(uri: Uri): String =
    throw new UnsupportedOperationException("Not supported by this provider")
  override def openAssetFile(uri: Uri, mode: String): AssetFileDescriptor = {
    ZipFileProvider.zip.map(file => new ZipFile(file)) orElse getZip() match {
      case Some(zip) =>
        if (ZipFileProvider.zip == None)
          ZipFileProvider.zip = Some(new File(zip.getName))
        val path = uri.getPath().substring(1) // path without leading '/'
        log.debug("open " + path + " from " + zip.getName())
        // iterate through zip file until filename has been reach
        Option(zip.getInputStream(new ZipEntry(path))) match {
          case Some(in) =>
            new AssetFileDescriptor(openPipeHelper(null, null, null,
              ZipFileProvider.ZipHandler(in, zip), this), 0, AssetFileDescriptor.UNKNOWN_LENGTH)
          case None =>
            zip.close()
            val message = "\"" + path + "\" not found in \"" + zip.getName() + "\""
            log.warn(message)
            throw new FileNotFoundException(message)
        }
      case None =>
        throw new FileNotFoundException("unable to open " + uri)
    }
  }
  def writeDataToPipe(output: ParcelFileDescriptor, uri: Uri, s: String, bundle: Bundle, source: ZipFileProvider.ZipHandler) {
    val buffer = new Array[Byte](8192)
    val fout = new FileOutputStream(output.getFileDescriptor())
    try {
      FileUtil.writeToStream(source.in, fout)
    } catch {
      case e: ZipException =>
        log.error(e.getMessage(), e)
      case e: IOException =>
        log.error(e.getMessage(), e)
    } finally {
      try { fout.close() } catch { case e: IOException => }
      try { source.in.close() } catch { case e: IOException => }
      try { source.zip.close() } catch { case e: IOException => }
    }
  }
  protected def getZip(): Option[ZipFile]
  protected def resetZip() = ZipFileProvider.zip = None
}

object ZipFileProvider {
  case class ZipHandler(in: InputStream, zip: ZipFile)
  @volatile private var zip: Option[File] = None
}
