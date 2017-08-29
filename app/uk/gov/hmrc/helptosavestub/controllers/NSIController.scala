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

import java.nio.charset.StandardCharsets
import java.util.Base64

import play.api.libs.json._
import play.api.mvc.{Action, Headers}
import uk.gov.hmrc.helptosavefrontend.models.NSIUserInfo
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.util.Logging

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object NSIController extends BaseController with Logging {

  val authorizationHeaderKey = "Authorization1"
  val authorizationValuePrefix = "Basic: "
  val testAuthHeader = "user:password"

  def isAuthorised(headers: Headers): Boolean = {
    val decoded: Option[String] =
      headers.headers
        .find(entry ⇒
          entry._1 == authorizationHeaderKey && entry._2.startsWith(authorizationValuePrefix))
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

  def createAccount() = Action.async { implicit request ⇒
    if (isAuthorised(request.headers)) {
      lazy val requestBodyText = request.body.asText.getOrElse("")

      request.body.asJson.map(_.validate[NSIUserInfo]) match {
        case None ⇒
          logger.error(s"No JSON found for create-account request: $requestBodyText")
          Future.successful(BadRequest(
            Json.toJson(SubmissionFailure(None, "No JSON found", ""))))

        case Some(er: JsError) ⇒
          logger.error(s"Could not parse JSON found for create-account request: $requestBodyText")
          Future.successful(BadRequest(
            Json.toJson(SubmissionFailure(None, "Invalid Json", er.toString))))

        case Some(JsSuccess(info, _)) ⇒
          logger.info("Responding to createAccount with 201")
          Future.successful(Created)
      }
    } else {
      logger.error("No authorisation data found in header")
      Future.successful(Unauthorized)
    }
  }
}

case class SubmissionFailure(errorMessageId: Option[String], errorMessage: String, errorDetail: String)

object SubmissionFailure {
  implicit val submissionFailureFormat: Writes[SubmissionFailure] = Json.writes[SubmissionFailure]
}
