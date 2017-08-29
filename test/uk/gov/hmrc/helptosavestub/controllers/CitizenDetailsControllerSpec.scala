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

import java.util.concurrent.TimeUnit.SECONDS

import akka.stream.Materializer
import akka.util.Timeout
import play.api.http.Status.OK
import play.api.libs.json.{Json, Reads}
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsJson
import uk.gov.hmrc.helptosavestub.controllers.CitizenDetailsController.{Address, Person, Response}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CitizenDetailsControllerSpec extends UnitSpec with WithFakeApplication {

  implicit val materializer: Materializer = fakeApplication.materializer
  implicit val timeout = Timeout(5, SECONDS)

  implicit val personReads: Reads[Person] = Json.reads[Person]

  implicit val addressReads: Reads[Address] = Json.reads[Address]

  implicit val responseReads: Reads[Response] = Json.reads[Response]

  val nino = "AE123456A"

  "GET /citizen-details/:nino/designatory-details" should {
    "return a successful response" in {
      val request = FakeRequest("GET", s"/citizen-details/$nino/designatory-details")

      val result = CitizenDetailsController.retrieveDetails(nino)(request)
      status(result) shouldBe OK

      val response = Json.fromJson[Response](contentAsJson(result))

      response.get.person.get.dateOfBirth should be(defined)
    }
  }

}
