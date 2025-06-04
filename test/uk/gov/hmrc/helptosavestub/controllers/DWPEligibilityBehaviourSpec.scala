/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.helptosavestub.controllers.DWPEligibilityBehaviour.Profile

class DWPEligibilityBehaviourSpec extends AnyWordSpec with Matchers with DWPEligibilityBehaviour {

  "DWPEligibilityBehaviour" should {
    "return eligible profile for NINO starting with AA" in {
      val result = getProfile("AA123456A")
      result shouldBe Some(Profile(Some(isUCClaimantAndEarningEnough), Some(eligibleResult(7))))
    }


    "return eligible profile with no UC details for NINO starting with WP9911" in {
      val result = getProfile("WP9911XYZ")
      result shouldBe Some(Profile(None, Some(eligibleResult(7))))
    }

    "return None for unknown NINO pattern" in {
      val result = getProfile("ZZ123456Z")
      result shouldBe None
    }

    "return ineligible profile for NINO starting with WP1010" in {
      val result = getProfile("WP1010ABC")
      result shouldBe Some(Profile(Some(isUCClaimantAndNotEarningEnough), Some(ineligibleResult(4))))
    }

    "return account already exists for NINO starting with SE03" in {
      val result = getProfile("SE031234A")
      result shouldBe Some(Profile(Some(notUCClaimant), Some(accountAlreadyExists)))
    }

    "return eligible profile with UC and sufficient income for NINO starting with WP11" in {
      val result = getProfile("WP112345A")
      result shouldBe Some(Profile(Some(isUCClaimantAndEarningEnough), Some(eligibleResult(6))))
    }

    "return None for NINO starting with WP9999" in {
      val result = getProfile("WP9999ABC")
      result shouldBe Some(Profile(None, None))
    }

    "return unknown result for NINO starting with WP99" in {
      val result = getProfile("WP991234A")
      result shouldBe Some(Profile(None, Some(unknownResult(2))))
    }

    "return eligible profile for NINO starting with WP0011" in {
      val result = getProfile("WP0011XYZ")
      result shouldBe Some(Profile(Some(notUCClaimant), Some(eligibleResult(7))))
    }

    "return eligible profile for NINO starting with KS3844" in {
      val result = getProfile("KS3844XYZ")
      result shouldBe Some(Profile(Some(isUCClaimantAndNotEarningEnough), Some(eligibleResult(7))))
    }

    "return eligible profile for NINO starting with KA66 and ending with 81A" in {
      val result = getProfile("KA66123481A")
      result shouldBe Some(Profile(Some(isUCClaimantAndEarningEnough), Some(eligibleResult(8))))
    }

    "return ineligible profile for NINO starting with BK64 and ending with 58A" in {
      val result = getProfile("BK64123458A")
      result shouldBe Some(Profile(Some(notUCClaimant), Some(ineligibleResult(3))))
    }

    "return ineligible profile for NINO starting with LA83 and ending with 15B" in {
      val result = getProfile("LA83123415B")
      result shouldBe Some(Profile(Some(isUCClaimantAndNotEarningEnough), Some(ineligibleResult(4))))
    }

    "return ineligible profile for NINO starting with WP00" in {
      val result = getProfile("WP001234A")
      result shouldBe Some(Profile(Some(notUCClaimant), Some(ineligibleResult(9))))
    }

    "return ineligible profile for NINO starting with HR1566" in {
      val result = getProfile("HR1566XYZ")
      result shouldBe Some(Profile(Some(isUCClaimantAndNotEarningEnough), Some(ineligibleResult(5))))
    }

    "return eligible profile for NINO starting with EX5359" in {
      val result = getProfile("EX5359XYZ")
      result shouldBe Some(Profile(Some(isUCClaimantAndEarningEnough), Some(eligibleResult(6))))
    }

    "return account already exists for NINO starting with LA83 and ending with 16B" in {
      val result = getProfile("LA83123416B")
      result shouldBe Some(Profile(Some(notUCClaimant), Some(accountAlreadyExists)))
    }
  }
}