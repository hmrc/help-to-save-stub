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
import play.api.libs.streams.Accumulator
import play.api.mvc.{AnyContentAsEmpty, Result}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.helptosavestub.Constants._

import scala.concurrent.Future

class SquidControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
  private val injector = fakeApplication.injector

  private def messagesApi = injector.instanceOf[MessagesApi]

  private def noCAKeyMap = Map[String, Map[String, String]]("wibble" -> Map())

  private def noCAKeyJson = Json.toJson(noCAKeyMap)

  private val goodCreateAccountMap: Map[String, String] =
    Map("forename" -> "Donald",
      "surname" -> "Duck",
      "address1" -> "1",
      "address2" -> "Test Street 2",
      "address3" -> "Test Place 3",
      "address4" -> "Test Place 4",
      "address5" -> "Test Place 5",
      "postcode" -> "GIR 0AA",
      "countryCode" -> "GB",
      "NINO" -> "WM123456C",
      "birthDate" -> "19920509",
      "communicationPreference" -> "02",
      "phoneNumber" -> "+44111 111 111",
      "emailAddress" -> "dduck@email.com",
      "registrationChannel" -> "online")

  private def generateJson(variants: Seq[(String, String)] = Seq()): JsValue = {
    Json.toJson(Map("createAccount" -> (goodCreateAccountMap ++ variants)))
  }

  private def generateJsonFromMap(m: Map[String, String]): JsValue = {
    Json.toJson(Map("createAccount" -> m))
  }

  private def fakeRequest = FakeRequest().withJsonBody(generateJson()).withHeaders((CONTENT_TYPE, "application/json"))

  private def makeFakeRequest(json: JsValue) = FakeRequest().withJsonBody(json).withHeaders((CONTENT_TYPE, "application/json"))

  private def buildRequest(json: JsValue) = FakeRequest("", "", FakeHeaders(), json.toString())

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
      assert(messagesApi.isDefinedAt("site.no-json"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.no-json")
    }

    "If the stub is sent a request with no JSON content it should have an Error object in the response with the " +
      "error detail set to message site.no-json-detail" in {
      val fakeRequestWithoutJson: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/help-to-save-stub/create-account").withHeaders((CONTENT_TYPE, "application/json"))
      val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithoutJson)
      status(result)
      val json: JsValue = contentAsJson(result)
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.no-json-detail"))
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
      assert(messagesApi.isDefinedAt("site.no-create-account-key"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.no-create-account-key")
    }

    "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
      "object in the response with the Error detail set to message site.no-create-account-key-detail" in {
      def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)

      val result: Future[Result] = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result)
      val json: JsValue = contentAsJson(result)
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.no-create-account-key-detail"))
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
      assert(messagesApi.isDefinedAt("site.pre-canned-error"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.pre-canned-error")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
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
      "LEADING_SPACES_ERROR_CODE (ZYRA0703) as the error code and site.leading-spaces-forename and " +
      "site.leading-spaces-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "    The Donald")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(LEADING_SPACES_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.leading-spaces-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.leading-spaces-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.leading-spaces-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.leading-spaces-forename-detail")
    }

    "if the stub is sent JSON with a forename with numeric characters, a bad request is returned with:" +
      "NUMERIC_CHARS_ERROR_CODE (ZYRA0705) as the error code and site.numeric-chars-forename and " +
      "site.numeric-chars-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "D0n4ld")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(NUMERIC_CHARS_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.numeric-chars-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.numeric-chars-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.numeric-chars-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.numeric-chars-forename-detail")
    }

    "if the stub is sent JSON with a forename with disallowed special characters, a bad request is returned with:" +
      "DISSALLOWED_CHARS_ERROR_CODE (ZYRA0711) as the error code and site.disallowed-chars-forename and " +
      "site.disallowed-chars-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "Dona$%#d")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(DISALLOWED_CHARS_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.disallowed-chars-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.disallowed-chars-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.disallowed-chars-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.disallowed-chars-forename-detail")
    }

    "if the stub is sent JSON with a forename with disallowed special characters in the first position, a bad request is returned with:" +
      "FIRST_CHAR_SPECIAL_ERROR_CODE (ZYRA0712) as the error code and site.first-char-special-forename and " +
      "site.first-char-special-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "&Donald")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(FIRST_CHAR_SPECIAL_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.first-char-special-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.first-char-special-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.first-char-special-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.first-char-special-forename-detail")
    }

    "if the stub is sent JSON with a forename with disallowed special characters in the last position, a bad request is returned with:" +
      "LAST_CHAR_SPECIAL_ERROR_CODE (ZYRA0713) as the error code and site.first-char-special-forename and " +
      "site.first-char-special-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "Donald-")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(LAST_CHAR_SPECIAL_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.last-char-special-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.last-char-special-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.last-char-special-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.last-char-special-forename-detail")
    }

    "if the stub is sent JSON with a forename with too few alphabetic characters at the begining, a bad request is returned with:" +
      "TOO_FEW_INITIAL_ALPHA_ERROR_CODE (ZYRA0714) as the error code and site.too-few-initial-alpha-forename and " +
      "site.too-few-initial-alpha-forename-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 3
      val badJson = generateJson(Seq(("forename", "D&&onald")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(TOO_FEW_INITIAL_ALPHA_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.too-few-initial-alpha-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.too-few-initial-alpha-forename-detail")
    }

    "if the stub is sent JSON with a forename with too few consecutive alphabetic characters, a bad request is returned with:" +
      "TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE (ZYRA0715) as the error code and site.too-few-consecutive-alpha-forename and " +
      "site.too-few-consecutive-alpha-forename-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 4
      val badJson = generateJson(Seq(("forename", "Don&l")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.too-few-consecutive-alpha-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.too-few-consecutive-alpha-forename-detail")
    }

    "if the stub is sent JSON with a forename with too many consecutive special characters, a bad request is returned with:" +
      "TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE (ZYRA0716) as the error code and site.too-many-consecutive-special-forename and " +
      "site.too-many-consecutive-special-forename-detail are returned in the error JSON and the appropriate message." in {

      val badJson = generateJson(Seq(("forename", "Don--aldddd")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      messagesApi.isDefinedAt("site.too-many-consecutive-special-forename") shouldBe true
      assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-forename"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.too-many-consecutive-special-forename")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      messagesApi.isDefinedAt("site.too-many-consecutive-special-forename-detail") shouldBe true
      assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-forename-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.too-many-consecutive-special-forename-detail")
    }

    "if the stub is sent JSON with a surname with leading spaces, a bad request is returned with:" +
      "LEADING_SPACES_ERROR_CODE (ZYRA0703) as the error code and site.leading-spaces-surname and " +
      "site.leading-spaces-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "     Duck")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(LEADING_SPACES_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.leading-spaces-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.leading-spaces-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.leading-spaces-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.leading-spaces-surname-detail")
    }

    "if the stub is sent JSON with a surname with numeric characters, a bad request is returned with:" +
      "NUMERIC_CHARS_ERROR_CODE (ZYRA0705) as the error code and site.numeric-chars-surname and " +
      "site.numeric-chars-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "D0n4ld")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(NUMERIC_CHARS_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.numeric-chars-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.numeric-chars-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.numeric-chars-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.numeric-chars-surname-detail")
    }

    "if the stub is sent JSON with a surname with disallowed special characters, a bad request is returned with:" +
      "DISSALLOWED_CHARS_ERROR_CODE (ZYRA0711) as the error code and site.disallowed-chars-surname and " +
      "site.disallowed-chars-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "Duck$%#chesky")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(DISALLOWED_CHARS_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.disallowed-chars-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.disallowed-chars-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.disallowed-chars-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.disallowed-chars-surname-detail")
    }

    "if the stub is sent JSON with a surname with disallowed special characters in the first position, a bad request is returned with:" +
      "FIRST_CHAR_SPECIAL_ERROR_CODE (ZYRA0712) as the error code and site.first-char-special-surname and " +
      "site.first-char-special-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "&Duckchesky")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(FIRST_CHAR_SPECIAL_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.first-char-special-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.first-char-special-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.first-char-special-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.first-char-special-surname-detail")
    }

    "if the stub is sent JSON with a surname with disallowed special characters in the last position, a bad request is returned with:" +
      "LAST_CHAR_SPECIAL_ERROR_CODE (ZYRA0713) as the error code and site.first-char-special-forename and " +
      "site.first-char-special-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "Duckchesky-")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(LAST_CHAR_SPECIAL_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.last-char-special-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.last-char-special-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.last-char-special-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.last-char-special-surname-detail")
    }

    "if the stub is sent JSON with a surname with too few alphabetic characters at the begining, a bad request is returned with:" +
      "TOO_FEW_INITIAL_ALPHA_ERROR_CODE (ZYRA0714) as the error code and site.too-few-initial-alpha-surname and " +
      "site.too-few-initial-alpha-surname-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 3
      val badJson = generateJson(Seq(("surname", "D&&uckchesky")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(TOO_FEW_INITIAL_ALPHA_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.too-few-initial-alpha-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.too-few-initial-alpha-surname-detail")
    }

    "if the stub is sent JSON with a surname with too few consecutive alphabetic characters, a bad request is returned with:" +
      "TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE (ZYRA0715) as the error code and site.too-few-consecutive-alpha-surname and " +
      "site.too-few-consecutive-alpha-surname-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 4
      val badJson = generateJson(Seq(("surname", "Don&ch")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.too-few-consecutive-alpha-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.too-few-consecutive-alpha-surname-detail")
    }

    "if the stub is sent JSON with a surname with too many consecutive special characters, a bad request is returned with:" +
      "TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE (ZYRA0716) as the error code and site.too-many-consecutive-special-surname and " +
      "site.too-many-consecutive-special-surname-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 2
      val badJson = generateJson(Seq(("surname", "duc--achesy")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-surname"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.too-many-consecutive-special-surname")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-surname-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.too-many-consecutive-special-surname-detail")
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
      assert(messagesApi.isDefinedAt("site.invalid-postcode"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.invalid-postcode")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.invalid-postcode-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.invalid-postcode-detail")
    }

    "if the stub is sent JSON with an unparseable or badly formatted date and error object is returned with code set to UNPARSABLE_DATE_ERROR_CODE, (CWFDAT02) " +
      " the message site.unparsable-date, and the detail site.unparseable-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "YYYY1336")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(UNPARSABLE_DATE_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.unparsable-date"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.unparsable-date")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.unparsable-date-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.unparsable-date-detail")
    }

    "if the stub is sent JSON with a numeric string of length 8 and the month is not between 01 and 12, an object is " +
      "returned with code set to BAD_MONTH_DATE_ERROR_CODE, (CWFDAT04) " +
      " the message site.bad-month-date, and the detail site.bad-month-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "20011305")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(BAD_MONTH_DATE_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-month-date"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.bad-month-date")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-month-date-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.bad-month-date-detail")
    }

    "if the stub is sent JSON with a numeric string of length 8 and the day is not valid for the date and year, an object is " +
      "returned with code set to BAD_DAY_DATE_ERROR_CODE, (CWFDAT03) " +
      " the message site.bad-day-date, and the detail site.bad-day-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "20020229")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(BAD_DAY_DATE_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-day-date"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.bad-day-date")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-day-date-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.bad-day-date-detail")
    }

    "if the stub is sent JSON with a numeric string of but the century is before 1800 or after 2099, an object is " +
      "returned with code set to BAD_DAY_CENTURY_ERROR_CODE, (CWFDAT06) " +
      " the message site.bad-century-date, and the detail site.bad-century-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "21050215")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(BAD_CENTURY_DATE_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-century-date"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.bad-century-date")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-century-date-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.bad-century-date-detail")
    }

    "if the stub is sent JSON with an unrecognized country code an object is " +
      "returned with code set to UNKNOWN_COUNTRY_CODE_ERROR_CODE, (TAR10005) " +
      " the message site.unknown-country-code, and the detail site.unknown-country-code-detail" in {
      val badJson = generateJson(Seq(("countryCode", "MJ")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(UNKNOWN_COUNTRY_CODE_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.unknown-country-code"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.unknown-country-code")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.unknown-country-code-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.unknown-country-code-detail")
    }

    "if the stub is sent JSON with an badly formatted NINO an object is " +
      "returned with code set to BAD_NINO_ERROR_CODE, (ZYRC0508) " +
      " the message site.bad-nino, and the detail site.bad-nino-detail" in {
      val badJson = generateJson(Seq(("NINO", "THIS-IS-NOT-A-NINO")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(BAD_NINO_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-nino"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.bad-nino")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-nino-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.bad-nino-detail")
    }

    "if the stub is sent JSON with a communicationPreference that is not one of 00, 02, then an object is " +
      "returned with code set to BAD_COMM_PREF_ERROR_CODE, (AAAA0005) " +
      " the message site.bad-comm-pref, and the detail site.bad-comm-pref-detail" in {
      val badJson = generateJson(Seq(("communicationPreference", "01")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(BAD_COMM_PREF_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-comm-pref"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.bad-comm-pref")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.bad-comm-pref-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.bad-comm-pref-detail")
    }

    "if the stub is sent JSON with a communicationPreference that is 02 but without an email then an object is " +
      "returned with code set to EMAIL_NEEDED_ERROR_CODE, (ZYMC0004) " +
      " the message site.email-needed, and the detail site.email-needed-detail" in {
      val badJson = generateJsonFromMap(goodCreateAccountMap - "emailAddress")
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe 400
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(EMAIL_NEEDED_ERROR_CODE)
      val errorMessage = (json \ "error" \ "errorMessage").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.email-needed"))
      errorMessage.getOrElse("") shouldBe messagesApi("site.email-needed")
      val errorDetail = (json \ "error" \ "errorDetail").get.asOpt[String]
      assert(messagesApi.isDefinedAt("site.email-needed-detail"))
      errorDetail.getOrElse("") shouldBe messagesApi("site.email-needed-detail")
    }
  }
}
