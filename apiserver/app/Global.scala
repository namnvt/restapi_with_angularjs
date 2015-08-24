import actors.{Start, TimeActor}
import akka.actor.Props
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.libs.Akka


object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val timeActor = Akka.system().actorOf(Props[TimeActor])
    val scheduler = QuartzSchedulerExtension(Akka.system())
    scheduler.schedule("Every30Seconds", timeActor, Start(1))
  }

  override def onStop(app: Application) = {
  }

}
