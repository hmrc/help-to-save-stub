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

package uk.gov.hmrc.helptosavestub.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.libs.json.{JsString, JsValue, Json, Writes, _}

import scala.util.{Failure, Success, Try}

case class CreateAccount(forename: String,
                         surname: String,
                         birthDate: LocalDate,
                         address1: String,
                         address2: String,
                         address3: Option[String],
                         address4: Option[String],
                         address5: Option[String],
                         postcode: String,
                         countryCode: Option[String],
                         NINO: String,
                         communicationPreference: String,
                         phoneNumber: Option[String],
                         registrationChannel: String,
                         emailAddress: Option[String]
                        )

object CreateAccount {

  val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  implicit val dateReads: Reads[LocalDate] = new Reads[LocalDate] {
    override def reads(o: JsValue): JsResult[LocalDate] = o match {
      case JsString(s) ⇒
        Try(LocalDate.parse(s, formatter)) match {
          case Success(d) ⇒ JsSuccess(d)
          case Failure(e) ⇒ JsError(e.getMessage)
        }
      case other ⇒
        JsError(s"Expected string but got $other")

    }
  }

  implicit val dateWrites: Writes[LocalDate] = new Writes[LocalDate] {
    override def writes(o: LocalDate): JsValue = JsString(o.format(DateTimeFormatter.ofPattern("YYYYMMdd")))
  }

  implicit val createAccountFormat: Format[CreateAccount] = Json.format[CreateAccount]
}

