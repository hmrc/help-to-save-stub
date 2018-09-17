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

import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavestub.controllers.BARSController.{BankDetails, Response}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

class BARSController extends BaseController {

  def validateBankDetails: Action[AnyContent] = Action { implicit request ⇒
    request.body.asJson.fold(BadRequest("No JSON in body")){ json ⇒
      json.validate[BankDetails] match {
        case play.api.libs.json.JsError(errors) ⇒
          BadRequest(s"Could not parse JSON: ${errors.mkString(";")}")

        case play.api.libs.json.JsSuccess(_, _) ⇒
          Ok(Json.toJson(Response(true)))

      }

    }

  }

}

object BARSController {

  case class BankDetails(sortCode: String, accountNumber: String)

  case class Response(accountNumberWithSortCodeIsValid: Boolean)

  implicit val reads: Reads[BankDetails] = Json.reads[BankDetails]

  implicit val writes: Writes[Response] = Json.writes[Response]

}
