/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.json.{ Format, Json }
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController

import smartstub._

object EdhController extends BaseController {

  type Date = String

  // case class NtpApplication(
  //   av_version_type: String,
  //   ind1_number_of_paid_jobs: Int,
  //   tci1_ntc_interest_level: String,
  //   tci1_individual_suspended: String,
  //   emp1_employment_type: String,
  //   emp1_employment_name_1: String,
  //   emp1_employment_name_2: String,
  //   sig1_disabled: String,
  //   sig1_hccdla: String,
  //   sig1_disabled_for_child_care: String,
  //   sig1_uk_national: String,
  //   sig1_residency_signal: String,
  //   sig1_residency_country: String,
  //   sig1_uk_employment: String,
  //   sig1_uk_employment_country: String,
  //   sig1_in_receipt_of_is: String,
  //   sig1_in_receipt_of_jsa_esa: String,
  //   sig1_in_receipt_of_mig: String,
  //   sig1_50_plus_premium_claim: String,
  //   sig1_related_ind_is_alive: String,
  //   sig1_number_of_hours_worked: String,
  //   sig1_total_hours_worked: Int,
  //   sig1_related_ind_lone_parent: String,
  //   pay1_account_name: String,
  //   pay1_bank_build_soc_name: String,
  //   pay1_building_society_ref: String,
  //   ind2_number_of_paid_jobs: Int,
  //   tci2_ntc_interest_level: String,
  //   tci2_individual_suspended: String,
  //   emp2_employment_type: String,
  //   emp2_employment_name_1: String,
  //   emp2_employment_name_2: String,
  //   sig2_disabled: String,
  //   sig2_hccdla: String,
  //   sig2_disabled_for_child_care: String,
  //   sig2_uk_national: String,
  //   sig2_residency_signal: String,
  //   sig2_residency_country: String,
  //   sig2_uk_employment: String,
  //   sig2_uk_employment_country: String,
  //   sig2_in_receipt_of_is: String,
  //   sig2_in_receipt_of_jsa_esa: String,
  //   sig2_in_receipt_of_mig: String,
  //   sig2_50_plus_premium_claim: String,
  //   sig2_related_ind_is_alive: String,
  //   sig2_number_of_hours_worked: String,
  //   sig2_total_hours_worked: Int,
  //   sig2_related_ind_lone_parent: String,
  //   pay2_account_name: String,
  //   pay2_building_society_ref: String
  // )

  case class NtpAward(
    aw_award_status: String, // OFTPCZ
    aw_tax_credit_period_startdate: String,
    aw_tax_credit_period_end_date: String,
    av_total_taper_household_award: Int,
    // av_total_childcare_incurred: Int,
    // av_py_income: Int,
    // av_cy_income: Int,
    ae_etc1_wtc_entitlement: String, // YN
    // ae_icc1_childcare_entitlement: String,
    // ae_icc2_child_entitlement: String,
    // ae_icc3_family_entitlement: String,
    // ae_icc4_baby_entitlement: String,
    av_end_date: String
  )

  // type NtpChild = Any

  case class NtpOutputSchema(
    nino: String,
    // applicantId: Option[Int],
    // application: Option[NtpApplication],
    awards: List[NtpAward]
    // children: List[NtpChild]
  )

  object EligibilityGenerator extends SmartStubGenerator[String, Boolean] {
    import org.scalacheck.Gen._

    def from(in: String): Option[Long] = fromNino(in)

    def generator(in: String) = oneOf(true,false)
  }

  object WtcGenerator extends SmartStubGenerator[String, NtpOutputSchema] {
    import org.scalacheck.Gen._
    def from(in: String): Option[Long] = fromNino(in)

    implicit def toDateString(d: LocalDate): Date =
      d.toString.filter(_ != '-')

    def dateF(f: Int,t: Int) =
      dateGen(f,t).map{_.toString.filter(_ != '-')}

    val genAward = for {
      status <- oneOf("OFTPCZ".toList.map(_.toString))
      household <- choose(-1000,2000)
      entitlement <- oneOf("Y","N")
      endDate <- dateGen(2015,2020)
      periodStartDate <- dateGen(2010,2015)
      periodEndDate <- dateGen(periodStartDate, LocalDate.of(2016,1,1))      
    } yield NtpAward (
      status,
      periodStartDate,
      periodEndDate,      
      household,
      entitlement,
      endDate
    )

    def generator(nino: String) = for {
      noAwards <- choose(0,8)
      awards <- listOfN(noAwards, genAward)
      award <- genAward
    } yield 
      NtpOutputSchema (
        nino,
        //        List(award)
        awards
      )
    
  }


  def uc(nino:String) = Action { implicit request =>

    EligibilityGenerator(nino).map { x => 
      Ok(Json.toJson(x))
    }.getOrElse{
      NotFound
    }
  }

  def wtc(nino:String) = Action { implicit request =>

    implicit val ntpAwardFormat: Format[NtpAward] = Json.format[NtpAward]
    implicit val ntpOutputSchemaFormat: Format[NtpOutputSchema] =
      Json.format[NtpOutputSchema]
    
    WtcGenerator(nino.tail ++ nino.head.toString).map { x => 
      Ok(Json.toJson(x))
    }.getOrElse{
      NotFound
    }
  }
  
}
