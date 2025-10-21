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
  def getAccountByNino(nino: String): Either[ErrorDetails, NSIGetAccountByNinoResponse] = // scalastyle:ignore cyclomatic.complexity line.size.limit
    nino match {
      case n if (n.startsWith("AA") || n.startsWith("AB")) || n.startsWith("BE") =>
        Right(NSIGetAccountByNinoResponse.bethNSIResponse())
      case n if (n.startsWith("EM200") || n.startsWith("EL07")) || n.startsWith("AC") || n.startsWith("AS409") =>
        Right(NSIGetAccountByNinoResponse.bethNSIResponse())
      case n if n.startsWith("EM002") => Left(NSIErrorResponse.missingVersionError)
      case n if n.startsWith("EM003") => Left(NSIErrorResponse.unsupportedVersionError)
      case n if n.startsWith("EM004") => Left(NSIErrorResponse.missingNinoError)
      case n if n.startsWith("EM005") => Left(NSIErrorResponse.badNinoError)
      case n if n.startsWith("EM006") => Left(NSIErrorResponse.unknownNinoError)
      case n if n.startsWith("EM012") => Left(NSIErrorResponse.missingSystemIdError)
      case n if n.startsWith("EM0") && n.endsWith("001A") =>
        Right(NSIGetAccountByNinoResponse.bethNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("002A") =>
        Right(NSIGetAccountByNinoResponse.peteNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("003A") =>
        Right(NSIGetAccountByNinoResponse.lauraNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("004A") =>
        Right(NSIGetAccountByNinoResponse.tonyNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("005A") =>
        Right(NSIGetAccountByNinoResponse.monikaNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("006A") =>
        Right(NSIGetAccountByNinoResponse.happyNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("007A") =>
        Right(NSIGetAccountByNinoResponse.takenNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("008A") =>
        Right(NSIGetAccountByNinoResponse.spencerNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("009A") =>
        Right(NSIGetAccountByNinoResponse.alexNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("010A") =>
        Right(NSIGetAccountByNinoResponse.closedAccountResponse())
      case n if n.startsWith("EM0") && n.endsWith("011A") =>
        Right(NSIGetAccountByNinoResponse.accountBlockedResponse())
      case n if n.startsWith("EM0") && n.endsWith("012A") =>
        Right(NSIGetAccountByNinoResponse.clientBlockedResponse())
      case n if n.startsWith("EM0") && n.endsWith("013A") =>
        Right(NSIGetAccountByNinoResponse.closedAccount2Response())
      case n if n.startsWith("EM0") && n.endsWith("014A") =>
        Right(NSIGetAccountByNinoResponse.closedAccount3Response())
      case n if n.startsWith("EM0") && n.endsWith("015A") =>
        Right(NSIGetAccountByNinoResponse.closedAccount4Response())
      case n if n.startsWith("EM0") && n.endsWith("016A") =>
        Right(NSIGetAccountByNinoResponse.accountUnspecifiedBlockedResponse())
      case n if n.startsWith("EM0") && n.endsWith("099A") =>
        Right(NSIGetAccountByNinoResponse.positiveBonusZeroBalanceResponse())
      case n if n.startsWith("EM0") && n.endsWith("098A") =>
        Right(NSIGetAccountByNinoResponse.zeroBonusPositiveBalanceResponse())
      case n if n.startsWith("TM7") && n.endsWith("915A") =>
        Right(NSIGetAccountByNinoResponse.annaNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("017A") =>
        Right(NSIGetAccountByNinoResponse.tomNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("018A") =>
        Right(NSIGetAccountByNinoResponse.angelaNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("019A") =>
        Right(NSIGetAccountByNinoResponse.ivoNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("020A") =>
        Right(NSIGetAccountByNinoResponse.arsenyNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("021A") =>
        Right(NSIGetAccountByNinoResponse.sunanNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("022A") =>
        Right(NSIGetAccountByNinoResponse.ranaNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("023A") =>
        Right(NSIGetAccountByNinoResponse.marshalNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("024A") =>
        Right(NSIGetAccountByNinoResponse.dennisNSIResponse())
      case n if n.startsWith("EM0") && n.endsWith("025A") =>
        Right(NSIGetAccountByNinoResponse.dennisNSIResponse( "C"))
      case _ => Left(NSIErrorResponse.unknownNinoError)
    }
}
