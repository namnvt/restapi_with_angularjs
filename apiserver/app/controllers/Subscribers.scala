package controllers

import com.github.tototoshi.play2.json4s.native._
import models._
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.libs.json.Json
import org.json4s.JsonDSL._

object Subscribers extends Controller with Json4s {

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

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
      formWithErrors => BadRequest(Json.obj("error" -> Json.obj("message" -> "User exists"))),
      form => {
        val subscriber = Subscriber.create(username = form.username, email = form.email, password = form.password)
        Ok(Json.obj("success" -> Json.obj("message" -> "User created successfully")))
      }
    )
  }
  def login = Action {
    Ok(Json.obj("success" -> Json.obj("message" -> "Logged in successfully" , "username" -> "Nam")))
  }
}
