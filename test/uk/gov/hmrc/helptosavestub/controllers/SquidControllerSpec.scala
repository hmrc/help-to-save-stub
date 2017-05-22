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

import play.api.test._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import play.api.libs.json.{JsDefined, JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SquidControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  def fakeRequest = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(Json.parse("""{"createAccount": {}}"""))
  val injector = fakeApplication.injector
  def messagesApi = injector.instanceOf[MessagesApi]
  def noCAKeyMap = Map[String, Map[String, String]]("wibble" -> Map())
  def noCAKeyJson = Json.toJson(noCAKeyMap)

  "Squid Controller" must {
    "return 200 when requested" in {
      val result = new SquidController(messagesApi).createAccount()(fakeRequest)
      status(result) shouldBe 200
    }
  }

  "The request should return some content" in {
    //Note that if this is returning a blank page, the controller configuration in application.conf is broken
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result)
    val len = result.body.contentLength.getOrElse(0L)
    len shouldBe 0
  }

  "If the stub is sent a request with no JSON content it should return a 400" in {
    val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account")
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result) shouldBe 400
  }

  "If the stub is sent a request with no JSON content it should have an Error object in the response with the errorMessageId set as AAAA0002" in {
    val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account")
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some("AAAA0002")
  }

  "If the stub is sent a request with no JSON content it should have an Error object in the response with the " +
    "error message set to message site.no-json" in {
    val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account")
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.no-json")
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should Return a 400" in {
    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(noCAKeyJson)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 400
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
    "object in the response with the errorMessageId set as AAAA0003" in {
    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(noCAKeyJson)
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some("AAAA0003")
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
    "object in the response with the Error message set to message site.no-create-account-key" in {
    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(noCAKeyJson)
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.no-create-account-key")
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
    "object in the response with the Error detail set to message site.no-create-account-key-detail" in {
    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(noCAKeyJson)
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.no-create-account-key-detail")
  }
}
