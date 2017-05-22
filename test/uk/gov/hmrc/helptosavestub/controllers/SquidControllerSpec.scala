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
import uk.gov.hmrc.helptosavestub.Constants._

import scala.concurrent.Future

class SquidControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
  val injector = fakeApplication.injector
  def messagesApi = injector.instanceOf[MessagesApi]
  def noCAKeyMap = Map[String, Map[String, String]]("wibble" -> Map())
  def noCAKeyJson = Json.toJson(noCAKeyMap)

  def generateJson(variants: Seq[(String, String)] = Seq()): JsValue = {
    val goodCreateAccountMap: Map[String, String] =
      Map("forename" -> "Donald",
        "surname" -> "Duck",
        "address1" -> "1",
        "address2" -> "Test Street 2",
        "address3" -> "Test Place 3",
        "address4" -> "Test Place 4",
        "address5" -> "Test Place 5",
        "postcode" -> "AB12 3CD",
        "countryCode" -> "GB",
        "NINO" -> "AA999999A",
        "CommunicationPreference" -> "02",
        "phoneNumber" -> "+44111 111 111",
        "emailAddress" -> "dduck@email.com",
        "registrationChannel" -> "online")
    Json.toJson(Map("createAccount" -> (goodCreateAccountMap ++ variants)))
  }

  def fakeRequest = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(generateJson())

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
    errorMessageId shouldBe Some(NO_JSON_ERROR_CODE)
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

  "If the stub is sent a request with no JSON content it should have an Error object in the response with the " +
    "error detail set to message site.no-json-detail" in {
    val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account")
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.no-json-detail")
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
    errorMessageId shouldBe Some(NO_CREATEACCOUNTKEY_ERROR_CODE)
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
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.no-create-account-key-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER400NNNL (where N is" +
    "number and L is letter, generate a bad request" in {
    val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER400456M")))
    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER400)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 400
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER400NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the errorCode set to PRECANNED_ERROR_CODE" in {
    val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER400456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER400)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER400NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the error message set to site.pre-canned-error" in {
    val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER400456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER400)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER400NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the error detail set to site.pre-canned-error-detail" in {
    val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER400456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER400)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER401NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER401456M")))
    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER400)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 401
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER401NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the errorCode set to PRECANNED_ERROR_CODE" in {
    val jsonBeginningWithER401 = generateJson(Seq(("NINO", "ER401456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER401)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER401NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the error message set to site.pre-canned-error" in {
    val jsonBeginningWithER401 = generateJson(Seq(("NINO", "ER401456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER401)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER401NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the error detail set to site.pre-canned-error-detail" in {
    val jsonBeginningWithER401 = generateJson(Seq(("NINO", "ER401456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER401)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER403NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER403 = generateJson(Seq(("NINO", "ER403456M")))
    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER403)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 403
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER403NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the errorCode set to PRECANNED_ERROR_CODE" in {
    val jsonBeginningWithER403 = generateJson(Seq(("NINO", "ER403456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER403)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER403NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the error message set to site.pre-canned-error" in {
    val jsonBeginningWithER403 = generateJson(Seq(("NINO", "ER403456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER403)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER403NNNL (where N is" +
    "number and L is letter, generate an error object in the response with the error detail set to site.pre-canned-error-detail" in {
    val jsonBeginningWithER403 = generateJson(Seq(("NINO", "ER403456M")))

    def fakeRequestWithBadContent = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(jsonBeginningWithER403)

    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    val json: JsValue = contentAsJson(result)
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }
}
