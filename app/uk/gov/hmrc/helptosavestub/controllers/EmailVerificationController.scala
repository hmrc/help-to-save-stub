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
import play.api.mvc.Action
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.controllers.EmailVerificationController.EmailVerificationRequest
import uk.gov.hmrc.helptosavestub.util.Logging

class EmailVerificationController extends BaseController with Logging {

  def verify = Action {
    implicit request ⇒
      request.body.asJson match {
        case None ⇒
          logger.warn("[EmailVerificationController] - no JSON in body")
          BadRequest

        case Some(json) ⇒
          json.validate[EmailVerificationRequest].fold({ e ⇒
            logger.warn(s"[EmailVerificationController] - could not parse JSON in body $e")
            BadRequest
          }, { emailVerificationRequest ⇒
            logger.info(s"[EmailVerificationController] A request has been made: $emailVerificationRequest")
            Ok
          })

      }
  }

}

object EmailVerificationController {
  case class EmailVerificationRequest(email: String, templateId: String,
                                      linkExpiryDuration: String, continueUrl: String,
                                      templateParameters: Map[String, String])

  object EmailVerificationRequest {
    implicit val format: Format[EmailVerificationRequest] = Json.format[EmailVerificationRequest]
  }
}