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

import akka.util.ByteString
import play.api.test._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import play.api.libs.json.{JsDefined, JsValue, Json}
import play.api.libs.streams.Accumulator
import play.api.mvc.{AnyContentAsEmpty, Result}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.helptosavestub.Constants._

import scala.concurrent.Future

class SquidControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
  val injector = fakeApplication.injector
  def messagesApi = injector.instanceOf[MessagesApi]
  def noCAKeyMap = Map[String, Map[String, String]]("wibble" -> Map())
  def noCAKeyJson = Json.toJson(noCAKeyMap)

  val goodCreateAccountMap: Map[String, String] =
    Map("forename" -> "Donald",
      "surname" -> "Duck",
      "address1" -> "1",
      "address2" -> "Test Street 2",
      "address3" -> "Test Place 3",
      "address4" -> "Test Place 4",
      "address5" -> "Test Place 5",
      "postcode" -> "GIR 0AA",
      "countryCode" -> "GB",
      "NINO" -> "AA999999A",
      "birthDate" ->"19920509",
      "communicationPreference" -> "02",
      "phoneNumber" -> "+44111 111 111",
      "emailAddress" -> "dduck@email.com",
      "registrationChannel" -> "online")

  def generateJson(variants: Seq[(String, String)] = Seq()): JsValue = {
    Json.toJson(Map("createAccount" -> (goodCreateAccountMap ++ variants)))
  }

  def generateJsonFromMap(m: Map[String, String]): JsValue = {
    Json.toJson(Map("createAccount" -> m))
  }

  def fakeRequest = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(generateJson()).withHeaders((CONTENT_TYPE, "application/json"))

  def makeFakeRequest(json: JsValue) = FakeRequest("POST", "/help-to-save-stub/create-account").withJsonBody(json).withHeaders((CONTENT_TYPE, "application/json"))

  def buildRequest(json: JsValue) = FakeRequest(
    "POST",
    "/help-to-save-stub/create-account",
    FakeHeaders(),
    json.toString())

  "Squid Controller" must {

    "return 415 if the mime type is not application/json" in {
      val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account")
      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
      status(result) shouldBe 415
    }

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
    val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account").withHeaders((CONTENT_TYPE, "application/json"))
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result) shouldBe 400
  }

  "If the stub is sent a request with no JSON content it should have an Error object in the response with the errorMessageId set as AAAA0002" in {
    val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account").withHeaders((CONTENT_TYPE, "application/json"))
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(NO_JSON_ERROR_CODE)
  }

  "If the stub is sent a request with no JSON content it should have an Error object in the response with the " +
    "error message set to message site.no-json" in {
    val fakeRequestWithoutJson = FakeRequest("POST", "/help-to-save-stub/create-account").withHeaders((CONTENT_TYPE, "application/json"))
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.no-json")
  }

  "If the stub is sent a request with no JSON content it should have an Error object in the response with the " +
    "error detail set to message site.no-json-detail" in {
    val fakeRequestWithoutJson: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/help-to-save-stub/create-account").withHeaders((CONTENT_TYPE, "application/json"))
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.no-json-detail")
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should Return a 400" in {
    def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 400
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
    "object in the response with the errorMessageId set as AAAA0003" in {
    def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(NO_CREATEACCOUNTKEY_ERROR_CODE)
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
    "object in the response with the Error message set to message site.no-create-account-key" in {
    def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.no-create-account-key")
  }

  "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
    "object in the response with the Error detail set to message site.no-create-account-key-detail" in {
    def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)
    val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result)
    val json: JsValue = contentAsJson(result)
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.no-create-account-key-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER400NNNL (where N is" +
    "number and L is letter, generate a bad request" in {
    val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER400456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER400)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 400
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER401NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER401456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER400)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 401
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER403NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER403 = generateJson(Seq(("NINO", "ER403456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER403)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 403
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER404NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER404 = generateJson(Seq(("NINO", "ER404456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER404)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 404
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER405NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER405 = generateJson(Seq(("NINO", "ER405456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER405)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 405
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER415NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER415 = generateJson(Seq(("NINO", "ER415456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER415)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 415
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER500NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER500 = generateJson(Seq(("NINO", "ER500456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER500)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 500
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER503NNNL (where N is" +
    "number and L is letter, generate an Unauthorized" in {
    val jsonBeginningWithER503 = generateJson(Seq(("NINO", "ER503456M")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER503)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 503
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(PRECANNED_RESPONSE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.pre-canned-error-detail")
  }

  "if the stub is sent some good JSON that can not be parsed into a CreateAccount case class then return an" +
    "UNABLE_TO_PARSE_COMMAND_ERROR_CODE error code" in {
    val mapWithoutForename = goodCreateAccountMap - "forename"
    def fakeRequest = makeFakeRequest(generateJsonFromMap(mapWithoutForename))
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 400
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(UNABLE_TO_PARSE_COMMAND_ERROR_CODE)
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 1)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "GIR 0AA")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 2)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "P09 WW")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 3)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "U009 DD")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 4)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB99 HH")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 5)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB990 HH")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 6)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB9E9 HH")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 7)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB9E9 HH")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 8)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "Z1 ZZ")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 9)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "AA1 ZZ")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 10)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "ZZZ1 ZZ")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 11)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "ZZZZ1 ZZ")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 12)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 9")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 13)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 99")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 14)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 999")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 15)" in {
    val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 9999")))
    def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequest)
    status(result) shouldBe 200
  }

  "The following real postcodes should all pass:" in {
    for (pc <- Seq("BFPO 9999", "BN44 1ST", "E98 1SN", "EH99 1SP", "BS98 1TL")) {
      var jsonWithGoodPostcode = generateJson(Seq(("postcode", pc)))
      var fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
      var result = new SquidController(messagesApi).createAccount()(fakeRequest)
      status(result) shouldBe 200
    }
  }

  "None of the following strings are postcodes" in {
    for (pc <- Seq("wibble", "DOG", "bn44 3dh", "Q99 MM")) {
      var jsonWithGoodPostcode = generateJson(Seq(("postcode", pc)))
      var fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
      var result = new SquidController(messagesApi).createAccount()(fakeRequest)
      status(result) shouldBe 400
    }
  }

  "if the stub is sent JSON with a forename with leading spaces, a bad request is returned with:" +
    "FORENAME_LEADING_SPACES_ERROR_CODE (ZYRA0703) as the error code and site.leading-spaces-forename and " +
    "site.leading-spaces-forename-detail are returned in the error JSON" +
    "and the appropriate message" in {
    val badJson = generateJson(Seq(("forename", "    The Donald")))
    def fakeRequestWithBadContent = makeFakeRequest(badJson)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 400
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(FORENAME_LEADING_SPACES_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.leading-spaces-forename")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.leading-spaces-forename-detail")
  }

  "if the stub is sent JSON with a forename with numeric characters, a bad request is returned with:" +
    "FORENAME_NUMERIC_CHARS_ERROR_CODE (ZYRA0705) as the error code and site.numeric-chars-forename and " +
    "site.numeric-chars-forename-detail are returned in the error JSON" +
    "and the appropriate message" in {
    val badJson = generateJson(Seq(("forename", "D0n4ld")))
    def fakeRequestWithBadContent = makeFakeRequest(badJson)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 400
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(FORENAME_NUMERIC_CHARS_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.numeric-chars-forename")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.numeric-chars-forename-detail")
  }

  "if the stub is sent JSON with invalid formatted postcode an error object is returned with code set to INVALID_POSTCODE_ERROR_CODE," +
    " the message site.invalid-postcode, and the detail site.invalid-postcode-detail" in {
    val jsonWithBadPostCode = generateJson(Seq(("postcode", "678889098")))
    def fakeRequestWithBadContent = makeFakeRequest(jsonWithBadPostCode)
    val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
    status(result) shouldBe 400
    val json: JsValue = contentAsJson(result)
    val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
    errorMessageId shouldBe Some(INVALID_POSTCODE_ERROR_CODE)
    val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
    errorMessage.getOrElse("") shouldBe messagesApi("site.invalid-postcode")
    val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
    errorDetail.getOrElse("") shouldBe messagesApi("site.invalid-postcode-detail")
  }
}
