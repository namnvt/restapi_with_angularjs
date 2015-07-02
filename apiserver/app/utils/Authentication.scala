package utils

import play.api.mvc.{Action, Controller, RequestHeader, Result}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSResponse, WS}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

trait Authentication {
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
/**
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
*/
object AuthUtils {
  /**def parseTokenFromCookie(implicit request: RequestHeader) = {
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

  def parseUserFromRequest(implicit request:RequestHeader):Option[String] = {
    val query = request.queryString.map { case (k, v) => k -> v.mkString }
    val token = request.cookies.get("Token") orElse query.get("Token")
    val username = request.session.get("username")
    Cache.get(username+".token") match {
      case `token` => username
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

  class AuthenticatedRequest[A](val username: String, val password: String, val request: Request[A]) extends WrappedRequest[A](request)

  object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
      val optionalCredentialsTuple = parseUserCredentials(request)

      optionalCredentialsTuple.map {
        credentials => block(new AuthenticatedRequest(credentials._1, credentials._2, request))
      } getOrElse Future.successful(Forbidden("Invalid User Credentials"))
    }
  }

  // Auth Continued
  class AuthorisedRequest[A](val perms: Option[List[String]], val request: AuthenticatedRequest[A]) extends WrappedRequest[A](request) {
    def username = request.username
    def password = request.password
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
