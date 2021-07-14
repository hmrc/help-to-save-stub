/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.implicits._
import uk.gov.hmrc.helptosavestub.controllers.BARSController.{BARSResponse, BankDetails}
import uk.gov.hmrc.helptosavestub.controllers.BankDetailsBehaviour.{CreateAccountResponse, Profile}
import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse}

import scala.util.{Success, Try}

trait BankDetailsBehaviour {

  val allowedSeparators: Set[Char] = Set(' ', '-', '–', '−', '—')

  def getBankProfile(bankDetails: BankDetails): Profile = {
    val accountNumberWithSortCodeIsValid = bankDetails match {
      case BankDetails(_, accountNumber) if accountNumber.startsWith("9")         => Some("no")
      case BankDetails(_, accountNumber) if accountNumber.startsWith("5")         => None
      case BankDetails(sortCode, _) if !validateSortCode(sortCode)                => Some("no")
      case BankDetails(_, accountNumber) if !validateAccountNumber(accountNumber) => Some("no")
      case _                                                                      => Some("yes")
    }

    val sortCodeIsPresentOnEISCD =
      bankDetails match {
        case BankDetails(_, accountNumber) if accountNumber.startsWith("9") => "no"
        case BankDetails(_, accountNumber) if accountNumber.startsWith("5") => "whoopsie"
        case _                                                              => "yes"
      }

    val barsResponse =
      accountNumberWithSortCodeIsValid.map(a ⇒ BARSResponse(a, sortCodeIsPresentOnEISCD))

    bankDetails match {
      case BankDetails(_, accountNumber) if accountNumber.startsWith("707") =>
        Profile(barsResponse, CreateAccountResponse(Left(NSIErrorResponse.incorrectAccountNumber)))
      case BankDetails(sortCode, _) if sortCode.startsWith("70-3") =>
        Profile(barsResponse, CreateAccountResponse(Left(NSIErrorResponse.incorrectSortCode)))
      case _ =>
        Profile(barsResponse, CreateAccountResponse(Right(())))
    }
  }

  // same validation as bank-account-reputation
  private def validateSortCode(sc: String): Boolean =
    if (sc.length =!= 6) {
      false
    } else {
      Try(sc.toInt) match {
        case Success(_) ⇒ true
        case _ ⇒ false
      }
    }

  // same validation as bank-account-reputation
  private def validateAccountNumber(ac: String): Boolean =
    if (ac.length =!= 8) {
      false
    } else {
      Try(ac.toInt) match {
        case Success(_) ⇒ true
        case _ ⇒ false
      }
    }

}

object BankDetailsBehaviour {

  case class CreateAccountResponse(response: Either[ErrorDetails, Unit])

  case class Profile(barsResponse: Option[BARSResponse], createAccountResponse: CreateAccountResponse)

}
