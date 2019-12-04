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

import akka.actor.{ActorSystem, Scheduler}
import com.google.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.util.Delays
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig

import scala.concurrent.ExecutionContext

@Singleton
class ITMPEnrolmentController @Inject()(actorSystem: ActorSystem, appConfig: AppConfig, cc: ControllerComponents)(
  implicit ec: ExecutionContext)
    extends DESController(cc, appConfig)
    with Delays {

  val scheduler: Scheduler                = actorSystem.scheduler
  val setItmpFlagDelayConfig: DelayConfig = Delays.config("set-itmp-flag", actorSystem.settings.config)

  def enrol(nino: String): Action[AnyContent] = desAuthorisedAction { implicit request ⇒
    withDelay(setItmpFlagDelayConfig) { () ⇒
      val response = if (nino.startsWith("HS403")) {
        logger.info("Received request to set ITMP flag: returning status 403 (FORBIDDEN)")
        Forbidden
      } else if (nino.startsWith("HS400")) {
        logger.info("Received request to set ITMP flag: returning status 400 (BAD REQUEST)")
        BadRequest
      } else if (nino.startsWith("HS404")) {
        logger.info("Received request to set ITMP flag: returning status 404 (NOT FOUND)")
        NotFound
      } else if (nino.startsWith("HS500")) {
        logger.info("Received request to set ITMP flag: returning status 500 (INTERNAL SERVER ERROR)")
        InternalServerError
      } else if (nino.startsWith("HS503")) {
        logger.info("Received request to set ITMP flag: returning status 503 (SERVICE UNAVAILABLE)")
        ServiceUnavailable
      } else if (nino.startsWith("TM04")) {
        logger.info("Received request to set ITMP flag: returning status 200 (OK) after timeout")
        Thread.sleep(90000)
        Ok
      } else {
        logger.info("Received request to set ITMP flag: returning status 200 (OK)")
        Ok
      }

      withDesCorrelationID(response)
    }
  }
}
