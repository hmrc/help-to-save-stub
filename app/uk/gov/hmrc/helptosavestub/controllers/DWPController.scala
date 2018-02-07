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

import org.scalacheck.Gen
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.helptosavestub.controllers.DWPController.UCDetails
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController

class DWPController extends BaseController with Logging {

  val wp01Json: UCDetails = UCDetails("Y", Some("Y"))
  val wp02Json: UCDetails = UCDetails("Y", Some("N"))
  val wp03Json: UCDetails = UCDetails("N", None)

  val ucGen: Gen[UCDetails] = {
    val booleanGen = Gen.oneOf("Y", "N")
    for {
      c ← booleanGen
      w ← Gen.option(booleanGen)
    } yield UCDetails(c, w)
  }

  def randomUCDetails(): UCDetails = ucGen.sample.getOrElse(sys.error(""))

  private def getHttpStatus(nino: String): Result = {
    val result = nino.substring(2, 5).toInt // scalastyle:ignore magic.number
    Status(result)
  }

  def dwpClaimantCheck(nino: String): Action[AnyContent] = Action {
    implicit request ⇒
      {
        if (nino.startsWith("WP01")) { Ok(Json.toJson(wp01Json)) }
        else if (nino.startsWith("WP02")) { Ok(Json.toJson(wp02Json)) }
        else if (nino.startsWith("WP03")) { Ok(Json.toJson(wp03Json)) }
        else if (nino.startsWith("WS")) { getHttpStatus(nino) }
        else { Ok(Json.toJson(randomUCDetails())) }
      }
  }

}

object DWPController {

  case class UCDetails(ucClaimant: String, withinThreshold: Option[String])

  object UCDetails {
    implicit val format: Format[UCDetails] = Json.format[UCDetails]
  }
}
