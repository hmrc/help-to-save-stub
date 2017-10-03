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

import org.scalacheck.Gen
import play.api.libs.json.{Format, Json}
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckController.EligibilityCheckResult
import uk.gov.hmrc.play.microservice.controller.BaseController
import hmrc.smartstub._
import uk.gov.hmrc.helptosavestub.util.Logging

class EligibilityCheckController extends BaseController with Logging {

  val eligibleString: String = "Eligible to HtS Account"

  val ineligibleString: String = "Ineligible to HtS Account"

  val alreadyOpenedAccountString: String = "An HtS account was opened previously (the HtS account may have been closed or inactive)"

  val eligibleReasonGen: Gen[String] = Gen.oneOf(
    "In receipt of UC and income sufficient",
    "Entitled to WTC and in receipt of positive WTC/CTC Tax Credit",
    "Entitled to WTC and in receipt of positive WTC/CTC Tax Credit and in receipt of UC and income sufficient"
  )

  val ineligibleReasonGen: Gen[String] = Gen.oneOf(
    alreadyOpenedAccountString,
    "Not entitled to WTC and in receipt of UC but income is insufficient",
    "Not entitled to WTC and not in receipt of UC",
    "Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and in receipt of UC but income is insufficient",
    "Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and not in receipt of UC"
  )

  def eligibilityCheck(nino: String): Action[AnyContent] = Action { implicit request ⇒
    logger.info(s"Received request to get eligibility for nino: $nino")
    val (result, reason): (String, Option[String]) =
      if (nino.startsWith("AC")) {
        // if nino start with AC return someone who has already opened an account in the past
        ineligibleString → Some(alreadyOpenedAccountString)
      } else if (!nino.toUpperCase().startsWith("NA")) {
        eligibleString → eligibleReasonGen.seeded(nino)
      } else {
        ineligibleString → ineligibleReasonGen.seeded(nino)
      }

    reason.fold[Result]{
      logger.warn("Could not generate reason: returning with status 500")
      InternalServerError
    }{ r ⇒
      val response = EligibilityCheckResult(result, r)
      logger.info(s"Returning response: $response ")
      Ok(Json.toJson(response))
    }
  }
}

object EligibilityCheckController {

  /**
   * Response from ITMP eligibility check
   */
  case class EligibilityCheckResult(result: String, reason: String)

  object EligibilityCheckResult {

    implicit val format: Format[EligibilityCheckResult] = Json.format[EligibilityCheckResult]

  }
}
