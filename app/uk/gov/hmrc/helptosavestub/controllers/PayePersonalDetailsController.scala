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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.util.Try

class PayePersonalDetailsController extends BaseController with DESController with Logging {

  def getPayeDetails(nino: String): Action[AnyContent] = desAuthorisedAction { implicit request ⇒
    val status: Option[Int] = nino match {
      case ninoStatusRegex(s) ⇒ Try(s.toInt).toOption
      case _                  ⇒ None
    }

    status match {
      case Some(s) ⇒ Status(s)(errorJson(s))
      case None ⇒
        logger.info(s"returning paye-personal-details from nino: $nino")
        Ok(Json.parse(payeDetails(nino)))
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

  def payeDetails(nino: String): String =
    s"""{
      |  "nino": "${nino.dropRight(1)}",
      |  "ninoSuffix": "${nino.takeRight(1)}",
      |  "names": {
      |    "1": {
      |      "sequenceNumber": 12345,
      |      "firstForenameOrInitial": "A",
      |      "surname": "Smith",
      |      "startDate": "2000-01-01"
      |    }
      |  },
      |  "sex": "M",
      |  "dateOfBirth": "1980-01-01",
      |  "deceased": false,
      |  "addresses": {
      |    "1": {
      |      "line1": "1 Station Road",
      |      "line2": "Town Centre",
      |      "line3": "Sometown",
      |      "line4": "Anyshire",
      |      "postcode": "AB12 3CD",
      |      "line5": "UK",
      |      "sequenceNumber": 1,
      |      "startDate": "2000-01-01"
      |    }
      |  },
      |  "phoneNumbers": {
      |    "1": {
      |      "telephoneNumber": "01999123456",
      |      "telephoneType": 1
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
