/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.helptosavestub.controllers.BARSController.BankDetails
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

class BARSController @Inject()(cc: ControllerComponents) extends BackendController(cc) with BankDetailsBehaviour {

  def validateBankDetails: Action[AnyContent] = Action { implicit request ⇒
    request.body.asJson.fold(BadRequest("No JSON in body")) { json ⇒
      (json \ "account").validate[BankDetails] match {
        case play.api.libs.json.JsError(errors) ⇒
          BadRequest(s"Could not parse JSON: ${errors.mkString(";")}")

        case play.api.libs.json.JsSuccess(bankDetails, _) ⇒
          getBankProfile(bankDetails).barsResponse.fold[Result](
            InternalServerError
          ) { bars ⇒
            Ok(Json.toJson(bars))
          }
      }
    }

  }

}

object BARSController {

  case class BankDetails(sortCode: String, accountNumber: String)

  case class BARSResponse(accountNumberWithSortCodeIsValid: String, sortCodeIsPresentOnEISCD: String)

  implicit val reads: Reads[BankDetails] = Json.reads[BankDetails]

  implicit val writes: Writes[BARSResponse] = Json.writes[BARSResponse]

}
