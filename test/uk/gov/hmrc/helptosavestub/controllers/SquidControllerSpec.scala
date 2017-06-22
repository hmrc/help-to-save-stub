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
import uk.gov.hmrc.helptosavestub.models.SquidModels.{AccountCommand, ContactDetails}
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

  private val goodAccount = {
    val contactDetails = ContactDetails(
      "1",
      "Test Street 2",
      Some("Test Place 3"),
      Some("Test Place 4"),
      Some("Test Place 5"),
      "GIR 0AA", Some("GB"),
      Some("dduck@email.com"),
      Some("+44111 111 111"),
     "02")

    AccountCommand("Donald",
      "Duck",
      "19920509",
      "WM123456C",
      contactDetails,
      "online")
  }

  private val squidController = new SquidController(messagesApi)

  private def generateJsonWithNino(n: String): JsValue = {
    Json.toJson(goodAccount copy (nino = n))
  }

  private def generateJsonWithForename(f: String): JsValue = {
    Json.toJson(goodAccount copy (forename = f))
  }

  private def generateJsonWithSurname(s: String): JsValue = {
    Json.toJson(goodAccount copy (surname = s))
  }

  private def generateJsonWithDateOfBirth(b: String): JsValue = {
    Json.toJson(goodAccount copy (dateOfBirth = b))
  }

  private def generateJsonWithPostcode(p: String): JsValue = {
    val cd = goodAccount.contactDetails copy (postcode = p)
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithCountryCode(cc: String): JsValue = {
    val cd = goodAccount.contactDetails copy (countryCode = Some(cc))
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithCommsPreference(cp: String): JsValue = {
    val cd = goodAccount.contactDetails copy (communicationPreference = cp)
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithNoEmail(): JsValue = {
    val cd = goodAccount.contactDetails copy (email = None)
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithAddress1(a: String): JsValue = {
    val cd = goodAccount.contactDetails copy (address1 = a)
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithAddress2(a: String): JsValue = {
    val cd = goodAccount.contactDetails copy (address2 = a)
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithAddress3(a: String): JsValue = {
    val cd = goodAccount.contactDetails copy (address3 = Some(a))
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithAddress4(a: String): JsValue = {
    val cd = goodAccount.contactDetails copy (address4 = Some(a))
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithAddress5(a: String): JsValue = {
    val cd = goodAccount.contactDetails copy (address5 = Some(a))
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithPhoneNumber(p: String): JsValue = {
    val cd = goodAccount.contactDetails copy (phoneNumber = Some(p))
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private def generateJsonWithEmailAddress(e: String): JsValue = {
    val cd = goodAccount.contactDetails copy (email = Some(e))
    Json.toJson(goodAccount copy (contactDetails = cd))
  }

  private val fakeRequest = FakeRequest().withJsonBody(Json.toJson(goodAccount)).withHeaders((CONTENT_TYPE, "application/json"))

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

    "if the stub is sent with JSon that contains a NINO matching ER400NNNL (where N is" +
      "number and L is letter, generate a bad request" in {
      val jsonBeginningWithER400 = generateJsonWithNino("ER400456M")

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

    "if the stub is sent with JSon that contains a NINO matching ER401NNNL (where N is" +
      "number and L is letter, generate an Unauthorized" in {
      val jsonBeginningWithER400 = generateJsonWithNino("ER401456M")
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

    "if the stub is sent with JSon that contains a NINO matching ER403NNNL (where N is" +
      "number and L is letter, generate an Unauthorized" in {
      val jsonBeginningWithER403 = generateJsonWithNino("ER403456M")
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

    "if the stub is sent with JSon that contains a NINO matching ER404NNNL (where N is" +
      "number and L is letter, generate an Not Found" in {
      val jsonBeginningWithER404 = generateJsonWithNino("ER404456M")

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

    "if the stub is sent with JSon that contains a NINO matching ER405NNNL (where N is" +
      "number and L is letter, generate an Method Not Allowed" in {
      val jsonBeginningWithER405 = generateJsonWithNino("ER405456M")

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

    "if the stub is sent with JSon that contains a NINO matching ER415NNNL (where N is" +
      "number and L is letter, generate an Unsupported Media Type" in {
      val jsonBeginningWithER415 = generateJsonWithNino("ER415456M")

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

    "if the stub is sent with JSon that contains a NINO matching ER500NNNL (where N is" +
      "number and L is letter, generate an Internal Server Error" in {
      val jsonBeginningWithER500 = generateJsonWithNino("ER500456M")

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

    "if the stub is sent with JSon that contains a NINO matching ER503NNNL (where N is" +
      "number and L is letter, generate an Service Unavailable" in {
      val jsonBeginningWithER503 = generateJsonWithNino("ER503456M")

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

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 1)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("GIR 0AA")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 2)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("P09 WW")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 3)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("U009 DD")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 4)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BB99 HH")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 5)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BB990 HH")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 6)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BB9E9 HH")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 7)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BB9E9 HH")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 8)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("Z1 ZZ")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 9)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("AA1 ZZ")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 10)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("ZZZ1 ZZ")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 11)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("ZZZZ1 ZZ")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 12)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BFPO 9")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 13)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BFPO 99")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 14)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BFPO 999")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "if the stub is sent JSON with a valid postcode a 200 is returned (Appendix A row 15)" in {
      val jsonWithGoodPostcode = generateJsonWithPostcode("BFPO 9999")

      def fakeRequest = makeFakeRequest(jsonWithGoodPostcode)

      val result = squidController.createAccount()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "The following real postcodes should all pass:" in {
      for (pc <- Seq("BFPO 9999", "BN44 1ST", "E98 1SN", "EH99 1SP", "BS98 1TL")) {
        var jsonWithGoodPostcode = generateJsonWithPostcode(pc)
        var fakeRequest = makeFakeRequest(jsonWithGoodPostcode)
        var result = squidController.createAccount()(fakeRequest)
        status(result) shouldBe Status.CREATED
      }
    }

    "if the stub is sent JSON with a forename with zero characters, a bad request is returned with:" +
      "FFORENAME_TOO_FEW_CHARS_ERROR_CODE (AAAA0006) as the error code and site.too-few-chars-forename and " +
      "site.too-few-chars-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJsonWithForename("")

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe FORENAME_TOO_FEW_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-few-chars-forename"))
          w.error.errorMessage shouldBe messagesApi("site.too-few-chars-forename")
          assert(messagesApi.isDefinedAt("site.too-few-chars-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-few-chars-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with more than 26 characters, a bad request is returned with:" +
      "FFORENAME_TOO_MANY_CHARS_ERROR_CODE (AAAA0007) as the error code and site.too-many-chars-forename and " +
      "site.too-many-chars-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJsonWithForename("A" * 27)

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe FORENAME_TOO_MANY_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-many-chars-forename"))
          w.error.errorMessage shouldBe messagesApi("site.too-many-chars-forename")
          assert(messagesApi.isDefinedAt("site.too-many-chars-forename-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-many-chars-forename-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with leading spaces, a bad request is returned with:" +
      "LEADING_SPACES_ERROR_CODE (ZYRA0703) as the error code and site.leading-spaces-forename and " +
      "site.leading-spaces-forename-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJsonWithForename("    The Donald")

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
      val badJson = generateJsonWithForename("D0n4ld")

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
      val badJson = generateJsonWithForename("Dona$%#d")

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

    "if the stub is sent JSON with a forename with too many consecutive special characters, a bad request is returned with:" +
      "TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE (ZYRA0716) as the error code and site.too-many-consecutive-special-forename and " +
      "site.too-many-consecutive-special-forename-detail are returned in the error JSON and the appropriate message." in {

      val badJson = generateJsonWithForename("Don--aldddd")

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

    "if the stub is sent JSON with a surname with zero characters, a bad request is returned with:" +
      "SURNAME_TOO_FEW_CHARS_ERROR_CODE (AAAA0008) as the error code and site.too-few-chars-surname and " +
      "site.too-few-chars-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJsonWithSurname("")

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe SURNAME_TOO_FEW_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-few-chars-surname"))
          w.error.errorMessage shouldBe messagesApi("site.too-few-chars-surname")
          assert(messagesApi.isDefinedAt("site.too-few-chars-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-few-chars-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a forename with more than 300 characters, a bad request is returned with:" +
      "SURNAME_TOO_MANY_CHARS_ERROR_CODE (AAAA0009) as the error code and site.too-many-chars-surname and " +
      "site.too-many-chars-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJsonWithSurname("A" * 301)

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe SURNAME_TOO_MANY_CHARS_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.too-many-chars-surname"))
          w.error.errorMessage shouldBe messagesApi("site.too-many-chars-surname")
          assert(messagesApi.isDefinedAt("site.too-many-chars-surname-detail"))
          w.error.errorDetail shouldBe messagesApi("site.too-many-chars-surname-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a surname with leading spaces, a bad request is returned with:" +
      "LEADING_SPACES_ERROR_CODE (ZYRA0703) as the error code and site.leading-spaces-surname and " +
      "site.leading-spaces-surname-detail are returned in the error JSON" +
      "and the appropriate message" in {
      val badJson = generateJsonWithSurname("     Duck")

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
      val badJson = generateJsonWithSurname("D0n4ld")

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
      val badJson = generateJsonWithSurname("Duck$%#chesky")
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
      val badJson = generateJsonWithSurname("&Duckchesky")

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
      val badJson = generateJsonWithSurname("Duckchesky-")

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

    "if the stub is sent JSON with a surname with too many consecutive special characters, a bad request is returned with:" +
      "TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE (ZYRA0716) as the error code and site.too-many-consecutive-special-surname and " +
      "site.too-many-consecutive-special-surname-detail are returned in the error JSON and the appropriate message." in {
      //TODO: Find out what this number should actually be - initially setting it to 2
      val badJson = generateJsonWithSurname("duc--achesy")

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
      val jsonWithBadPostCode = generateJsonWithPostcode("HHHHHHHHHHHHHH678889098")

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
      val badJson = generateJsonWithDateOfBirth("YYYY1336")
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

    "if the stub is sent JSON with a numeric string of length 8 representing a date before 1st Jan 1800, an object is " +
      "returned with code set to BAD_DATE_TOO_EARLY_ERROR_CODE, (AAAA0010) " +
      " the message site.bad-date-too-early, and the detail site.bad-too-early-detail" in {
      val badJson = generateJsonWithDateOfBirth("17991231")

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe BAD_DATE_TOO_EARLY_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.bad-date-too-early"))
          w.error.errorMessage shouldBe messagesApi("site.bad-date-too-early")
          assert(messagesApi.isDefinedAt("site.bad-date-too-early-detail"))
          w.error.errorDetail shouldBe messagesApi("site.bad-date-too-early-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with a numeric string of length 8 representing a date in the future, an object is " +
      "returned with code set to BAD_DATE_TOO_LATE_ERROR_CODE, (AAAA0011) " +
      " the message site.bad-date-too-late, and the detail site.bad-too-late-detail" in {

      val today = java.time.LocalDate.now()
      val tomorrow = today.plus(1, java.time.temporal.ChronoUnit.DAYS)
      val year = tomorrow.getYear
      val month = tomorrow.getMonthValue
      val monthStr = if (month < 10) "0" + month else month
      val day = tomorrow.getDayOfMonth
      val dayStr = if (day < 10) "0" + day else day
      val badJson = generateJsonWithDateOfBirth(s"$year$monthStr$dayStr")

      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe BAD_DATE_TOO_LATE_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.bad-date-too-late"))
          w.error.errorMessage shouldBe messagesApi("site.bad-date-too-late")
          assert(messagesApi.isDefinedAt("site.bad-date-too-late-detail"))
          w.error.errorDetail shouldBe messagesApi("site.bad-date-too-late-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent JSON with an unrecognized country code an object is " +
      "returned with code set to UNKNOWN_COUNTRY_CODE_ERROR_CODE, (TAR10005) " +
      " the message site.unknown-country-code, and the detail site.unknown-country-code-detail" in {
      val badJson = generateJsonWithCountryCode("MJ")
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
      val badJson = generateJsonWithNino("THIS-IS-NOT-A-NINO")
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
      val badJson = generateJsonWithCommsPreference("01")
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
      val badJson = generateJsonWithNoEmail()
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

    "if the stub is sent an address1 with an empty string then an object is returned" +
      "with a code ADDRESS_ONE_TOO_SHORT_ERROR_CODE (AAAA0012) and" +
      "the message site.address1-empty, and the detail site.address1-empty-detail" in {
      val badJson = generateJsonWithAddress1("")
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe ADDRESS_ONE_TOO_SHORT_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.address1-empty"))
          w.error.errorMessage shouldBe messagesApi("site.address1-empty")
          assert(messagesApi.isDefinedAt("site.address1-empty-detail"))
          w.error.errorDetail shouldBe messagesApi("site.address1-empty-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an address1 with a string longer than 35 then an object is returned" +
      "with a code ADDRESS_ONE_TOO_LONG_ERROR_CODE (AAAA0013) and" +
      "the message site.address1-too-long, and the detail site.address1-too-long-detail" in {
      val badJson = generateJsonWithAddress1("A" * 36)
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe ADDRESS_ONE_TOO_LONG_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.address1-too-long"))
          w.error.errorMessage shouldBe messagesApi("site.address1-too-long")
          assert(messagesApi.isDefinedAt("site.address1-too-long-detail"))
          w.error.errorDetail shouldBe messagesApi("site.address1-too-long-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an address2 with an empty string then an object is returned" +
      "with a code ADDRESS_TWO_TOO_SHORT_ERROR_CODE (AAAA0014) and" +
      "the message site.address2-empty, and the detail site.address2-empty-detail" in {
      val badJson = generateJsonWithAddress2("")
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe ADDRESS_TWO_TOO_SHORT_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.address2-empty"))
          w.error.errorMessage shouldBe messagesApi("site.address2-empty")
          assert(messagesApi.isDefinedAt("site.address2-empty-detail"))
          w.error.errorDetail shouldBe messagesApi("site.address2-empty-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an address2 with a string longer than 35 then an object is returned" +
      "with a code ADDRESS_TWO_TOO_LONG_ERROR_CODE (AAAA0015) and" +
      "the message site.address2-too-long, and the detail site.address2-too-long-detail" in {
      val badJson = generateJsonWithAddress2("A" * 36)
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe ADDRESS_TWO_TOO_LONG_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.address2-too-long"))
          w.error.errorMessage shouldBe messagesApi("site.address2-too-long")
          assert(messagesApi.isDefinedAt("site.address2-too-long-detail"))
          w.error.errorDetail shouldBe messagesApi("site.address2-too-long-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an address3 with a string longer than 35 then an object is returned" +
      "with a code ADDRESS_THREE_TOO_LONG_ERROR_CODE (AAAA0016) and" +
      "the message site.address3-too-long, and the detail site.address3-too-long-detail" in {
      val badJson = generateJsonWithAddress3("A" * 36)
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe ADDRESS_THREE_TOO_LONG_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.address3-too-long"))
          w.error.errorMessage shouldBe messagesApi("site.address3-too-long")
          assert(messagesApi.isDefinedAt("site.address3-too-long-detail"))
          w.error.errorDetail shouldBe messagesApi("site.address3-too-long-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an address4 with a string longer than 35 then an object is returned" +
      "with a code ADDRESS_FOUR_TOO_LONG_ERROR_CODE (AAAA0017) and" +
      "the message site.address4-too-long, and the detail site.address4-too-long-detail" in {
      val badJson = generateJsonWithAddress4("A" * 36)
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe ADDRESS_FOUR_TOO_LONG_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.address4-too-long"))
          w.error.errorMessage shouldBe messagesApi("site.address4-too-long")
          assert(messagesApi.isDefinedAt("site.address4-too-long-detail"))
          w.error.errorDetail shouldBe messagesApi("site.address4-too-long-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an address5 with a string longer than 35 then an object is returned" +
      "with a code ADDRESS_FIVE_TOO_LONG_ERROR_CODE (AAAA0018) and" +
      "the message site.address5-too-long, and the detail site.address4-too-long-detail" in {
      val badJson = generateJsonWithAddress5("A" * 36)
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe ADDRESS_FIVE_TOO_LONG_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.address5-too-long"))
          w.error.errorMessage shouldBe messagesApi("site.address5-too-long")
          assert(messagesApi.isDefinedAt("site.address5-too-long-detail"))
          w.error.errorDetail shouldBe messagesApi("site.address5-too-long-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an phone number with a string longer than 15 then an object is returned" +
      "with a code PHONE_NUMBER_TOO_LONG_ERROR_CODE (AAAA0019) and" +
      "the message site.phone-number-too-long, and the detail site.phone-number-too-long-detail" in {
      val badJson = generateJsonWithPhoneNumber("A" * 16)
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe PHONE_NUMBER_TOO_LONG_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.phone-number-too-long"))
          w.error.errorMessage shouldBe messagesApi("site.phone-number-too-long")
          assert(messagesApi.isDefinedAt("site.phone-number-too-long-detail"))
          w.error.errorDetail shouldBe messagesApi("site.phone-number-too-long-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an email address with a string longer than 254 then an object is returned" +
      "with a code EMAIL_ADDRESS_TOO_LONG_ERROR_CODE (AAAA0020) and" +
      "the message site.email-address-too-long, and the detail site.email-address-too-long-detail" in {
      val badJson = generateJsonWithEmailAddress(("A" * 64) + "@" + ("A" * 252))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe EMAIL_ADDRESS_TOO_LONG_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.email-address-too-long"))
          w.error.errorMessage shouldBe messagesApi("site.email-address-too-long")
          assert(messagesApi.isDefinedAt("site.email-address-too-long-detail"))
          w.error.errorDetail shouldBe messagesApi("site.email-address-too-long-detail")
        case JsError(_) => fail
      }
    }

    "if the stub is sent an email address with a string that doesn't match the validation expression then an object is returned" +
      "with a code EMAIL_ADDRESS_INVALID_ERROR_CODE (AAAA0021) and" +
      "the message site.email-address-invalid, and the detail site.email-address-invalid-detail" in {
      val badJson = generateJsonWithEmailAddress(("A" * 65) + "@" + ("A" * 100))
      def fakeRequestWithBadContent = makeFakeRequest(badJson)

      val result = squidController.createAccount()(fakeRequestWithBadContent)
      status(result) shouldBe Status.BAD_REQUEST

      val wrapper = Json.fromJson[TestErrorWrapper](contentAsJson(result))
      wrapper match {
        case JsSuccess(w, _) =>
          w.error.errorMessageId shouldBe EMAIL_ADDRESS_INVALID_ERROR_CODE
          assert(messagesApi.isDefinedAt("site.email-address-invalid"))
          w.error.errorMessage shouldBe messagesApi("site.email-address-invalid")
          assert(messagesApi.isDefinedAt("site.email-address-invalid-detail"))
          w.error.errorDetail shouldBe messagesApi("site.email-address-invalid-detail")
        case JsError(_) => fail
      }
    }
  }
}
