package controllers

import api._
import api.ApiError._
import api.JsonCombinators._
import models.User
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.i18n.{ MessagesApi, I18nSupport }

class Users @Inject() (val messagesApi: MessagesApi) extends api.ApiController with I18nSupport {

  def usernames = ApiAction { implicit request =>
    User.list.flatMap { list =>
      ok(list.map(u => Json.obj("id" -> u.id, "name" -> u.name)))
    }
  }

}