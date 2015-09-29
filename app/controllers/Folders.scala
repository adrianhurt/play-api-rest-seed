package controllers

import api._
import api.ApiError._
import api.JsonCombinators._
import models.Folder
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.i18n.{ MessagesApi }

class Folders @Inject() (val messagesApi: MessagesApi) extends api.ApiController {

  def list(sort: Option[String], p: Int, s: Int) = SecuredApiAction { implicit request =>
    sortedPage(sort, Folder.sortingFields, default = "order") { sortingFields =>
      Folder.page(request.userId, sortingFields, p, s)
    }
  }

  // Returns the Location header, but not the folder information within the content body.
  def insert = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Folder] { folder =>
      Folder.insert(request.userId, folder.name).flatMap {
        case (id, newFolder) => created(Api.locationHeader(routes.Folders.info(id)))
      }
    }
  }

  def info(id: Long) = SecuredApiAction { implicit request =>
    maybeItem(Folder.findById(id))
  }

  def update(id: Long) = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Folder] { folder =>
      Folder.basicUpdate(id, folder.name).flatMap { isOk =>
        if (isOk) noContent() else errorInternal
      }
    }
  }

  def updateOrder(id: Long, newOrder: Int) = SecuredApiAction { implicit request =>
    Folder.updateOrder(id, newOrder).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def delete(id: Long) = SecuredApiAction { implicit request =>
    Folder.delete(id).flatMap { _ =>
      noContent()
    }
  }

}