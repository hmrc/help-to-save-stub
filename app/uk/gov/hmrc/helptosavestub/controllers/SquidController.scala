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

import javax.inject.{Inject, Singleton}

import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.Constants._

@Singleton
class SquidController @Inject()(val messagesApi: MessagesApi) extends BaseController {

  private def errorJson(code: String, messageKey: String = "", detailKey: String = ""): JsValue = {
    val errorMap: Map[String, Map[String, String]] =
      Map("error" ->
        Map("errorMessageId" -> code,
          "errorMessage" -> messagesApi(messageKey),
          "errorDetail" -> messagesApi(detailKey)))
    Json.toJson(errorMap)
  }

  def createAccount(): Action[AnyContent] = Action { implicit request =>
    request.body.asJson match {
      case None => BadRequest(Json.toJson(errorJson(NO_JSON_ERROR_CODE, "site.no-json", "site.no-json-detail")))

      case Some(json: JsValue) => {
        if (json.asInstanceOf[JsObject].fields.size != 1 || (json.asInstanceOf[JsObject].fields.head._1 != "createAccount")) {
          BadRequest(errorJson(NO_CREATEACCOUNTKEY_ERROR_CODE, "site.no-create-account-key", "site.no-create-account-key-detail"))
        } else {
          //TODO: Add something to check Json against schema
          val nino = (json \ "createAccount" \ "NINO").get.asOpt[String]
          nino match {
            case Some(aNino) if (aNino.startsWith("ER400")) => BadRequest(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if (aNino.startsWith("ER401")) => Unauthorized(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if (aNino.startsWith("ER403")) => Forbidden(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if (aNino.startsWith("ER404")) => NotFound(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if (aNino.startsWith("ER405")) => MethodNotAllowed(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if (aNino.startsWith("ER415")) => UnsupportedMediaType(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if (aNino.startsWith("ER500")) => InternalServerError(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if (aNino.startsWith("ER503")) => ServiceUnavailable(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(n) => Ok
            case None => Ok
          }
        }
      }
    }
  }
}
