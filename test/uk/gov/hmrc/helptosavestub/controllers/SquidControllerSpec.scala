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
import play.api.i18n.MessagesApi
import play.api.libs.json._
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.helptosavestub.Constants._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import scala.concurrent.Future

object SquidControllerSpec {
  private case class TestError(errorMessageId: String, errorMessage: String, errorDetail: String)

  private object TestError {
    implicit val formatError: Format[TestError] = Json.format[TestError]
  }

  private case class TestErrorWrapper(error: TestError)

  private object TestErrorWrapper {
    implicit val formatWrapper: Format[TestErrorWrapper] = Json.format[TestErrorWrapper]
  }
}

class SquidControllerSpec extends UnitSpec with WithFakeApplication {
  import SquidControllerSpec._

  private val injector = fakeApplication.injector

  private val messagesApi = injector.instanceOf[MessagesApi]

  private val noCAKeyMap = Map[String, Map[String, String]]("wibble" -> Map())

  private val noCAKeyJson = Json.toJson(noCAKeyMap)

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

  private val squidController = new SquidController(messagesApi)

  private def generateJson(variants: Seq[(String, String)] = Seq()): JsValue = {
    Json.toJson(Map("createAccount" -> (goodCreateAccountMap ++ variants)))
  }

  private def generateJsonFromMap(m: Map[String, String]): JsValue = {
    Json.toJson(Map("createAccount" -> m))
  }

  private val fakeRequest = FakeRequest().withJsonBody(generateJson()).withHeaders((CONTENT_TYPE, "application/json"))

  private def makeFakeRequest(json: JsValue) = FakeRequest().withJsonBody(json).withHeaders((CONTENT_TYPE, "application/json"))

  private def buildRequest(json: JsValue) = FakeRequest("", "", FakeHeaders(), json.toString())

  "Squid Controller" must {

    "return 415 if the mime type is not application/json" in {
      val fakeRequestWithoutJson = FakeRequest()
      val result = squidController.createAccount()(fakeRequestWithoutJson)
      status(result) shouldBe Status.UNSUPPORTED_MEDIA_TYPE
    }

    "return Status.CREATED when requested" in {
      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "The request should return some content" in {
      //Note that if this is returning a blank page, the controller configuration in application.conf is broken
      val result = squidController.createAccount()(fakeRequest)
      status(result)
      val len = result.body.contentLength.getOrElse(0L)
      len shouldBe 0
    }

    "If the stub is sent a request with no JSON content it should return a 400" in {
      val fakeRequestWithoutJson = FakeRequest().withHeaders((CONTENT_TYPE, "application/json"))
      val result = squidController.createAccount()(fakeRequestWithoutJson)
      status(result) shouldBe Status.BAD_REQUEST
    }

    "If the stub is sent a request with no JSON content it should have an Error object in the response with the errorMessageId set as AAAA0002" in {
      val fakeRequestWithoutJson = FakeRequest().withHeaders((CONTENT_TYPE, "application/json"))
      val result: Future[Result] = squidController.createAccount()(fakeRequestWithoutJson)
      status(result)
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) => w.error.errorMessageId shouldBe NO_JSON_ERROR_CODE
        case JsError(_) => fail
      }
    }

    "If the stub is sent a request with no JSON content it should have an Error object in the response with the " +
      "error message set to message site.no-json" in {
      val fakeRequestWithoutJson = FakeRequest().withHeaders((CONTENT_TYPE, "application/json"))
      val result: Future[Result] = squidController.createAccount()(fakeRequestWithoutJson)
      status(result)
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) => w.error.errorMessage shouldBe messagesApi("site.no-json")
        case JsError(_) => fail
      }
    }

    "If the stub is sent a request with no JSON content it should have an Error object in the response with the " +
      "error detail set to message site.no-json-detail" in {
      val fakeRequestWithoutJson: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders((CONTENT_TYPE, "application/json"))
      val result: Future[Result] = squidController.createAccount()(fakeRequestWithoutJson)
      status(result)
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) => w.error.errorDetail shouldBe messagesApi("site.no-json-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that does not contain a createAccount key at the top level it should Return a 400" in {
      def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)
      val result = new SquidController(messagesApi).createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST
    }

    "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
      "object in the response with the Error message set to message site.no-create-account-key" in {
      def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)
      val result: Future[Result] = squidController.createAccount()(fakeRequestWithBadContent)
      status(result)
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) => w.error.errorMessage shouldBe messagesApi("site.no-create-account-key")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that does not contain a createAccount key at the top level it should have an Error " +
      "object in the response with the Error detail set to message site.no-create-account-key-detail" in {
      def fakeRequestWithBadContent = makeFakeRequest(noCAKeyJson)
      val result: Future[Result] = squidController.createAccount()(fakeRequestWithBadContent)
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) => w.error.errorDetail shouldBe messagesApi("site.no-create-account-key-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER400NNNL (where N is" +
      "number and L is letter, generate a bad request" in {
      val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER400456M")))

      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER400)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER401NNNL (where N is" +
      "number and L is letter, generate an Unauthorized" in {
      val jsonBeginningWithER400 = generateJson(Seq(("NINO", "ER401456M")))
      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER400)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.UNAUTHORIZED
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER403NNNL (where N is" +
      "number and L is letter, generate an Unauthorized" in {
      val jsonBeginningWithER403 = generateJson(Seq(("NINO", "ER403456M")))
      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER403)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.FORBIDDEN

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER404NNNL (where N is" +
      "number and L is letter, generate an Not Found" in {
      val jsonBeginningWithER404 = generateJson(Seq(("NINO", "ER404456M")))

      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER404)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.NOT_FOUND

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER405NNNL (where N is" +
      "number and L is letter, generate an Method Not Allowed" in {
      val jsonBeginningWithER405 = generateJson(Seq(("NINO", "ER405456M")))

      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER405)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.METHOD_NOT_ALLOWED
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER415NNNL (where N is" +
      "number and L is letter, generate an Unsupported Media Type" in {
      val jsonBeginningWithER415 = generateJson(Seq(("NINO", "ER415456M")))

      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER415)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER500NNNL (where N is" +
      "number and L is letter, generate an Internal Server Error" in {
      val jsonBeginningWithER500 = generateJson(Seq(("NINO", "ER500456M")))

      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER500)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe INTERNAL_SERVER_ERROR
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent with JSon that contains a good createAccount command and has a NINO matching ER503NNNL (where N is" +
      "number and L is letter, generate an Service Unavailable" in {
      val jsonBeginningWithER503 = generateJson(Seq(("NINO", "ER503456M")))

      def fakeRequestWithBadContent = makeFakeRequest(jsonBeginningWithER503)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.SERVICE_UNAVAILABLE
      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PRECANNED_RESPONSE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.pre-canned-error"))
          w.error.errorMessage shouldBe messagesApi("site.pre-canned-error")
          assert(messagesApi.isDefinedAt("site.pre-canned-error-detail"))
          w.error.errorDetail shouldBe messagesApi("site.pre-canned-error-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent some good JSON that can not be parsed into a CreateAccount case class then return an" +
      " UNABLE_TO_PARSE_COMMAND_ERROR_CODE error code" in {
      val mapWithoutForename = goodCreateAccountMap - "forename"

      def fakeRequest = makeFakeRequest(generateJsonFromMap(mapWithoutForename))

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
      val json: JsValue = contentAsJson(result)
      val errorMessageId = (json \ "error" \ "errorMessageId").get.asOpt[String]
      errorMessageId shouldBe Some(UNABLE_TO_PARSE_COMMAND_ERROR_CODE)
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 1)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "GIR 0AA")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 2)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "P09 WW")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 3)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "U009 DD")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 4)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB99 HH")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 5)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB990 HH")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 6)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB9E9 HH")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 7)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BB9E9 HH")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 8)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "Z1 ZZ")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 9)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "AA1 ZZ")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 10)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "ZZZ1 ZZ")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 11)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "ZZZZ1 ZZ")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 12)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 9")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 13)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 99")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 14)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 999")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 15)" in {
      val jsonWithGoodPostcode = generateJson(Seq(("postcode", "BFPO 9999")))

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "The following real postcodes should all pass:" in {
      for (pc <- Seq("BFPO 9999", "BN44 1ST", "E98 1SN", "EH99 1SP", "BS98 1TL")) {
        var jsonWithGoodPostcode = generateJson(Seq(("postcode", pc)))
        var fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
        var result = squidController.createAccount()(fakeRequest)
        status(result) shouldBe Status.CREATED
      }
    }

    "None of the following strings are postcodes" in {
      for (pc <- Seq("wibble", "DOG", "bn44 3dh", "Q99 MM")) {
        var jsonWithGoodPostcode = generateJson(Seq(("postcode", pc)))
        var fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
        var result = squidController.createAccount()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
      }
    }

    "if the stub is sent JSON with a forename with leading spaces, a bad request is returned with:" +
      "LEADING_SPACES_ERROR_CODE (ZYRA0703) as the error code and site.leading-spaces-forename and " +
      "site.leading-spaces-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "    The Donald")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe LEADING_SPACES_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.leading-spaces-forename"))
          w.error.errorMessage shouldBe messagesApi("site.leading-spaces-forename")
          assert(messagesApi.isDefinedAt("site.leading-spaces-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.leading-spaces-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with numeric characters, a bad request is returned with:" +
      "NUMERIC_CHARS_ERROR_CODE (ZYRA0705) as the error code and site.numeric-chars-forename and " +
      "site.numeric-chars-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "D0n4ld")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe NUMERIC_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.numeric-chars-forename"))
          w.error.errorMessage shouldBe messagesApi("site.numeric-chars-forename")
          assert(messagesApi.isDefinedAt("site.numeric-chars-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.numeric-chars-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with disallowed special characters, a bad request is returned with:" +
      "DISSALLOWED_CHARS_ERROR_CODE (ZYRA0711) as the error code and site.disallowed-chars-forename and " +
      "site.disallowed-chars-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "Dona$%#d")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe DISALLOWED_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.disallowed-chars-forename"))
          w.error.errorMessage shouldBe messagesApi("site.disallowed-chars-forename")
          assert(messagesApi.isDefinedAt("site.disallowed-chars-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.disallowed-chars-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with disallowed special characters in the first position, a bad request is returned with:" +
      "FIRST_CHAR_SPECIAL_ERROR_CODE (ZYRA0712) as the error code and site.first-char-special-forename and " +
      "site.first-char-special-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "&Donald")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe FIRST_CHAR_SPECIAL_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.first-char-special-forename"))
          w.error.errorMessage shouldBe messagesApi("site.first-char-special-forename")
          assert(messagesApi.isDefinedAt("site.first-char-special-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.first-char-special-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with disallowed special characters in the last position, a bad request is returned with:" +
      "LAST_CHAR_SPECIAL_ERROR_CODE (ZYRA0713) as the error code and site.first-char-special-forename and " +
      "site.first-char-special-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("forename", "Donald-")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe LAST_CHAR_SPECIAL_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.last-char-special-forename"))
          w.error.errorMessage shouldBe messagesApi("site.last-char-special-forename")
          assert(messagesApi.isDefinedAt("site.last-char-special-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.last-char-special-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with too few alphabetic characters at the begining, a bad request is returned with:" +
      "TOO_FEW_INITIAL_ALPHA_ERROR_CODE (ZYRA0714) as the error code and site.too-few-initial-alpha-forename and " +
      "site.too-few-initial-alpha-forename-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 3
      val badJson = generateJson(Seq(("forename", "D&&onald")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe TOO_FEW_INITIAL_ALPHA_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-forename"))
          w.error.errorMessage shouldBe messagesApi("site.too-few-initial-alpha-forename")
          assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-few-initial-alpha-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with too few consecutive alphabetic characters, a bad request is returned with:" +
      "TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE (ZYRA0715) as the error code and site.too-few-consecutive-alpha-forename and " +
      "site.too-few-consecutive-alpha-forename-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 4
      val badJson = generateJson(Seq(("forename", "Don&l")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-forename"))
          w.error.errorMessage shouldBe messagesApi("site.too-few-consecutive-alpha-forename")
          assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-few-consecutive-alpha-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with too many consecutive special characters, a bad request is returned with:" +
      "TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE (ZYRA0716) as the error code and site.too-many-consecutive-special-forename and " +
      "site.too-many-consecutive-special-forename-detail are returned in the error JSON and the appropriate message." in {

      val badJson = generateJson(Seq(("forename", "Don--aldddd")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-forename"))
          w.error.errorMessage shouldBe messagesApi("site.too-many-consecutive-special-forename")
          assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-many-consecutive-special-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with leading spaces, a bad request is returned with:" +
      "LEADING_SPACES_ERROR_CODE (ZYRA0703) as the error code and site.leading-spaces-surname and " +
      "site.leading-spaces-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "     Duck")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe LEADING_SPACES_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.leading-spaces-surname"))
          w.error.errorMessage shouldBe messagesApi("site.leading-spaces-surname")
          assert(messagesApi.isDefinedAt("site.leading-spaces-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.leading-spaces-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with numeric characters, a bad request is returned with:" +
      "NUMERIC_CHARS_ERROR_CODE (ZYRA0705) as the error code and site.numeric-chars-surname and " +
      "site.numeric-chars-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "D0n4ld")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe NUMERIC_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.numeric-chars-surname"))
          w.error.errorMessage shouldBe messagesApi("site.numeric-chars-surname")
          assert(messagesApi.isDefinedAt("site.numeric-chars-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.numeric-chars-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with disallowed special characters, a bad request is returned with:" +
      "DISSALLOWED_CHARS_ERROR_CODE (ZYRA0711) as the error code and site.disallowed-chars-surname and " +
      "site.disallowed-chars-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "Duck$%#chesky")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe DISALLOWED_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.disallowed-chars-surname"))
          w.error.errorMessage shouldBe messagesApi("site.disallowed-chars-surname")
          assert(messagesApi.isDefinedAt("site.disallowed-chars-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.disallowed-chars-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with disallowed special characters in the first position, a bad request is returned with:" +
      "FIRST_CHAR_SPECIAL_ERROR_CODE (ZYRA0712) as the error code and site.first-char-special-surname and " +
      "site.first-char-special-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "&Duckchesky")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe FIRST_CHAR_SPECIAL_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.first-char-special-surname"))
          w.error.errorMessage shouldBe messagesApi("site.first-char-special-surname")
          assert(messagesApi.isDefinedAt("site.first-char-special-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.first-char-special-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with disallowed special characters in the last position, a bad request is returned with:" +
      "LAST_CHAR_SPECIAL_ERROR_CODE (ZYRA0713) as the error code and site.first-char-special-forename and " +
      "site.first-char-special-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJson(Seq(("surname", "Duckchesky-")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe LAST_CHAR_SPECIAL_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.last-char-special-surname"))
          w.error.errorMessage shouldBe messagesApi("site.last-char-special-surname")
          assert(messagesApi.isDefinedAt("site.last-char-special-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.last-char-special-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with too few alphabetic characters at the begining, a bad request is returned with:" +
      "TOO_FEW_INITIAL_ALPHA_ERROR_CODE (ZYRA0714) as the error code and site.too-few-initial-alpha-surname and " +
      "site.too-few-initial-alpha-surname-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 3
      val badJson = generateJson(Seq(("surname", "D&&uckchesky")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe TOO_FEW_INITIAL_ALPHA_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-surname"))
          w.error.errorMessage shouldBe messagesApi("site.too-few-initial-alpha-surname")
          assert(messagesApi.isDefinedAt("site.too-few-initial-alpha-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-few-initial-alpha-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with too few consecutive alphabetic characters, a bad request is returned with:" +
      "TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE (ZYRA0715) as the error code and site.too-few-consecutive-alpha-surname and " +
      "site.too-few-consecutive-alpha-surname-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 4
      val badJson = generateJson(Seq(("surname", "Don&ch")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-surname"))
          w.error.errorMessage shouldBe messagesApi("site.too-few-consecutive-alpha-surname")
          assert(messagesApi.isDefinedAt("site.too-few-consecutive-alpha-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-few-consecutive-alpha-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with too many consecutive special characters, a bad request is returned with:" +
      "TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE (ZYRA0716) as the error code and site.too-many-consecutive-special-surname and " +
      "site.too-many-consecutive-special-surname-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 2
      val badJson = generateJson(Seq(("surname", "duc--achesy")))

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-surname"))
          w.error.errorMessage shouldBe messagesApi("site.too-many-consecutive-special-surname")
          assert(messagesApi.isDefinedAt("site.too-many-consecutive-special-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-many-consecutive-special-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with invalid formatted postcode an error object is returned with code set to INVALID_POSTCODE_ERROR_CODE," +
      " the message site.invalid-postcode, and the detail site.invalid-postcode-detail" in {
      val jsonWithBadPostCode = generateJson(Seq(("postcode", "678889098")))

      def fakeRequestWithBadContent = makeFakeRequest(jsonWithBadPostCode)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe INVALID_POSTCODE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.invalid-postcode"))
          w.error.errorMessage shouldBe messagesApi("site.invalid-postcode")
          assert(messagesApi.isDefinedAt("site.invalid-postcode-detail"))
          w.error.errorDetail shouldBe messagesApi("site.invalid-postcode-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with an unparseable or badly formatted date and error object is returned with code set to UNPARSABLE_DATE_ERROR_CODE, (CWFDAT02) " +
      " the message site.unparsable-date, and the detail site.unparseable-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "YYYY1336")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe UNPARSABLE_DATE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.unparsable-date"))
          w.error.errorMessage shouldBe messagesApi("site.unparsable-date")
          assert(messagesApi.isDefinedAt("site.unparsable-date-detail"))
          w.error.errorDetail shouldBe messagesApi("site.unparsable-date-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a numeric string of length 8 and the month is not between 01 and 12, an object is " +
      "returned with code set to BAD_MONTH_DATE_ERROR_CODE, (CWFDAT04) " +
      " the message site.bad-month-date, and the detail site.bad-month-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "20011305")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe BAD_MONTH_DATE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.bad-month-date"))
          w.error.errorMessage shouldBe messagesApi("site.bad-month-date")
          assert(messagesApi.isDefinedAt("site.bad-month-date-detail"))
          w.error.errorDetail shouldBe messagesApi("site.bad-month-date-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a numeric string of length 8 and the day is not valid for the date and year, an object is " +
      "returned with code set to BAD_DAY_DATE_ERROR_CODE, (CWFDAT03) " +
      " the message site.bad-day-date, and the detail site.bad-day-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "20020229")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe BAD_DAY_DATE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.bad-day-date"))
          w.error.errorMessage shouldBe messagesApi("site.bad-day-date")
          assert(messagesApi.isDefinedAt("site.bad-month-date-detail"))
          w.error.errorDetail shouldBe messagesApi("site.bad-day-date-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a numeric string of but the century is before 1800 or after 2099, an object is " +
      "returned with code set to BAD_DAY_CENTURY_ERROR_CODE, (CWFDAT06) " +
      " the message site.bad-century-date, and the detail site.bad-century-date-detail" in {
      val badJson = generateJson(Seq(("birthDate", "21050215")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe BAD_CENTURY_DATE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.bad-century-date"))
          w.error.errorMessage shouldBe messagesApi("site.bad-century-date")
          assert(messagesApi.isDefinedAt("site.bad-century-date-detail"))
          w.error.errorDetail shouldBe messagesApi("site.bad-century-date-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with an unrecognized country code an object is " +
      "returned with code set to UNKNOWN_COUNTRY_CODE_ERROR_CODE, (TAR10005) " +
      " the message site.unknown-country-code, and the detail site.unknown-country-code-detail" in {
      val badJson = generateJson(Seq(("countryCode", "MJ")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe UNKNOWN_COUNTRY_CODE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.unknown-country-code"))
          w.error.errorMessage shouldBe messagesApi("site.unknown-country-code")
          assert(messagesApi.isDefinedAt("site.unknown-country-code-detail"))
          w.error.errorDetail shouldBe messagesApi("site.unknown-country-code-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with an badly formatted NINO an object is " +
      "returned with code set to BAD_NINO_ERROR_CODE, (ZYRC0508) " +
      " the message site.bad-nino, and the detail site.bad-nino-detail" in {
      val badJson = generateJson(Seq(("NINO", "THIS-IS-NOT-A-NINO")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe BAD_NINO_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.bad-nino"))
          w.error.errorMessage shouldBe messagesApi("site.bad-nino")
          assert(messagesApi.isDefinedAt("site.bad-nino-detail"))
          w.error.errorDetail shouldBe messagesApi("site.bad-nino-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a communicationPreference that is not one of 00, 02, then an object is " +
      "returned with code set to BAD_COMM_PREF_ERROR_CODE, (AAAA0005) " +
      " the message site.bad-comm-pref, and the detail site.bad-comm-pref-detail" in {
      val badJson = generateJson(Seq(("communicationPreference", "01")))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe BAD_COMM_PREF_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.bad-comm-pref"))
          w.error.errorMessage shouldBe messagesApi("site.bad-comm-pref")
          assert(messagesApi.isDefinedAt("site.bad-comm-pref-detail"))
          w.error.errorDetail shouldBe messagesApi("site.bad-comm-pref-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a communicationPreference that is 02 but without an email then an object is " +
      "returned with code set to EMAIL_NEEDED_ERROR_CODE, (ZYMC0004) " +
      " the message site.email-needed, and the detail site.email-needed-detail" in {
      val badJson = generateJsonFromMap(goodCreateAccountMap - "emailAddress")
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe EMAIL_NEEDED_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.email-needed"))
          w.error.errorMessage shouldBe messagesApi("site.email-needed")
          assert(messagesApi.isDefinedAt("site.email-needed-detail"))
          w.error.errorDetail shouldBe messagesApi("site.email-needed-detail")
        case JsError(_) => fail
      }
    }
  }
}
