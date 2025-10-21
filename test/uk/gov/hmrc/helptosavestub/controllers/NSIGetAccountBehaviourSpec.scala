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
      val result = NSIGetAccountBehaviour.getAccountByNino("AA123456A")
      result shouldBe Right(NSIGetAccountByNinoResponse.bethNSIResponse())
    }

    "return missingVersionError for NINOs starting with EM002" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("EM002123A")
      result shouldBe Left(NSIErrorResponse.missingVersionError)
    }

    "return unsupportedVersionError for NINOs starting with EM003" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("EM003123A")
      result shouldBe Left(NSIErrorResponse.unsupportedVersionError)
    }

    "return specific named responses for known suffixes" in {
      val result1 = NSIGetAccountBehaviour.getAccountByNino("EM000001A")
      result1 shouldBe Right(NSIGetAccountByNinoResponse.bethNSIResponse())

      val result2 = NSIGetAccountBehaviour.getAccountByNino("EM000002A")
      result2 shouldBe Right(NSIGetAccountByNinoResponse.peteNSIResponse())

      val result3 = NSIGetAccountBehaviour.getAccountByNino("EM000025A")
      result3 shouldBe Right(NSIGetAccountByNinoResponse.dennisNSIResponse("C"))
    }

    "return unknownNinoError for unrecognized NINOs" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("ZZ123456A")
      result shouldBe Left(NSIErrorResponse.unknownNinoError)
    }

    "return bethNSIResponse for NINOs starting with AA, AB, BE, EM200, EL07, AC, AS409" in {
      val validPrefixes = Seq("AA", "AB", "BE", "EM200", "EL07", "AC", "AS409")
      validPrefixes.foreach { prefix =>
        val result = NSIGetAccountBehaviour.getAccountByNino(s"${prefix}123456A")
        result shouldBe Right(NSIGetAccountByNinoResponse.bethNSIResponse())
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
        NSIGetAccountBehaviour.getAccountByNino(nino) shouldBe Left(expectedError)
      }
    }

    "return correct named responses for EM0xxxxA suffixes" in {
      val testCases = Seq(
        "EM000001A" -> ((_: Option[String]) =>  NSIGetAccountByNinoResponse.bethNSIResponse ()),
        "EM000002A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.peteNSIResponse ()),
        "EM000003A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.lauraNSIResponse ()),
        "EM000004A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.tonyNSIResponse ()),
        "EM000005A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.monikaNSIResponse ()),
        "EM000006A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.happyNSIResponse ()),
        "EM000007A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.takenNSIResponse ()),
        "EM000008A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.spencerNSIResponse ()),
        "EM000009A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.alexNSIResponse ()),
        "EM000010A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.closedAccountResponse ()),
        "EM000011A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.accountBlockedResponse ()),
        "EM000012A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.clientBlockedResponse ()),
        "EM000013A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.closedAccount2Response ()),
        "EM000014A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.closedAccount3Response ()),
        "EM000015A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.closedAccount4Response ()),
        "EM000016A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.accountUnspecifiedBlockedResponse ()),
        "EM000099A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.positiveBonusZeroBalanceResponse ()),
        "EM000098A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.zeroBonusPositiveBalanceResponse ()),
        "TM7915915A" ->((_: Option[String]) => NSIGetAccountByNinoResponse.annaNSIResponse ()),
        "EM000017A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.tomNSIResponse ()),
        "EM000018A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.angelaNSIResponse ()),
        "EM000019A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.ivoNSIResponse ()),
        "EM000020A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.arsenyNSIResponse ()),
        "EM000021A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.sunanNSIResponse ()),
        "EM000022A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.ranaNSIResponse ()),
        "EM000023A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.marshalNSIResponse ()),
        "EM000024A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.dennisNSIResponse ()),
        "EM000025A" -> ((_: Option[String]) => NSIGetAccountByNinoResponse.dennisNSIResponse("C"))
      )

      val correlationId = Some("corr-id")

      testCases.foreach { case (nino, expectedResponseFn) =>
        val result = NSIGetAccountBehaviour.getAccountByNino(nino)
        result shouldBe Right(expectedResponseFn(correlationId))
      }
    }

    "return unknownNinoError for unmatched NINOs" in {
      val result = NSIGetAccountBehaviour.getAccountByNino("ZZ999999A")
      result shouldBe Left(NSIErrorResponse.unknownNinoError)
    }
  }
}
