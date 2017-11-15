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

import cats.instances.list._
import cats.instances.string._
import cats.syntax.eq._
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController

// trait for controllers mimicking DES
trait DESController { this: BaseController with Logging ⇒

  private val expectedAuthorisationHeader = List("Bearer: test")

  def desAuthorisedAction(body: Request[AnyContent] ⇒ Result): Action[AnyContent] = Action { request ⇒
    val authorisationHeaders = request.headers.getAll("Authorization")

    if (authorisationHeaders.toList === expectedAuthorisationHeader) {
      body(request)
    } else {
      logger.warn(s"Request did not contain expected authorisation header. Received: $authorisationHeaders")
      Unauthorized
    }
  }

}
