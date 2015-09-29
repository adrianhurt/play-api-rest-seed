package controllers

import api.ApiError._
import api.JsonCombinators._
import models.{ User, ApiToken }
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.i18n.{ MessagesApi }
import play.api.libs.json._
import play.api.libs.functional.syntax._

class Account @Inject() (val messagesApi: MessagesApi) extends api.ApiController {

  def info = SecuredApiAction { implicit request =>
    maybeItem(User.findById(request.userId))
  }

  def update = SecuredApiActionWithBody { implicit request =>
    readFromRequest[User] { user =>
      User.update(request.userId, user.name).flatMap { isOk =>
        if (isOk) noContent() else errorInternal
      }
    }
  }

  implicit val pwdsReads: Reads[Tuple2[String, String]] = (
    (__ \ "old").read[String](Reads.minLength[String](1)) and
      (__ \ "new").read[String](Reads.minLength[String](6)) tupled
  )

  def updatePassword = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Tuple2[String, String]] {
      case (oldPwd, newPwd) =>
        User.findById(request.userId).flatMap {
          case None => errorUserNotFound
          case Some(user) if (oldPwd != user.password) => errorCustom("api.error.reset.pwd.old.incorrect")
          case Some(user) => User.updatePassword(request.userId, newPwd).flatMap { isOk =>
            if (isOk) noContent() else errorInternal
          }
        }
    }
  }

  def delete = SecuredApiAction { implicit request =>
    ApiToken.delete(request.token).flatMap { _ =>
      User.delete(request.userId).flatMap { _ =>
        noContent()
      }
    }
  }

}