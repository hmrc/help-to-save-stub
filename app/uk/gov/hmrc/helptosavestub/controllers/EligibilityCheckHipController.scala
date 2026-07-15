/*
 * Copyright 2026 HM Revenue & Customs
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

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.implicits.{catsSyntaxEq, catsSyntaxTuple2Semigroupal}
import com.google.inject.Inject
import jakarta.inject.Singleton
import org.apache.pekko.actor.{ActorSystem, Scheduler}
import play.api.libs.json.*
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.controllers.DWPEligibilityBehaviour.Profile
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckController.EligibilityCheckResult
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckHipController.{EligibilityCheckHipRequest, EligibilityCheckHipResult, ErrorResponse, FailureResponse, Failures}
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.helptosavestub.util.{Delays, ValidatedOrErrorStrings}

import scala.concurrent.ExecutionContext
import scala.util.Try

@Singleton
class EligibilityCheckHipController @Inject() (actorSystem: ActorSystem, cc: ControllerComponents)(
  implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DownstreamAuthController(cc)
    with DWPEligibilityBehaviour
    with Delays {

  val scheduler: Scheduler                     = actorSystem.scheduler
  val checkEligibilityDelayConfig: DelayConfig = Delays.config("check-eligibility")
  private val ninoStatusRegex                  = """ES(\d{3}).*""".r
  private val validNinoFormat                  = """[A-Z]{2}\d{6}[A-D]""".r


  def eligibilityCheck(
                        identifier: String): Action[AnyContent] =
    authorisedAction(appConfig.hipHeaders, Some(appConfig.goUKOriginatorId)) { implicit request =>
      withDelay(checkEligibilityDelayConfig) { () =>

        logger.info(s"Received eligibility check request for identifier: $identifier .")      

        val response = identifier match {
          case s if !validNinoFormat.matches(s) =>
            Status(400)(Json.toJson(ErrorResponse("Hip", FailureResponse(List(Failures("Constraint Violation - Invalid/Missing input parameter", "400.1"))))))
          case s if s.startsWith("WP1144") || s.startsWith("AA1231") =>
            NotFound
          case ninoStatusRegex(s) =>
            Status(Try(s.toInt).getOrElse(0))
          case _ =>
            request.body.asJson match {
              case None =>
                logger.warn("[EligibilityCheckHipController] - no JSON in body")
                Status(400)(Json.toJson(ErrorResponse("Hip", FailureResponse(List(Failures("Constraint Violation - Invalid/Missing input parameter", "400.1"))))))

              case Some(json) =>
                json
                  .validate[EligibilityCheckHipRequest]
                  .fold(
                    { e =>
                      logger.warn(s"[EligibilityCheckHipController] - could not parse JSON in body $e")
                      Status(400)(Json.toJson(ErrorResponse("Hip", FailureResponse(List(Failures("HTTP message not readable", "400.2"))))))
                    }, { eligibilityCheckRequest =>
                      logger.info(s"[EligibilityCheckHipController] A request has been made: $eligibilityCheckRequest")
                      val universalCreditAwardStatus = eligibilityCheckRequest.universalCreditAwardStatus.map(if(_)"Y" else "N")
                      val withinThreshold = eligibilityCheckRequest.withinThreshold.map(if(_)"Y" else "N")
                      getResponse(identifier, universalCreditAwardStatus,withinThreshold)
                    }
                  )
            }
        }
        withCorrelationID(response, request.headers.get("correlationId"))
      }
    }

  private def getResponse(
                           nino: String,
                           universalCreditClaimant: Option[String],
                           withinThreshold: Option[String]): Result = {
    val upperCaseNINO = nino.toUpperCase()
    if (upperCaseNINO.startsWith("EL")) {
      Ok(toHipResult(eligibleResult(getReasonCodeFromNino(nino))).toJson)
    } else if (upperCaseNINO.startsWith("AC")) {
      Ok(toHipResult(alreadyHasAccountResult).toJson)
    } else if (upperCaseNINO.startsWith("DS01")) {
      Ok(manualRejectionResult.toJson)
    } else if (upperCaseNINO.startsWith("TM02")) {
      Thread.sleep(90000)
      Ok(toHipResult(eligibleResult(7)).toJson)
    } else {
      getProfile(nino).fold(Ok(toHipResult(eligibleResult(7)).toJson))(
        handleProfile(_, nino, universalCreditClaimant, withinThreshold))
    }
  }

  private val manualRejectionResult: EligibilityCheckHipResult =
    EligibilityCheckHipResult("CUSTOMER INELIGIBLE FOR HTS ACCOUNT", "MANUAL")



  private val reasonMap: Map[Int, String] = Map(
    1   -> "HTS ACCOUNT HELD ALREADY",
    2   -> "NOT ENTITLED TO WTC AND UC NOT CHECKED",
    3   -> "ENTITLED TO WTC BUT NOT IN RECEIPT OF POSITIVE TAX CREDIT AND NOT IN RECEIPT DWP UC",
    4   -> "ENTITLED TO WTC BUT NOT IN RECEIPT OF POSITIVE TAX CREDIT AND IN RECEIPT DWP UC BUT INCOME INSUFFICIENT",
    5   -> "NOT ENTITLED TO WTC AND IN RECEIPT OF UC BUT INCOME INSUFFICIENT",
    6   -> "IN RECEIPT OF DWP UC AND INCOME SUFFICIENT",
    7   -> "ENTITLED TO WTC AND RECEIVE POSITIVE TAX CREDIT",
    8   -> "ENTITLED TO WTC AND RECEIVE POSITIVE TAX CREDIT AND IN RECEIPT OF DWP UC AND INCOME SUFFICIENT",
    9   -> "NOT ENTITLED TO WTC AND NOT IN RECEIPT OF UC",
    10  -> "MANUAL"
  )

  private val resultMap: Map[Int, String] = Map(
    1 -> "CUSTOMER ELIGIBLE FOR HTS ACCOUNT",
    2 -> "CUSTOMER INELIGIBLE FOR HTS ACCOUNT",
    4 -> "UNKNOWN ELIGIBILITY BECAUSE CALL TO DWP FAILED"
  )

  private def toHipResult(r: EligibilityCheckResult): EligibilityCheckHipResult = {
   EligibilityCheckHipResult(
      eligibilityResult = resultMap.getOrElse(r.resultCode, resultMap(2)),
      eligibilityReason = reasonMap(r.reasonCode)
    )
  }
  private def handleProfile(
                             profile: Profile,
                             nino: String,
                             universalCreditClaimant: Option[String],
                             withinThreshold: Option[String]): Result =
    ucParametersValidation(profile)(universalCreditClaimant, withinThreshold).fold(
      { e =>
        logger.warn(
          s"Invalid UC parameters passed into eligibility call for NINO $nino: " +
            s"[universalCreditClaimant: ${universalCreditClaimant.getOrElse("-")}, withinThreshold: ${withinThreshold
              .getOrElse("-")}]. Errors were: " +
            s"${e.toList.mkString("; ")}")
        BadRequest
      }, { _ =>
        profile.eligibiltyCheckResult.fold[Result](InternalServerError)(r => Ok(toHipResult(r).toJson))
      }
    )

  private def ucParametersValidation(profile: Profile)(
    universalCreditClaimant: Option[String], // scalastyle:ignore
    withinThreshold: Option[String]): ValidatedNel[String, Unit] = {
    def reasonCodeIs(code: Int): Boolean = profile.eligibiltyCheckResult.map(_.reasonCode).contains(code)

    profile.uCDetails match {
      case None =>
        if (universalCreditClaimant.isEmpty && withinThreshold.isEmpty) {
          Valid(())
        } else {
          Invalid("eligibility profile had no ucDetails but received parameters in request").toValidatedNel
        }

      case Some(p) =>
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

        (universalCreditClaimantCheck, withinThresholdCheck).mapN { case _ => () }
    }

  }

}

object EligibilityCheckHipController {

  /**
   * Response from ITMP eligibility check
   *
   * @param result 1 = CUSTOMER ELIGIBLE FOR HTS ACCOUNT
   *               2 = CUSTOMER INELIGIBLE FOR HTS ACCOUNT
 *
   * @param reason 1   = HTS ACCOUNT HELD ALREADY
   *               2   = NOT ENTITLED TO WTC AND UC NOT CHECKED
   *               3   = ENTITLED TO WTC BUT NOT IN RECEIPT OF POSITIVE TAX CREDIT AND NOT IN RECEIPT DWP UC
   *               4   = ENTITLED TO WTC BUT NOT IN RECEIPT OF POSITIVE TAX CREDIT AND IN RECEIPT DWP UC BUT INCOME INSUFFICIENT
   *               5   = NOT ENTITLED TO WTC AND IN RECEIPT OF UC BUT INCOME INSUFFICIENT
   *               6   = IN RECEIPT OF DWP UC AND INCOME SUFFICIENT
   *               7   = ENTITLED TO WTC AND RECEIVE POSITIVE TAX CREDIT
   *               8   = ENTITLED TO WTC AND RECEIVE POSITIVE TAX CREDIT AND IN RECEIPT OF DWP UC AND INCOME SUFFICIENT
   *               9   = NOT ENTITLED TO WTC AND NOT IN RECEIPT OF UC
   *               10  = MANUAL
   */
  case class EligibilityCheckHipResult(eligibilityResult: String, eligibilityReason: String)

  object EligibilityCheckHipResult {

    implicit val format: OFormat[EligibilityCheckHipResult] = Json.format[EligibilityCheckHipResult]

    implicit class EligibilityCheckHipResultOps(val r: EligibilityCheckHipResult) extends AnyVal {
      def toJson: JsValue = Json.toJson(r)
    }

  }

  enum NewTaxCreditStatus(val value: String) {
    case ninoNotFound extends NewTaxCreditStatus("NINO not found")
    case ninoNoNTC extends NewTaxCreditStatus("NINO not currently in an NTC current year award")
    case ninoNTC extends NewTaxCreditStatus("Current NTC current year award found")
  }



  enum WorkingTaxCreditEntitlement(val value: String) {
    case awardIncludesWTC extends WorkingTaxCreditEntitlement("Current Award includes a WTC entitlement")
    case awardNotIncludeWTC extends WorkingTaxCreditEntitlement("Current Award does not include a WTC entitlement")
  }


  case class EligibilityCheckHipRequest(newTaxCreditStatus : NewTaxCreditStatus,
                                        workingTaxCreditEntitlement : WorkingTaxCreditEntitlement,
                                        workingTaxCreditTaperedHouseholdAward : Double,
                                        childTaxCreditTaperedHouseholdAward : Double,
                                        universalCreditAwardStatus : Option[Boolean],
                                        withinThreshold : Option[Boolean])

  object EligibilityCheckHipRequest {
      implicit val newTaxCreditStatusFormat: Format[NewTaxCreditStatus] = new Format[NewTaxCreditStatus] {
        def reads(json: JsValue): JsResult[NewTaxCreditStatus] = json match {
          case JsString(str) =>
            NewTaxCreditStatus.values.find(_.value == str) match {
              case Some(enumValue) => JsSuccess(enumValue)
              case None => JsError(s"Unknown NewTaxCreditStatus value: $str")
            }
          case _ => JsError("Expected a JSON String")
        }

        def writes(creditStatus: NewTaxCreditStatus): JsValue = JsString(creditStatus.value)
      }

      implicit val WorkingTaxCreditEntitlementFormat: Format[WorkingTaxCreditEntitlement] = new Format[WorkingTaxCreditEntitlement] {
        def reads(json: JsValue): JsResult[WorkingTaxCreditEntitlement] = json match {
          case JsString(str) =>
            WorkingTaxCreditEntitlement.values.find(_.value == str) match {
              case Some(enumValue) => JsSuccess(enumValue)
              case None => JsError(s"Unknown WorkingTaxCreditEntitlement value: $str")
            }
          case _ => JsError("Expected a JSON String")
        }

        def writes(creditEntitlement: WorkingTaxCreditEntitlement): JsValue = JsString(creditEntitlement.value)
      }

      implicit val format: OFormat[EligibilityCheckHipRequest] =   Json.format[EligibilityCheckHipRequest]

  }

  case class Failures(reason: String, code: String)

  implicit val failureFormat: OFormat[Failures] = Json.format[Failures]
  
  case class FailureResponse(failures: List[Failures])
  
  implicit val failureResponseFormat : OFormat[FailureResponse] = Json.format[FailureResponse]


  case class ErrorResponse(origin: String, response: FailureResponse)

  implicit val errorResponseFormat: OFormat[ErrorResponse] =  Json.format[ErrorResponse]

}

