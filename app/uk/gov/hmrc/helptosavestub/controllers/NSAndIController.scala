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

import play.api.libs.json.{JsResultException, JsValue}
import play.api.mvc.Action
import uk.gov.hmrc.helptosavestub.models.CreateAccount
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future


object NSAndIController   extends BaseController{

  def  createAccount() = Action.async { implicit request =>
    val payload: JsValue = request.body.asJson match {
      case Some(json) => json
      case  _ => throw new Exception("No Json :(")
    }
    //todo make more funotional :( also add in the rest of the nsAndI stuff
     try{payload.as[CreateAccount] match {
       case c =>
         Future.successful(Created)
       case _ =>
         Future.successful(BadRequest)
     }} catch {
       case ex:Exception =>   Future.successful(BadRequest)
     }
  }
}
