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

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.models.EligibilityResult
import uk.gov.hmrc.play.microservice.controller.BaseController

object EligibilityCheckController extends EligibilityCheckController

trait EligibilityCheckController extends BaseController {

  def eligibilityCheck(nino: String) = Action { implicit request =>
    val isEligible = nino match {
      case s if s.startsWith("AE") ⇒ true
      case _ ⇒ false
    }
    Ok(Json.toJson(EligibilityResult(isEligible)))
  }
}
