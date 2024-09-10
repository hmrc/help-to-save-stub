/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse, NSIGetAccountByNinoResponse}

object NSIGetAccountBehaviour {
  // linter:ignore // ignores all warnings
  def getAccountByNino(nino: String, correlationId: Option[String]): Either[ErrorDetails, NSIGetAccountByNinoResponse] = // scalastyle:ignore cyclomatic.complexity line.size.limit
    nino match {
      case n if (n.startsWith("AA") || n.startsWith("AB")) || n.startsWith("BE") =>
        Right(NSIGetAccountByNinoResponse.bethNSIResponse(correlationId))
      case n if (n.startsWith("EM200") || n.startsWith("EL07")) || n.startsWith("AC") || n.startsWith("AS409") =>
        Right(NSIGetAccountByNinoResponse.bethNSIResponse(correlationId))
      case n if n.startsWith("EM002") => Left(NSIErrorResponse.missingVersionError)
      case n if n.startsWith("EM003") => Left(NSIErrorResponse.unsupportedVersionError)
      case n if n.startsWith("EM004") => Left(NSIErrorResponse.missingNinoError)
      case n if n.startsWith("EM005") => Left(NSIErrorResponse.badNinoError)
      case n if n.startsWith("EM006") => Left(NSIErrorResponse.unknownNinoError)
      case n if n.startsWith("EM012") => Left(NSIErrorResponse.missingSystemIdError)
      case n if n.startsWith("EM0") && n.endsWith("001A") =>
        Right(NSIGetAccountByNinoResponse.bethNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("002A") =>
        Right(NSIGetAccountByNinoResponse.peteNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("003A") =>
        Right(NSIGetAccountByNinoResponse.lauraNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("004A") =>
        Right(NSIGetAccountByNinoResponse.tonyNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("005A") =>
        Right(NSIGetAccountByNinoResponse.monikaNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("006A") =>
        Right(NSIGetAccountByNinoResponse.happyNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("007A") =>
        Right(NSIGetAccountByNinoResponse.takenNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("008A") =>
        Right(NSIGetAccountByNinoResponse.spencerNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("009A") =>
        Right(NSIGetAccountByNinoResponse.alexNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("010A") =>
        Right(NSIGetAccountByNinoResponse.closedAccountResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("011A") =>
        Right(NSIGetAccountByNinoResponse.accountBlockedResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("012A") =>
        Right(NSIGetAccountByNinoResponse.clientBlockedResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("013A") =>
        Right(NSIGetAccountByNinoResponse.closedAccount2Response(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("014A") =>
        Right(NSIGetAccountByNinoResponse.closedAccount3Response(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("015A") =>
        Right(NSIGetAccountByNinoResponse.closedAccount4Response(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("016A") =>
        Right(NSIGetAccountByNinoResponse.accountUnspecifiedBlockedResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("099A") =>
        Right(NSIGetAccountByNinoResponse.positiveBonusZeroBalanceResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("098A") =>
        Right(NSIGetAccountByNinoResponse.zeroBonusPositiveBalanceResponse(correlationId))
      case n if n.startsWith("TM7") && n.endsWith("915A") =>
        Right(NSIGetAccountByNinoResponse.annaNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("017A") =>
        Right(NSIGetAccountByNinoResponse.tomNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("018A") =>
        Right(NSIGetAccountByNinoResponse.angelaNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("019A") =>
        Right(NSIGetAccountByNinoResponse.ivoNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("020A") =>
        Right(NSIGetAccountByNinoResponse.arsenyNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("021A") =>
        Right(NSIGetAccountByNinoResponse.sunanNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("022A") =>
        Right(NSIGetAccountByNinoResponse.ranaNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("023A") =>
        Right(NSIGetAccountByNinoResponse.marshalNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("024A") =>
        Right(NSIGetAccountByNinoResponse.dennisNSIResponse(correlationId))
      case n if n.startsWith("EM0") && n.endsWith("025A") =>
        Right(NSIGetAccountByNinoResponse.dennisNSIResponse(correlationId, "C"))
      case _ => Left(NSIErrorResponse.unknownNinoError)
    }
}
