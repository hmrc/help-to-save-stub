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

import ai.x.play.json.Jsonx
import play.api.Logger
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse}

object NSIGetAccountBehaviour {

  def getAccountByNino(nino: String, correlationId: Option[String]): Either[ErrorDetails, NSIGetAccountByNinoResponse] = // scalastyle:ignore cyclomatic.complexity line.size.limit
    nino match {
      case "EM000001A" ⇒ Right(NSIGetAccountByNinoResponse.bethNSIResponse(correlationId))
      case "EM000002A" ⇒ Right(NSIGetAccountByNinoResponse.peteNSIResponse(correlationId))
      case "EM000003A" ⇒ Right(NSIGetAccountByNinoResponse.lauraNSIResponse(correlationId))
      case "EM000004A" ⇒ Right(NSIGetAccountByNinoResponse.tonyNSIResponse(correlationId))
      case "EM000005A" ⇒ Right(NSIGetAccountByNinoResponse.monikaNSIResponse(correlationId))
      case "EM000006A" ⇒ Right(NSIGetAccountByNinoResponse.happyNSIResponse(correlationId))
      case "EM000007A" ⇒ Right(NSIGetAccountByNinoResponse.takenNSIResponse(correlationId))
      case "EM000008A" ⇒ Right(NSIGetAccountByNinoResponse.spencerNSIResponse(correlationId))
      case "EM000009A" ⇒ Right(NSIGetAccountByNinoResponse.alexNSIResponse(correlationId))
      case "EM000010A" ⇒ Right(NSIGetAccountByNinoResponse.closedAccountResponse(correlationId))
      case "TM739915A" ⇒ Right(NSIGetAccountByNinoResponse.annaNSIResponse(correlationId))
      case "NB123533B" ⇒ nsiGetAccountResponseFromFile("NB123533B.json")
      case _           ⇒ Left(NSIErrorResponse.unknownNinoError)
    }

  private def nsiGetAccountResponseFromFile(name: String): Either[ErrorDetails, NSIGetAccountByNinoResponse] = {
    val resourceName = s"/nsi/account/$name"
    val inputStreamIfExists = Option(getClass.getResourceAsStream(resourceName))
    inputStreamIfExists match {
      case Some(inputStream) ⇒
        try {
          val response: NSIGetAccountByNinoResponse = Json.parse(inputStream).as[NSIGetAccountByNinoResponse]
          Right(response)
        } finally {
          inputStream.close()
        }
      case None ⇒
        Logger.warn(s"Could not find resource $resourceName")
        Left(NSIErrorResponse.unknownNinoError)
    }
  }

  case class NSIGetAccountByNinoResponse(version:                   String,
                                         correlationId:             Option[String],
                                         accountNumber:             String,
                                         availableWithdrawal:       String,
                                         accountBalance:            String,
                                         accountClosedFlag:         String,
                                         accountClosureDate:        Option[LocalDate],
                                         accountClosingBalance:     Option[String],
                                         accountBlockingCode:       String,
                                         accountBlockingReasonCode: String,
                                         currentInvestmentMonth:    CurrentInvestmentMonth,
                                         clientForename:            String,
                                         clientSurname:             String,
                                         dateOfBirth:               LocalDate,
                                         addressLine1:              String,
                                         addressLine2:              String,
                                         addressLine3:              String,
                                         addressLine4:              String,
                                         addressLine5:              String,
                                         postcode:                  String,
                                         countryCode:               String,
                                         emailAddress:              String,
                                         commsPreference:           String,
                                         clientBlockingCode:        String,
                                         clientBlockingReasonCode:  String,
                                         clientCancellationStatus:  String,
                                         nbaAccountNumber:          String,
                                         nbaPayee:                  String,
                                         nbaRollNumber:             Option[String],
                                         nbaSortCode:               String,
                                         terms:                     List[Term])

  case class CurrentInvestmentMonth(investmentRemaining: String, investmentLimit: String, endDate: LocalDate)

  object CurrentInvestmentMonth {

    implicit val format: Format[CurrentInvestmentMonth] = Json.format[CurrentInvestmentMonth]
  }

  case class Term(termNumber: Int, startDate: LocalDate, endDate: LocalDate, maxBalance: String, bonusEstimate: String, bonusPaid: String)

  object Term {

    implicit val format: Format[Term] = Json.format[Term]
  }

  object NSIGetAccountByNinoResponse {

    @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Equals", "org.wartremover.warts.IsInstanceOf"))
    implicit val format: Format[NSIGetAccountByNinoResponse] = Jsonx.formatCaseClass[NSIGetAccountByNinoResponse]

    val bethCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

    val bethTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "200.00", "100.00", "0.00"),
                                           Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00"))

    def bethNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112057", "175.00", "200.00", " ", None, None, "00", "00", bethCIM, "Beth", "Planner", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mrs B Planner", None, "801497", bethTerms)

    val peteCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("9.88", "50.00", LocalDate.of(2018, 3, 31))

    val peteTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 9, 1), LocalDate.of(2019, 8, 31), "190.12", "95.06", "0.00"),
                                           Term(2, LocalDate.of(2019, 9, 1), LocalDate.of(2021, 8, 31), "0.00", "0.00", "0.00"))

    def peteNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112058", "165.12", "190.12", " ", None, None, "00", "00", peteCIM, "Pete", "Loveday", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", peteTerms)

    val lauraCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))

    val lauraTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 3, 1), LocalDate.of(2019, 2, 28), "135.00", "67.50", "0.00"),
                                            Term(2, LocalDate.of(2019, 3, 1), LocalDate.of(2021, 2, 28), "0.00", "0.00", "0.00"))

    def lauraNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112059", "110.00", "135.00", " ", None, None, "00", "00", lauraCIM, "Laura", "Detavoidskiene", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", lauraTerms)

    val tonyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))

    val tonyTerms: List[Term] = List[Term](Term(1, LocalDate.of(2018, 10, 1), LocalDate.of(2020, 9, 30), "75.00", "37.50", "0.00"),
                                           Term(2, LocalDate.of(2020, 10, 1), LocalDate.of(2022, 9, 30), "0.00", "0.00", "0.00"))

    def tonyNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112060", "50.00", "75.00", " ", None, None, "00", "00", tonyCIM, "Tony", "Loveday", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", tonyTerms)

    val monikaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))

    val monikaTerms: List[Term] = List[Term](Term(1, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 2, 29), "0.00", "0.00", "0.00"),
                                             Term(2, LocalDate.of(2020, 3, 1), LocalDate.of(2022, 2, 28), "0.00", "0.00", "0.00"))

    def monikaNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112061", "0.00", "0.00", " ", None, None, "00", "00", monikaCIM, "Monika", "Detavoidskiene", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", monikaTerms)

    val happyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

    val happyTerms: List[Term] = List[Term](Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "1200.00", "600.00", "0.00"),
                                            Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "2400.00", "600.00", "0.00"))

    def happyNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112062", "0.00", "2400.00", " ", None, None, "00", "00", happyCIM, "Happy", "Saver", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", happyTerms)

    val takenCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))

    val takenTerms: List[Term] = List[Term](Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "1200.00", "600.00", "0.00"),
                                            Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "0.00", "0.00", "0.00"))

    def takenNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112063", "0.00", "0.00", " ", None, None, "00", "00", takenCIM, "Taken", "Out", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", takenTerms)

    val spencerCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("40.00", "50.00", LocalDate.of(2018, 3, 31))

    val spencerTerms: List[Term] = List[Term](Term(1, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "832.00", "416.00", "0.00"),
                                              Term(2, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 2, 29), "0.00", "0.00", "0.00"))

    def spencerNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112064", "0.00", "832.00", " ", None, None, "00", "00", spencerCIM, "Spencer", "Waller", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", spencerTerms)

    val alexCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("13.00", "50.00", LocalDate.of(2018, 3, 31))

    val alexTerms: List[Term] = List[Term](Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "900.00", "450.00", "0.00"),
                                           Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "1270.00", "185.00", "0.00"))

    def alexNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112065", "0.00", "1270.00", " ", None, None, "00", "00", alexCIM, "Alex", "Millar", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", alexTerms)

    val annaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))

    val annaTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 10, 1), LocalDate.of(2019, 9, 30), "75.00", "37.50", "0.00"),
                                           Term(2, LocalDate.of(2019, 10, 1), LocalDate.of(2021, 9, 30), "0.00", "0.00", "0.00"))

    def annaNSIResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112066", "0.00", "75.00", " ", None, None, "00", "00", annaCIM, "Anna", "Smith", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", None, "801497", annaTerms)

    val closedCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

    val closedTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "200.00", "100.00", "0.00"),
                                             Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00"))

    def closedAccountResponse(correlationId: Option[String]): NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", correlationId,
      "1100000112067", "0.00", "0.00", "C", Some(LocalDate.of(2018, 3, 5)), Some("200.00"), "00", "00", closedCIM,
      "Closed", "Account", LocalDate.of(1963, 11, 1), "Line 1", "Line 2",
      " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mrs C Account", None, "801497", closedTerms)
  }
}
