package controllers

import akka.actor.Props
import akka.util.Timeout
import com.github.tototoshi.play2.json4s.native._
import models._
import org.json4s._
import akka.util.Timeout

import org.json4s.ext.JodaTimeSerializers
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import play.api.libs.Codecs
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import org.json4s.JsonDSL._
import play.api.cache.Cache
import play.api.Play.current
import play.libs.Akka
import akka.pattern.ask
import utils.AuthUtils
import actors._
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._

object Subscribers extends Controller with AuthUtils with Json4s {

  val timerActor = Akka.system.actorOf(Props[WSTimerActor])
  case class SubscriberForm(username: String, email: Option[String] = None, password: String)
  private val subscriberForm = Form(
    mapping(
      "username" -> text.verifying(nonEmpty),
      "email"  -> optional(text),
      "password" -> text.verifying(nonEmpty)
    )(SubscriberForm.apply)(SubscriberForm.unapply)
  )

  def signup = Action { implicit req =>
    subscriberForm.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.obj("error" -> Json.obj("message" -> "Invalid request!"))),
      form => {
        val subscriber = Subscriber.create(username = form.username, email = form.email, password = Codecs.sha1(form.password))
        Ok(Json.obj("success" -> Json.obj("message" -> "User created successfully"))).withSession("username" -> subscriber.username)
      }
    )
  }

  def login = Action {  implicit req =>
    subscriberForm.bindFromRequest.fold(
      formWithErrors => BadRequest(Json.obj("error" -> Json.obj("message" -> "Invalid request!"))),
      form => {
          Subscriber.findByUserAndPassword(username = form.username, password =  Codecs.sha1(form.password)).map { sc =>
            val uuid = java.util.UUID.randomUUID.toString
            println("random:" + uuid)
            Cache.set(uuid, org.json4s.native.JsonMethods.compact(org.json4s.native.JsonMethods.render(Extraction.decompose(sc))))
            val cacheduuid = Cache.get(uuid)
            for (value <- cacheduuid) {
              // value is of type String
              val sc1 = org.json4s.native.JsonMethods.parse(string2JsonInput(value.toString)).extract[Subscriber]
              println(sc1.username)
            }
            Cache.get(uuid) match {
              case Some(value) => org.json4s.native.JsonMethods.parse(string2JsonInput(value.toString)).extract[Subscriber]
              case _ => println("NULL")
            }
            Ok(Json.obj("success" -> Json.obj("message" -> "Logged in successfully", "user" -> sc.username, "Token" -> uuid))).withSession("username" -> sc.username).withCookies(Cookie("Token", uuid))
        }.getOrElse {
          Unauthorized
        }
      }
    )
  }
  def all = AuthenticatedAction { implicit req =>
    Ok(Extraction.decompose(Company.findAll))
  }
  def isAuthenticated = AuthenticatedAction {
    implicit req =>
      Ok(Json.obj("success" -> Json.obj("message" -> "User is logged in already", "user" -> req.subscriber.username)))
  }

  def logout = LogoutAction {
    Ok(Json.obj("success" -> Json.obj("message" -> "Logged out successfully"))).withNewSession.discardingCookies(DiscardingCookie("Token"))
  }
  /**
   * This function crate a WebSocket using the
   * enumertator linked to the current user,
   * retreived from the TaskActor.
   */
  def indexWS = AuthenticatedWS {
    username =>

      implicit val timeout = Timeout(3 seconds)

      // using the ask pattern of Akka,
      // get the enumerator for that user
      (timerActor ? StartSocket(username)) map {
        enumerator =>

          // create a Iteratee which ignore the input and
          // and send a SocketClosed message to the actor when
          // connection is closed from the client
          Right((Iteratee.ignore[JsValue] map {
            _ =>
              timerActor ! SocketClosed(username)
          }, enumerator.asInstanceOf[Enumerator[JsValue]]))
      }
  }
}
