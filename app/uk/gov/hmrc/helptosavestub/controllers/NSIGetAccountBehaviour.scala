/*
 * Copyright 2019 HM Revenue & Customs
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

import ai.x.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse}

object NSIGetAccountBehaviour {

  def getAccountByNino(nino: String, correlationId: Option[String]): Either[ErrorDetails, NSIGetAccountByNinoResponse] = // scalastyle:ignore cyclomatic.complexity line.size.limit
    nino match {
      case n if (n.startsWith("EM200") || n.startsWith("EL07")) || n.startsWith("AC") || n.startsWith("AS409") ⇒
        Right(NSIGetAccountByNinoResponse.bethNSIResponse(correlationId))
      case n if n.startsWith("EM002") ⇒ Left(NSIErrorResponse.missingVersionError)
      case n if n.startsWith("EM003") ⇒ Left(NSIErrorResponse.unsupportedVersionError)
      case n if n.startsWith("EM004") ⇒ Left(NSIErrorResponse.missingNinoError)
      case n if n.startsWith("EM005") ⇒ Left(NSIErrorResponse.badNinoError)
      case n if n.startsWith("EM006") ⇒ Left(NSIErrorResponse.unknownNinoError)
      case n if n.startsWith("EM012") ⇒ Left(NSIErrorResponse.missingSystemIdError)
      case n if n.startsWith("EM0") && n.endsWith("001A") ⇒
        Right(NSIGetAccountByNinoResponse.bethNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("002A") ⇒
        Right(NSIGetAccountByNinoResponse.peteNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("003A") ⇒
        Right(NSIGetAccountByNinoResponse.lauraNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("004A") ⇒
        Right(NSIGetAccountByNinoResponse.tonyNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("005A") ⇒
        Right(NSIGetAccountByNinoResponse.monikaNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("006A") ⇒
        Right(NSIGetAccountByNinoResponse.happyNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("007A") ⇒
        Right(NSIGetAccountByNinoResponse.takenNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("008A") ⇒
        Right(NSIGetAccountByNinoResponse.spencerNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("009A") ⇒
        Right(NSIGetAccountByNinoResponse.alexNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("010A") ⇒
        Right(NSIGetAccountByNinoResponse.closedAccountResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("011A") ⇒
        Right(NSIGetAccountByNinoResponse.accountBlockedResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("012A") ⇒
        Right(NSIGetAccountByNinoResponse.clientBlockedResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("013A") ⇒
        Right(NSIGetAccountByNinoResponse.closedAccount2Response(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("014A") ⇒
        Right(NSIGetAccountByNinoResponse.closedAccount3Response(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("015A") ⇒
        Right(NSIGetAccountByNinoResponse.closedAccount4Response(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("016A") ⇒
        Right(NSIGetAccountByNinoResponse.accountUnspecifiedBlockedResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("099A") ⇒
        Right(NSIGetAccountByNinoResponse.positiveBonusZeroBalanceResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("098A") ⇒
        Right(NSIGetAccountByNinoResponse.zeroBonusPositiveBalanceResponse(correlationId))
      case n if n.startsWith("TM7") && n.endsWith("915A") ⇒
        Right(NSIGetAccountByNinoResponse.annaNSIResponse(correlationId))
      case _ ⇒ Left(NSIErrorResponse.unknownNinoError)
    }

  case class NSIGetAccountByNinoResponse(
    version: String,
    correlationId: Option[String],
    accountNumber: String,
    availableWithdrawal: String,
    accountBalance: String,
    accountClosedFlag: String,
    accountClosureDate: Option[LocalDate],
    accountClosingBalance: Option[String],
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
    postcode: String,
    countryCode: String,
    emailAddress: Option[String],
    commsPreference: String,
    clientBlockingCode: String,
    clientBlockingReasonCode: String,
    clientCancellationStatus: String,
    nbaAccountNumber: String,
    nbaPayee: String,
    nbaRollNumber: Option[String],
    nbaSortCode: String,
    terms: List[Term])

  case class CurrentInvestmentMonth(investmentRemaining: String, investmentLimit: String, endDate: LocalDate)

  case class Term(
    termNumber: Int,
    startDate: LocalDate,
    endDate: LocalDate,
    maxBalance: String,
    bonusEstimate: String,
    bonusPaid: String)

  object CurrentInvestmentMonth {

    implicit val format: Format[CurrentInvestmentMonth] = Json.format[CurrentInvestmentMonth]
  }

  object Term {

    implicit val format: Format[Term] = Json.format[Term]
  }

  object NSIGetAccountByNinoResponse {

    @SuppressWarnings(
      Array("org.wartremover.warts.Any", "org.wartremover.warts.Equals", "org.wartremover.warts.IsInstanceOf"))
    implicit val format: Format[NSIGetAccountByNinoResponse] = Jsonx.formatCaseClass[NSIGetAccountByNinoResponse]

    val bethCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

    val bethTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
      Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
    )
    val peteCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("9.88", "50.00", LocalDate.of(2018, 3, 31))
    val peteTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 9, 1), LocalDate.of(2019, 8, 31), "190.12", "95.06", "0.00"),
      Term(2, LocalDate.of(2019, 9, 1), LocalDate.of(2021, 8, 31), "0.00", "0.00", "0.00")
    )
    val lauraCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
    val lauraTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 3, 1), LocalDate.of(2019, 2, 28), "135.00", "67.50", "0.00"),
      Term(2, LocalDate.of(2019, 3, 1), LocalDate.of(2021, 2, 28), "0.00", "0.00", "0.00")
    )
    val tonyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))
    val tonyTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 10, 1), LocalDate.of(2019, 9, 30), "75.00", "37.50", "0.00"),
      Term(2, LocalDate.of(2019, 10, 1), LocalDate.of(2021, 9, 30), "0.00", "0.00", "0.00")
    )
    val monikaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
    val monikaTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 2, 29), "0.00", "0.00", "0.00"),
      Term(2, LocalDate.of(2020, 3, 1), LocalDate.of(2022, 2, 28), "0.00", "0.00", "0.00")
    )
    val happyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
    val happyTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "1200.00", "600.00", "600.00"),
      Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "2400.00", "600.00", "600.00")
    )
    val takenCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
    val takenTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "1200.00", "600.00", "600.00"),
      Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "0.00", "0.00", "0.00")
    )
    val spencerCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("40.00", "50.00", LocalDate.of(2018, 3, 31))
    val spencerTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "832.00", "416.00", "416.00"),
      Term(2, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 2, 29), "0.00", "0.00", "0.00")
    )
    val alexCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("13.00", "50.00", LocalDate.of(2018, 3, 31))
    val alexTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "900.00", "450.00", "450.00"),
      Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "1270.00", "185.00", "0.00")
    )
    val annaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))
    val annaTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 10, 1), LocalDate.of(2019, 9, 30), "75.00", "37.50", "0.00"),
      Term(2, LocalDate.of(2019, 10, 1), LocalDate.of(2021, 9, 30), "0.00", "0.00", "0.00")
    )
    val accountBlockedCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
    val accountBlockedTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
      Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
    )
    val clientBlockedCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
    val clientBlockedTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
      Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
    )
    val positiveBonusZeroBalanceCIM: CurrentInvestmentMonth =
      CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
    // make sure term 2 lasts a long time so we are always in term 2 during testing
    val positiveBonusZeroBalanceTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "2400.00", "600.00", "600.00"),
      Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2916, 2, 28), "2400.00", "600.00", "0.00")
    )
    val zeroBonusPositiveBalanceCIM: CurrentInvestmentMonth =
      CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
    // make sure term 2 lasts a long time so we are always in term 2 during testing
    val zeroBonusPositiveBalanceTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "2400.00", "600.00", "600.00"),
      Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2916, 2, 28), "2400.00", "0.00", "0.00")
    )
    val closedCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
    val closedTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "00.00", "0.00"),
      Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
    )
    val closed2CIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
    val closed2Terms: List[Term] = List[Term](
      Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "0.00", "0.00", "0.00"),
      Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "0.00", "0.00", "0.00")
    )
    val closed3CIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
    val closed3Terms: List[Term] = List[Term](
      Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "0.00", "0.00", "0.00"),
      Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "100.00", "0.00", "0.00")
    )
    val closed4CIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
    val closed4Terms: List[Term] = List[Term](
      Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "100.00", "50.00", "50.00"),
      Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "200.00", "0.00", "0.00")
    )
    val accountUnspecifiedBlockedCIM: CurrentInvestmentMonth =
      CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
    val accountUnspecifiedBlockedTerms: List[Term] = List[Term](
      Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
      Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
    )

    def bethNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112057",
        "225.00",
        "250.00",
        " ",
        None,
        None,
        "00",
        "00",
        bethCIM,
        "Beth",
        "Planner",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mrs B Planner",
        None,
        "801497",
        bethTerms
      )

    def peteNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112058",
        "165.12",
        "190.12",
        " ",
        None,
        None,
        "00",
        "00",
        peteCIM,
        "Pete",
        "Loveday",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        peteTerms
      )

    def lauraNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112059",
        "110.00",
        "135.00",
        " ",
        None,
        None,
        "00",
        "00",
        lauraCIM,
        "Laura",
        "Detavoidskiene",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        lauraTerms
      )

    def tonyNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112060",
        "50.00",
        "75.00",
        " ",
        None,
        None,
        "00",
        "00",
        tonyCIM,
        "Tony",
        "Loveday",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        None,
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        tonyTerms
      )

    def monikaNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112061",
        "0.00",
        "0.00",
        " ",
        None,
        None,
        "00",
        "00",
        monikaCIM,
        "Monika",
        "Detavoidskiene",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        monikaTerms
      )

    def happyNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112062",
        "0.00",
        "2400.00",
        " ",
        None,
        None,
        "00",
        "00",
        happyCIM,
        "Happy",
        "Saver",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        happyTerms
      )

    def takenNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112063",
        "0.00",
        "0.00",
        " ",
        None,
        None,
        "00",
        "00",
        takenCIM,
        "Taken",
        "Out",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        takenTerms
      )

    def spencerNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112064",
        "0.00",
        "832.00",
        " ",
        None,
        None,
        "00",
        "00",
        spencerCIM,
        "Spencer",
        "Waller",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        spencerTerms
      )

    def alexNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112065",
        "0.00",
        "1270.00",
        " ",
        None,
        None,
        "00",
        "00",
        alexCIM,
        "Alex",
        "Millar",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        alexTerms
      )

    def annaNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112066",
        "0.00",
        "75.00",
        " ",
        None,
        None,
        "00",
        "00",
        annaCIM,
        "Anna",
        "Smith",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        annaTerms
      )

    def accountBlockedResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112067",
        "0.00",
        "250.00",
        " ",
        None,
        None,
        "12",
        "4B",
        accountBlockedCIM,
        "Account",
        "Blocked",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mrs A Blocked",
        None,
        "801497",
        accountBlockedTerms
      )

    def clientBlockedResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112068",
        "0.00",
        "250.00",
        " ",
        None,
        None,
        "00",
        "00",
        clientBlockedCIM,
        "Client",
        "Blocked",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "12",
        "4B",
        " ",
        "11111111",
        "Mrs C Blocked",
        None,
        "801497",
        clientBlockedTerms
      )

    def positiveBonusZeroBalanceResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112069",
        "0.00",
        "0.00",
        " ",
        None,
        None,
        "00",
        "00",
        positiveBonusZeroBalanceCIM,
        "FirstTerm",
        "Saver",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        positiveBonusZeroBalanceTerms
      )

    def zeroBonusPositiveBalanceResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112070",
        "2400.00",
        "2400.00",
        " ",
        None,
        None,
        "00",
        "00",
        zeroBonusPositiveBalanceCIM,
        "FirstTerm",
        "Saver",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mr P Smith",
        None,
        "801497",
        zeroBonusPositiveBalanceTerms
      )

    def closedAccountResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112071",
        "0.00",
        "0.00",
        "C",
        Some(LocalDate.of(2018, 3, 5)),
        Some("250.00"),
        "00",
        "00",
        closedCIM,
        "Closed",
        "Account",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mrs C Account",
        None,
        "801497",
        closedTerms
      )

    def closedAccount2Response(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112072",
        "0.00",
        "0.00",
        "C",
        Some(LocalDate.of(2018, 3, 5)),
        Some("0.00"),
        "00",
        "00",
        closed2CIM,
        "Closed",
        "Account Two",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mrs C Account Two",
        None,
        "801497",
        closed2Terms
      )

    def closedAccount3Response(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112073",
        "0.00",
        "0.00",
        "C",
        Some(LocalDate.of(2018, 3, 5)),
        Some("100.00"),
        "00",
        "00",
        closed3CIM,
        "Closed",
        "Account Three",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mrs C Account Three",
        None,
        "801497",
        closed3Terms
      )

    def closedAccount4Response(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112074",
        "0.00",
        "0.00",
        "C",
        Some(LocalDate.of(2018, 3, 5)),
        Some("200.00"),
        "00",
        "00",
        closed4CIM,
        "Closed",
        "Account Four",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mrs C Account Four",
        None,
        "801497",
        closed4Terms
      )

    def accountUnspecifiedBlockedResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse =
      NSIGetAccountByNinoResponse(
        "V1.0",
        correlationId,
        "1100000112075",
        "0.00",
        "250.00",
        " ",
        None,
        None,
        "11",
        "4B",
        accountUnspecifiedBlockedCIM,
        "AccountPayment",
        "Blocked",
        LocalDate.of(1963, 11, 1),
        "Line 1",
        "Line 2",
        " ",
        " ",
        " ",
        "SV1 1QA",
        "GB",
        Some("email.address@domain.com"),
        "02",
        "00",
        "00",
        " ",
        "11111111",
        "Mrs A P Blocked",
        None,
        "801497",
        accountUnspecifiedBlockedTerms
      )
  }
}
