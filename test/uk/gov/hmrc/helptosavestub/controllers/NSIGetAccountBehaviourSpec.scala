/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.helptosavestub.models._

class NSIGetAccountBehaviourSpec extends AnyWordSpec with Matchers {

  "NSIGetAccountBehaviour.getAccountByNino" should {

    "return bethNSIResponse for NINOs starting with AA, AB, or BE" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("AA123456A", Some("corr-id"))
      result shouldBe Right(NSIGetAccountByNinoResponse.bethNSIResponse(Some("corr-id")))
    }

    "return missingVersionError for NINOs starting with EM002" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("EM002123A", None)
      result shouldBe Left(NSIErrorResponse.missingVersionError)
    }

    "return unsupportedVersionError for NINOs starting with EM003" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("EM003123A", None)
      result shouldBe Left(NSIErrorResponse.unsupportedVersionError)
    }

    "return specific named responses for known suffixes" in {
      val result1 = NSIGetAccountBehaviour.getAccountByNino("EM000001A", Some("corr-id"))
      result1 shouldBe Right(NSIGetAccountByNinoResponse.bethNSIResponse(Some("corr-id")))

      val result2 = NSIGetAccountBehaviour.getAccountByNino("EM000002A", Some("corr-id"))
      result2 shouldBe Right(NSIGetAccountByNinoResponse.peteNSIResponse(Some("corr-id")))

      val result3 = NSIGetAccountBehaviour.getAccountByNino("EM000025A", Some("corr-id"))
      result3 shouldBe Right(NSIGetAccountByNinoResponse.dennisNSIResponse(Some("corr-id"), "C"))
    }

    "return unknownNinoError for unrecognized NINOs" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("ZZ123456A", None)
      result shouldBe Left(NSIErrorResponse.unknownNinoError)
    }

    "return bethNSIResponse for NINOs starting with AA, AB, BE, EM200, EL07, AC, AS409" in {
      val validPrefixes = Seq("AA", "AB", "BE", "EM200", "EL07", "AC", "AS409")
      validPrefixes.foreach { prefix =>
        val result = NSIGetAccountBehaviour.getAccountByNino(s"${prefix}123456A", Some("corr-id"))
        result shouldBe Right(NSIGetAccountByNinoResponse.bethNSIResponse(Some("corr-id")))
      }
    }

    "return appropriate error responses for specific EM00x prefixes" in {
      val errorCases = Seq(
        "EM002123A" -> NSIErrorResponse.missingVersionError,
        "EM003123A" -> NSIErrorResponse.unsupportedVersionError,
        "EM004123A" -> NSIErrorResponse.missingNinoError,
        "EM005123A" -> NSIErrorResponse.badNinoError,
        "EM006123A" -> NSIErrorResponse.unknownNinoError,
        "EM012123A" -> NSIErrorResponse.missingSystemIdError
      )

      errorCases.foreach { case (nino, expectedError) =>
        NSIGetAccountBehaviour.getAccountByNino(nino, None) shouldBe Left(expectedError)
      }
    }

    "return correct named responses for EM0xxxxA suffixes" in {
      val testCases = Seq(
        "EM000001A" -> ((cid: Option[String]) =>  NSIGetAccountByNinoResponse.bethNSIResponse (cid)),
        "EM000002A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.peteNSIResponse (cid)),
        "EM000003A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.lauraNSIResponse (cid)),
        "EM000004A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.tonyNSIResponse (cid)),
        "EM000005A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.monikaNSIResponse (cid)),
        "EM000006A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.happyNSIResponse (cid)),
        "EM000007A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.takenNSIResponse (cid)),
        "EM000008A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.spencerNSIResponse (cid)),
        "EM000009A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.alexNSIResponse (cid)),
        "EM000010A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.closedAccountResponse (cid)),
        "EM000011A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.accountBlockedResponse (cid)),
        "EM000012A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.clientBlockedResponse (cid)),
        "EM000013A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.closedAccount2Response (cid)),
        "EM000014A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.closedAccount3Response (cid)),
        "EM000015A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.closedAccount4Response (cid)),
        "EM000016A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.accountUnspecifiedBlockedResponse (cid)),
        "EM000099A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.positiveBonusZeroBalanceResponse (cid)),
        "EM000098A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.zeroBonusPositiveBalanceResponse (cid)),
        "TM7915915A" ->((cid: Option[String]) => NSIGetAccountByNinoResponse.annaNSIResponse (cid)),
        "EM000017A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.tomNSIResponse (cid)),
        "EM000018A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.angelaNSIResponse (cid)),
        "EM000019A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.ivoNSIResponse (cid)),
        "EM000020A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.arsenyNSIResponse (cid)),
        "EM000021A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.sunanNSIResponse (cid)),
        "EM000022A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.ranaNSIResponse (cid)),
        "EM000023A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.marshalNSIResponse (cid)),
        "EM000024A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.dennisNSIResponse (cid)),
        "EM000025A" -> ((cid: Option[String]) => NSIGetAccountByNinoResponse.dennisNSIResponse(cid, "C"))
      )

      val correlationId = Some("corr-id")

      testCases.foreach { case (nino, expectedResponseFn) =>
        val result = NSIGetAccountBehaviour.getAccountByNino(nino, correlationId)
        result shouldBe Right(expectedResponseFn(correlationId))
      }
    }

    "return unknownNinoError for unmatched NINOs" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("ZZ999999A", None)
      result shouldBe Left(NSIErrorResponse.unknownNinoError)
    }
  }
}
