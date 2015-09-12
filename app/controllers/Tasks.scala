package controllers

import api._
import api.ApiError._
import api.JsonCombinators._
import models.Task
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.i18n.{ MessagesApi, I18nSupport }
import java.util.Date

class Tasks @Inject() (val messagesApi: MessagesApi) extends api.ApiController with I18nSupport {

  def list(folderId: Long, q: Option[String], done: Option[Boolean], sort: Option[String], p: Int, s: Int) = SecuredGetAction { implicit request =>
    sortedPage(sort, Task.sortingFields, default = "order") { sortingFields =>
      Task.page(folderId, q, done, sortingFields, p, s)
    }
  }

  def insert(folderId: Long) = SecuredPostAction { implicit request =>
    readFromRequest[Task] { task =>
      Task.insert(folderId, task.text, new Date(), task.deadline).flatMap {
        case (id, newTask) =>
          ok(newTask)
      }
    }
  }

  def info(id: Long) = SecuredGetAction { implicit request =>
    maybeItem(Task.findById(id))
  }

  def update(id: Long) = SecuredPutAction { implicit request =>
    readFromRequest[Task] { task =>
      Task.basicUpdate(id, task.text, task.deadline).flatMap { isOk =>
        if (isOk) noContent() else errorInternal
      }
    }
  }

  def updateOrder(id: Long, newOrder: Int) = SecuredPutAction { implicit request =>
    Task.updateOrder(id, newOrder).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def updateFolder(id: Long, folderId: Long) = SecuredPutAction { implicit request =>
    Task.updateFolder(id, folderId).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def updateDone(id: Long, done: Boolean) = SecuredPutAction { implicit request =>
    Task.updateDone(id, done).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def delete(id: Long) = SecuredDeleteAction { implicit request =>
    Task.delete(id).flatMap { _ =>
      noContent()
    }
  }

}