package utils

import controllers.Subscribers._
import models.Subscriber
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc._
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.libs.ws.{WSResponse, WS}
import play.api.libs.concurrent._
import play.api.libs.iteratee._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.Play.current
import play.api.cache.Cache

trait AuthUtils {
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
  /**
   self:Controller =>

  def AuthenticateMe(f: String => Result) = Action { implicit request =>
    val user = AuthUtils.parseUserFromRequest

    if(user.isEmpty)
      Forbidden("Invalid username or password")
    else {
      f(user.get)
    }
  }
}

 * http://blog.shinetech.com/2015/04/21/playing-with-play-framework-2-3-x-rest-pipelines-and-scala/
object Conditions {
  type Condition = (User => Either[String, Unit])
  def isPremiumUser:Condition = {
    user => if(user.isPremium)
      Right()
    else
      Left("User must be premium")
  }


  def balanceGreaterThan(required:Int):Condition = {
    user => if(user.balance > required)
      Right()
    else
      Left(s"User balance must be > $required")
  }
}

trait PremiumUsersOnly {
  self:Authentication =>
  accessConditions = accessConditions :+ Conditions.isPremiumUser
}

  trait BalanceCheck {
  self:Authentication =>
  def getRequiredBalance:Int
  accessConditions = accessConditions :+ Conditions.balanceGreaterThan(getRequiredBalance)
}

object AuthUtils {
def parseTokenFromCookie(implicit request: RequestHeader) = {
    val token = request.cookies.get("Token")
    val username = request.session.get("username")
    token match {
      case Cache.get(username+".token") => username
      case _ => None
    }
  }

  def parseTokenFromQueryString(implicit request:RequestHeader) = {
    val query = request.queryString.map { case (k, v) => k -> v.mkString }
    val token = query get ("Token")
    val username = request.session.get("username")
    token match {
      case Cache.get(username+".token") => username
      case _ => None
    }

  }*/
  def getParamFromCookie(implicit request:RequestHeader, key:String):Option[String] = {
    val token = request.cookies.get("Token")
    token match {
      case Some(c) => Option(c.value)
      case _ => None
    }
  }
  def getParamFromQueryString(implicit request:RequestHeader, key:String):Option[String] = {
    val query = request.queryString.map {
      case (k, v) => k -> v.mkString
    }
    val token = query get ("Token")
    token match {
      case Some(c) => Option(c.toString)
      case _ => None
    }
  }
  def parseUserFromRequest(implicit request:RequestHeader):Option[Subscriber] = {
    val token = getParamFromCookie(request, "Token") orElse getParamFromQueryString(request,"Token")
    println(token)
    val cachedUser = token match {
      case Some(s) => Cache.get(s.toString)
      case _ => None
    }
    cachedUser match {
      case Some(s) => Option(org.json4s.native.JsonMethods.parse(string2JsonInput(s.toString)).extract[Subscriber])
      case _ => None
    }
  }
  def logoutUser(implicit request:RequestHeader) = {
    val token = getParamFromQueryString(request,"Token") orElse getParamFromCookie(request, "Token")
    val cachedUser = token match {
      case Some(s) => Cache.remove(s.toString)
      case _ => None
    }
  }
  /**
   * Helper method for extracting user credentials from an incoming http request
   * @param request the client request
   * @return an optional tuple containing the extracted username and password
   */
  def parseUserCredentials(request: RequestHeader): Option[(String, String)] = {
    // Retrieve the credentials from the request params
    val query = request.queryString.map {
      case (k, v) => k -> v.mkString
    }

    for {
      user <- query.get("username")
      pass <- query.get("password")
    } yield (user, pass)
  }

  /**
   * Helper method to search for specified user credentials in a json object
   * @param jsResponse the WebServices response containing the json body data
   * @param credentials the user credentials to match again
   * @return the json object containing the specified credentials
   */
  def getUserFromJs(jsResponse: WSResponse, credentials: (String, String)) = {
    val jsonArr = Json.parse(jsResponse.body).as[List[JsObject]]
    jsonArr.find( jsObj => (jsObj \ "username").as[String] == credentials._1 && (jsObj \ "password").as[String] == credentials._2)
  }

  /**class AuthenticatedRequest[A](val username: String, val password: String, val request: Request[A]) extends WrappedRequest[A](request)

  object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
      val optionalCredentialsTuple = parseUserCredentials(request)

      optionalCredentialsTuple.map {
        credentials => block(new AuthenticatedRequest(credentials._1, credentials._2, request))
      } getOrElse Future.successful(Forbidden("Invalid User Credentials"))
    }
  }*/
  class AuthenticatedRequest[A](val subscriber: Subscriber, val request: Request[A]) extends WrappedRequest[A](request)

  object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
      val optionalCredentialsTuple = parseUserFromRequest(request)
      optionalCredentialsTuple.map {
        sub => block(new AuthenticatedRequest(sub, request))
      } getOrElse Future.successful(Forbidden("Invalid User Credentials"))
    }
  }
  def AuthenticatedWS(f: => String => Future[Either[Result, (Iteratee[JsValue, Unit], Enumerator[JsValue])]]): WebSocket[JsValue, JsValue] = {

    // this function create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
    // the iteratee ignore the input and do nothing,
    // and the enumerator just send a 'not authorized message'
    // and close the socket, sending Enumerator.eof
    def errorFuture = {
      // Just consume and ignore the input
      val in = Iteratee.ignore[JsValue]

      // Send a single 'Hello!' message and close
      val out = Enumerator(Json.toJson("not authorized")).andThen(Enumerator.eof)

      Future {
        Left(Unauthorized)
      }
    }

    WebSocket.tryAccept[JsValue] {
      request =>
        parseUserFromRequest(request) match {
          case None =>
            errorFuture
          case Some(sub) =>
            f(sub.username)
        }
    }
  }
  object LogoutAction extends ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
      logoutUser(request)
      block(request)
    }
  }
  // Auth Continued
  class AuthorisedRequest[A](val perms: Option[List[String]], val request: AuthenticatedRequest[A]) extends WrappedRequest[A](request) {
    def username = request.subscriber.username
    def password = request.subscriber.password
  }

  def PermissionAction(perms: Option[List[String]]) = new ActionTransformer[AuthenticatedRequest, AuthorisedRequest] {
    def transform[A](request: AuthenticatedRequest[A]) = Future.successful {
      new AuthorisedRequest(perms, request)
    }
  }

  object AuthorisedAction extends ActionFunction[AuthorisedRequest, AuthorisedRequest] {
    override def invokeBlock[A](request: AuthorisedRequest[A], block: (AuthorisedRequest[A] => Future[Result])): Future[Result] =
    {
      val result = block(request)
      val isAuthorised = request.perms.map {
        permissions => Authorise((request.username, request.password), permissions)
      } getOrElse Future.successful(true)

      isAuthorised.flatMap {
        case true => result
        case false => Future.successful(Forbidden("Not authorised to be here"))
      }
    }
  }

  def Authorise(credentials: (String, String), permissions: List[String]): Future[Boolean] = {
    val futureJsUserArray = WS.url("http://www.json-generator.com/api/json/get/cfLxEnRoAy?indent=2").get()
    futureJsUserArray.map{ jsResponse =>
      getUserFromJs(jsResponse, credentials).exists(_ => true)
    }
  }
}
