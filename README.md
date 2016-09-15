# Play API REST Template [Play 2.5 - Scala]

Template to show how to implement an API using Play Framework.

__There is another companion project to test this template [here](https://github.com/adrianhurt/play-api-rest-tester).__

__Note:__ All this information is also available as a tutorial if you run the app using [Activator UI](http://typesafe.com/platform/getstarted).

The characteristics and objectives of this template are:

* RESTful.
* JSON: for request and response bodies.
* Assumes SSL everywhere (SSL is not covered in this template). 
* Use of Authentication Token to sign in.
* As much standard as possible.
* Allow pagination.
* Allow filtering, sorting and searching.
* Priorize the simplicity for creating new actions and controllers.
* Allow enveloping for those API clients those haven't access to HTTP Headers.

What this template does NOT explain:

* SSL: you can configure Play to do that. Or better, configure your web server.
* Security Issues like password storing.
* ETag, Last-Modified.
* GZIP.
* DB storage.

Please, __read this__ interesting post:
[Best Practices for Designing a Pragmatic RESTful API](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api)
 (also [in Spanish](http://elbauldelprogramador.com/buenas-practicas-para-el-diseno-de-una-api-restful-pragmatica/)).

First of all, this is a template, not a library. It simply tries to give you some clues to develop your own API. So copy the files you need, adapt them to your code and play with it.

And please, don't forget starring this project if you consider it has been useful for you.

Also check my other projects:

* [Play-Bootstrap - Play library for Bootstrap [Scala & Java]](https://adrianhurt.github.io/play-bootstrap)
* [Play Multidomain Seed [Play 2.5 - Scala]](https://github.com/adrianhurt/play-multidomain-seed)
* [Play Silhouette Credentials Seed [Play 2.5 - Scala]](https://github.com/adrianhurt/play-silhouette-credentials-seed)
* [Play Multidomain Auth [Play 2.5 - Scala]](https://github.com/adrianhurt/play-multidomain-auth)

## Preview

The main idea is to simplify the code for a common API. Let's see a preview example. Imagine you have a list of "todo" tasks. And only for logged users.

__conf/routes__

    GET         /tasks            controllers.Tasks.list(q: Option[String], done: Option[Boolean], sort: Option[String], page: Int ?= 1, size: Int ?= 10)
    GET         /tasks/:id        controllers.Tasks.info(id: Long)
    PUT         /tasks/:id        controllers.Tasks.update(id: Long)
    DELETE      /tasks/:id        controllers.Tasks.delete(id: Long)

Note you can paginate, filter, search and sort your lists:

    /tasks?size=20&page=3
    /tasks?q=blah&done=true
    /tasks?sort=-deadline,-date,order

__app/controllers/Tasks.scala__

```scala
class Tasks @Inject() (val messagesApi: MessagesApi) extends api.ApiController {

  // Returns a 'page' of tasks with searching, filtering, sorting and pagination
  def list(q: Option[String], done: Option[Boolean], sort: Option[String], p: Int, s: Int) = SecuredApiAction { implicit request =>
    sortedPage(sort, Task.sortingFields, default = "order") { sortingFields =>
      Task.page(q, done, sortingFields, p, s)
    }
  }

  // Returns a tasks or an 'ItemNotFound' error
  def info(id: Long) = SecuredApiAction { implicit request =>
    maybeItem(Task.findById(id))
  }

  // Updates a task
  def update(id: Long) = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Task] { task =>
      Task.basicUpdate(id, task.text, task.deadline).flatMap { isOk =>
        if (isOk) noContent() else errorInternal
      }
    }
  }

  // Deletes a task
  def delete(id: Long) = SecuredApiAction { implicit request =>
    Task.delete(id).flatMap { _ =>
      noContent()
    }
  }
}
```

Let's see how to get this.

## Basic structure

It's simple. In Play you have the classes `Controller`, `Action[A]`, `Request[A]` and `Result`. With this template we have their equivalences:

* `Controller` -> `ApiController`
* `Action[A]` -> `ApiAction`, `ApiActionWithBody`, `SecuredApiAction` and `SecuredApiActionWithBody`.
* `Request[A]` -> `ApiRequest[A]` and `SecuredApiRequest[A]`
* `Result` -> `ApiResponse` and `ApiError`, both extending the trait `ApiResult`.

### Api

Object that contains common values and methods.

### ApiRequest[A] and SecuredApiRequest[A]

Every request should have the following headers:

* __Content-Type__: application/json
* __Date__: with format _"E, dd MMM yyyy HH:mm:ss 'GMT'"_
* __X-Api-Key__: with the corresponding Api Key
* __X-Auth-Token__: with the corresponding Auth Token (only for __secured requests__)
* __Accept-Language__: with the desired language (optional)

`ApiRequest[A]` it's a simple `RequestWrapper[A]` with several additional values:

* `apiKeyOpt: Option[String]`: with the Api Key taken from the `X-Api-Key` header.
* `dateOpt: Option[DateTime]`: with the date taken from the header.
* `tokenOpt: Option[String]`: with the Auth Token taken from the `X-Auth-Token` header.

`SecuredApiRequest[A]` extends from `ApiRequest[A]` and it's used for secured requests where the user must be signed in.

* `apiKey: String`
* `date: DateTime`
* `token: String`
* `userId: Long`: with the id of the signed user.

`UserAwareApiRequest[A]` can be used when user authentication is optional. It extends from `ApiRequest[A]` add the following fields: 

* `apiKey: String`
* `date: DateTime`
* `isLogged: Boolean`: indicating if the user is logged or not.
* `token: Option[String]`
* `userId: Option[Long]`: with the id of the signed user.

### ApiResult

It's a trait that stores the information required to create a `Result`. It stores the Status Code, the JSON value and the headers for the response.

### ApiResponse

It extends from `ApiResult` and it represents a successful response for the request. The available Status Codes are: `STATUS_OK`, `STATUS_CREATED`, `STATUS_ACCEPTED` and `STATUS_NOCONTENT`. And it provides the following factory methods:

```scala
ok(json: JsValue, headers: (String, String)*)
ok[A](json: JsValue, page: Page[A], headers: (String, String)*)
created(json: JsValue, headers: (String, String)*)
created(headers: (String, String)*)
accepted(json: JsValue, headers: (String, String)*)
accepted(headers: (String, String)*)
noContent(headers: (String, String)*)
```

### ApiError

It extends from `ApiResult` and it represents an error response for the request. It stores a specific error code, a descriptive message and an optional additional object with more information. The JSON structure is the like the following:

```json
{ "code": 400, "msg": "Bad Request" }
```

or

```json
{ "code": 125, "msg": "Malformed body", "info": "additional information about the error" }
```

The available Status Codes are: `STATUS_BADREQUEST`, `STATUS_UNAUTHORIZED`, `STATUS_FORBIDDEN`, `STATUS_NOTFOUND` and `STATUS_INTERNAL_SERVER`. And it provides a list of predefined code errors and their corresponding factory methods.

### ApiController

It's simply a trait that extends from `Controller` and adds a set of utilities to do the life easier.

#### Actions

There are a list of useful actions for each request method:

```scala
ApiAction(action: ApiRequest[Unit] => Future[ApiResult])
ApiActionWithBody(action: ApiRequest[JsValue] => Future[ApiResult])
```

And their equivalences for secured requests (and optional ones):

```scala
SecuredApiAction(action: SecuredApiRequest[Unit] => Future[ApiResult])
SecuredApiActionWithBody(action: SecuredApiRequest[JsValue] => Future[ApiResult])

UserAwareApiAction(action: UserAwareApiRequest[Unit] => Future[ApiResult])
UserAwareApiActionWithBody(action: UserAwareApiRequest[JsValue] => Future[ApiResult])
```

#### Creating `ApiResults` from writable JSON objects

There are a set of useful methods to create `ApiResults` from JSON objects:

```scala
ok[A](obj: A, headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
ok[A](futObj: Future[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
maybeItem[A](opt: Option[A], headers: (String, String)*)(implicit w: Writes[A], req: RequestHeader): Future[ApiResult]
maybeItem[A](futOpt: Future[Option[A]], headers: (String, String)*)(implicit w: Writes[A], req: RequestHeader): Future[ApiResult]
page[A](p: Page[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
page[A](futP: Future[Page[A]], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
created[A](obj: A, headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
created[A](futObj: Future[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
created(headers: (String, String)*): Future[ApiResult]
accepted[A](obj: A, headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
accepted[A](futObj: Future[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult]
accepted(headers: (String, String)*): Future[ApiResult]
noContent(headers: (String, String)*): Future[ApiResult]
```

## Pagination

There is a simple class to hold the pagination information:

```scala
case class Page[+A](items: Seq[A], page: Int, size: Int, total: Long) {
  def offset = (page - 1) * size + 1
}
```

Then, we need to create a `Page` class with our list of items and simply call the `page()` method of`ApiController`.

```scala
val itemsPage: Future[Page[Item]] = ...
itemsPage.map(page(_))
```

or simply:

```scala
page(itemsPage)
```

It will add automatically the following headers to the response:

* `X-Page`: current page (int)
* `X-Page-From`: the position of the first item of this page within the total of items (int)
* `X-Page-Size`: number of items for each page (int)
* `X-Page-Total`: total number of items (int)

## Filtering, searching and sorting

Filtering and searching are very straightforward. Imagine you have an action to list the tasks in a TO-DO list. If you would like to add the ability to filter those that are done or not, or to seach for a specific query term, you only have to add this parameters and handle them to implement them within your search engine.

    /tasks?q=blah&done=true
    # Route
    GET    /tasks  controllers.Tasks.list(q: Option[String], done: Option[Boolean]) 

However, sorting is trickier. Let's see this example:

    /tasks?sort=-deadline,-date,order
    # Route
    GET    /tasks  controllers.Tasks.list(sort: Option[String]) 

The `sort` parameter have a comma separated list of signed fields, ordered by priority. So for the example, you would like to order by deadline in descendent order, then by date in descendent order, and finally by the task's order within the list in ascendent order. To make it a bit easier, `ApiController` defines the following method

```scala
processSortByParam(
  sortBy: Option[String], 
  allowedFields: Seq[String], 
  default: String): Either[ApiError, Seq[(String, Boolean)]]
```

It takes the `sort` parameter, a list of available allowed fields to sort by, and a default sorting string. Then it returns a list of pairs `(String, Boolean)` with the corresponding field and order (true if it's descendent), or an `ApiError` if any of the field is not allowed.

Then, you can implement the sorting as you want from this `Seq[(field, order)]`, depending on your DB storage.

## Envelope

There are cases where an API client can't access to the HTTP Headers. For that cases you can add the parameter `envelope=true` to the query and it will encapsulate the data information and the headers within a JSON object like that:

```json
{ "data": "your-json-response", "status": "status-code", "headers": { "header1": "value1", "header2": "value2" } }
```

## How to use it: a simple TODO list example

This template has implemented a basic TODO example, so you can check it. But here let's see some examples.

Your controllers should extend `ApiController` and `I18nSupport`.

    class Tasks @Inject() (val messagesApi: MessagesApi) extends api.ApiController { … }

To list the tasks and allow searching, filtering, sorting and pagination:

```scala
def list(folderId: Long, q: Option[String], done: Option[Boolean], sort: Option[String], p: Int, s: Int) = 
  SecuredApiAction { implicit request =>
    sortedPage(sort, Task.sortingFields, default = "order") { sortingFields =>
      Task.page(folderId, q, done, sortingFields, p, s)
    }
  }
```

Where `Task.page(…)` implements the functionality to return a `Page[Task]` with the corresponding tasks applying this parameters.

To insert new items, there is another method in `ApiController` that reads a writable object and returns an `ApiError` if needed.

```scala
def insert(folderId: Long) = SecuredApiActionWithBody { implicit request =>
  readFromRequest[Task] { task =>
    Task.insert(folderId, task.text, new Date(), task.deadline).flatMap {
      case (id, newTask) => created(newTask)
    }
  }
}
```

It would return the new created task within the body response. But if you would like to return an empty body you may want to add a `Location` header with the created URI:

```scala
created(Api.locationHeader(routes.Tasks.info(id)))
```

To return a single item, update it and delete it:

```scala
def info(id: Long) = SecuredApiAction { implicit request =>
  maybeItem(Task.findById(id))
}

def update(id: Long) = SecuredApiActionWithBody { implicit request =>
  readFromRequest[Task] { task =>
    Task.basicUpdate(id, task.text, task.deadline).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }
}

def delete(id: Long) = SecuredApiAction { implicit request =>
  Task.delete(id).flatMap { _ => noContent() }
}
```

The authorization based on the Auth Token is done within the `Auth` controller. It handles the sign in and sign out actions to handle the tokens, and sign up one to register a new user.

### FakeDB

For the example, a fake DB is implemented. The implementation is not relevant, but you can check all the information within the DB in your browser at `/fakeDB`. That will let you check what's happening while you're testing with the [tester](https://github.com/adrianhurt/play-api-rest-tester).

## Test Units

Although there is a companion [tester project](https://github.com/adrianhurt/play-api-rest-tester), don't forget to use unit testing for production. You can see some examples in this template.
