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

import play.api.http.Status
import play.api.libs.json.{JsSuccess, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.helptosavestub.models.{CreateAccount, EligibilityResult}
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.libs.json._

class NSAndIControllerSpec extends UnitSpec with WithFakeApplication{
  val testCreateAccount = CreateAccount("Donald2","Duck","19900101","1",",Test Street 2",Some("Test Place 3"),
    Some("Test Place 4"),Some("Test Place 52"),"AB12 3CD",Some("GB"),"AA999999A","02",Some("+447111 111 111"),"online",Some("dduck@email.com"))
  "Post /" should {
    "return a successful Create Account" in {
      val request = FakeRequest()
          .withHeaders(("Authorization","Testing123"))

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
        .withHeaders(("Authorization","Testing123"))
        .withJsonBody(Json.toJson(testCreateAccount.toString))
      val result = NSIController.createAccount()(request)
      status(result) shouldBe BAD_REQUEST
    }
  }
}
