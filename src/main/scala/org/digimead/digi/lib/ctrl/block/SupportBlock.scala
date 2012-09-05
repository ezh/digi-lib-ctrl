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

package org.digimead.digi.lib.ctrl.block

import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicInteger

import scala.actors.Future
import scala.actors.Futures
import scala.annotation.implicitNotFound
import scala.ref.WeakReference

import org.digimead.digi.lib.ctrl.AnyBase
import org.digimead.digi.lib.ctrl.ext.XAPI
import org.digimead.digi.lib.ctrl.ext.XResource
import org.digimead.digi.lib.aop.Loggable
import org.digimead.digi.lib.ctrl.base.AppComponent
import org.digimead.digi.lib.ctrl.declaration.DConstant
import org.digimead.digi.lib.ctrl.declaration.DTimeout
import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.ctrl.message.Dispatcher
import org.digimead.digi.lib.ctrl.message.IAmWarn
import org.digimead.digi.lib.ctrl.message.IAmYell

import com.commonsware.cwac.merge.MergeAdapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class SupportBlock(val context: Context,
  val projectUri: Future[Uri],
  val issuesUri: Future[Uri],
  val emailTo: Future[String],
  val emailSubject: Future[String],
  val voicePhone: Future[String],
  val skypeUser: Future[String],
  val icqUser: Future[String])(implicit val dispatcher: Dispatcher) extends Block[SupportBlock.Item] with Logging {
  val itemProject = SupportBlock.Item(XResource.getString(context, "block_support_project_title").getOrElse("project %s").format(XResource.getString(context, "app_name").get),
    XResource.getString(context, "block_support_project_description").getOrElse("open %s project web site").format(XResource.getString(context, "app_name").get), "ic_block_support_project")
  val itemIssues = SupportBlock.Item(XResource.getString(context, "block_support_issues_title").getOrElse("view or submit an issue"),
    XResource.getString(context, "block_support_issues_description").getOrElse("bug reports, feature requests and enhancements"), "ic_block_support_issues")
  val itemEmail = SupportBlock.Item(XResource.getString(context, "block_email_title").getOrElse("send message"),
    XResource.getString(context, "block_support_email_description").getOrElse("email us directly"), "ic_block_support_message")
  val itemChat = SupportBlock.Item(XResource.getString(context, "block_chat_title").getOrElse("live chat via Skype, VoIP, ..."),
    XResource.getString(context, "block_support_chat_description").getOrElse("please call from 06:00 to 18:00 UTC"), "ic_block_support_chat")
  val items = Seq(itemProject, itemIssues, itemEmail, itemChat)
  private lazy val header = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater].
    inflate(XResource.getId(context, "header", "layout"), null).asInstanceOf[TextView]
  private lazy val adapter = new SupportBlock.Adapter(context, XResource.getId(context, "block_list_item", "layout"), items)
  SupportBlock.block = Some(this)
  @Loggable
  def appendTo(mergeAdapter: MergeAdapter) = {
    log.debug("append " + getClass.getName + " to MergeAdapter")
    header.setText(Html.fromHtml(XResource.getString(context, "block_support_title").getOrElse("support")))
    mergeAdapter.addView(header)
    mergeAdapter.addAdapter(adapter)
  }
  @Loggable
  def onListItemClick(l: ListView, v: View, item: SupportBlock.Item) = {
    item match {
      case this.itemProject => // jump to project
        Futures.future {
          Futures.awaitAll(SupportBlock.retriveTimeout, projectUri).asInstanceOf[List[Option[Uri]]] match {
            case List(Some(projectUri)) =>
              log.debug("open project web site at " + projectUri)
              try {
                val intent = new Intent(Intent.ACTION_VIEW, projectUri)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                AppComponent.Context match {
                  case Some(activity) if activity.isInstanceOf[Activity] => activity.startActivity(intent)
                  case _ => context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
              } catch {
                case e =>
                  IAmYell("Unable to open project link: " + projectUri, e)
              }
            case _ =>
              IAmWarn("Unable to get project link information, timeout")
          }
        }
      case this.itemIssues => // jump to issues
        Futures.future {
          Futures.awaitAll(SupportBlock.retriveTimeout, issuesUri).asInstanceOf[List[Option[Uri]]] match {
            case List(Some(issuesUri)) =>
              log.debug("open issues web page at " + issuesUri)
              try {
                val intent = new Intent(Intent.ACTION_VIEW, issuesUri)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                AppComponent.Context match {
                  case Some(activity) if activity.isInstanceOf[Activity] => activity.startActivity(intent)
                  case _ => context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
              } catch {
                case e =>
                  IAmYell("Unable to open issues web page link: " + issuesUri, e)
              }
            case _ =>
              IAmWarn("Unable to get issues web page information, timeout")
          }
        }
      case this.itemEmail => // create email
        Futures.future {
          Futures.awaitAll(SupportBlock.retriveTimeout, emailTo, emailSubject).asInstanceOf[List[Option[String]]] match {
            case List(Some(emailTo), Some(emailSubject)) =>
              log.debug("send email to " + emailTo)
              try {
                val uri = Uri.parse("mailto:" + emailTo)
                val intent = new Intent(Intent.ACTION_SENDTO, uri)
                intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                AppComponent.Context match {
                  case Some(activity) if activity.isInstanceOf[Activity] => activity.startActivity(intent)
                  case _ => context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
              } catch {
                case e =>
                  IAmYell("Unable 'send to' email: " + emailTo + " / " + emailSubject, e)
              }
            case _ =>
              IAmWarn("Unable to get email information, timeout")
          }
        }
      case this.itemChat => // show context menu with call/skype
        log.debug("open context menu for voice call")
        l.showContextMenuForChild(v)
      case item =>
        log.fatal("unsupported context menu item " + item)
    }
  }
  @Loggable
  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo, item: SupportBlock.Item) {
    log.debug("create context menu for " + item.name)
    menu.setHeaderTitle(item.name)
    if (item.icon.nonEmpty)
      XResource.getId(context, item.icon, "drawable") match {
        case i if i != 0 =>
          menu.setHeaderIcon(i)
        case _ =>
      }
    item match {
      case this.itemProject =>
        menu.add(Menu.NONE, XResource.getId(context, "block_link_copy"), 1,
          XResource.getString(context, "block_link_copy").getOrElse("Copy link"))
        menu.add(Menu.NONE, XResource.getId(context, "block_link_send"), 2,
          XResource.getString(context, "block_link_send").getOrElse("Send link to ..."))
      case this.itemIssues =>
        menu.add(Menu.NONE, XResource.getId(context, "block_link_copy"), 1,
          XResource.getString(context, "block_link_copy").getOrElse("Copy link"))
        menu.add(Menu.NONE, XResource.getId(context, "block_link_send"), 2,
          XResource.getString(context, "block_link_send").getOrElse("Send link to ..."))
      case this.itemEmail =>
      // none
      case this.itemChat =>
        menu.add(Menu.NONE, XResource.getId(context, "block_support_voice_contact"), 1,
          XResource.getString(context, "block_support_voice_contact").getOrElse("Copy phone number, USA/Canada Toll Free"))
        menu.add(Menu.NONE, XResource.getId(context, "block_support_skype_contact"), 1,
          XResource.getString(context, "block_support_skype_contact").getOrElse("Copy Skype account id"))
        menu.add(Menu.NONE, XResource.getId(context, "block_support_icq_contact"), 1,
          XResource.getString(context, "block_support_icq_contact").getOrElse("Copy ICQ account id"))
      case item =>
        log.fatal("unsupported context menu item " + item)
    }
  }
  @Loggable
  override def onContextItemSelected(menuItem: MenuItem, item: SupportBlock.Item): Boolean = {
    item match {
      case this.itemProject =>
        Futures.future {
          Futures.awaitAll(SupportBlock.retriveTimeout, projectUri).asInstanceOf[List[Option[Uri]]] match {
            case List(Some(projectUri)) =>
              menuItem.getItemId match {
                case id if id == XResource.getId(context, "block_link_copy") =>
                  Block.copyLink(context, item, projectUri.toString)
                case id if id == XResource.getId(context, "block_link_send") =>
                  Block.sendLink(context, item, item.name, projectUri.toString)
                case item =>
                  log.fatal("skip unknown menu item " + item)
              }
            case _ =>
              IAmWarn("Unable to get project link information, timeout")
          }
        }
        true
      case this.itemIssues =>
        Futures.future {
          Futures.awaitAll(SupportBlock.retriveTimeout, issuesUri).asInstanceOf[List[Option[Uri]]] match {
            case List(Some(issuesUri)) =>
              menuItem.getItemId match {
                case id if id == XResource.getId(context, "block_link_copy") =>
                  Block.copyLink(context, item, issuesUri.toString)
                case id if id == XResource.getId(context, "block_link_send") =>
                  Block.sendLink(context, item, item.name, issuesUri.toString)
                case item =>
                  log.fatal("skip unknown menu item " + item)
              }
            case _ =>
              IAmWarn("Unable to get open issues information, timeout")
          }
        }
        true
      case this.itemEmail =>
        false
      case this.itemChat =>
        Futures.future {
          Futures.awaitAll(SupportBlock.retriveTimeout, voicePhone, skypeUser, icqUser).asInstanceOf[List[Option[String]]] match {
            case List(Some(voicePhone), Some(skypeUser), Some(icqUser)) =>
              menuItem.getItemId match {
                case id if id == XResource.getId(context, "block_support_voice_contact") =>
                  log.debug("copy to clipboard phone " + voicePhone)
                  try {
                    val message = XResource.getString(context, "block_support_copy_voice_contact").
                      getOrElse("Copy to clipboard phone \"" + voicePhone + "\"")
                    AnyBase.runOnUiThread {
                      try {
                        XAPI.clipboardManager(context).setText(voicePhone)
                        Toast.makeText(context, message, DConstant.toastTimeout).show()
                      } catch {
                        case e =>
                          IAmYell("Unable to copy to clipboard phone " + voicePhone, e)
                      }
                    }
                  } catch {
                    case e =>
                      IAmYell("Unable to copy to clipboard phone " + voicePhone, e)
                  }
                case id if id == XResource.getId(context, "block_support_skype_contact") =>
                  log.debug("copy to clipboard Skype account id " + skypeUser)
                  try {
                    val message = XResource.getString(context, "block_support_copy_skype_contact").
                      getOrElse("Copy to clipboard Skype account \"" + skypeUser + "\"")
                    AnyBase.runOnUiThread {
                      try {
                        XAPI.clipboardManager(context).setText(skypeUser)
                        Toast.makeText(context, message, DConstant.toastTimeout).show()
                      } catch {
                        case e =>
                          IAmYell("Unable to copy to clipboard Skype account id " + skypeUser, e)
                      }
                    }
                  } catch {
                    case e =>
                      IAmYell("Unable to copy to clipboard Skype account id " + skypeUser, e)
                  }
                case id if id == XResource.getId(context, "block_support_icq_contact") =>
                  log.debug("copy to clipboard ICQ account id " + icqUser)
                  try {
                    val message = XResource.getString(context, "block_support_copy_icq_contact").
                      getOrElse("Copy to clipboard ICQ account \"" + icqUser + "\"")
                    AnyBase.runOnUiThread {
                      try {
                        XAPI.clipboardManager(context).setText(icqUser)
                        Toast.makeText(context, message, DConstant.toastTimeout).show()
                      } catch {
                        case e =>
                          IAmYell("Unable to copy to clipboard ICQ account id " + icqUser, e)
                      }
                    }
                    true
                  } catch {
                    case e =>
                      IAmYell("Unable to copy to clipboard Skype account id " + skypeUser, e)
                  }
                case id =>
                  log.fatal("unknown context menu id " + id)
              }
            case _ =>
              IAmWarn("Unable to get contact information, timeout")
          }
        }
        true
      case item =>
        log.fatal("unsupported context menu item " + item)
        false
    }
  }
}

object SupportBlock extends Logging {
  @volatile private var block: Option[SupportBlock] = None
  private val retriveTimeout = DTimeout.normal
  private val name = "name"
  private val description = "description"
  sealed case class Item(id: Int)(val name: String, val description: String, val icon: String = "") extends Block.Item
  object Item {
    private val counter = new AtomicInteger(0)
    def apply(name: String, description: String) = new Item(counter.getAndIncrement)(name, description)
    def apply(name: String, description: String, icon: String) = new Item(counter.getAndIncrement)(name, description, icon)
  }
  class Adapter(context: Context, textViewResourceId: Int, data: Seq[Item])
    extends ArrayAdapter(context, textViewResourceId, android.R.id.text1, data.toArray) {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val item = data(position)
      item.view.get match {
        case None =>
          val view = inflater.inflate(textViewResourceId, null)
          val text1 = view.findViewById(android.R.id.text1).asInstanceOf[TextView]
          val text2 = view.findViewById(android.R.id.text2).asInstanceOf[TextView]
          val icon = view.findViewById(android.R.id.icon1).asInstanceOf[ImageView]
          text2.setVisibility(View.VISIBLE)
          text1.setText(Html.fromHtml(item.name))
          setCallTimeDescription(item, text2)
          if (item.icon.nonEmpty)
            XResource.getId(context, item.icon, "drawable") match {
              case i if i != 0 =>
                icon.setVisibility(View.VISIBLE)
                icon.setImageDrawable(context.getResources.getDrawable(i))
              case _ =>
            }
          Level.novice(view)
          item.view = new WeakReference(view)
          view
        case Some(view) =>
          val text2 = view.findViewById(android.R.id.text2).asInstanceOf[TextView]
          setCallTimeDescription(item, text2)
          view
      }
    }
    private def setCallTimeDescription(item: SupportBlock.Item, text2: TextView) {
      block match {
        case Some(block) if item == block.itemChat =>
          val hour = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.HOUR_OF_DAY)
          log.trace("current hour " + hour)
          if (hour >= 6 && hour <= 18)
            text2.setText(Html.fromHtml("<font color='green'>" + item.description + "</font>"))
          else
            text2.setText(Html.fromHtml("<font color='red'>" + item.description + "</font>"))
        case _ =>
          text2.setText(Html.fromHtml(item.description))
      }
    }
  }
}
