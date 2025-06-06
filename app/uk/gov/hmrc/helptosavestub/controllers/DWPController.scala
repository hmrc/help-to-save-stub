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

import cats.instances.string.*
import cats.syntax.eq.*
import com.google.inject.Inject
import org.apache.pekko.actor.{ActorSystem, Scheduler}
import org.scalacheck.Gen
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.controllers.DWPController.UCDetails
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.helptosavestub.util.{Delays, Logging}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import scala.concurrent.ExecutionContext

class DWPController @Inject()(actorSystem: ActorSystem, cc: ControllerComponents)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends BackendController(cc)
    with Logging
    with DWPEligibilityBehaviour
    with Delays {

  val scheduler: Scheduler = actorSystem.scheduler

  val checkUCStatusDelayConfig: DelayConfig = Delays.config("check-uc-status")

  val ucGen: Gen[UCDetails] = {
    val booleanGen = Gen.oneOf("Y", "N")
    for {
      c <- booleanGen
      w <- if (c === "Y") { booleanGen.map(Some(_)) } else { Gen.const(None) }
    } yield UCDetails(c, w)
  }

  def dwpClaimantCheck(
    nino: String,
    systemId: String,
    thresholdAmount: Double,
    transactionId: Option[UUID]): Action[AnyContent] = Action.async { _ =>
    withDelay(checkUCStatusDelayConfig) { () =>
      logger.info(
        s"The following details were passed into dwpClaimantCheck: nino: $nino, systemId: $systemId, " +
          s"thresholdAmount: $thresholdAmount, transactionId: $transactionId")

      if (nino.startsWith("WS")) {
        getHttpStatus(nino)
      } else if (nino.startsWith("WT")) {
        Thread.sleep(43000) // scalastyle:ignore magic.number
        Ok("Timeout")
      } else {
        getProfile(nino).fold(Ok(Json.toJson(randomUCDetails()))) {
          _.uCDetails.fold[Result](InternalServerError)(ucDetails => Ok(Json.toJson(ucDetails)))
        }
      }
    }
  }

  def randomUCDetails(): UCDetails = ucGen.sample.getOrElse(sys.error(""))

  private def getHttpStatus(nino: String): Result = {
    val result = nino.substring(2, 5).toInt // scalastyle:ignore magic.number
    Status(result)
  }
}

object DWPController {

  case class UCDetails(ucClaimant: String, withinThreshold: Option[String])

  object UCDetails {
    implicit val format: Format[UCDetails] = Json.format[UCDetails]
  }
}
