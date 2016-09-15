package models

import api.ApiRequestHeader
import play.api.mvc.RequestHeader
import play.api.libs.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/*
* Stores all the information of a request. Specially used for store the errors in the DB.
*/
case class ApiLog(
    id: Long,
    date: DateTime,
    ip: String,
    apiKey: Option[String],
    token: Option[String],
    method: String,
    uri: String,
    requestBody: Option[String],
    responseStatus: Int,
    responseBody: Option[String]
) {
  def dateStr: String = ApiLog.dtf.print(date)
}
object ApiLog {
  import FakeDB.logs

  private val dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:ss:mm")

  def findById(id: Long): Future[Option[ApiLog]] = Future.successful {
    logs.get(id)
  }

  def insert[R <: RequestHeader](request: ApiRequestHeader[R], status: Int, json: JsValue): Future[(Long, ApiLog)] = Future.successful {
    logs.insert(ApiLog(
      _,
      date = request.dateOrNow,
      ip = request.remoteAddress,
      apiKey = request.apiKeyOpt,
      token = request.tokenOpt,
      method = request.method,
      uri = request.uri,
      requestBody = request.maybeBody,
      responseStatus = status,
      responseBody = if (json == JsNull) None else Some(Json.prettyPrint(json))
    ))
  }

  def delete(id: Long): Future[Unit] = Future.successful {
    logs.delete(id)
  }

}