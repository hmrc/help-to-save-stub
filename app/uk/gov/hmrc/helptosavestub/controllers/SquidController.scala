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

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsError, _}
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.Constants._
import uk.gov.hmrc.helptosavestub.models.SquidModels.AccountCommand
import uk.gov.hmrc.play.microservice.controller.BaseController

@Singleton
class SquidController @Inject()(val messagesApi: MessagesApi) extends BaseController {

  import SquidController._

  private val logger = Logger("SquidController")

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

  private val ninoRegex = """^(([A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TW-Z])([0-9]{2})([0-9]{2})([0-9]{2})([A-D]{1})|((XX)(99)(99)(99)(X)))$""".r

  private def errorJson(error: Error): JsValue = {
    val errorMap =
      Map("error" ->
        Map("errorMessageId" -> error.code,
          "errorMessage" -> messagesApi(error.messageKey),
          "errorDetail" -> messagesApi(error.messageDetail)))

    Json.toJson(errorMap)
  }

  private def hasNumericChars(str: String): Boolean = str.exists(_.isDigit)

  private def hasDisallowedChars(str: String): Boolean = !"""^[a-zA-Z&\.-]*$""".r.pattern.matcher(str).matches

  private def hasSpecialInFirstPlace(str: String): Boolean = """^[&\.-].*""".r.pattern.matcher(str).matches

  private def hasSpecialInLastPlace(str: String): Boolean = """.*[&\.-]$""".r.pattern.matcher(str).matches

  private def hasInsufficientConsecutiveAlphaChars(str: String) = !"^.*[a-zA-Z]{4}.*".r.pattern.matcher(str).matches

  private def hasTooManyConsecutiveSpecialChars(str: String) = """^.*[&\.-]{2}.*""".r.pattern.matcher(str).matches

  private def invalidPostcode(postcode: String): Boolean = {
   !"""[a-zA-Z0-9\s]{3,10}$""".r.pattern.matcher(postcode).matches()
  }

  private def unparsableLocalDate(str: String) = {
    !"""^\d{8}$""".r.pattern.matcher(str).matches()
  }

  private def yearFromDate(date: String): Option[Int] = {
    if (unparsableLocalDate(date)) {
      None
    } else {
      Some(date.substring(0, 4).toInt)
    }
  }

  private def before1800(date: String): Boolean = yearFromDate(date).fold(false) {_ < 1800}

  private def futureDate(date: String): Boolean = {
    val d = java.time.LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)
    val today = java.time.LocalDate.now()
    d.isAfter(today)
  }

  private def monthFromDate(date: String): Option[Int] = {
    if (unparsableLocalDate(date)) {
      None
    } else {
      Some(date.substring(4, 6).toInt)
    }
  }

  private def dayFromDate(date: String): Option[Int] = {
    if (unparsableLocalDate(date)) {
      None
    } else {
      Some(date.substring(6, 8).toInt)
    }
  }

  private def invalidDay(date: String): Boolean = {
    val dayNumber = dayFromDate(date)
    val monthNumber = monthFromDate(date)
    val yearNumber = yearFromDate(date)

    if (dayNumber.isDefined && monthNumber.isDefined && yearNumber.isDefined) {
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
    yearNumber.fold(false) { year => year < 1800 || year > 2099 }
  }

  private def invalidNino(nino: String): Boolean = {
    !ninoRegex.pattern.matcher(nino).matches()
  }

  private def invalidCommunicationPreference(pref: String): Boolean = {
    pref != "00" && pref != "02"
  }

  private def emailRequired(pref: String, emailAddress: Option[String]) = {
    pref == "02" && emailAddress.isEmpty
  }

  private def validateCreateAccount(createAccount: AccountCommand): Either[Error, AccountCommand] = {
    logger.info(createAccount.toString)
    createAccount match {
      case ca if ca.forename.isEmpty => Left(Error(FORENAME_TOO_FEW_CHARS_ERROR_CODE, "site.too-few-chars-forename", "site.too-few-chars-forename-detail"))
      case ca if ca.forename.length > 26 => Left(Error(FORENAME_TOO_MANY_CHARS_ERROR_CODE, "site.too-many-chars-forename", "site.too-many-chars-forename-detail"))
      case ca if ca.forename.startsWith(" ") => Left(Error(LEADING_SPACES_ERROR_CODE, "site.leading-spaces-forename", "site.leading-spaces-forename-detail"))
      case ca if hasNumericChars(ca.forename) => Left(Error(NUMERIC_CHARS_ERROR_CODE, "site.numeric-chars-forename", "site.numeric-chars-forename-detail"))
      case ca if hasDisallowedChars(ca.forename) => Left(Error(DISALLOWED_CHARS_ERROR_CODE, "site.disallowed-chars-forename", "site.disallowed-chars-forename-detail"))
      case ca if hasTooManyConsecutiveSpecialChars(ca.forename) => Left(Error(TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE, "site.too-many-consecutive-special-forename", "site.too-many-consecutive-special-forename-detail"))
      case ca if ca.surname.startsWith(" ") => Left(Error(LEADING_SPACES_ERROR_CODE, "site.leading-spaces-surname", "site.leading-spaces-surname-detail"))
      case ca if ca.surname.isEmpty => Left(Error(SURNAME_TOO_FEW_CHARS_ERROR_CODE, "site.too-few-chars-surname", "site.too-few-chars-surname-detail"))
      case ca if ca.surname.length > 300 => Left(Error(SURNAME_TOO_MANY_CHARS_ERROR_CODE, "site.too-many-chars-surname", "site.too-many-chars-surname-detail"))
      case ca if hasNumericChars(ca.surname) => Left(Error(NUMERIC_CHARS_ERROR_CODE, "site.numeric-chars-surname", "site.numeric-chars-surname-detail"))
      case ca if hasDisallowedChars(ca.surname) => Left(Error(DISALLOWED_CHARS_ERROR_CODE, "site.disallowed-chars-surname", "site.disallowed-chars-surname-detail"))
      case ca if hasSpecialInFirstPlace(ca.surname) => Left(Error(FIRST_CHAR_SPECIAL_ERROR_CODE, "site.first-char-special-surname", "site.first-char-special-surname-detail"))
      case ca if hasSpecialInLastPlace(ca.surname) => Left(Error(LAST_CHAR_SPECIAL_ERROR_CODE, "site.last-char-special-surname", "site.last-char-special-surname-detail"))
      case ca if hasTooManyConsecutiveSpecialChars(ca.surname) => Left(Error(TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE, "site.too-many-consecutive-special-surname", "site.too-many-consecutive-special-surname-detail"))
      case ca if invalidPostcode(ca.contactDetails.postcode) => Left(Error(INVALID_POSTCODE_ERROR_CODE, "site.invalid-postcode", "site.invalid-postcode-detail"))
      case ca if unparsableLocalDate(ca.dateOfBirth) => Left(Error(UNPARSABLE_DATE_ERROR_CODE, "site.unparsable-date", "site.unparsable-date-detail"))
      case ca if before1800(ca.dateOfBirth) => Left(Error(BAD_DATE_TOO_EARLY_ERROR_CODE, "site.bad-date-too-early", "site.bad-date-too-early-detail"))
      case ca if futureDate(ca.dateOfBirth) => Left(Error(BAD_DATE_TOO_LATE_ERROR_CODE, "site.bad-date-too-late", "site.bad-date-too-late-detail"))
      case ca if !ca.contactDetails.countryCode.exists(countryCodes.contains) => Left(Error(UNKNOWN_COUNTRY_CODE_ERROR_CODE, "site.unknown-country-code", "site.unknown-country-code-detail"))
      case ca if invalidNino(ca.nino) => Left(Error(BAD_NINO_ERROR_CODE, "site.bad-nino", "site.bad-nino-detail"))
      case ca if invalidCommunicationPreference(ca.contactDetails.communicationPreference) => Left(Error(BAD_COMM_PREF_ERROR_CODE, "site.bad-comm-pref", "site.bad-comm-pref-detail"))
      case ca if emailRequired(ca.contactDetails.communicationPreference, ca.contactDetails.email) => Left(Error(EMAIL_NEEDED_ERROR_CODE, "site.email-needed", "site.email-needed-detail"))
      case ca if ca.contactDetails.address1.isEmpty => Left(Error(ADDRESS_ONE_TOO_SHORT_ERROR_CODE, "site.address1-empty", "site.address1-empty-detail"))
      case ca if ca.contactDetails.address1.length > 35 => Left(Error(ADDRESS_ONE_TOO_LONG_ERROR_CODE, "site.address1-too-long", "site.address1-too-long-detail"))
      case ca if ca.contactDetails.address2.isEmpty => Left(Error(ADDRESS_TWO_TOO_SHORT_ERROR_CODE, "site.address2-empty", "site.address2-empty-detail"))
      case ca if ca.contactDetails.address2.length > 35 => Left(Error(ADDRESS_TWO_TOO_LONG_ERROR_CODE, "site.address2-too-long", "site.address2-too-long-detail"))
      case ca if ca.contactDetails.address3.getOrElse("").length > 35 => Left(Error(ADDRESS_THREE_TOO_LONG_ERROR_CODE, "site.address3-too-long", "site.address3-too-long-detail"))
      case ca if ca.contactDetails.address4.getOrElse("").length > 35 => Left(Error(ADDRESS_FOUR_TOO_LONG_ERROR_CODE, "site.address4-too-long", "site.address4-too-long-detail"))
      case ca if ca.contactDetails.address5.getOrElse("").length > 35 => Left(Error(ADDRESS_FIVE_TOO_LONG_ERROR_CODE, "site.address5-too-long", "site.address5-too-long-detail"))
      case ca if ca.contactDetails.phoneNumber.getOrElse("").length > 15 => Left(Error(PHONE_NUMBER_TOO_LONG_ERROR_CODE, "site.phone-number-too-long", "site.phone-number-too-long-detail"))
      case _ => Right(createAccount)
    }
  }

  def processBody(request: Request[AnyContent]): Result = {
    request.body.asJson match {
      case None => BadRequest(Json.toJson(errorJson(Error(NO_JSON_ERROR_CODE, "site.no-json", "site.no-json-detail"))))

      case Some(json: JsValue) =>
        val createAccount = Json.fromJson[AccountCommand](json)
        createAccount match {
          case JsError(errors) => BadRequest(errorJson(Error(UNABLE_TO_PARSE_COMMAND_ERROR_CODE, "site.no-json", "site.no-json-detail")))

          case JsSuccess(wrappedCreateAccount, _) =>
            val nino = createAccount.get.nino
            nino match {
              case aNino if aNino.startsWith("ER400") => BadRequest(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case aNino if aNino.startsWith("ER401") => Unauthorized(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case aNino if aNino.startsWith("ER403") => Forbidden(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case aNino if aNino.startsWith("ER404") => NotFound(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case aNino if aNino.startsWith("ER405") => MethodNotAllowed(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case aNino if aNino.startsWith("ER415") => UnsupportedMediaType(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case aNino if aNino.startsWith("ER500") => InternalServerError(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case aNino if aNino.startsWith("ER503") => ServiceUnavailable(errorJson(Error(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail")))
              case _ =>
                validateCreateAccount(createAccount.get) match {
                  case Right(_) => Created
                  case Left(error) =>
                    val errJson = errorJson(error)
                    logger.error(errJson.toString())
                    BadRequest(errJson)

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

object SquidController {

  private case class Error(code: String, messageKey: String, messageDetail: String)

}