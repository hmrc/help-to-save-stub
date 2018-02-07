package uk.gov.hmrc.helptosavestub.controllers

import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PayePersonalDetailsControllerSpec extends UnitSpec with WithFakeApplication {
  private val fakeRequest = FakeRequest("GET", "/pay-as-you-earn/02.00.00/individuals/AE123456C")
    .withHeaders("Authorization" → "Bearer test")

  val payeDetailsController = new PayePersonalDetailsController

  "GET /pay-as-you-earn/02.00.00/individuals/AE123456C" should {

    "returns paye details for a valid NINO" in {
      val result = payeDetailsController.getPayeDetails("AE123456C")(fakeRequest)

      status(result) shouldBe Status.OK
      val json = contentAsString(result)

      json === Json.toJson(payeDetailsController.payeDetails("AE123456C"))

    }

    "handles 404 cases when supplied NINO cant be found in DES" in {
      val result = payeDetailsController.getPayeDetails("PD404123C")(fakeRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "handles 500 cases when there is internal error" in {
      val result = payeDetailsController.getPayeDetails("PD500123C")(fakeRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }
}
