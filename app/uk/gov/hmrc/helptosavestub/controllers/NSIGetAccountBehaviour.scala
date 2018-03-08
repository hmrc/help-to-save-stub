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
import ai.x.play.json.Jsonx
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.helptosavestub.models.NSIErrorResponse

import scala.concurrent.ExecutionContext

trait NSIGetAccountBehaviour {
  import NSIGetAccountBehaviour._

  def getErrorResponse(nino: String)(implicit ec: ExecutionContext): Option[NSIErrorResponse] =
    if (nino.startsWith("EC01")) {
      Some(NSIErrorResponse.missingVersionResponse)
    } else if (nino.startsWith("EC02")) {
      Some(NSIErrorResponse.unsupportedVersionResponse)
    } else if (nino.startsWith("EC03")) {
      Some(NSIErrorResponse.missingNinoResponse)
    } else if (nino.startsWith("EC04")) {
      Some(NSIErrorResponse.badNinoResponse)
    } else if (nino.startsWith("EC05")) {
      Some(NSIErrorResponse.unknownNinoResponse)
    } else {
      None
    }

  //Need to update error NINOs when they have been finalized
  def getAccountByNino(nino: String)(implicit ec: ExecutionContext): Option[NSIGetAccountByNinoResponse] =
    if (nino.equals("EM000001A")) {
      Some(NSIGetAccountByNinoResponse.bethNSIResponse)
    } else if (nino.equals("EM000002A")) {
      Some(NSIGetAccountByNinoResponse.peteResponse)
    } else if (nino.equals("EM000003A")) {
      Some(NSIGetAccountByNinoResponse.lauraResponse)
    } else if (nino.equals("EM000004A")) {
      Some(NSIGetAccountByNinoResponse.tonyResponse)
    } else if (nino.equals("EM000005A")) {
      Some(NSIGetAccountByNinoResponse.monikaResponse)
    } else if (nino.equals("EM000006A")) {
      Some(NSIGetAccountByNinoResponse.happyResponse)
    } else if (nino.equals("EM000007A")) {
      Some(NSIGetAccountByNinoResponse.takenResponse)
    } else if (nino.equals("EM000008A")) {
      Some(NSIGetAccountByNinoResponse.spencerResponse)
    } else if (nino.equals("EM000009A")) {
      Some(NSIGetAccountByNinoResponse.alexResponse)
    } else if (nino.equals("TM739915A")) {
      Some(NSIGetAccountByNinoResponse.annaResponse)
    } else {
      None
    }

}

object NSIGetAccountBehaviour {

  case class NSIGetAccountByNinoResponse(version:                   String,
                                         correlationId:             UUID,
                                         accountNumber:             Long,
                                         availableWithdrawal:       String,
                                         accountBalance:            String,
                                         accountClosedFlag:         Boolean,
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
                                         postCode:                  String,
                                         countryCode:               String,
                                         emailAddress:              String,
                                         commsPreference:           String,
                                         clientBlockingCode:        String,
                                         clientBlockingReasonCode:  String,
                                         clientCancellationStatus:  String,
                                         nbaAccountNumber:          String,
                                         nbaPayee:                  String,
                                         nbaRollNumber:             String,
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

    implicit lazy val format: Format[NSIGetAccountByNinoResponse] = Jsonx.formatCaseClass[NSIGetAccountByNinoResponse]

    //    def apply(version: String, correlationId: UUID, accountNumber: Long, availableWithdrawal: String, accountBalance: String,
    //              accountClosedFlag: Boolean, accountBlockingCode: String, accountBlockingReasonCode: String,
    //              currentInvestmentMonth: CurrentInvestmentMonth, clientForename: String, clientSurname: String, dateOfBirth: LocalDate,
    //              addressLine1: String, addressLine2: String, addressLine3: String, addressLine4: String, addressLine5: String,
    //              postCode: String, countryCode: String, emailAddress: String, commsPreference: String, clientBlockingCode: String,
    //              clientBlockingReasonCode: String, clientCancellationStatus: String, nbaAccountNumber: String, nbaPayee: String,
    //              nbaRollNumber: String, nbaSortCode: String, terms: Array[Term]): NSIGetAccountByNinoResponse =
    //      NSIGetAccountByNinoResponse(version, correlationId, accountNumber, availableWithdrawal, accountBalance,
    //                                  accountClosedFlag, accountBlockingCode, accountBlockingReasonCode,
    //                                  currentInvestmentMonth, clientForename, clientSurname, dateOfBirth,
    //                                  addressLine1, addressLine2, addressLine3, addressLine4, addressLine5,
    //                                  postCode, countryCode, emailAddress, commsPreference, clientBlockingCode,
    //                                  clientBlockingReasonCode, clientCancellationStatus, nbaAccountNumber, nbaPayee,
    //                                  nbaRollNumber, nbaSortCode, terms)

    val bethCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

    val bethTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 11, 1), "200.00", "100.00", "0.00"),
                                           Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 11, 1), "0.00", "0.00", "0.00"))

    val bethNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", UUID("551485a3-001d-91e8-060e-890c40505bd7"), 1100000112057l, "175.00",
    "200.00", false, "00", "00", bethCIM, "Beth", "Planner", LocalDate.of(1963, 11, 1), "Line 1", "Line 2", " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
    "02", "00", "00", " ", "11111111", "Mrs B Planner", " ", "801497", bethTerms)


    val peteCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("9.88", "50.00", LocalDate.of(2018, 3, 31))

    val peteTerms: List[Term] = List[Term](Term(1, LocalDate.of(2017, 9, 1), LocalDate.of(2019, 9, 1), "190.12", "95.06", "0.00"),
                                           Term(2, LocalDate.of(2019, 9, 1), LocalDate.of(2021, 9, 1), "0.00", "0.00", "0.00"))

    val peteNSIResponse: NSIGetAccountByNinoResponse = NSIGetAccountByNinoResponse("V1.0", UUID("551485a3-001d-91e8-060e-890c40505bd7"), 1100000112057l, "165.12",
      "190.12", false, "00", "00", peteCIM, "Pete", "Loveday", LocalDate.of(1963, 11, 1), "Line 1", "Line 2", " ", " ", " ", "SV1 1QA", "GB", "email.address@domain.com",
      "02", "00", "00", " ", "11111111", "Mr P Smith", " ", "801497", peteTerms)

    val peteResponse = Json.parse(
      """
        |{
        |      "version":"V1.0",
        |      "correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |      "accountNumber":"1100000112057",
        |      "availableWithdrawal":"165.12",
        |      "accountBalance":"190.12",
        |      "accountClosedFlag":" ",
        |      "accountBlockingCode":"00",
        |      "accountBlockingReasonCode":"00",
        |      "currentInvestmentMonth": {
        |        "investmentRemaining": "9.88",
        |        "investmentLimit": "50.00",
        |        "endDate": "2018-03-31"
        |      },
        |
        |      "clientForename":"Pete",
        |      "clientSurname":"Loveday",
        |      "dateOfBirth":"1963-11-01",
        |      "addressLine1":"Line 1",
        |      "addressLine2":"Line 2",
        |      "addressLine3":" ",
        |      "addressLine4":" ",
        |      "addressLine5":" ",
        |      "postCode":"SV1 1QA",
        |      "countryCode":"GB",
        |      "emailAddress":"email.address@domain.com",
        |      "commsPreference":"02",
        |      "clientBlockingCode":"00",
        |      "clientBlockingReasonCode":"00",
        |      "clientCancellationStatus":" ",
        |
        |      "nbaAccountNumber":"11111111",
        |      "nbaPayee":"Mr J Smith",
        |      "nbaRollNumber":" ",
        |      "nbaSortCode":"801497",
        |
        |      "terms": [
        |      {
        |        "termNumber":"1",
        |        "startDate":"2017-09-01",
        |        "endDate":"2019-09-01",
        |        "maxBalance":"190.12",
        |        "bonusEstimate":"95.06",
        |        "bonusPaid":"0.00"
        |      },
        |      {
        |        "termNumber":"2",
        |        "startDate":"2019-09-01",
        |        "endDate":"2021-09-01",
        |        "maxBalance":"0.00",
        |        "bonusEstimate":"0.00",
        |        "bonusPaid":"0.00"
        |      }
        |      ]
        |    }
      """.stripMargin)

    val lauraResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"110.00",
        |	"accountBalance":"135.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "50.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Laura",
        |	"clientSurname":"Detavoidskiene",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mr J Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2017-03-01",
        |			"endDate":"2019-03-01",
        |			"maxBalance":"135.00",
        |			"bonusEstimate":"67.50",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2019-03-01",
        |			"endDate":"2021-03-01",
        |			"maxBalance":"0.00",
        |			"bonusEstimate":"0.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )

    val tonyResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"50.00",
        |	"accountBalance":"75.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "45.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Tony",
        |	"clientSurname":"Loveday",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mr J Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2018-10-01",
        |			"endDate":"2020-10-01",
        |			"maxBalance":"75.00",
        |			"bonusEstimate":"37.50",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2020-10-01",
        |			"endDate":"2022-10-01",
        |			"maxBalance":"0.00",
        |			"bonusEstimate":"0.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )

    val monikaResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"0.00",
        |	"accountBalance":"0.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "50.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Monika",
        |	"clientSurname":"Detavoidskiene",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mr J Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2018-03-01",
        |			"endDate":"2020-03-01",
        |			"maxBalance":"0.00",
        |			"bonusEstimate":"0.00",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2020-03-01",
        |			"endDate":"2022-03-01",
        |			"maxBalance":"0.00",
        |			"bonusEstimate":"0.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )

    val happyResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"0.00",
        |	"accountBalance":"0.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "0.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Happy",
        |	"clientSurname":"Saver",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mr J Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2014-03-01",
        |			"endDate":"2016-03-01",
        |			"maxBalance":"1200.00",
        |			"bonusEstimate":"600.00",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2016-03-01",
        |			"endDate":"2018-03-01",
        |			"maxBalance":"2400.00",
        |			"bonusEstimate":"600.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )

    val takenResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"0.00",
        |	"accountBalance":"0.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "50.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Taken",
        |	"clientSurname":"Out",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mr J Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2014-03-01",
        |			"endDate":"2016-03-01",
        |			"maxBalance":"1200.00",
        |			"bonusEstimate":"600.00",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2016-03-01",
        |			"endDate":"2018-03-01",
        |			"maxBalance":"0.00",
        |			"bonusEstimate":"0.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )

    val spencerResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"0.00",
        |	"accountBalance":"0.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "40.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Spencer",
        |	"clientSurname":"Waller",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mr J Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2016-03-01",
        |			"endDate":"2018-03-01",
        |			"maxBalance":"832.00",
        |			"bonusEstimate":"416.00",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2018-03-01",
        |			"endDate":"2020-03-01",
        |			"maxBalance":"0.00",
        |			"bonusEstimate":"0.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )

    val alexResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"0.00",
        |	"accountBalance":"0.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "13.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Alex",
        |	"clientSurname":"Millar",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mr J Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2015-02-01",
        |			"endDate":"2017-02-01",
        |			"maxBalance":"900.00",
        |			"bonusEstimate":"450.00",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2017-02-01",
        |			"endDate":"2019-02-01",
        |			"maxBalance":"1270.00",
        |			"bonusEstimate":"185.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )

    val annaResponse = Json.parse(
      """
        |{
        |	"version":"V1.0",
        |	"correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
        |	"accountNumber":"1100000112057",
        |	"availableWithdrawal":"0.00",
        |	"accountBalance":"0.00",
        |	"accountClosedFlag":" ",
        |	"accountBlockingCode":"00",
        |	"accountBlockingReasonCode":"00",
        |	"currentInvestmentMonth": {
        |		"investmentRemaining": "45.00",
        |		"investmentLimit": "50.00",
        |		"endDate": "2018-03-31"
        |	},
        |
        |	"clientForename":"Anna",
        |	"clientSurname":"Smith",
        |	"dateOfBirth":"1963-11-01",
        |	"addressLine1":"Line 1",
        |	"addressLine2":"Line 2",
        |	"addressLine3":" ",
        |	"addressLine4":" ",
        |	"addressLine5":" ",
        |	"postCode":"SV1 1QA",
        |	"countryCode":"GB",
        |	"emailAddress":"email.address@domain.com",
        |	"commsPreference":"02",
        |	"clientBlockingCode":"00",
        |	"clientBlockingReasonCode":"00",
        |	"clientCancellationStatus":" ",
        |
        |	"nbaAccountNumber":"11111111",
        |	"nbaPayee":"Mrs A Smith",
        |	"nbaRollNumber":" ",
        |	"nbaSortCode":"801497",
        |
        |	"terms": [
        |		{
        |			"termNumber":"1",
        |			"startDate":"2017-10-01",
        |			"endDate":"2019-10-01",
        |			"maxBalance":"75.00",
        |			"bonusEstimate":"37.50",
        |			"bonusPaid":"0.00"
        |		},
        |		{
        |			"termNumber":"2",
        |			"startDate":"2019-10-01",
        |			"endDate":"2021-10-01",
        |			"maxBalance":"0.00",
        |			"bonusEstimate":"0.00",
        |			"bonusPaid":"0.00"
        |		}
        |	]
        |}
      """.stripMargin
    )
  }
}
