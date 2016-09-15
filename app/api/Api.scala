package api

import java.util.Date
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import play.api.i18n.Lang
import play.api.mvc.{ Call, RequestHeader }

/*
* Set of general values and methods for the API
*/
object Api {

  //////////////////////////////////////////////////////////////////////
  // Headers

  final val HEADER_CONTENT_TYPE = "Content-Type"
  final val HEADER_CONTENT_LANGUAGE = "Content-Language"
  final val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
  final val HEADER_DATE = "Date"
  final val HEADER_LOCATION = "Location"
  final val HEADER_API_KEY = "X-Api-Key"
  final val HEADER_AUTH_TOKEN = "X-Auth-Token"

  final val HEADER_PAGE = "X-Page"
  final val HEADER_PAGE_FROM = "X-Page-From"
  final val HEADER_PAGE_SIZE = "X-Page-Size"
  final val HEADER_PAGE_TOTAL = "X-Page-Total"

  def basicHeaders(implicit lang: Lang) = Seq(
    HEADER_DATE -> printHeaderDate(new DateTime()),
    HEADER_CONTENT_LANGUAGE -> lang.language
  )

  def locationHeader(uri: String): (String, String) = HEADER_LOCATION -> uri
  def locationHeader(call: Call)(implicit request: RequestHeader): (String, String) = locationHeader(call.absoluteURL())

  //////////////////////////////////////////////////////////////////////
  // Date and joda.DateTime utils

  private final val longDateTimeFormatter = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss 'GMT'").withLocale(Locale.ENGLISH).withZoneUTC()
  def parseHeaderDate(dateStr: String): DateTime = longDateTimeFormatter.parseDateTime(dateStr)
  def printHeaderDate(date: DateTime): String = longDateTimeFormatter.print(date)

  private final val dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
  def parseDateTime(dateStr: String): Date = dateTimeFormatter.parse(dateStr)
  def printDateTime(date: Date): String = dateTimeFormatter.format(date)
  private final val dateFormatter = new SimpleDateFormat("dd-MM-yyyy")
  def parseDate(dateStr: String): Date = dateFormatter.parse(dateStr)
  def printDate(date: Date): String = dateFormatter.format(date)

  //////////////////////////////////////////////////////////////////////
  // Sorting

  object Sorting {
    final val ASC = false
    final val DESC = true
  }
}