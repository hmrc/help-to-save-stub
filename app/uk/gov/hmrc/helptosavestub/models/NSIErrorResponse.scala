package uk.gov.hmrc.helptosavestub.models

import play.api.libs.json.Json

object NSIErrorResponse {


  val missingVersionResponse = Json.parse(
    """{
      |"version": "V1.0",
      |  "correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
      |  "error":
      |    {
      |      "errorMessageId": "HTS-API015-002",
      |      "errorMessage": "Missing version.",
      |      "errorDetail": "Field: version"
      |    }
      |}""")

  val unsupportedVersionResponse = Json.parse(
    """{
      |  "version": "V1.0",
      |  "correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
      |  "error":
      |    {
      |      "errorMessageId": "HTS-API015-003",
      |      "errorMessage": "Unsupported service version. Expected V1.0, received v1",
      |      "errorDetail": "Field: version"
      |    }
      |}""")


  val missingNinoResponse = Json.parse(
    """{
      |  "version": "V1.0",
      |  "correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
      |  "error":
      |    {
      |      "errorMessageId": "HTS-API015-004",
      |      "errorMessage": "Missing NINO.",
      |      "errorDetail": "Field: NINO"
      |    }
      |}""")

  val badNinoResponse = Json.parse(
    """{
      |  "version": "V1.0",
      |  "correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
      |  "error":
      |    {
      |      "errorMessageId": "HTS-API015-005",
      |      "errorMessage": "Bad NINO.Format is incorrect (XX999999X) for this nino",
      |      "errorDetail": "Field: NINO"
      |    }
      |}""")

  val unknownNinoResponse = Json.parse(
    """{
      |  "version": "V1.0",
      |  "correlationId":"551485a3-001d-91e8-060e-890c40505bd7",
      |  "error":
      |    {
      |      "errorMessageId": "HTS-API015-006",
      |      "errorMessage": "Unknown NINO. No HTS account found for this nino",
      |      "errorDetail": "Field: NINO"
      |    }
      |}""")

  }

