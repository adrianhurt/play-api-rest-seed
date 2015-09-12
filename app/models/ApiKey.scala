package models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/*
* Stores the Api Key information
*/
case class ApiKey(
  apiKey: String,
  name: String,
  active: Boolean)
object ApiKey {
  import FakeDB.apiKeys

  def isActive(apiKey: String): Future[Option[Boolean]] = Future.successful {
    apiKeys.find(_.apiKey == apiKey).map(_.active)
  }

}
