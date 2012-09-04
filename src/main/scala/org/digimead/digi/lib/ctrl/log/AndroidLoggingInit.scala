package org.digimead.digi.lib.ctrl.log

import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.log.Logger
import org.digimead.digi.lib.log.RichLogger
import android.content.Context

class AndroidLoggingInit extends Logging.Init {
  val logPrefix = "@" // prefix for all adb logcat TAGs, everyone may change (but should not) it on his/her own risk
  val isTraceExtraEnabled = false
  val isTraceEnabled = true
  val isDebugEnabled = true
  val isInfoEnabled = true
  val isWarnEnabled = true
  val isErrorEnabled = true
  val shutdownHook = new Thread() { override def run() = Logging.deinit }
  val richLoggerBuilder = (name) => new RichLogger(name)
  val flushLimit = 1000
  val loggers = Seq[Logger]()
}
