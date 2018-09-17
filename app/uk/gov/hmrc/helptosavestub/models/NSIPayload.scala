/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.helptosavestub.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.libs.json._
import uk.gov.hmrc.helptosavestub.models.NSIPayload.ContactDetails

import scala.util.{Failure, Success, Try}

case class NSIPayload(forename:            String,
                      surname:             String,
                      dateOfBirth:         LocalDate,
                      nino:                String,
                      contactDetails:      ContactDetails,
                      registrationChannel: String              = "online",
                      nbaDetails:          Option[BankDetails] = None,
                      version:             Option[String],
                      systemId:            Option[String])

object NSIPayload {

  case class ContactDetails(address1:                String,
                            address2:                String,
                            address3:                Option[String],
                            address4:                Option[String],
                            address5:                Option[String],
                            postcode:                String,
                            countryCode:             Option[String],
                            email:                   Option[String],
                            phoneNumber:             Option[String] = None,
                            communicationPreference: String         = "02")

  implicit val dateFormat: Format[LocalDate] = new Format[LocalDate] {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    override def writes(o: LocalDate): JsValue = JsString(o.format(formatter))

    override def reads(json: JsValue): JsResult[LocalDate] = json match {
      case JsString(s) ⇒
        Try(LocalDate.parse(s, formatter)) match {
          case Success(date)  ⇒ JsSuccess(date)
          case Failure(error) ⇒ JsError(s"Could not parse date as yyyyMMdd: ${error.getMessage}")
        }

      case other ⇒ JsError(s"Expected string but got $other")
    }
  }

  implicit val contactDetailsFormat: Format[ContactDetails] = Json.format[ContactDetails]

  implicit val nsiPayloadFormat: Format[NSIPayload] = Json.format[NSIPayload]

}

