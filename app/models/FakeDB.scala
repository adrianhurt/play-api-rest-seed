package models

import api.Page
import java.text.SimpleDateFormat
import scala.collection.mutable.Map

/*
* A fake DB to store and load all the data
*/
object FakeDB {

  val dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")

  // API KEYS
  val apiKeys = FakeTable(
    1L -> ApiKey(apiKey = "AbCdEfGhIjK1", name = "ios-app", active = true),
    2L -> ApiKey(apiKey = "AbCdEfGhIjK2", name = "android-app", active = true)
  )

  // TOKENS
  val tokens = FakeTable[ApiToken]()

  // API REQUEST LOG
  val logs = FakeTable[ApiLog]()

  // USERS
  val users = FakeTable(
    1L -> User(1L, "user1@mail.com", "123456", "User 1", true, true),
    2L -> User(2L, "user2@mail.com", "123456", "User 2", true, true),
    3L -> User(3L, "user3@mail.com", "123456", "User 3", true, true)
  )

  // FOLDERS
  val folders = FakeTable(
    1L -> Folder(1L, 1L, 0, "Personal"),
    2L -> Folder(2L, 1L, 1, "Work"),
    3L -> Folder(3L, 1L, 2, "Home")
  )

  // TASKS
  val tasks = FakeTable(
    1L -> Task(1L, 1L, 0, "Shirts on dry cleaner", dt.parse("2015-09-06 10:11:00"), Some(dt.parse("2015-09-08 17:00:00")), true),
    2L -> Task(2L, 1L, 1, "Gift for Mum's birthday", dt.parse("2015-09-05 12:24:32"), Some(dt.parse("2015-10-22 00:00:00")), false),
    3L -> Task(3L, 1L, 2, "Plan the Barcelona's trip", dt.parse("2015-09-06 14:41:11"), None, false),
    4L -> Task(4L, 2L, 0, "Check monday's report", dt.parse("2015-09-06 09:21:00"), Some(dt.parse("2015-09-08 18:00:00")), false),
    5L -> Task(5L, 2L, 1, "Call conference with Jonh", dt.parse("2015-09-06 11:37:00"), Some(dt.parse("2015-09-07 18:00:00")), false),
    6L -> Task(6L, 3L, 0, "Fix the lamp", dt.parse("2015-08-16 21:22:00"), None, false),
    7L -> Task(7L, 3L, 1, "Buy coffee", dt.parse("2015-09-05 08:12:00"), None, false)
  )

  /*
	* Fake table that emulates a SQL table with an auto-increment index
	*/
  case class FakeTable[A](var table: Map[Long, A], var incr: Long) {
    def nextId: Long = {
      if (!table.contains(incr))
        incr
      else {
        incr += 1
        nextId
      }
    }
    def get(id: Long): Option[A] = table.get(id)
    def find(p: A => Boolean): Option[A] = table.values.find(p)
    def insert(a: Long => A): (Long, A) = {
      val id = nextId
      val tuple = (id -> a(id))
      table += tuple
      incr += 1
      tuple
    }
    def update(id: Long)(f: A => A): Boolean = {
      get(id).map { a =>
        table += (id -> f(a))
        true
      }.getOrElse(false)
    }
    def delete(id: Long): Unit = table -= id
    def delete(p: A => Boolean): Unit = table = table.filterNot { case (id, a) => p(a) }

    def values: List[A] = table.values.toList
    def map[B](f: A => B): List[B] = values.map(f)
    def filter(p: A => Boolean): List[A] = values.filter(p)
    def exists(p: A => Boolean): Boolean = values.exists(p)
    def count(p: A => Boolean): Int = values.count(p)
    def size: Int = table.size
    def isEmpty: Boolean = size == 0

    def page(p: Int, s: Int)(filterFunc: A => Boolean)(sortFuncs: ((A, A) => Boolean)*): Page[A] = {
      val items = filter(filterFunc)
      val sorted = sortFuncs.foldRight(items)((f, items) => items.sortWith(f))
      Page(
        items = sorted.drop((p - 1) * s).take(s),
        page = p,
        size = s,
        total = sorted.size
      )
    }
  }

  object FakeTable {
    def apply[A](elements: (Long, A)*): FakeTable[A] = apply(Map(elements: _*), elements.size + 1)
  }

}
