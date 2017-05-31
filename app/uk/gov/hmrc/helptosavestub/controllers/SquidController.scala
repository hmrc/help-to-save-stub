/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helptosavestub.controllers

import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}
import java.time.temporal.TemporalAdjusters
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsError, _}
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.Constants._
import uk.gov.hmrc.helptosavestub.models.{AccountCommand, CreateAccount}

import scala.reflect.internal.ClassfileConstants.FlagTranslation
import scala.util.{Failure, Success, Try}

@Singleton
class SquidController @Inject()(val messagesApi: MessagesApi) extends BaseController {

  private val countryCodes = Set[String](
    "GM", "CZ", "VA", "BS", "ZW", "ZM", "YE", "VN", "VE", "VU",
    "UZ", "UY", "US", "AE", "UA", "UG", "TV", "TM", "TR", "TN",
    "TT", "TO", "TG", "TH", "TZ", "TJ", "SY", "CH", "SE", "SZ",
    "SR", "SD", "LK", "ES", "SS", "ZA", "SO", "SB", "SI", "SK",
    "SG", "SL", "SC", "RS", "SN", "SA", "ST", "SM", "WS", "VC",
    "LC", "KN", "RW", "RU", "RO", "QA", "PT", "PL", "PH", "PE",
    "PY", "PG", "PA", "PW", "PK", "OM", "NO", "NG", "NE", "NI",
    "NZ", "NL", "NP", "NR", "NA", "MZ", "MA", "ME", "MN", "MC",
    "MD", "FM", "MX", "MU", "MR", "MH", "MT", "ML", "MV", "MY",
    "MW", "MG", "MK", "LU", "LT", "LI", "LY", "LR", "LS", "LB",
    "LV", "LA", "KG", "KW", "XK", "KR", "KP", "KI", "KE", "KZ",
    "JO", "JP", "JM", "CI", "IT", "IL", "IE", "IQ", "IR", "ID",
    "IN", "IS", "HU", "HN", "HT", "GY", "GW", "GN", "GT", "GD",
    "GR", "GH", "DE", "GE", "GA", "FR", "FI", "FJ", "ET", "EE",
    "ER", "GQ", "SV", "EG", "EC", "TL", "DO", "DM", "DJ", "DK",
    "CY", "CU", "HR", "CR", "CD", "CG", "KM", "CO", "CN", "CL",
    "TD", "CF", "CV", "CA", "CM", "KH", "BI", "MM", "BF", "BG",
    "BN", "BR", "BW", "BA", "BO", "BT", "BJ", "BZ", "BE", "BY",
    "BB", "BD", "BH", "AZ", "AT", "AU", "AM", "AR", "AG", "AO",
    "AD", "DZ", "AL", "AF", "GB", "CS", "YU", "DD", "SU")

  private def errorJson(code: String, messageKey: String = "", detailKey: String = ""): JsValue = {
    val errorMap: Map[String, Map[String, String]] =
      Map("error" ->
        Map("errorMessageId" -> code,
          "errorMessage" -> messagesApi(messageKey),
          "errorDetail" -> messagesApi(detailKey)))
    Json.toJson(errorMap)
  }

  private def errorJsonWithTriple(triple: (String, String, String)): JsValue = {
    val errorMap: Map[String, Map[String, String]] =
      Map("error" ->
        Map("errorMessageId" -> triple._1,
          "errorMessage" -> triple._2,
          "errorDetail" -> triple._3))
    Json.toJson(errorMap)
  }

  private def hasNumericChars(str: String): Boolean = """.*\d.*""".r.pattern.matcher(str).matches

  private def hasDisallowedChars(str: String): Boolean = !"""^[a-zA-Z&\.-]*$""".r.pattern.matcher(str).matches

  private def hasSpecialInFirstPlace(str: String): Boolean = """^[&\.-].*""".r.pattern.matcher(str).matches

  private def hasSpecialInLastPlace(str: String): Boolean = """.*[&\.-]$""".r.pattern.matcher(str).matches

  private def hasInsufficientAlphaCharsAtStart(str: String) = !"^[a-zA-Z]{3}.*".r.pattern.matcher(str).matches

  private def hasInsufficientConsecutiveAlphaChars(str: String) = !"^.*[a-zA-Z]{4}.*".r.pattern.matcher(str).matches

  private def hasTooManyConsecutiveSpecialChars(str: String) = """^.*[&\.-]{2}.*""".r.pattern.matcher(str).matches

  private def invalidPostcode(postcode: String): Boolean = {
    val noSpacesPostcode = postcode.filter {
      _ != ' '
    }
    val row2Expr = """^[A-P|R-U|WYZ]\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row3Expr = """^[A-P|R-U|WYZ]\d\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row4Expr = """^[A-P|R-U|WYZ][A-H|K-Y]\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row5Expr = """^[A-P|R-U|WYZ][A-H|K-Y]\d\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row6Expr = """^[A-P|R-U|WYZ][A-H|K-Y]\d[ABEHMNPR|V-Y]\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row7Expr = """^[A-P|R-U|WYZ]\d[A-H|HJK|S-U|W]\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row8Expr = "^[A-Z]1ZZ$".r
    val row9Expr = "^[A-Z][A-Z]1ZZ$".r
    val row10Expr = "^[A-Z][A-Z][A-Z]1ZZ$".r
    val row11Expr = "^[A-Z][A-Z][A-Z][A-Z]1ZZ$".r
    val row12Expr = """^BFPO\d$""".r
    val row13Expr = """^BFPO\d\d$""".r
    val row14Expr = """^BFPO\d\d\d$""".r
    val row15Expr = """^BFPO\d\d\d\d$""".r

    (noSpacesPostcode != "GIR0AA") &&
      (noSpacesPostcode match {
        case row2Expr() | row3Expr() | row4Expr() | row5Expr() | row6Expr() | row7Expr() | row8Expr() | row9Expr()
             | row10Expr() | row11Expr() | row12Expr() | row13Expr() | row14Expr() | row15Expr() => false
        case _ => true
      })

    //        val noSpacesPostcode = postcode.filter{_ != ' '}
    //        val postcodeRegex =     ("""^((GIR)(0AA)|([A-PR-UWYZ]([0-9]{1,2}|([A-HK-Y][0-9]|[A-HK-Y][0-9]([0-9]|[ABEHMNPRV-Y]))|""" +
    //          """[0-9][A-HJKS-UW]))([0-9][ABD-HJLNP-UW-Z]{2})|(([A-Z]{1,4})(1ZZ))|((BFPO)([0-9]{1,4})))$""").r
    //        !postcodeRegex.pattern.matcher(noSpacesPostcode).matches()
  }

  private def unparsableLocalDate(str: String) = {
    !"""^\d{8}$""".r.pattern.matcher(str).matches()
  }

  private def yearFromDate(date: String): Try[Int] = {
    if (unparsableLocalDate(date)) {
      Failure(new IllegalArgumentException())
    } else {
      Try(date.substring(0, 4).toInt)
    }
  }

  private def monthFromDate(date: String): Try[Int] = {
    if (unparsableLocalDate(date)) {
      Failure(new IllegalArgumentException())
    } else {
      Try(date.substring(4, 6).toInt)
    }
  }

  private def dayFromDate(date: String): Try[Int] = {
    if (unparsableLocalDate(date)) {
      Failure(new IllegalArgumentException())
    } else {
      Try(date.substring(6, 8).toInt)
    }
  }

  private def invalidDay(date: String): Boolean = {
    val dayNumber: Try[Int] = dayFromDate(date)
    val monthNumber = monthFromDate(date)
    val yearNumber = yearFromDate(date)

    if (dayNumber.isSuccess && monthNumber.isSuccess && yearNumber.isSuccess) {
      val day = dayNumber.getOrElse(0)
      val month = monthNumber.getOrElse(0)
      val year = yearNumber.getOrElse(0)
      val firstDayOfMonthDate = java.time.LocalDate.of(year, month, 1)
      val lastDayOfMonthDate = firstDayOfMonthDate.`with`(TemporalAdjusters.lastDayOfMonth())
      val lastDay = lastDayOfMonthDate
      day < 1 || day > lastDay.getDayOfMonth
    } else {
      true
    }
  }

  private def invalidCentury(date: String): Boolean = {
    val yearNumber = yearFromDate(date)

    if (yearNumber.isSuccess) {
      val year = yearNumber.getOrElse(0)
      year < 1800 || year > 2099
    } else {
      true
    }
  }

  //Helper method
  private def mt(code: String, mk0: String, mk1: String): (String, String, String) = (code, messagesApi(mk0), messagesApi(mk1))

  private def validateCreateAccount(json: JsValue): Either[(String, String, String), AccountCommand] = {
    //Convert incoming json to a case class
    val parseResult = Json.fromJson[AccountCommand]((json \ "createAccount").get)

    parseResult match {
      case JsSuccess(createAccount, _) =>
        if (createAccount.forename.startsWith(" ")) {
          Left(mt(LEADING_SPACES_ERROR_CODE, "site.leading-spaces-forename", "site.leading-spaces-forename-detail"))
        } else if (hasNumericChars(createAccount.forename)) {
          Left(mt(NUMERIC_CHARS_ERROR_CODE, "site.numeric-chars-forename", "site.numeric-chars-forename-detail"))
        } else if (hasDisallowedChars(createAccount.forename)) {
          Left(mt(DISALLOWED_CHARS_ERROR_CODE, "site.disallowed-chars-forename", "site.disallowed-chars-forename-detail"))
        } else if (hasSpecialInFirstPlace(createAccount.forename)) {
          Left(mt(FIRST_CHAR_SPECIAL_ERROR_CODE, "site.first-char-special-forename", "site.first-char-special-forename-detail"))
        } else if (hasSpecialInLastPlace(createAccount.forename)) {
          Left(mt(LAST_CHAR_SPECIAL_ERROR_CODE, "site.last-char-special-forename", "site.last-char-special-forename-detail"))
        } else if (hasInsufficientAlphaCharsAtStart(createAccount.forename)) {
          Left(mt(TOO_FEW_INITIAL_ALPHA_ERROR_CODE, "site.too-few-initial-alpha-forename", "site.too-few-initial-alpha-forename-detail"))
        } else if (hasInsufficientConsecutiveAlphaChars(createAccount.forename)) {
          Left(mt(TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE, "site.too-few-consecutive-alpha-forename", "site.too-few-consecutive-alpha-forename-detail"))
        } else if (hasTooManyConsecutiveSpecialChars(createAccount.forename)) {
          Left(mt(TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE, "site.too-many-consecutive-special-forename", "site.too-many-consecutive-special-forename-detail"))
        } else if (createAccount.surname.startsWith(" ")) {
          Left(mt(LEADING_SPACES_ERROR_CODE, "site.leading-spaces-surname", "site.leading-spaces-surname-detail"))
        } else if (invalidPostcode(createAccount.postcode)) {
          Left(mt(INVALID_POSTCODE_ERROR_CODE, "site.invalid-postcode", "site.invalid-postcode-detail"))
        } else if (hasNumericChars(createAccount.surname)) {
          Left(mt(NUMERIC_CHARS_ERROR_CODE, "site.numeric-chars-surname", "site.numeric-chars-surname-detail"))
        } else if (hasDisallowedChars(createAccount.surname)) {
          Left(mt(DISALLOWED_CHARS_ERROR_CODE, "site.disallowed-chars-surname", "site.disallowed-chars-surname-detail"))
        } else if (hasSpecialInFirstPlace(createAccount.surname)) {
          Left(mt(FIRST_CHAR_SPECIAL_ERROR_CODE, "site.first-char-special-surname", "site.first-char-special-surname-detail"))
        } else if (hasSpecialInLastPlace(createAccount.surname)) {
          Left(mt(LAST_CHAR_SPECIAL_ERROR_CODE, "site.last-char-special-surname", "site.last-char-special-surname-detail"))
        } else if (hasInsufficientAlphaCharsAtStart(createAccount.surname)) {
          Left(mt(TOO_FEW_INITIAL_ALPHA_ERROR_CODE, "site.too-few-initial-alpha-surname", "site.too-few-initial-alpha-surname-detail"))
        } else if (hasInsufficientConsecutiveAlphaChars(createAccount.surname)) {
          Left(mt(TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE, "site.too-few-consecutive-alpha-surname", "site.too-few-consecutive-alpha-surname-detail"))
        } else if (hasTooManyConsecutiveSpecialChars(createAccount.surname)) {
          Left(mt(TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE, "site.too-many-consecutive-special-surname", "site.too-many-consecutive-special-surname-detail"))
        } else if (unparsableLocalDate(createAccount.birthDate)) {
          Left(mt(UNPARSABLE_DATE_ERROR_CODE, "site.unparsable-date", "site.unparsable-date-detail"))
        } else if (monthFromDate(createAccount.birthDate).getOrElse(13) > 12) {
          Left(mt(BAD_MONTH_DATE_ERROR_CODE, "site.bad-month-date", "site.bad-month-date-detail"))
        } else if (invalidDay(createAccount.birthDate)) {
          Left(mt(BAD_DAY_DATE_ERROR_CODE, "site.bad-day-date", "site.bad-day-date-detail"))
        } else if (invalidCentury(createAccount.birthDate)) {
          Left(mt(BAD_CENTURY_DATE_ERROR_CODE, "site.bad-century-date", "site.bad-century-date-detail"))
        } else if (!countryCodes.contains(createAccount.countryCode.getOrElse(""))) {
          Left(mt(UNKNOWN_COUNTRY_CODE_ERROR_CODE, "site.unknown-country-code", "site.unknown-country-code-detail"))
        } else {
          Right(createAccount)
        }

      case JsError(errors) => Left((UNABLE_TO_PARSE_COMMAND_ERROR_CODE, messagesApi("site.unparsable-command"), errors.toString()))
    }
  }

  def processBody(request: Request[AnyContent]): Result = {
    request.body.asJson match {
      case None => BadRequest(Json.toJson(errorJson(NO_JSON_ERROR_CODE, "site.no-json", "site.no-json-detail")))

      case Some(json: JsValue) => {
        if (json.asInstanceOf[JsObject].fields.size != 1 || (json.asInstanceOf[JsObject].fields.head._1 != "createAccount")) {
          BadRequest(errorJson(NO_CREATEACCOUNTKEY_ERROR_CODE, "site.no-create-account-key", "site.no-create-account-key-detail"))
        } else {
          val nino = (json \ "createAccount" \ "NINO").get.asOpt[String]
          nino match {
            case Some(aNino) if aNino.startsWith("ER400") => BadRequest(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER401") => Unauthorized(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER403") => Forbidden(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER404") => NotFound(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER405") => MethodNotAllowed(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER415") => UnsupportedMediaType(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER500") => InternalServerError(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER503") => ServiceUnavailable(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(n) => {
              validateCreateAccount(json) match {
                case Right(_) => Ok
                case Left(errorTriple) => BadRequest(errorJsonWithTriple(errorTriple))
              }
            }
            case None => Ok
          }
        }
      }
    }
  }

  def createAccount(): Action[AnyContent] = Action { request =>
    val mimeType = request.headers.toSimpleMap.get(CONTENT_TYPE)

    mimeType match {
      case Some("application/json") => processBody(request)
      case _ => UnsupportedMediaType
    }
  }
}
