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
import play.api.libs.json.Json
import uk.gov.hmrc.helptosavestub.controllers.NSIGetTransactionsBehaviour.{NSIGetTransactionsByNinoResponse, Transaction}
import uk.gov.hmrc.helptosavestub.models.NSIErrorResponse

import java.time.LocalDate

class NSIGetTransactionsBehaviourSpec extends AnyWordSpec with Matchers {

  "NSIGetTransactionsBehaviour.getTransactionsByNino" should {

    "return the correct response for known NINOs" in {
      val knownNinos = Map(
        "EM000000001A" -> NSIGetTransactionsByNinoResponse.bethResponse _,
        "EM000000002A" -> NSIGetTransactionsByNinoResponse.peteResponse _,
        "EM000000003A" -> NSIGetTransactionsByNinoResponse.lauraResponse _,
        "EM000000004A" -> NSIGetTransactionsByNinoResponse.tonyResponse _,
        "EM000000005A" -> NSIGetTransactionsByNinoResponse.monikaResponse _,
        "EM000000006A" -> NSIGetTransactionsByNinoResponse.happyResponse _,
        "EM000000007A" -> NSIGetTransactionsByNinoResponse.takenResponse _,
        "EM000000008A" -> NSIGetTransactionsByNinoResponse.spencerResponse _,
        "EM000000009A" -> NSIGetTransactionsByNinoResponse.alexResponse _,
        "EM000000010A" -> NSIGetTransactionsByNinoResponse.closedAccountResponse _,
        "EM000000011A" -> NSIGetTransactionsByNinoResponse.accountBlockedResponse _,
        "EM000000012A" -> NSIGetTransactionsByNinoResponse.clientBlockedResponse _,
        "EM000000013A" -> NSIGetTransactionsByNinoResponse.closedAccount2Response _,
        "EM000000014A" -> NSIGetTransactionsByNinoResponse.closedAccount3Response _,
        "EM000000015A" -> NSIGetTransactionsByNinoResponse.closedAccount4Response _,
        "EM000000016A" -> NSIGetTransactionsByNinoResponse.accountUnspecifiedBlockedResponse _,
        "TM700000915A" -> NSIGetTransactionsByNinoResponse.annaResponse _,
        "EM000000017A" -> NSIGetTransactionsByNinoResponse.tomResponse _,
        "EM000000018A" -> NSIGetTransactionsByNinoResponse.angelaResponse _,
        "EM000000019A" -> NSIGetTransactionsByNinoResponse.ivoResponse _,
        "EM000000020A" -> NSIGetTransactionsByNinoResponse.arsenyResponse _,
        "EM000000021A" -> NSIGetTransactionsByNinoResponse.sunanResponse _,
        "EM000000022A" -> NSIGetTransactionsByNinoResponse.ranaResponse _,
        "EM000000023A" -> NSIGetTransactionsByNinoResponse.marshalResponse _,
        "EM000000024A" -> NSIGetTransactionsByNinoResponse.dennisResponse _
      )

      val correlationId = Some("test-correlation-id")

      knownNinos.foreach { case (nino, expectedResponseFn) =>
        val result = NSIGetTransactionsBehaviour.getTransactionsByNino(nino, correlationId)
        result shouldBe Right(expectedResponseFn(correlationId))
      }
    }

    "return an error for unknown NINOs" in {
      val unknownNino = "ZZ999999Z"
      val result = NSIGetTransactionsBehaviour.getTransactionsByNino(unknownNino, Some("test-correlation-id"))
      result shouldBe Left(NSIErrorResponse.unknownNinoError)
    }
  }

  "Transaction" should {
    "serialize and deserialize to/from JSON correctly" in {
      val transaction = Transaction(
        sequence = "1",
        amount = "50.00",
        operation = "C",
        description = "Debit card online deposit",
        transactionReference = "B8C29ZY4A00A0018",
        transactionDate = LocalDate.of(2017, 11, 7),
        accountingDate = LocalDate.of(2017, 11, 7)
      )

      val json = Json.toJson(transaction)
      val parsed = json.as[Transaction]

      parsed shouldBe transaction
    }
  }

  "NSIGetTransactionsByNinoResponse" should {
    "serialize and deserialize to/from JSON correctly" in {
      val response = NSIGetTransactionsByNinoResponse(
        version = "1.0",
        correlationId = Some("test-correlation-id"),
        finalBalance = "250.00",
        startBalance = "0.00",
        transactions = NSIGetTransactionsByNinoResponse.bethTransaction
      )

      val json = Json.toJson(response)
      val parsed = json.as[NSIGetTransactionsByNinoResponse]

      parsed shouldBe response
    }

    "return correct bethResponse" in {
      val correlationId = Some("test-id")
      val response = NSIGetTransactionsByNinoResponse.bethResponse(correlationId)

      response.version shouldBe "V1.0"
      response.correlationId shouldBe correlationId
      response.finalBalance shouldBe "250.00"
      response.startBalance shouldBe "0.00"
      response.transactions shouldBe NSIGetTransactionsByNinoResponse.bethTransaction
    }

    "return correct peteResponse" in {
      val correlationId = Some("test-id")
      val response = NSIGetTransactionsByNinoResponse.peteResponse(correlationId)

      response.finalBalance shouldBe "190.12"
      response.transactions shouldBe NSIGetTransactionsByNinoResponse.peteTransaction
    }

    "return correct lauraResponse" in {
      val correlationId = Some("test-id")
      val response = NSIGetTransactionsByNinoResponse.lauraResponse(correlationId)

      response.finalBalance shouldBe "135.00"
      response.transactions shouldBe NSIGetTransactionsByNinoResponse.lauraTransaction
    }

    "return correct tonyResponse" in {
      val correlationId = Some("test-id")
      val response = NSIGetTransactionsByNinoResponse.tonyResponse(correlationId)

      response.finalBalance shouldBe "75.00"
      response.transactions shouldBe NSIGetTransactionsByNinoResponse.tonyTransaction
    }
  }
}
