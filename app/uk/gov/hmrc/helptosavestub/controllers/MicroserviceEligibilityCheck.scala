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

import java.time.LocalDate

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.models.{ContactPreference, EligibilityResult, UserDetails}
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

object MicroserviceEligibilityCheck extends MicroserviceEligibilityCheck

trait MicroserviceEligibilityCheck extends BaseController {

	val user = UserDetails(
		"Bob Bobber",
		"12345678",
		LocalDate.now(),
		"bob@email.com",
		"0879371657",
		List("Happy land","happy street"),
		ContactPreference.Email
	)

	def eligibilityCheck(nino:String) = Action.async { implicit request =>
			Future.successful(Ok(Json.toJson(EligibilityResult(Some(user)))))
	}

}
