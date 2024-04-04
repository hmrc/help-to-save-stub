/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.http.Status.{ACCEPTED, OK}
import play.api.test.FakeRequest

class EmailControllerSpec extends TestSupport {
  val emailController = new EmailController(testCC)
  "email controller" should {
    "return Accepted on send" in {
      val request = FakeRequest()
      val result = await(emailController.send()(request))
      status(result) should equal(ACCEPTED)
    }
    "return Ok on Unblock" in {
      val request = FakeRequest()
      val result = await(emailController.unblock("email")(request))
      status(result) should equal(OK)
    }
  }
}
