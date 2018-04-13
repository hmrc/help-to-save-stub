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

import cats.instances.string._
import cats.syntax.eq._
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.util.Try

class EligibilityCheckController extends BaseController with DESController with Logging with DWPEligibilityBehaviour {

  def eligibilityCheck(nino: String, ucClaimant: Option[Boolean] = None, withinThreshold: Option[Boolean] = None): Action[AnyContent] =
    desAuthorisedAction { implicit request ⇒
      logger.info(s"Received eligibility check request for nino: $nino. UC parameters in the request are: " +
        s"ucClaimant: ${ucClaimant.map(_.toString).getOrElse("-")}, " +
        s"withinThresold: ${withinThreshold.map(_.toString).getOrElse("-")}")

      val status: Option[Int] = nino match {
        case ninoStatusRegex(s) ⇒ Try(s.toInt).toOption
        case s if s.startsWith("WP1144") || s === "AA123123A" ⇒ Some(404)
        case _ ⇒ None
      }

      status match {
        case Some(s) ⇒
          Status(s)(errorJson(s))

        case None ⇒
          getResponse(nino)
      }
    }

  private def getResponse(nino: String): Result = {
    val upperCaseNINO = nino.toUpperCase()
    // Allow early system integration testing with DWP, with DES stubbed
    if (upperCaseNINO.startsWith("LW634114")) {
      Ok(ineligibleResult(9).toJson())
    } else if (upperCaseNINO.startsWith("ZX368514")) {
      Ok(ineligibleResult(3).toJson())
    } else if (upperCaseNINO.startsWith("BJ825714")) {
      Ok(eligibleResult(7).toJson())
    } else if (upperCaseNINO.startsWith("HG737615")) {
      Ok(ineligibleResult(5).toJson())
    } else if (upperCaseNINO.startsWith("EK978215")) {
      Ok(ineligibleResult(4).toJson())
    } else if (upperCaseNINO.startsWith("KS384413")) {
      Ok(eligibleResult(7).toJson())
    } else if (upperCaseNINO.startsWith("GH987015") || upperCaseNINO.startsWith("LX405614")) {
      Ok(eligibleResult(6).toJson())
    } else if (upperCaseNINO.startsWith("EX535913")) {
      Ok(eligibleResult(8).toJson())
    } // Private BETA:
    // Start NINO with EL07 to specify an eligible applicant in receipt of WTC (with reason code 7)
    //
    // After private BETA:
    // Start NINO with EL06 to specify an eligible applicant in receipt of UC (with reason code 6)
    // Start NINO with EL08 to specify an eligible applicant in receipt of WTC and UC (with reason code 8)
    else if (upperCaseNINO.startsWith("EL")) {
      Ok(eligibleResult(getReasonCodeFromNino(nino)).toJson())
    } // Private BETA:
    // Start NINO with NE02 to specify an ineligible applicant in receipt of WTC (with reason code 2)
    // Start NINO with NE03 to specify an ineligible applicant in receipt of WTC (with reason code 3)
    //
    // After private BETA:
    // Start NINO with NE06 to specify an ineligible applicant in receipt of UC (with reason code 6)
    // Start NINO with NE08 to specify an ineligible applicant in receipt of WTC and UC (with reason code 8)
    else if (upperCaseNINO.startsWith("NE")) {
      Ok(ineligibleResult(getReasonCodeFromNino(nino)).toJson())
    } // Start NINO with AC to specify an existing account holder (with reason code 1)
    else if (upperCaseNINO.startsWith("AC")) {
      Ok(alreadyHasAccountResult.toJson())
    } else if (upperCaseNINO.startsWith("EE")) {
      Ok(invalidResultCode.toJson())
    } // Start NINO with TM02 to force eligibility check to time out
    else if (upperCaseNINO.startsWith("TM02")) {
      Thread.sleep(90000)
      Ok(eligibleResult(7).toJson())
    } // Start NINO with anything else to specify an eligible applicant
    else {
      getProfile(nino).
        fold(Ok(eligibleResult(7).toJson())) {
          _.eligibiltyCheckResult.fold[Result](InternalServerError)(r ⇒
            Ok(r.toJson()))
        }
    }
  }

  private val ninoStatusRegex = """ES(\d{3}).*""".r

  private def errorJson(status: Int): JsValue = Json.parse(
    s"""
       |{
       |  "code":   "${io.netty.handler.codec.http.HttpResponseStatus.valueOf(status).reasonPhrase()}",
       |  "reason": "intentional error"
       |}
       """.stripMargin)
}

object EligibilityCheckController {

  /**
   * Response from ITMP eligibility check
   *
   * @param result 1 = Eligible to HtS Account
   *               2 = Ineligible to HtS Account
   *               3 = HtS account already exists
   * @param reason 1 = HtS account was previously created
   *               2 = Not entitled to WTC and UC not checked
   *               3 = Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and not in receipt of UC
   *               4 = Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and in receipt of UC but income is insufficient
   *               5 = Not entitled to WTC and in receipt of UC but income is insufficient
   *               6 = In receipt of UC and income sufficient
   *               7 = Entitled to WTC and in receipt of positive WTC/CTC Tax Credit
   *               8 = Entitled to WTC and in receipt of positive WTC/CTC Tax Credit and in receipt of UC and income sufficient
   *               9 = Not entitled to WTC and not in receipt of UC
   *               N.B. 1-5 & 9 represent reasons for ineligibility and 6-8 repesents reasons for eligibility
   */
  case class EligibilityCheckResult(result: String, resultCode: Int, reason: String, reasonCode: Int)

  object EligibilityCheckResult {

    implicit val format: Format[EligibilityCheckResult] = Json.format[EligibilityCheckResult]

    implicit class EligibilityCheckResultOps(val r: EligibilityCheckResult) extends AnyVal {
      def toJson(): JsValue = Json.toJson(r)
    }

  }

}
