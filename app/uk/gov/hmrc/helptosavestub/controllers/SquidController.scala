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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.microservice.controller.BaseController

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
      case None => BadRequest(Json.toJson(errorJson("AAAA0002", "site.no-json")))

      case Some(j: JsValue) => {
        val json = j.as[Map[String, JsValue]]
        if (json.size != 1 || (json.keys.toList.head != "createAccount")) {
          BadRequest(errorJson("AAAA0003", "site.no-create-account-key", "site.no-create-account-key-detail"))
        } else {
          Ok
        }
      }
    }
  }
}
