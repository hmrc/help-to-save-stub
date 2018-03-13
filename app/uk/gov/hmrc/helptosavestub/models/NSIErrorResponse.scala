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

package uk.gov.hmrc.helptosavestub.models

import java.util.UUID

import play.api.libs.json.{Format, Json}

case class NSIErrorResponse(version: String, correlationId: String, error: ErrorDetails)

case class ErrorDetails(errorMessageId: String, errorMessage: String, errorDetail: String)

object ErrorDetails {

  implicit val format: Format[ErrorDetails] = Json.format[ErrorDetails]
}

object NSIErrorResponse {

  implicit val format: Format[NSIErrorResponse] = Json.format[NSIErrorResponse]

  //val correlationId: UUID = UUID.randomUUID()

  val missingVersionError: ErrorDetails = ErrorDetails("HTS-API015-002", "Missing version.", "Field: version")
  def missingVersionResponse(correlationId: String): NSIErrorResponse = NSIErrorResponse("V1.0", correlationId, missingVersionError)

  val unsupportedVersionError: ErrorDetails = ErrorDetails("HTS-API015-003", "Unsupported service version. Expected V1.0, received v1", "Field: version")
  def unsupportedVersionResponse(correlationId: String): NSIErrorResponse = NSIErrorResponse("V1.0", correlationId, unsupportedVersionError)

  val missingNinoError: ErrorDetails = ErrorDetails("HTS-API015-004", "Missing NINO.", "Field: NINO")
  def missingNinoResponse(correlationId: String): NSIErrorResponse = NSIErrorResponse("V1.0", correlationId, missingNinoError)

  val badNinoError: ErrorDetails = ErrorDetails("HTS-API015-005", "Bad NINO.Format is incorrect (XX999999X) for this nino", "Field: NINO")
  def badNinoResponse(correlationId: String): NSIErrorResponse = NSIErrorResponse("V1.0", correlationId, badNinoError)

  val unknownNinoError: ErrorDetails = ErrorDetails("HTS-API015-006", "Unknown NINO. No HTS account found for this nino", "Field: NINO")
  def unknownNinoResponse(correlationId: String): NSIErrorResponse = NSIErrorResponse("V1.0", correlationId, unknownNinoError)

  val missingSystemIdError: ErrorDetails = ErrorDetails("HTS-API015-007", "Missing systemId.", "Field: SystemId")
  def missingSystemIdResponse(correlationId: String): NSIErrorResponse = NSIErrorResponse("V1.0", correlationId, missingSystemIdError)

  val unsupportedSystemIdError: ErrorDetails = ErrorDetails("HTS-API015-008", "Unsupported systemId ", "Field: systemId")
  def unsupportedSystemIdResponse(correlationId: String): NSIErrorResponse = NSIErrorResponse("V1.0", correlationId, unsupportedSystemIdError)

}

