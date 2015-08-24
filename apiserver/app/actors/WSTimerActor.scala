package actors



import play.api.libs.json._
import play.api.libs.json.Json._

import akka.actor.Actor

import play.api.libs.iteratee.{Concurrent, Enumerator}

import play.api.libs.iteratee.Concurrent.Channel
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * User: Luigi Antonini
 * Date: 19/07/13
 * Time: 15.38
 */
/**
 * Created by nam.nvt on 8/3/2015.
 */
class WSTimerActor extends Actor {

  // crate a scheduler to send a message to this actor every socket
  val cancellable = context.system.scheduler.schedule(0 second, 1 second, self, UpdateTime())

  case class UserChannel(username: String, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

  lazy val log = Logger("application." + this.getClass.getName)

  // this map relate every user with his UserChannel
  var webSockets = Map[String, UserChannel]()

  // this map relate every user with his current time
  var usersTimes = Map[String, String]()

  override def receive = {

    case StartSocket(username) =>

      log.debug(s"start new socket for user $username")

      // get or create the touple (Enumerator[JsValue], Channel[JsValue]) for current user
      // Channel is very useful class, it allows to write data inside its related
      // enumerator, that allow to create WebSocket or Streams around that enumerator and
      // write data iside that using its related Channel
      val userChannel: UserChannel = webSockets.get(username) getOrElse {
        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        UserChannel(username, 0, broadcast._1, broadcast._2)
      }

      // if user open more then one connection, increment just a counter instead of create
      // another touple (Enumerator, Channel), and return current enumerator,
      // in that way when we write in the channel,
      // all opened WebSocket of that user receive the same data
      userChannel.channelsCount = userChannel.channelsCount + 1
      webSockets += (username -> userChannel)

      log debug s"channel for user : $username count : ${userChannel.channelsCount}"
      log debug s"channel count : ${webSockets.size}"

      // return the enumerator related to the user channel,
      // this will be used for create the WebSocket
      sender ! userChannel.enumerator

    case UpdateTime() =>

      // increase the current time for every user,
      // and send current time to the user,
      usersTimes.foreach {
        case (username, millis) =>
          usersTimes += (username -> (millis + 1000))

          val json = Map("data" -> toJson(millis))

          // writing data to tha channel,
          // will send data to all WebSocket opend form every user
          webSockets.get(username).get.channel push Json.toJson(json)
      }


    case StartWS(username) =>
      usersTimes += (username -> "")

    case StopWS(username) =>
      removeUserTimer(username)

      val json = Map("data" -> toJson(""))
      webSockets.get(username).get.channel push Json.toJson(json)

    case SocketClosed(username) =>

      log debug s"closed socket for $username"

      val userChannel = webSockets.get(username).get

      if (userChannel.channelsCount > 1) {
        userChannel.channelsCount = userChannel.channelsCount - 1
        webSockets += (username -> userChannel)
        log debug s"channel for user : $username count : ${userChannel.channelsCount}"
      } else {
        removeUserChannel(username)
        removeUserTimer(username)
        log debug s"removed channel and timer for $username"
      }

  }

  def removeUserTimer(username: String) = usersTimes -= username
  def removeUserChannel(username: String) = webSockets -= username

}


sealed trait SocketMessage

case class StartSocket(username: String) extends SocketMessage

case class SocketClosed(username: String) extends SocketMessage

case class UpdateTime() extends SocketMessage

case class StartWS(username: String) extends SocketMessage

case class StopWS(username: String) extends SocketMessage