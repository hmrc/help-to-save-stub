/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckHipController.{ErrorResponse, FailureResponse, Failures}
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import scala.concurrent.Future

// class for controllers mimicking DES to extend
class DownstreamAuthController @Inject()(cc: ControllerComponents)
    extends BackendController(cc)
    with Logging {

  def authorisedAction(expectedHeaders : String, govUKOriginatorID : Option[String] = None)(body: Request[AnyContent] => Future[Result]): Action[AnyContent] = Action.async { request =>
    val headers = request.headers
    val authHeaders = headers.getAll("Authorization")

    if (authHeaders.contains(expectedHeaders)) {
        val originatorId = headers.get("gov-uk-originator-id")
        val correlationId = headers.get("correlationId")
        if(govUKOriginatorID.isDefined && !(correlationId.isDefined && originatorId.contains(govUKOriginatorID.get))){
          logger.warn(s"Request did not contain expected header. Received originatorId: $originatorId correlationId: $correlationId ")
          Future.successful(Status(BAD_REQUEST)(Json.toJson(ErrorResponse("Hip", FailureResponse(List(Failures("Constraint Violation - Invalid/Missing header", "400.1")))))))
      } else body(request)
    } else {
      logger.warn(s"Request did not contain expected authorisation header. Received: $authHeaders")
      Future.successful(Unauthorized)
    }
  }

  def withCorrelationID(response: Result, correlationId: Option[String] = None): Result =
    response.withHeaders("CorrelationId" -> correlationId.getOrElse(UUID.randomUUID().toString))
}