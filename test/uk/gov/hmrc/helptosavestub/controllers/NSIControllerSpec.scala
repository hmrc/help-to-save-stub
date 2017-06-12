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

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Base64

import play.api.libs.json.{JsString, JsValue, Json, Writes}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.helptosavestub.models.CreateAccount
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class NSIControllerSpec extends UnitSpec with WithFakeApplication {

  val testCreateAccount = CreateAccount(
    "Donald", "Duck", LocalDate.of(1990, 1, 1), "1", ",Test Street 2", None,
    None, None, "BN124XH", Some("GB"), "AA999999A",
    "02",None, "online", Some("dduck@email.com"))

  val authHeader = {
    val encoded = new String(Base64.getEncoder().encode("user:password".getBytes(StandardCharsets.UTF_8)))
    "Authorization1" → s"Basic: $encoded"
  }

  "Post /create-account  " should {
    "return a successful Create Account" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
        .withJsonBody(Json.toJson(testCreateAccount))

      val result = NSIController.createAccount()(request)
      status(result) shouldBe CREATED
    }

    "return a 401  UNAUTHORIZED" in {
      val request = FakeRequest()
        .withJsonBody(Json.toJson(testCreateAccount))
      val result = NSIController.createAccount()(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 for a bad request in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
        .withJsonBody(Json.toJson(testCreateAccount.copy(emailAddress = None)))
      val result = NSIController.createAccount()(request)
      status(result) shouldBe BAD_REQUEST
    }
  }
}
