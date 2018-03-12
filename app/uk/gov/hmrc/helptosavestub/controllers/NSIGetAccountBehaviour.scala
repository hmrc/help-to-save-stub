/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.LocalDate
import java.util.UUID

import cats.instances.string._
import cats.syntax.eq._
import play.api.libs.json.{Format, Json}

import scala.concurrent.ExecutionContext

object NSIGetAccountBehaviour {

  def getAccountByNino(nino: String)(implicit ec: ExecutionContext): NSIGetAccountByNinoResponse =
    if (nino === "EM000001A") {
      NSIGetAccountByNinoResponse.bethNSIResponse
    } else if (nino === "EM000002A") {
      NSIGetAccountByNinoResponse.peteNSIResponse
    } else if (nino === "EM000003A") {
      NSIGetAccountByNinoResponse.lauraNSIResponse
    } else if (nino === "EM000004A") {
      NSIGetAccountByNinoResponse.tonyNSIResponse
    } else if (nino === "EM000005A") {
      NSIGetAccountByNinoResponse.monikaNSIResponse
    } else if (nino === "EM000006A") {
      NSIGetAccountByNinoResponse.happyNSIResponse
    } else if (nino === "EM000007A") {
      NSIGetAccountByNinoResponse.takenNSIResponse
    } else if (nino === "EM000008A") {
      NSIGetAccountByNinoResponse.spencerNSIResponse
    } else if (nino === "EM000009A") {
      NSIGetAccountByNinoResponse.alexNSIResponse
    } else if (nino === "TM739915A") {
      NSIGetAccountByNinoResponse.annaNSIResponse
    } else {
      NSIGetAccountByNinoResponse.annaNSIResponse ///maybe need to update this???
    }

  case class NSIGetAccountByNinoResponse(version: String,
                                         correlationId: UUID,
                                         accountNumber: Long,
                                         availableWithdrawal: String,
                                         accountBalance: String,
                                         accountClosedFlag: Boolean,
                                         accountBlockingCode: String,
                                         accountBlockingReasonCode: String,
                                         currentInvestmentMonth: CurrentInvestmentMonth,
                                         clientForename: String,
                                         clientSurname: String,
                                         dateOfBirth: LocalDate,
                                         addressLine1: String,
                                         addressLine2: String,
                                         addressLine3: String,
                                         addressLine4: String,
                                         addressLine5: String,
                                         postCode: String,
                                         countryCode: String,
                                         emailAddress: String,
                                         commsPreference: String,
                                         clientBlockingCode: String,
                                         clientBlockingReasonCode: String,
                                         clientCancellationStatus: String,
                                         nbaAccountNumber: String,
                                         nbaPayee: String,
                                         nbaRollNumber: String,
                                         nbaSortCode: String,
                                         terms: List[Term])

  case class CurrentInvestmentMonth(investmentRemaining: String, investmentLimit: String, endDate: LocalDate)

  object CurrentInvestmentMonth {

    implicit val format: Format[CurrentInvestmentMonth] = Json.format[CurrentInvestmentMonth]
  }

  case class Term(termNumber: Int, startDate: LocalDate, endDate: LocalDate, maxBalance: String, bonusEstimate: String, bonusPaid: String)

  object Term {

    implicit val format: Format[Term] = Json.format[Term]
  }

  object NSIGetAccountByNinoResponse {

    implicit val format: Format[NSIGetAccountByNinoResponse] = Jsonx.formatCaseClass[NSIGetAccountByNinoResponse]

    val correlationId: UUID = UUID.randomUUID()

    val bethCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

    val bethTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 11, 1), "200.00", "100.00", "0.00"),
      Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 11, 1), "0.00", "0.00", "0.00"))

    val bethNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "175.00", "200.00", false, "00", "00", bethCIM, "Beth", "Planner", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mrs B Planner", " ", "801497", bethTerms)

    val peteCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("9.88", "50.00", LocalDate.of(2018, 3, 31))

    val peteTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 9, 1), LocalDate.of(2019, 9, 1), "190.12", "95.06", "0.00"),
      Term(2, LocalDate.of(2019, 9, 1), LocalDate.of(2021, 9, 1), "0.00", "0.00", "0.00"))

    val peteNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "165.12", "190.12", false, "00", "00", peteCIM, "Pete", "Loveday", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", peteTerms)

    val lauraCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))

    val lauraTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 3, 1), LocalDate.of(2019, 3, 1), "135.00", "67.50", "0.00"),
      Term(2, LocalDate.of(2019, 3, 1), LocalDate.of(2021, 3, 1), "0.00", "0.00", "0.00"))

    val lauraNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "110.00", "135.00", false, "00", "00", lauraCIM, "Laura", "Detavoidskiene", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", lauraTerms)

    val tonyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))

    val tonyTerms: List[Term] = List[Term](Term(1, LocalDate.of(2018, 10, 1), LocalDate.of(2020, 10, 1), "75.00", "37.50", "0.00"),
      Term(2, LocalDate.of(2019, 3, 1), LocalDate.of(2021, 3, 1), "0.00", "0.00", "0.00"))

    val tonyNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "50.00", "75.00", false, "00", "00", tonyCIM, "Tony", "Loveday", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", tonyTerms)

    val monikaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))

    val monikaTerms: List[Term] = List[Term](Term(1, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 3, 1), "0.00", "0.00", "0.00"),
      Term(2, LocalDate.of(2020, 3, 1), LocalDate.of(2022, 3, 1), "0.00", "0.00", "0.00"))

    val monikaNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "0.00", "0.00", false, "00", "00", monikaCIM, "Monika", "Detavoidskiene", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", monikaTerms)

    val happyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

    val happyTerms: List[Term] = List[Term](Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 3, 1), "1200.00", "600.00", "0.00"),
      Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 3, 1), "2400.00", "600.00", "0.00"))

    val happyNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "0.00", "0.00", false, "00", "00", happyCIM, "Happy", "Saver", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", happyTerms)

    val takenCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))

    val takenTerms: List[Term] = List[Term](Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 3, 1), "1200.00", "600.00", "0.00"),
      Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 3, 1), "0.00", "0.00", "0.00"))

    val takenNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "0.00", "0.00", false, "00", "00", takenCIM, "Taken", "Out", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", takenTerms)

    val spencerCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("40.00", "50.00", LocalDate.of(2018, 3, 31))

    val spencerTerms: List[Term] = List[Term](Term(1, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 3, 1), "832.00", "416.00", "0.00"),
      Term(2, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 3, 1), "0.00", "0.00", "0.00"))

    val spencerNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "0.00", "0.00", false, "00", "00", spencerCIM, "Spencer", "Waller", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", spencerTerms)

    val alexCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("13.00", "50.00", LocalDate.of(2018, 3, 31))

    val alexTerms: List[Term] = List[Term](Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 2, 1), "900.00", "450.00", "0.00"),
      Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 2, 1), "1270.00", "185.00", "0.00"))

    val alexNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "0.00", "0.00", false, "00", "00", alexCIM, "Alex", "Millar", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", alexTerms)

    val annaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))

    val annaTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 10, 1), LocalDate.of(2019, 10, 1), "75.00", "37.50", "0.00"),
      Term(2, LocalDate.of(2019, 10, 1), LocalDate.of(2021, 10, 1), "0.00", "0.00", "0.00"))

    val annaNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      1100000112057l, "0.00", "0.00", false, "00", "00", annaCIM, "Anna", "Smith", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", annaTerms)

  }

}
