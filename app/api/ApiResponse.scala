package api

import Api._
import play.api.libs.json._

/*
* Successful response for an ApiRequest.
*/
case class ApiResponse(status: Int, json: JsValue, headers: Seq[(String, String)]) extends ApiResult

object ApiResponse {

  //////////////////////////////////////////////////////////////////////
  // Status Codes

  final val STATUS_OK = 200
  final val STATUS_CREATED = 201
  final val STATUS_ACCEPTED = 202
  final val STATUS_NOCONTENT = 204

  //////////////////////////////////////////////////////////////////////
  // Predefined responses

  def ok(json: JsValue, headers: (String, String)*) = apply(STATUS_OK, json, headers)
  def ok[A](json: JsValue, page: Page[A], headers: (String, String)*) = apply(STATUS_OK, json, headers ++ Seq(
    HEADER_PAGE -> page.page.toString,
    HEADER_PAGE_FROM -> page.offset.toString,
    HEADER_PAGE_SIZE -> page.size.toString,
    HEADER_PAGE_TOTAL -> page.total.toString
  ))
  def created(json: JsValue, headers: (String, String)*) = apply(STATUS_CREATED, json, headers)
  def created(headers: (String, String)*) = apply(STATUS_CREATED, JsNull, headers)
  def accepted(json: JsValue, headers: (String, String)*) = apply(STATUS_ACCEPTED, json, headers)
  def accepted(headers: (String, String)*) = apply(STATUS_ACCEPTED, JsNull, headers)
  def noContent(headers: (String, String)*) = apply(STATUS_NOCONTENT, JsNull, headers)

}