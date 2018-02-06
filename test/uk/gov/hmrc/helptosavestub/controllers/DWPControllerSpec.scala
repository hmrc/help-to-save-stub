package uk.gov.hmrc.helptosavestub.controllers

import play.api.mvc._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DWPControllerSpec extends UnitSpec with WithFakeApplication {

  val fakeRequest = FakeRequest("GET", "/").withHeaders("Authorization" → "Bearer test")

  val dwpController = new DWPController

  val wp01JsonTest = Json.parse("""{"ucClaimant": "Y", "withinThreshold" : "Y"}""")
  val wp02JsonTest = Json.parse("""{"ucClaimant": "Y", "withinThreshold" : "N"}""")
  val wp03JsonTest = Json.parse("""{"ucClaimant": "N"}""")

  "dwpEligibilityCheck" must {
    "return a 200 status along with (Y, Y) json payload when given a nino starting with WP01" in {
      val result = dwpController.dwpClaimantCheck("WP010123A")
      result shouldBe Ok
      result.body should contain (wp01JsonTest)
      result.
    }
  }

}