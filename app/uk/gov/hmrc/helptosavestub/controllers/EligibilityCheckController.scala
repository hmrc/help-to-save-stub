/*
 * Copyright 2019 HM Revenue & Customs
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

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.StatusCode
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.instances.option._
import cats.instances.string._
import cats.syntax.apply._
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.controllers.DWPEligibilityBehaviour.Profile
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.helptosavestub.util.{Delays, ValidatedOrErrorStrings}

import scala.concurrent.ExecutionContext
import scala.util.Try

@Singleton
class EligibilityCheckController @Inject()(actorSystem: ActorSystem, appConfig: AppConfig, cc: ControllerComponents)(
  implicit ec: ExecutionContext)
    extends DESController(cc, appConfig)
    with DWPEligibilityBehaviour
    with Delays {

  val scheduler: Scheduler                     = actorSystem.scheduler
  val checkEligibilityDelayConfig: DelayConfig = Delays.config("check-eligibility", actorSystem.settings.config)

  def eligibilityCheck(
    nino: String,
    universalCreditClaimant: Option[String],
    withinThreshold: Option[String]): Action[AnyContent] =
    desAuthorisedAction { implicit request ⇒
      withDelay(checkEligibilityDelayConfig) { () ⇒
        logger.info(
          s"Received eligibility check request for nino: $nino. UC parameters in the request are: " +
            s"ucClaimant: ${universalCreditClaimant.getOrElse("-")}, " +
            s"withinThreshold: ${withinThreshold.getOrElse("-")}")

        val status: Option[Int] = nino match {
          case ninoStatusRegex(s) ⇒ Try(s.toInt).toOption
          // Scenario 2
          case s if s.startsWith("WP1144") || s.startsWith("AA1231") ⇒ Some(404)
          case _ ⇒ None
        }

        val response = status match {
          case Some(s) ⇒
            Status(s)(errorJson(s))

          case None ⇒
            getResponse(nino, universalCreditClaimant, withinThreshold)
        }

        withDesCorrelationID(response)
      }
    }

  private def getResponse(
    nino: String,
    universalCreditClaimant: Option[String],
    withinThreshold: Option[String]): Result = {
    val upperCaseNINO = nino.toUpperCase()
    // Private BETA:
    // Start NINO with EL07 to specify an eligible applicant in receipt of WTC (with reason code 7)
    //
    // After private BETA:
    // Start NINO with EL06 to specify an eligible applicant in receipt of UC (with reason code 6)
    // Start NINO with EL08 to specify an eligible applicant in receipt of WTC and UC (with reason code 8)
    if (upperCaseNINO.startsWith("EL")) {
      Ok(eligibleResult(getReasonCodeFromNino(nino)).toJson())
    } else if (upperCaseNINO.startsWith("AC")) {
      Ok(alreadyHasAccountResult.toJson())
    } else if (upperCaseNINO.startsWith("EE")) {
      Ok(invalidResultCode.toJson())
    } // Start NINO with TM02 to force eligibility check to time out
    else if (upperCaseNINO.startsWith("TM02")) {
      Thread.sleep(90000)
      Ok(eligibleResult(7).toJson())
    } // Start NINO with anything else to specify an eligible applicant
    else {
      getProfile(nino).fold(Ok(eligibleResult(7).toJson()))(
        handleProfile(_, nino, universalCreditClaimant, withinThreshold))
    }
  }

  private def handleProfile(
    profile: Profile,
    nino: String,
    universalCreditClaimant: Option[String],
    withinThreshold: Option[String]): Result =
    ucParametersValidation(profile)(universalCreditClaimant, withinThreshold).fold(
      { e ⇒
        logger.warn(
          s"Invalid UC parameters passed into eligibility call for NINO $nino: " +
            s"[universalCreditClaimant: ${universalCreditClaimant.getOrElse("-")}, withinThreshold: ${withinThreshold
              .getOrElse("-")}]. Errors were: " +
            s"${e.toList.mkString("; ")}")
        BadRequest
      }, { _ ⇒
        profile.eligibiltyCheckResult.fold[Result](InternalServerError)(r ⇒ Ok(r.toJson()))
      }
    )

  private def ucParametersValidation(profile: Profile)(
    universalCreditClaimant: Option[String], // scalastyle:ignore
    withinThreshold: Option[String]): ValidatedNel[String, Unit] = {
    def reasonCodeIs(code: Int): Boolean = profile.eligibiltyCheckResult.map(_.reasonCode).contains(code)

    profile.uCDetails match {
      case None ⇒
        if (universalCreditClaimant.isEmpty && withinThreshold.isEmpty) {
          Valid(())
        } else {
          Invalid("eligibility profile had no ucDetails but received parameters in request").toValidatedNel
        }

      case Some(p) ⇒
        val universalCreditClaimantCheck: ValidatedOrErrorStrings[Unit] =
          // don't worry about the inputs if the eligibility reason is not entirely to do with UC
          if (universalCreditClaimant.contains(p.ucClaimant) || reasonCodeIs(7) || reasonCodeIs(8)) {
            Valid(())
          } else {
            Invalid(s"expected universalCreditClaimant '${p.ucClaimant}' but received value '${universalCreditClaimant
              .getOrElse("")}'").toValidatedNel
          }

        val withinThresholdCheck: ValidatedOrErrorStrings[Unit] =
          if (p.withinThreshold === withinThreshold || reasonCodeIs(7) || reasonCodeIs(8)) {
            Valid(())
          } else {
            Invalid(s"expected withinThreshold '${p.withinThreshold
              .getOrElse("")}' but received value '${withinThreshold.getOrElse("")}'").toValidatedNel
          }

        (universalCreditClaimantCheck, withinThresholdCheck).mapN { case _ ⇒ () }
    }

  }

  private val ninoStatusRegex = """ES(\d{3}).*""".r

  private def errorJson(status: Int): JsValue = Json.parse(s"""
       |{
       |  "code":   "${StatusCode.int2StatusCode(status).reason()}",
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
    *               4 = Unknown eligibility because call to DWP failed"
    * @param reason 1 = HtS account was previously created
    *               2 = Not entitled to WTC and UC not checked
    *               3 = Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and not in receipt of UC
    *               4 = Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and in receipt of UC but income is insufficient
    *               5 = Ineligible to HtS Account: Not entitled to WTC and in receipt of UC but income is insufficient
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
