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

import java.nio.charset.StandardCharsets
import java.util.Base64

import cats.data.Validated.{Invalid, Valid}
import cats.data.{Validated, ValidatedNel}
import cats.instances.string._
import cats.syntax.either._
import cats.syntax.eq._
import cats.syntax.cartesian._
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Headers, Request, Result}
import uk.gov.hmrc.helptosavestub
import uk.gov.hmrc.helptosavestub.controllers.NSIGetAccountBehaviour.getAccountByNino
import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse, NSIUserInfo}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.util.{Logging, NINO}

import scala.util.{Failure, Success, Try}

object NSIController extends BaseController with Logging {

  val authorizationHeaderKey: String = "Authorization-test"
  val authorizationValuePrefix: String = "Basic: "
  val testAuthHeader: String = "username:password"

  def isAuthorised(headers: Headers): Boolean = {
    val decoded: Option[String] =
      headers.headers
        .find(entry ⇒
          entry._1 === authorizationHeaderKey && entry._2.startsWith(authorizationValuePrefix))
        .flatMap{
          case (_, h) ⇒
            val decoded = Try(Base64.getDecoder.decode(h.stripPrefix(authorizationValuePrefix)))
            decoded match {
              case Success(bytes) ⇒
                Some(new String(bytes, StandardCharsets.UTF_8))

              case Failure(error) ⇒
                logger.error(s"Could not decode authorization details: header value was $h. ${error.getMessage}", error)
                None
            }
        }

    decoded.contains(testAuthHeader)
  }

  def withNSIUserInfo(description: String)(body: NSIUserInfo ⇒ Result)(implicit request: Request[AnyContent]): Result = {
    lazy val requestBodyText = request.body.asText.getOrElse("")

    request.body.asJson.map(_.validate[NSIUserInfo]) match {
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

  def updateEmailOrHealthCheck(): Action[AnyContent] = Action { implicit request ⇒
    withNSIUserInfo("update email or health check") { nsiUserInfo ⇒
      val description = if (nsiUserInfo.nino === "XX999999X") "health check" else "update email"
      handleRequest(nsiUserInfo, Ok, description)
    }
  }

  def createAccount(): Action[AnyContent] = Action { implicit request ⇒
    val description = "create account"
    withNSIUserInfo(description){ nsiUserInfo ⇒ handleRequest(nsiUserInfo, Created, description) }
  }

  def handleRequest(nsiUserInfo: NSIUserInfo, successResult: Result, description: String)(implicit request: Request[AnyContent]): Result = {
    if (isAuthorised(request.headers)) {
      val status: Option[Int] = nsiUserInfo.nino match {
        case ninoStatusRegex(s) ⇒ Try(s.toInt).toOption
        case _                  ⇒ None
      }

      logger.info(s"Responding to $description with ${status.getOrElse(successResult.header.status)}")

      status.fold(successResult){ s ⇒
        Status(s)(Json.toJson(SubmissionFailureResponse(
          SubmissionFailure(Some("ID"), "intentional error", s"extracted status $s from nino ${nsiUserInfo.nino}"))
        ))
      }
    } else {
      logger.error(s"No authorisation data found in header for $description request")
      Unauthorized
    }
  }

  private val ninoStatusRegex = """AS(\d{3}).*""".r

  def getAccount(correlationId: Option[String],
                 nino:          Option[String],
                 version:       Option[String],
                 systemId:      Option[String]): Action[AnyContent] = Action {
    implicit request ⇒
      validateParams(correlationId, nino, version, systemId).fold(
        errors ⇒ {
          logger.warn("[Handle Account Query] invalid params")
          BadRequest(Json.toJson(NSIErrorResponse(version, correlationId, errors.toList)))
        }, {
          validatedNino ⇒
            if (validatedNino.contains("401")) {
              Unauthorized
            } else if (validatedNino.contains("500")) {
              InternalServerError
            } else {
              val maybeAccount = getAccountByNino(validatedNino, correlationId)
              maybeAccount match {
                case Right(a) ⇒ Ok(Json.toJson(a))
                case Left(e)  ⇒ BadRequest(Json.toJson(NSIErrorResponse(version, correlationId, Seq(e))))
              }
            }
        })
  }

  private def validateParams(correlationId: Option[String],
                             nino:          Option[String],
                             version:       Option[String],
                             systemId:      Option[String]): ValidatedNel[ErrorDetails, NINO] = {

    val versionValidation: ValidatedNel[ErrorDetails, String] = version.fold[Validated[ErrorDetails, String]](
      Validated.Invalid(NSIErrorResponse.missingVersionError)
    )(
        v ⇒ if (v =!= "V1.0") Validated.Invalid(NSIErrorResponse.unsupportedVersionError) else Validated.Valid(v)
      ).toValidatedNel

    val ninoValidation: ValidatedNel[ErrorDetails, String] = nino.fold[Validated[ErrorDetails, String]]({
      Invalid(NSIErrorResponse.missingNinoError)
    }
    ){
      case maybeNino ⇒ if (!maybeNino.matches(helptosavestub.util.ninoRegex.regex)) {
        Invalid(NSIErrorResponse.badNinoError)
      } else { Valid(maybeNino) }
    }.toValidatedNel

    val systemIdValidation: ValidatedNel[ErrorDetails, String] = systemId.fold[Validated[ErrorDetails, String]](
      Validated.Invalid(NSIErrorResponse.missingSystemIdError)
    )(
        sid ⇒ Validated.Valid(sid)
      ).toValidatedNel

    (versionValidation |@| ninoValidation |@| systemIdValidation)
      .map {
        case (v, validatedNino, sid) ⇒ validatedNino
      }

  }

}

case class SubmissionFailure(errorMessageId: Option[String], errorMessage: String, errorDetail: String)

object SubmissionFailure {
  implicit val submissionFailureFormat: Writes[SubmissionFailure] = Json.writes[SubmissionFailure]
}

case class SubmissionFailureResponse(error: SubmissionFailure)

object SubmissionFailureResponse {
  implicit val submissionFailureResponseFormat: Writes[SubmissionFailureResponse] = Json.writes[SubmissionFailureResponse]
}
