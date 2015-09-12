package models

import api.Page
import api.Api.Sorting._
import java.util.Date
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Folder(
  id: Long,
  userId: Long,
  order: Int,
  name: String)

object Folder {
  import FakeDB.folders

  private def last(userId: Long): Int = (-1 +: folders.filter(_.userId == userId).map(_.order)).max

  def findById(id: Long): Future[Option[Folder]] = Future.successful {
    folders.get(id)
  }

  def insert(userId: Long, name: String): Future[(Long, Folder)] = Future.successful {
    folders.insert(Folder(_, userId, order = last(userId) + 1, name))
  }

  def basicUpdate(id: Long, name: String): Future[Boolean] = Future.successful {
    folders.update(id)(_.copy(name = name))
  }

  def updateOrder(id: Long, order: Int): Future[Boolean] = Future.successful {
    folders.update(id) { folder =>
      val newOrder = Math.max(0, Math.min(last(folder.userId), order))
      val oldOrder = folder.order
      if (newOrder == oldOrder)
        folder
      else {
        if (newOrder > oldOrder) {
          folders.filter(f => f.userId == folder.userId && f.order > oldOrder && f.order <= newOrder).map { f =>
            folders.update(f.id)(_.copy(order = f.order - 1))
          }
        } else if (newOrder < oldOrder) {
          folders.filter(f => f.userId == folder.userId && f.order >= newOrder && f.order < oldOrder).map { f =>
            folders.update(f.id)(_.copy(order = f.order + 1))
          }
        }
        folder.copy(order = newOrder)
      }
    }
  }

  def delete(id: Long): Future[Unit] = updateOrder(id, Int.MaxValue).map { hasUpdated =>
    if (hasUpdated) {
      FakeDB.tasks.delete(_.folderId == id)
      folders.delete(id)
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PAGINATION utilities

  /*
	* Returns a Page[Folder] with the user's folders
	* - sortFields: list of sorting params indicating their fields and if it should be ordered in ascending or descending order. Ex: Seq(("+", "order"), ("-", "name"))
	*/
  def page(userId: Long, sortingFields: Seq[(String, Boolean)], p: Int, s: Int): Future[Page[Folder]] = Future.successful {
    folders.page(p, s)(_.userId == userId)(sortingFields.map(sortingFunc): _*)
  }

  // List with all the available sorting fields.
  val sortingFields = Seq("id", "order", "name")
  // Defines a sorting function for the pair (field, order)
  def sortingFunc(fieldsWithOrder: (String, Boolean)): (Folder, Folder) => Boolean = fieldsWithOrder match {
    case ("id", ASC) => _.id < _.id
    case ("id", DESC) => _.id > _.id
    case ("order", ASC) => _.order < _.order
    case ("order", DESC) => _.order > _.order
    case ("name", ASC) => (a, b) => (a.name compareTo b.name) < 0
    case ("name", DESC) => (a, b) => (a.name compareTo b.name) > 0
    case _ => (_, _) => false
  }

}
