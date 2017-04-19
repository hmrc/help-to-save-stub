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

package uk.gov.hmrc.helptosavestub

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.helptosavestub.config.{Email, UserDetailsConnectorImpl}
import uk.gov.hmrc.play.test.WithFakeApplication

class UserDetailsConnectorSpec extends FlatSpec
  with Matchers
  with WireMockSupport
  with WithFakeApplication
  with ScalaFutures
  with WithHeaderCarrier {

  implicit val config: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(1, Seconds)), interval = scaled(Span(100, Millis)))

  behavior of "UserDetailsConnector"

  "GET getUserEmail" should
    "return successful response for a given user id" in {
    val id = "1"
    stubUserDetails(id)
    val result = new UserDetailsConnectorImpl().getEmail(id)
    result.futureValue should be(Email("test@test.com"))
  }
}
