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

package org.digimead.digi.lib.ctrl.ext

import java.util.concurrent.atomic.AtomicReference

import scala.Option.option2Iterable

import org.digimead.digi.lib.ctrl.R
import org.digimead.digi.lib.log.Logging

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

/*
 * Example:
 * abstract class CustomAlertDialog(icon: Option[Int], extContent: Option[View]) extends SherlockDialogFragment with XAlertDialog {
 *  def this(icon: Option[Int], extContent: Int) =
 *   this(icon, AppComponent.AppContext.flatMap {
 *     context =>
 *       val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
 *       Option(inflater.inflate(extContent, null))
 *   })
 *  def this(icon: Option[Int]) = this(icon, None)
 *  def this() = this(None, None)
 */

trait XAlertDialog extends XDialog with Logging {
  val icon: Option[Int]
  val extContent: Option[View]

  protected val contentView = new AtomicReference[Option[View]](None)
  protected val customView = new AtomicReference[Option[View]](None)
  protected val positiveView = new AtomicReference[Option[View]](None)
  protected val neutralView = new AtomicReference[Option[View]](None)
  protected val negativeView = new AtomicReference[Option[View]](None)
  protected lazy val (cachedModal,
    modalContent,
    modalCustomContent,
    modalNegative,
    modalNeutral,
    modalPositive) =
    XAlertDialog.buildModal(getDialogActivity, title, message, extContent, icon, positive, neutral, negative, tag)
  protected lazy val (cachedEmbedded,
    embeddedContent,
    embeddedCustomContent,
    embeddedNegative,
    embeddedNeutral,
    embeddedPositive) =
    XAlertDialog.buildEmbedded(getDialogActivity, title, message, extContent, icon,
      positive, neutral, negative, R.layout.fragment_dialog, tag)
  protected lazy val cachedEmbeddedAttr = XResource.getAttributeSet(getDialogActivity, R.layout.fragment_dialog)
  protected lazy val positive: Option[(Int, XDialog.ButtonListener[_ <: XDialog])] = None
  protected lazy val neutral: Option[(Int, XDialog.ButtonListener[_ <: XDialog])] = None
  protected lazy val negative: Option[(Int, XDialog.ButtonListener[_ <: XDialog])] = None

  def title: CharSequence
  def message: Option[CharSequence]
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    log.debug("XAlertDialog::onCreateView")
    super.onCreateView(inflater, container, savedInstanceState)
    if (getShowsDialog) {
      null
    } else {
      val context = getDialogActivity
      Option(cachedEmbedded.getParent).foreach(_.asInstanceOf[ViewGroup].removeView(cachedEmbedded))
      cachedEmbeddedAttr.foreach(attr => cachedEmbedded.setLayoutParams(container.generateLayoutParams(attr)))
      contentView.set(embeddedContent)
      customView.set(embeddedCustomContent)
      positiveView.set(Option(cachedEmbedded.findViewById(android.R.id.button3)))
      neutralView.set(Option(cachedEmbedded.findViewById(android.R.id.button2)))
      negativeView.set(Option(cachedEmbedded.findViewById(android.R.id.button1)))
      cachedEmbedded
    }
  }
  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    log.debug("XAlertDialog::onCreateDialog")
    super.onCreateDialog(savedInstanceState)
    cachedModal.show
    (modalContent, message) match {
      case (Some(content), Some(message)) =>
        content.setVisibility(View.VISIBLE)
        content.setText(message)
      case (Some(content), None) =>
        content.setVisibility(View.GONE)
      case (None, Some(message)) =>
        cachedModal.setMessage(message)
      case (None, None) =>
        try {
          cachedModal.setMessage(null)
        } catch {
          case e =>
            log.warn("unable to reset dialog content")
            cachedModal.setMessage("")
        }
    }
    cachedModal.setTitle(title)
    contentView.set(modalContent)
    customView.set(modalCustomContent)
    positiveView.set(Option(cachedModal.getButton(DialogInterface.BUTTON_POSITIVE)))
    neutralView.set(Option(cachedModal.getButton(DialogInterface.BUTTON_NEUTRAL)))
    negativeView.set(Option(cachedModal.getButton(DialogInterface.BUTTON_NEGATIVE)))
    cachedModal
  }
  override def onDestroyView() {
    log.debug("XAlertDialog::onDestroyView")
    super.onDestroyView
    positiveView.set(None)
    neutralView.set(None)
    negativeView.set(None)
  }
}

object XAlertDialog extends Logging {
  protected def buildModal(context: FragmentActivity, title: CharSequence,
    message: Option[CharSequence], extContent: Option[View], icon: Option[Int],
    positive: Option[(Int, XDialog.ButtonListener[_ <: XDialog])],
    neutral: Option[(Int, XDialog.ButtonListener[_ <: XDialog])],
    negative: Option[(Int, XDialog.ButtonListener[_ <: XDialog])],
    tag: String): (AlertDialog, Option[TextView], Option[View], Option[Button], Option[Button], Option[Button]) = {
    log.debug("XAlertDialog::buildModal for " + tag)
    val scale = context.getResources().getDisplayMetrics().density
    val padding = (10 * scale).toInt
    val builder = new AlertDialog.Builder(context).setTitle(title)
    val customContentView = extContent.map(extContent => {
      extContent.setPadding(padding, padding, padding, padding)
      builder.setView(extContent)
      extContent
    })
    icon.foreach(builder.setIcon)
    negative.foreach(t => builder.setNegativeButton(t._1, t._2))
    neutral.foreach(t => builder.setNeutralButton(t._1, t._2))
    positive.foreach(t => builder.setPositiveButton(t._1, t._2))

    val contentView = customContentView match {
      case Some(customContentView) =>
        Option(customContentView.findViewById(android.R.id.custom).asInstanceOf[ViewGroup]).map {
          extViewContent =>
            val contentView = new TextView(context)
            contentView.setTextAppearance(context, android.R.style.TextAppearance_Medium)
            contentView.setPadding(0, 0, 0, padding)
            extViewContent.addView(contentView, 0, new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            contentView
        }
      case None =>
        message.foreach(builder.setMessage)
        None
    }
    val dialog = builder.create()
    /*
     * ABSOLUTELY CRAZY BEHAVIOR, emulator, API 10
     * without dialog.show most of the time (sometimes, rarely not)
     * 
     * android.util.AndroidRuntimeException: requestFeature() must be called before adding content
     * at com.android.internal.policy.impl.PhoneWindow.requestFeature(PhoneWindow.java:181)
     * at com.android.internal.app.AlertController.installContent(AlertController.java:199)
     * at android.app.AlertDialog.onCreate(AlertDialog.java:251)
     * at android.app.Dialog.dispatchOnCreate(Dialog.java:307)
     * at android.app.Dialog.show(Dialog.java:225)
     * at android.support.v4.app.DialogFragment.onStart(DialogFragment.java:385)
     */
    dialog.show
    dialog.hide
    val negativeView = negative.flatMap(n => Option(dialog.getButton(0)))
    val neutralView = neutral.flatMap(n => Option(dialog.getButton(1)))
    val positiveView = positive.flatMap(n => Option(dialog.getButton(2)))
    (dialog, contentView, customContentView, negativeView, neutralView, positiveView)
  }
  protected def buildEmbedded(context: FragmentActivity, title: CharSequence,
    message: Option[CharSequence], extContent: Option[View], icon: Option[Int],
    positive: Option[(Int, XDialog.ButtonListener[_ <: XDialog])],
    neutral: Option[(Int, XDialog.ButtonListener[_ <: XDialog])],
    negative: Option[(Int, XDialog.ButtonListener[_ <: XDialog])],
    baseView: Int, tag: String): (View, Option[TextView], Option[View], Option[Button], Option[Button], Option[Button]) = {
    log.debug("XAlertDialog::buildEmbedded for " + tag)
    def setButtonListener(bView: Button, title: Int, callback: XDialog.ButtonListener[_ <: XDialog], whichButton: Int) {
      bView.setVisibility(View.VISIBLE)
      bView.setText(title)
      bView.setOnClickListener(new View.OnClickListener { def onClick(v: View) = callback.onClick(null, whichButton) })
    }
    val view = LayoutInflater.from(context).inflate(baseView, null)
    val contentView = view.findViewById(android.R.id.custom).asInstanceOf[TextView]
    val titleView = view.findViewById(android.R.id.title).asInstanceOf[TextView]
    titleView.setText(title)
    icon.foreach {
      icon =>
        val iconContainer = view.findViewById(android.R.id.icon).asInstanceOf[ImageView]
        iconContainer.setImageResource(icon)
        iconContainer.setVisibility(View.VISIBLE)
    }
    val customContentView = extContent.map(extContent => {
      val parent = contentView.getParent.asInstanceOf[ViewGroup]
      parent.addView(extContent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
      extContent
    })
    message match {
      case Some(message) =>
        contentView.setText(message)
      case None =>
        contentView.setVisibility(View.GONE)
    }
    if (negative.nonEmpty || neutral.nonEmpty || positive.nonEmpty)
      view.findViewById(android.R.id.summary).setVisibility(View.VISIBLE)
    val negativeView = negative.map(t => {
      val buttonView = view.findViewById(android.R.id.button1).asInstanceOf[Button]
      setButtonListener(buttonView, t._1, t._2, DialogInterface.BUTTON_NEGATIVE)
      buttonView
    })
    val neutralView = neutral.map(t => {
      val buttonView = view.findViewById(android.R.id.button2).asInstanceOf[Button]
      setButtonListener(buttonView, t._1, t._2, DialogInterface.BUTTON_NEUTRAL)
      buttonView
    })
    val positiveView = positive.map(t => {
      val buttonView = view.findViewById(android.R.id.button3).asInstanceOf[Button]
      setButtonListener(buttonView, t._1, t._2, DialogInterface.BUTTON_POSITIVE)
      buttonView
    })
    (view, Some(contentView), customContentView, negativeView, neutralView, positiveView)
  }
}
