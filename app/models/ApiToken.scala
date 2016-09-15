package models

import org.joda.time.DateTime
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/*
* Stores the Auth Token information. Each token belongs to a Api Key and user
*/
case class ApiToken(
    token: String, // UUID 36 digits
    apiKey: String,
    expirationTime: DateTime,
    userId: Long
) {
  def isExpired = expirationTime.isBeforeNow
}

object ApiToken {
  import FakeDB.tokens

  def findByTokenAndApiKey(token: String, apiKey: String): Future[Option[ApiToken]] = Future.successful {
    tokens.find(t => t.token == token && t.apiKey == apiKey)
  }

  def create(apiKey: String, userId: Long): Future[String] = Future.successful {
    // Be sure the uuid is not already taken for another token
    def newUUID: String = {
      val uuid = UUID.randomUUID().toString
      if (!tokens.exists(_.token == uuid)) uuid else newUUID
    }
    val token = newUUID
    tokens.insert(_ => ApiToken(token, apiKey, expirationTime = (new DateTime()) plusMinutes 10, userId))
    token
  }

  def delete(token: String): Future[Unit] = Future.successful {
    tokens.delete(_.token == token)
  }
}
