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

package uk.gov.hmrc.helptosavestub.controllers

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, ZoneId}

import org.scalacheck.Gen
import org.scalacheck.Gen.{listOfN, numChar}
import uk.gov.hmrc.smartstub._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.controllers.PayePersonalDetailsController._

import scala.util.Try

class PayePersonalDetailsController extends BaseController with DESController with Logging {

  def getPayeDetails(nino: String): Action[AnyContent] = desAuthorisedAction { implicit request ⇒
    val status: Option[Int] = nino match {
      case ninoStatusRegex(s) ⇒ Try(s.toInt).toOption
      case _                  ⇒ None
    }

    status match {
      case Some(s) ⇒
        Status(s)(errorJson(s))

      case None ⇒
        logger.info(s"returning paye-personal-details from nino: $nino")
        payeDetails(nino).seeded(nino).fold[Result]{
          logger.warn("Could not generate PayeDetails")
          InternalServerError
        }{ s ⇒
          println(s"\n\n$s\n\n")
          Ok(Json.parse(s))
        }
    }
  }

  private def errorJson(status: Int): JsValue = Json.parse(
    s"""
       |{
       |  "code":   "${io.netty.handler.codec.http.HttpResponseStatus.valueOf(status).reasonPhrase()}",
       |  "reason": "intentional error"
       |}
       """.stripMargin)

  private val ninoStatusRegex = """PD(\d{3}).*""".r

  private[controllers] def payeDetails(nino: String) = for {
    sex ← Gen.gender
    name ← Gen.forename(sex)
    surname ← Gen.surname
    date ← Gen.choose(100L, 1000L).map(LocalDate.ofEpochDay)
    address ← Gen.ukAddress
    postcode ← Gen.postcode
    countryCode ← Gen.choose(1, 250)
    callingCode ← Gen.choose(1, 250)
    telephoneType ← Gen.oneOf(1, 2, 7)
    dialingCode ← listOfN(5, numChar).map{ _.mkString }
    convertedAreaDiallingCode ← listOfN(3, numChar).map{ _.mkString }
    phoneNumber ← listOfN(6, numChar).map{ _.mkString }
  } yield s"""{
      |  "nino": "${nino.dropRight(1)}",
      |  "ninoSuffix": "${nino.takeRight(1)}",
      |  "names": {
      |    "1": {
      |      "sequenceNumber": 12345,
      |      "firstForenameOrInitial": "$name",
      |      "surname": "$surname",
      |      "startDate": "2000-01-01"
      |    }
      |  },
      |  "sex": "${sex.fold("F", "M")}",
      |  "dateOfBirth": "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
      |  "deceased": false,
      |  "addresses": {
      |    "1": {
      |      "line1": "${address.headOption.getOrElse("1 the road")}",
      |      "line2": "${address.drop(1).headOption.getOrElse("The Place")}",
      |      "line3": "Sometown",
      |      "line4": "Anyshire",
      |      "line5": "Line 5",
      |      "countryCode": $countryCode,
      |      "postcode": "$postcode",
      |      "sequenceNumber": 1,
      |      "startDate": "2000-01-01"
      |    }
      |  },
      |  "phoneNumbers": {
      |    "$telephoneType": {
      |      "callingCode": $callingCode,
      |      "telephoneType": $telephoneType,
      |      "areaDiallingCode": "$dialingCode",
      |      "convertedAreaDiallingCode": "$convertedAreaDiallingCode",
      |      "telephoneNumber": "$phoneNumber"
      |    }
      |  },
      |  "accountStatus": 0,
      |  "manualCorrespondenceInd": false,
      |  "dateOfEntry": "2000-01-01",
      |  "dateOfRegistration": "2000-01-01",
      |  "registrationType": 0,
      |  "hasSelfAssessmentAccount": false,
      |  "audioOutputRequired": false,
      |  "brailleOutputRequired": false,
      |  "largePrintOutputRequired": false,
      |  "welshOutputRequired": false
      |}""".stripMargin
}

object PayePersonalDetailsController {

  implicit class GenderOps(val gender: Gender) extends AnyVal {
    def fold[A](female: ⇒ A, male: ⇒ A): A = gender match {
      case Female ⇒ female
      case Male   ⇒ male
    }
  }

}
