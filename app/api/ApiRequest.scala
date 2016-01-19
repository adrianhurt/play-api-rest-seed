package api

import Api._
import play.api.mvc._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import scala.util.Try
import play.api.libs.json._

/*
* Wrapped Request with additional information for the API (headers: Api Key, Date, Auth-Token, ...)
*/
trait ApiRequestHeader[R <: RequestHeader] {
  val request: R
  val apiKeyOpt: Option[String] = request.headers.get(HEADER_API_KEY)
  val dateOptTry: Option[Try[DateTime]] = request.headers.get(HEADER_DATE).map { dateStr =>
    Try(parseHeaderDate(dateStr))
  }
  val dateOpt: Option[DateTime] = dateOptTry.filter(_.isSuccess).map(_.get)
  val tokenOpt: Option[String] = request.headers.get(HEADER_AUTH_TOKEN)

  def dateOrNow: DateTime = dateOpt.getOrElse(new DateTime())
  def remoteAddress: String = request.remoteAddress
  def method: String = request.method
  def uri: String = request.uri
  def maybeBody: Option[String] = None
}

case class ApiRequestHeaderImpl(val request: RequestHeader) extends ApiRequestHeader[RequestHeader]

/*
* ApiRequestHeader for requests that don't require authentication
*/
class ApiRequest[A](val request: Request[A]) extends WrappedRequest[A](request) with ApiRequestHeader[Request[A]] {
  override def remoteAddress = request.remoteAddress
  override def method = request.method
  override def uri = request.uri
  override def maybeBody: Option[String] = request.body match {
    case body: JsValue => Some(Json.prettyPrint(body))
    case body: String => if (body.length > 0) Some(body) else None
    case body => Some(body.toString)
  }
}
object ApiRequest {
  def apply[A](request: Request[A]): ApiRequest[A] = new ApiRequest[A](request)
}

/*
* ApiRequest for user aware requests
*/
case class UserAwareApiRequest[A](override val request: Request[A], apiKey: String, date: DateTime, token: Option[String], userId: Option[Long]) extends ApiRequest[A](request) {
  def isLogged = userId.isDefined
}

/*
* ApiRequest for authenticated requests
*/
case class SecuredApiRequest[A](override val request: Request[A], apiKey: String, date: DateTime, token: String, userId: Long) extends ApiRequest[A](request)

