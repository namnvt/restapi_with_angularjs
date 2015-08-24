package actors

import akka.actor.Actor
import akka.actor.Actor.Receive
import play.api.libs.Codecs
import models.Subscriber
/**
 * Created by nam.nvt on 7/9/2015.
 */
class TimeActor extends Actor {
  override def receive = {
    case Start(userId) =>
      println(s"-----------------------------------------------------Start, $userId")
    case Stop(userId) =>
      print("-----------------------------------------------------Stop, $userId")
  }
}
case class Start(userId: Int)

case class Stop(userId: Int)