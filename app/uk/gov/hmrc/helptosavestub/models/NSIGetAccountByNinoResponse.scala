package uk.gov.hmrc.helptosavestub.models

import play.api.libs.json.Json

object NSIGetAccountByNinoResponse {

  val bethResponse = Json.parse(
    """
      |{
      |      "version":"V1.0",
      |      "correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
      |      "accountNumber":"1100000112057",
      |      "availableWithdrawal":"175.00",
      |      "accountBalance":"200.00",
      |      "accountClosedFlag":" ",
      |      "accountBlockingCode":"00",
      |      "accountBlockingReasonCode":"00",
      |      "currentInvestmentMonth": {
      |        "investmentRemaining": "0.00",
      |        "investmentLimit": "50.00",
      |        "endDate": "2018-03-31"
      |      },
      |
      |      "clientForename":"Beth",
      |      "clientSurname":"Planner",
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
      |        "startDate":"2017-11-01",
      |        "endDate":"2019-11-01",
      |        "maxBalance":"200.00",
      |        "bonusEstimate":"100.00",
      |        "bonusPaid":"0.00"
      |      },
      |      {
      |        "termNumber":"2",
      |        "startDate":"2019-11-01",
      |        "endDate":"2021-11-01",
      |        "maxBalance":"0.00",
      |        "bonusEstimate":"0.00",
      |        "bonusPaid":"0.00"
      |      }
      |      ]
      |    }
    """.stripMargin
  )

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
      |		"investmentRemaining": "55.00",
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
}