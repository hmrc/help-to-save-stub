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

import play.api.mvc.*
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController

import java.util.UUID
import scala.concurrent.Future

// class for controllers mimicking IF to extend
trait IFController extends BackendBaseController with Logging {

  private def expectedHeaders(implicit appConfig: AppConfig) =
    appConfig.ifHeaders

  def ifAuthorisedAction(body: Request[AnyContent] => Future[Result])(
    implicit appConfig: AppConfig): Action[AnyContent] = Action.async { request =>
    val authHeaders = request.headers.getAll("Authorization")
    if (authHeaders.contains(expectedHeaders)) {
      body(request)
    } else {
      logger.warn(s"Request did not contain expected authorisation header. Received: $authHeaders")
      Future.successful(Unauthorized)
    }
  }

  def withIfCorrelationID(response: Result)(implicit appConfig: AppConfig): Result =
    response.withHeaders(
      "CorrelationId" -> UUID.randomUUID().toString,
      "Etag"          -> appConfig.runModeConfiguration.underlying.getString("microservice.Etag"))

}
