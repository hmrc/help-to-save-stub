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

import java.nio.charset.StandardCharsets
import java.util.Base64

import akka.actor.{ActorSystem, Scheduler}
import cats.data.Validated.{Invalid, Valid}
import cats.data.{Validated, ValidatedNel}
import cats.instances.string._
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import org.scalacheck.Gen
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.helptosavestub
import uk.gov.hmrc.helptosavestub.controllers.BARSController.BankDetails
import uk.gov.hmrc.helptosavestub.controllers.BankDetailsBehaviour.Profile
import uk.gov.hmrc.helptosavestub.controllers.NSIGetAccountBehaviour.getAccountByNino
import uk.gov.hmrc.helptosavestub.controllers.NSIGetTransactionsBehaviour.getTransactionsByNino
import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse, NSIPayload}
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.helptosavestub.util.{Delays, Logging, NINO}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Singleton
class NSIController @Inject()(actorSystem: ActorSystem, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging
    with BankDetailsBehaviour
    with Delays {

  val scheduler: Scheduler = actorSystem.scheduler

  val authorizationHeaderKeys: List[String] = List("Authorization-test", "Authorization")
  val authorizationValuePrefix: String      = "Basic "
  val testAuthHeader: String                = "username:password"

  val createAccountDelayConfig: DelayConfig   = Delays.config("create-account", actorSystem.settings.config)
  val getAccountDelayConfig: DelayConfig      = Delays.config("get-account", actorSystem.settings.config)
  val updateAccountDelayConfig: DelayConfig   = Delays.config("update-account", actorSystem.settings.config)
  val getTransactionsDelayConfig: DelayConfig = Delays.config("get-transactions", actorSystem.settings.config)

  def isAuthorised(headers: Headers): Either[String, Unit] = {
    val decoded: Either[String, String] =
      headers.headers
        .find(entry ⇒
          authorizationHeaderKeys.exists(
            entry._1.toLowerCase === _.toLowerCase && entry._2.startsWith(authorizationValuePrefix)))
        .fold[Either[String, String]](
          Left("Could not find authorization header")
        ) {
          case (_, h) ⇒
            val decoded = Try(Base64.getDecoder.decode(h.stripPrefix(authorizationValuePrefix)))
            decoded match {
              case Success(bytes) ⇒
                Right(new String(bytes, StandardCharsets.UTF_8))

              case Failure(error) ⇒
                Left(s"Could not decode authorization details: header value was $h. ${error.getMessage}")

            }
        }

    decoded.flatMap { s ⇒
      if (s === testAuthHeader) {
        Right(())
      } else {
        Left(s"Authorisation value '$s' was not equal to expected value")
      }
    }
  }

  def withNSIPayload(description: String)(body: NSIPayload ⇒ Result)(implicit request: Request[AnyContent]): Result = {
    lazy val requestBodyText = request.body.asText.getOrElse("")

    request.body.asJson.map(_.validate[NSIPayload]) match {
      case None ⇒
        logger.error(s"No JSON found for $description request: $requestBodyText")
        BadRequest(Json.toJson(SubmissionFailureResponse(SubmissionFailure(None, "No JSON found", ""))))

      case Some(er: JsError) ⇒
        logger.error(s"Could not parse JSON found for $description request: $requestBodyText")
        BadRequest(Json.toJson(SubmissionFailureResponse(SubmissionFailure(None, "Invalid Json", er.toString))))

      case Some(JsSuccess(info, _)) ⇒
        body(info)
    }
  }

  def updateEmailOrHealthCheck(): Action[AnyContent] = Action.async { implicit request ⇒
    withDelay(updateAccountDelayConfig) { () ⇒
      withNSIPayload("update email or health check") { nsiUserInfo ⇒
        val description = if (nsiUserInfo.nino === "XX999999X") "NS&I health check" else "update email"
        handleRequest(nsiUserInfo, Ok, description)
      }
    }
  }

  def createAccount(): Action[AnyContent] = Action.async { implicit request ⇒
    withDelay(createAccountDelayConfig) { () ⇒
      val description = "create account"
      withNSIPayload(description) { nsiPayload ⇒
        nsiPayload.nbaDetails match {
          case Some(bankDetails) ⇒
            getBankProfile(BankDetails(bankDetails.sortCode, bankDetails.accountNumber)) match {
              case Profile(_, createAccountResponse) ⇒
                createAccountResponse.response match {
                  case Right(()) ⇒
                    val accountNumber = generateAccountNumberJson
                    handleRequest(nsiPayload, Created(accountNumber), description)
                  case Left(error) ⇒
                    logger.warn(s"Create Account request failed, errorDetails are: $error")
                    BadRequest(
                      Json.toJson(SubmissionFailureResponse(
                        SubmissionFailure(Some(error.errorMessageId), error.errorMessage, error.errorDetail))))
                }
            }

          case None ⇒
            val accountNumber = generateAccountNumberJson
            handleRequest(nsiPayload, Created(accountNumber), description)
        }
      }
    }
  }

  def handleRequest(nsiPayload: NSIPayload, successResult: Result, description: String)(
    implicit request: Request[AnyContent]): Result =
    isAuthorised(request.headers).fold(
      { e ⇒
        logger.error(e)
        Unauthorized
      }, { _ ⇒
        val status: Option[Int] = nsiPayload.nino match {
          case ninoStatusRegex(s) ⇒ Try(s.toInt).toOption
          case _ ⇒ None
        }

        logger.info(
          s"Responding to $description with status: ${status.getOrElse(successResult.header.status)}, nsiPayload from the request is: $nsiPayload ")

        status.fold(successResult) { s ⇒
          Status(s)(
            Json.toJson(SubmissionFailureResponse(
              SubmissionFailure(Some("ID"), "intentional error", s"extracted status $s from nino ${nsiPayload.nino}"))))
        }
      }
    )

  private def generateAccountNumberJson: JsValue =
    Json.parse(s"""{
      |"accountNumber": "${Gen
                    .listOfN(10, Gen.numChar)
                    .sample
                    .map(_.mkString(""))
                    .getOrElse(sys.error("Could not generate account number"))}"
      |}
    """.stripMargin)

  private val ninoStatusRegex = """AS(\d{3}).*""".r

  def getAccount(
    correlationId: Option[String],
    nino: Option[String],
    version: Option[String],
    systemId: Option[String]): Action[AnyContent] = Action.async { implicit request ⇒
    withDelay[Result](getAccountDelayConfig) { () ⇒
      validateParams(nino, version, systemId).fold(
        errors ⇒ {
          logger.warn("[Handle Account Query] invalid params")
          BadRequest(Json.toJson(NSIErrorResponse(version, correlationId, errors.toList)))
        }, { validatedNino ⇒
          if (validatedNino.contains("401")) {
            Unauthorized
          } else if (validatedNino.contains("500")) {
            InternalServerError
          } else {
            val maybeAccount = getAccountByNino(validatedNino, correlationId)
            maybeAccount match {
              case Right(a) ⇒ Ok(Json.toJson(a))
              case Left(e) ⇒ BadRequest(Json.toJson(NSIErrorResponse(version, correlationId, Seq(e))))
            }
          }
        }
      )
    }
  }

  def getTransactions(
    correlationId: Option[String],
    nino: Option[String],
    version: Option[String],
    systemId: Option[String]): Action[AnyContent] = Action.async { implicit request ⇒
    withDelay(getTransactionsDelayConfig) { () ⇒
      validateParams(nino, version, systemId).fold(
        errors ⇒ {
          logger.warn("[Handle Transactions Query] invalid params")
          BadRequest(Json.toJson(NSIErrorResponse(version, correlationId, errors.toList)))
        }, { validatedNino ⇒
          if (validatedNino.contains("401")) {
            Unauthorized
          } else if (validatedNino.contains("500")) {
            InternalServerError
          } else {
            val maybeAccount = getTransactionsByNino(validatedNino, correlationId)
            maybeAccount match {
              case Right(a) ⇒ Ok(Json.toJson(a))
              case Left(e) ⇒ BadRequest(Json.toJson(NSIErrorResponse(version, correlationId, Seq(e))))
            }
          }
        }
      )
    }
  }

  type ValidatedOrErrorDetails[A] = ValidatedNel[ErrorDetails, A]

  private def validateParams(
    nino: Option[String],
    version: Option[String],
    systemId: Option[String]): ValidatedNel[ErrorDetails, NINO] = {

    val versionValidation: ValidatedOrErrorDetails[String] = version
      .fold[Validated[ErrorDetails, String]](
        Validated.Invalid(NSIErrorResponse.missingVersionError)
      )(
        v ⇒ if (v =!= "V1.0") Validated.Invalid(NSIErrorResponse.unsupportedVersionError) else Validated.Valid(v)
      )
      .toValidatedNel

    val ninoValidation: ValidatedOrErrorDetails[String] = nino
      .fold[Validated[ErrorDetails, String]]({
        Invalid(NSIErrorResponse.missingNinoError)
      }) {
        case maybeNino ⇒
          if (!maybeNino.matches(helptosavestub.util.ninoRegex.regex)) {
            Invalid(NSIErrorResponse.badNinoError)
          } else { Valid(maybeNino) }
      }
      .toValidatedNel

    val systemIdValidation: ValidatedOrErrorDetails[String] = systemId
      .fold[Validated[ErrorDetails, String]](
        Validated.Invalid(NSIErrorResponse.missingSystemIdError)
      )(
        sid ⇒ Validated.Valid(sid)
      )
      .toValidatedNel

    (versionValidation, ninoValidation, systemIdValidation)
      .mapN {
        case (_, validatedNino, _) ⇒ validatedNino
      }

  }

}

case class SubmissionFailure(errorMessageId: Option[String], errorMessage: String, errorDetail: String)

object SubmissionFailure {
  implicit val submissionFailureFormat: Writes[SubmissionFailure] = Json.writes[SubmissionFailure]
}

case class SubmissionFailureResponse(error: SubmissionFailure)

object SubmissionFailureResponse {
  implicit val submissionFailureResponseFormat: Writes[SubmissionFailureResponse] =
    Json.writes[SubmissionFailureResponse]
}
