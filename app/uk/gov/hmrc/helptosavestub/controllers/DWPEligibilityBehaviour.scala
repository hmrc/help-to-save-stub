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

import uk.gov.hmrc.helptosavestub.controllers.DWPController.UCDetails
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckController.EligibilityCheckResult

import scala.util.Try

trait DWPEligibilityBehaviour {
  import DWPEligibilityBehaviour._

  def getProfile(nino: String): Option[Profile] = // scalastyle:ignore
    if (nino.startsWith("WP9911")) {
      Some(Profile(None, Some(eligibleResult(7))))
    } else if (nino.startsWith("WP9999")) {
      Some(Profile(None, None))
    } else if (nino.startsWith("WP99")) {
      Some(Profile(None, Some(unknownResult(2))))
    } else if (nino.startsWith("WP0011") || nino.startsWith("BJ825714")) {
      Some(Profile(notUCClaimant, eligibleResult(7)))
    } else if (nino.startsWith("WP1011") || nino.startsWith("KS384413")) {
      Some(Profile(isUCClaimantAndNotEarningEnough, eligibleResult(7)))
    } else if (nino.startsWith("WP1111") || nino.startsWith("EX535913")) {
      Some(Profile(isUCClaimantAndEarningEnough, eligibleResult(8)))
    } else if (nino.startsWith("WP0010") || nino.startsWith("ZX368514")) {
      Some(Profile(notUCClaimant, ineligibleResult(3)))
    } else if (nino.startsWith("WP1010") || nino.startsWith("EK978215")) {
      Some(Profile(isUCClaimantAndNotEarningEnough, ineligibleResult(4)))
    } else if (nino.startsWith("WP00") || nino.startsWith("LW634114")) {
      Some(Profile(notUCClaimant, ineligibleResult(9)))
    } else if (nino.startsWith("WP10") || nino.startsWith("HG737615")) {
      Some(Profile(isUCClaimantAndNotEarningEnough, ineligibleResult(5)))
    } else if (nino.startsWith("WP11") || nino.startsWith("LX405614") || nino.startsWith("GH987015")) {
      Some(Profile(isUCClaimantAndEarningEnough, eligibleResult(6)))
    } else {
      None
    }

  val isUCClaimantAndEarningEnough: UCDetails = UCDetails("Y", Some("Y"))
  val isUCClaimantAndNotEarningEnough: UCDetails = UCDetails("Y", Some("N"))
  val notUCClaimant: UCDetails = UCDetails("N", None)

  val reasonMappings: Map[Int, String] = Map(
    1 → "HtS account was previously created",
    2 → "Not entitled to WTC and UC not checked",
    3 → "Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and not in receipt of UC",
    4 → "Entitled to WTC but not in receipt of positive WTC/CTC Tax Credit (nil TC) and in receipt of UC but income is insufficient",
    5 → "Not entitled to WTC and in receipt of UC but income is insufficient",
    6 → "In receipt of UC and income sufficient",
    7 → "Entitled to WTC and in receipt of positive WTC/CTC Tax Credit",
    8 → "Entitled to WTC and in receipt of positive WTC/CTC Tax Credit and in receipt of UC and income sufficient",
    9 → "Not entitled to WTC and not in receipt of UC"
  )

  val resultMappings: Map[Int, String] = Map(
    1 → "Eligible to HtS Account",
    2 → "Ineligible to HtS Account",
    3 → "HtS account already exists",
    4 → "Unknown eligibility because call to DWP failed",
    99 → "INVALID RESULT WHICH DES SHOULD NEVER SEND"
  )

  val alreadyHasAccountResult: EligibilityCheckResult =
    EligibilityCheckResult("HtS account already exists", 3, "HtS account was previously created", 1)

  val invalidResultCode: EligibilityCheckResult =
    EligibilityCheckResult("INVALID RESULT WHICH DES SHOULD NEVER SEND", 99, "Not entitled to WTC and not in receipt of UC", 2)

  def eligibleResult(reasonCode: Int): EligibilityCheckResult = {
    val reason = reasonMappings.getOrElse(reasonCode, sys.error(s"Could not find eligibility reason for code $reasonCode"))
    EligibilityCheckResult("Eligible to HtS Account", 1, reason, reasonCode)
  }

  def ineligibleResult(reasonCode: Int): EligibilityCheckResult = {
    val reason = reasonMappings.getOrElse(reasonCode, sys.error(s"Could not find eligibility reason for code $reasonCode"))
    EligibilityCheckResult("Ineligible to HtS Account", 2, reason, reasonCode)
  }
  def unknownResult(reasonCode: Int): EligibilityCheckResult = {
    val reason = reasonMappings.getOrElse(reasonCode, sys.error(s"Could not find eligibility reason for code $reasonCode"))
    EligibilityCheckResult("Unknown eligibility because call to DWP failed", 4, reason, reasonCode)
  }

  def getReasonCodeFromNino(nino: String): Int =
    Try(nino.substring(3, 4).toInt)
      .getOrElse(sys.error(s"Error getting reason code from fourth character of NINO $nino"))

}

object DWPEligibilityBehaviour {

  case class Profile(uCDetails:             Option[UCDetails],
                     eligibiltyCheckResult: Option[EligibilityCheckResult])

  object Profile {

    def apply(uCDetails:             UCDetails,
              eligibiltyCheckResult: EligibilityCheckResult): Profile =
      Profile(Some(uCDetails), Some(eligibiltyCheckResult))
  }

}
