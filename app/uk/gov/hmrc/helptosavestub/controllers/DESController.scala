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
import play.api.mvc.*
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import scala.concurrent.Future

// class for controllers mimicking DES to extend
class DESController @Inject()(cc: ControllerComponents, appConfig: AppConfig)
    extends BackendController(cc)
    with Logging {

  private val expectedDESHeaders = appConfig.desHeaders

  def desAuthorisedAction(body: Request[AnyContent] => Future[Result]): Action[AnyContent] = Action.async { request =>
    val authHeaders = request.headers.getAll("Authorization")

    if (authHeaders.contains(expectedDESHeaders)) {
      body(request)
    } else {
      logger.warn(s"Request did not contain expected authorisation header. Received: $authHeaders")
      Future.successful(Unauthorized)
    }
  }

  def withDesCorrelationID(response: Result): Result =
    response.withHeaders("CorrelationId" -> UUID.randomUUID().toString)

}
