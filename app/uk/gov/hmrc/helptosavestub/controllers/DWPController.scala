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

import java.util.UUID

import akka.actor.{ActorSystem, Scheduler}
import cats.instances.string._
import cats.syntax.eq._
import com.google.inject.Inject
import org.scalacheck.Gen
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.helptosavestub.controllers.DWPController.UCDetails
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.helptosavestub.util.{Delays, Logging}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext

class DWPController @Inject() (actorSystem: ActorSystem)(implicit ec: ExecutionContext)
  extends BaseController with Logging with DWPEligibilityBehaviour with Delays {

  val scheduler: Scheduler = actorSystem.scheduler

  val checkUCStatusDelayConfig: DelayConfig = Delays.config("check-uc-status", actorSystem.settings.config)

  val ucGen: Gen[UCDetails] = {
    val booleanGen = Gen.oneOf("Y", "N")
    for {
      c ← booleanGen
      w ← if (c === "Y") { booleanGen.map(Some(_)) } else { Gen.const(None) }
    } yield UCDetails(c, w)
  }

  def randomUCDetails(): UCDetails = ucGen.sample.getOrElse(sys.error(""))

  val healthCheckResponse: JsValue =
    Json.parse("""
                 |{
                 |  "RequestService" : {
                 |    "healthy" : true
                 |  }
                 |}
               """.stripMargin)

  private def getHttpStatus(nino: String): Result = {
    val result = nino.substring(2, 5).toInt // scalastyle:ignore magic.number
    Status(result)
  }

  def dwpClaimantCheck(nino: String, systemId: String, thresholdAmount: Double, transactionId: Option[UUID]): Action[AnyContent] = Action.async {
    implicit request ⇒
      withDelay(checkUCStatusDelayConfig) { () ⇒
        logger.info(s"The following details were passed into dwpClaimantCheck: nino: $nino, systemId: $systemId, " +
          s"thresholdAmount: $thresholdAmount, transactionId: $transactionId")

        if (nino.startsWith("WS")) {
          getHttpStatus(nino)
        } else if (nino.startsWith("WT")) {
          Thread.sleep(43000) // scalastyle:ignore magic.number
          Ok("Timeout")
        } else {
          getProfile(nino).
            fold(Ok(Json.toJson(randomUCDetails()))) {
              _.uCDetails.fold[Result](InternalServerError)(ucDetails ⇒
                Ok(Json.toJson(ucDetails)))
            }
        }
      }
  }

  def dwpHealthCheck(): Action[AnyContent] = Action { implicit request ⇒
    logger.info("Responding to DWP health check with status 200")
    Ok(healthCheckResponse)
  }

}

object DWPController {

  case class UCDetails(ucClaimant: String, withinThreshold: Option[String])

  object UCDetails {
    implicit val format: Format[UCDetails] = Json.format[UCDetails]
  }
}
