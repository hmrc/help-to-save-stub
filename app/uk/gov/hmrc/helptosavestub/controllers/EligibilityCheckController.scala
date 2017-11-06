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

import play.api.libs.json.{Format, Json}
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckController.EligibilityCheckResult
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.util.Try

class EligibilityCheckController extends BaseController {

  val resultMappings: Map[Int, String] = Map(
    1 → "Eligible to HtS Account",
    2 → "Ineligible to HtS Account",
    3 → "HtS account already exists",
    99 → "INVALID RESULT WHICH DES SHOULD NEVER SEND"
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

  val alreadyHasAccountResult: EligibilityCheckResult =
    EligibilityCheckResult("HtS account already exists", 3, "HtS account was previously created", 1)

  val invalidResultCode: EligibilityCheckResult =
    EligibilityCheckResult("INVALID RESULT WHICH DES SHOULD NEVER SEND", 99, "Not entitled to WTC and not in receipt of UC", 2)

  def eligibleResult(reasonCode: Int): EligibilityCheckResult = {
    val reason = reasonMappings.getOrElse(reasonCode, sys.error(s"Could not find eligibility reason for code $reasonCode"))
    EligibilityCheckResult("Eligible to HtS Account", 1, reason, reasonCode)
  }

  def ineligibleResult(reasonCode: Int): EligibilityCheckResult = {
    val reason = reasonMappings.getOrElse(reasonCode, sys.error(s"Could not find eligibility reason for code $reasonCode"))
    EligibilityCheckResult("Ineligible to HtS Account", 2, reason, reasonCode)
  }

  def getReasonCodeFromNino(nino: String): Int =
    Try(nino.substring(3, 4).toInt)
      .getOrElse(sys.error(s"Error getting reason code from fourth character of NINO $nino"))

  def eligibilityCheck(nino: String): Action[AnyContent] = Action { implicit request ⇒
    val response: Option[EligibilityCheckResult] =
      // Comments are for the Test & Release Services (T&RS) team
      // Private BETA:
      // Start NINO with EL07 to specify an eligible applicant in receipt of WTC (with reason code 7)
      //
      // After private BETA:
      // Start NINO with EL06 to specify an eligible applicant in receipt of UC (with reason code 6)
      // Start NINO with EL08 to specify an eligible applicant in receipt of WTC and UC (with reason code 8)
      if (nino.toUpperCase().startsWith("EL")) {
        Some(eligibleResult(getReasonCodeFromNino(nino)))
      } // Private BETA:
      // Start NINO with NE02 to specify an ineligible applicant in receipt of WTC (with reason code 2)
      // Start NINO with NE03 to specify an ineligible applicant in receipt of WTC (with reason code 3)
      //
      // After private BETA:
      // Start NINO with NE06 to specify an ineligible applicant in receipt of UC (with reason code 6)
      // Start NINO with NE08 to specify an ineligible applicant in receipt of WTC and UC (with reason code 8)
      else if (nino.toUpperCase().startsWith("NE")) {
        Some(ineligibleResult(getReasonCodeFromNino(nino)))
      } // Start NINO with AC to specify an existing account holder (with reason code 1)
      else if (nino.startsWith("AC")) {
        Some(alreadyHasAccountResult)
      } // Start NINO with EE to specify an invalid result code
      else if (nino.startsWith("EE")) {
        Some(invalidResultCode)
      } // Start NINO with anything else to specify an eligible applicant
      else {
        Some(eligibleResult(7))
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
