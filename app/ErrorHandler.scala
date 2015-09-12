import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.{ Lang, MessagesApi, I18nSupport }
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._

import api.{ ApiError, ApiResult, ApiRequestHeaderImpl }
import api.ApiError._

class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router],
    val messagesApi: MessagesApi) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with I18nSupport {

  // 400 - Bad request. Called when a route is found, but it was not possible to bind the request parameters
  override def onBadRequest(request: RequestHeader, message: String) =
    jsError(errorBadRequest(message), request)

  // 403 - Forbidden
  override def onForbidden(request: RequestHeader, message: String) =
    jsError(errorForbidden, request)

  // 404 - Page not found error
  override def onNotFound(request: RequestHeader, message: String) =
    jsError(errorNotFound, request)

  // 4XX - An error in the 4xx series, which is not handled by any of the other methods in this class already
  override def onOtherClientError(request: RequestHeader, statusCode: Int, message: String) =
    jsError(ApiError(statusCode, message), request)

  // 500 - Internal server error
  override def onServerError(request: RequestHeader, exception: Throwable) =
    jsError(errorInternalServer(exception.getMessage), request)

  private def jsError(error: ApiError, request: RequestHeader): Future[Result] = Future.successful {
    error.saveLog(ApiRequestHeaderImpl(request)).toResult(request, implicitly[Lang])
  }

}