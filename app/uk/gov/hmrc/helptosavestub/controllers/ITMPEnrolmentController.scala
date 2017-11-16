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

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController

object ITMPEnrolmentController extends BaseController with DESController with Logging {

  def enrol(nino: String): Action[AnyContent] = desAuthorisedAction { implicit request ⇒
    if (nino.startsWith("C")) {
      logger.info("Received request to set ITMP flag: returning status 403 (FORBIDDEN)")
      Forbidden
    } else if (nino.startsWith("E")) {
      logger.info("Received request to set ITMP flag: returning status 500 (INTERNAL SERVER ERROR)")
      InternalServerError
    } else if (nino.startsWith("TM04")) {
      logger.info("Received request to set ITMP flag: returning status 200 (OK) after timeout")
      Thread.sleep(90000)
      Ok
    } else {
      logger.info("Received request to set ITMP flag: returning status 200 (OK)")
      Ok
    }
  }
}
