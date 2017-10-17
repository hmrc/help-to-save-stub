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
import uk.gov.hmrc.play.microservice.controller.BaseController
import hmrc.smartstub._
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckController.EligibilityCheckResult

class EligibilityCheckController extends BaseController {

  val resultMappings: Map[Int, String] = Map(
    1 → "Eligible to HtS Account",
    2 → "Ineligible to HtS Account",
    3 → "HtS account already exists"
  )

  val reasonMappings: Map[Int, String] = Map(
    1 → "HtS account was previously created",
    2 → "Not entitled to WTC and not in receipt of UC",
    3 → "Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and not in receipt of UC",
    4 → "Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and in receipt of UC but income is insufficient",
    5 → "Not entitled to WTC and in receipt of UC but income is insufficient",
    6 → "In receipt of UC and income sufficient",
    7 → "Entitled to WTC and in receipt of positive WTC/CTC Tax Credit",
    8 → "Entitled to WTC and in receipt of positive WTC/CTC Tax Credit and in receipt of UC and income sufficient"
  )

  val eligibleReasonGen: Gen[EligibilityCheckResult] =
    Gen.choose(6, 8) // scalastyle:ignore magic.number
      .map{ i ⇒
        val reason = reasonMappings.getOrElse(i, sys.error(s"Could not find eligibility reason for code $i"))
        EligibilityCheckResult("Eligible to HtS Account", 1, reason, i)
      }

  val ineligibleReasonGen: Gen[EligibilityCheckResult] =
    Gen.choose(2, 5) // scalastyle:ignore magic.number
      .map{ i ⇒
        val reason = reasonMappings.getOrElse(i, sys.error(s"Could not find ineligibility reason for code $i"))
        EligibilityCheckResult("Ineligible to HtS Account", 2, reason, i)
      }

  val alreadyHasAccountResult: EligibilityCheckResult =
    EligibilityCheckResult("HtS account already exists", 3, "HtS account was previously created", 1)

  def eligibilityCheck(nino: String): Action[AnyContent] = Action { implicit request ⇒
    val response: Option[EligibilityCheckResult] =
      if (nino.startsWith("AC")) {
        // if nino start with AC return someone who has already opened an account in the past
        Some(alreadyHasAccountResult)
      } else if (nino.toUpperCase().startsWith("NA")) {
        ineligibleReasonGen.seeded(nino)
      } else {
        eligibleReasonGen.seeded(nino)
      }

    response.fold[Result](InternalServerError)(r ⇒ Ok(Json.toJson(r)))
  }
}

object EligibilityCheckController {

  /**
   * Response from ITMP eligibility check
   *
   * @param result 1 = Eligible to HtS Account
   *               2 = Ineligible to HtS Account
   *               3 = HtS account already exists
   * @param reason 1 = HtS account was previously created
   *               2 = Not entitled to WTC and not in receipt of UC
   *               3 = Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and not in receipt of UC
   *               4 = Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and in receipt of UC but income is insufficient
   *               5 = Not entitled to WTC and in receipt of UC but income is insufficient
   *               6 = In receipt of UC and income sufficient
   *               7 = Entitled to WTC and in receipt of positive WTC/CTC Tax Credit
   *               8 = Entitled to WTC and in receipt of positive WTC/CTC Tax Credit and in receipt of UC and income sufficient
   *               N.B. 1-5 represent reasons for ineligibility and 6-8 repesents reasons for eligibility
   */
  case class EligibilityCheckResult(result: String, resultCode: Int, reason: String, reasonCode: Int)

  object EligibilityCheckResult {

    implicit val format: Format[EligibilityCheckResult] = Json.format[EligibilityCheckResult]

  }
}
