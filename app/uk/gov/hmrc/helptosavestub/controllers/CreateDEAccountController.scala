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
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavestub.models.NSIUserInfo
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController

object CreateDEAccountController extends BaseController with Logging {

  def createDEAccount(): Action[AnyContent] = Action { implicit request ⇒
    lazy val requestBodyText = request.body.asText.getOrElse("")

    request.body.asJson.map(_.validate[NSIUserInfo]) match {
      case None ⇒
        logger.error(s"No JSON found for /create-de-account request: $requestBodyText")
        BadRequest(Json.toJson(SubmissionFailureResponse(SubmissionFailure(None, "No JSON found", ""))))

      case Some(er: JsError) ⇒
        logger.error(s"Could not parse JSON found for /create-de-account request: $requestBodyText")
        BadRequest(Json.toJson(SubmissionFailureResponse(SubmissionFailure(None, "Invalid Json", er.toString))))

      case Some(JsSuccess(nsiUserInfo, _)) ⇒

        val response =
          if (nsiUserInfo.registrationChannel =!= "callCentre" ||
            nsiUserInfo.contactDetails.communicationPreference =!= "00") {
            BadRequest(Json.toJson(
              SubmissionFailureResponse(SubmissionFailure(None, "Invalid Json data", "check registrationChannel and communicationPreference fields")
              )))
          } else {
            Created
          }

        logger.info(s"Responding to /create-de-account with status s${response.header.status}")

        response
    }
  }
}
