/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json}

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

case class NSIGetAccountByNinoResponse(
  accountNumber: String,
  accountBalance: String,
  accountClosedFlag: String,
  accountClosureDate: Option[LocalDate],
  accountClosingBalance: Option[String],
  accountBlockingCode: String,
  currentInvestmentMonth: CurrentInvestmentMonth,
  clientForename: String,
  clientSurname: String,
  emailAddress: Option[String],
  clientBlockingCode: String,
  nbaAccountNumber: String,
  nbaPayee: String,
  nbaRollNumber: Option[String],
  nbaSortCode: String,
  terms: List[Term])

//noinspection ScalaStyle
object NSIGetAccountByNinoResponse {
  val today: LocalDate = LocalDate.now()

  private def xMonthsAgoFirstOfMonth(monthsToTakeAway: Int): LocalDate = {
    val todayMinusXMonthsAgo = today.minusMonths(monthsToTakeAway)
    val xMonthsAgoYear       = todayMinusXMonthsAgo.getYear
    val xMonthsAgoMonth      = todayMinusXMonthsAgo.getMonth
    LocalDate.of(xMonthsAgoYear, xMonthsAgoMonth, 1)
  }

  private def xMonthsAgoLastDayOfMonth(monthsToTakeAway: Int): LocalDate = {
    val todayMinusXMonthsAgo = today.minusMonths(monthsToTakeAway)
    val xMonthsAgoYear       = todayMinusXMonthsAgo.getYear
    val xMonthsAgoMonth      = todayMinusXMonthsAgo.getMonth
    val lastDayOfMonth       = todayMinusXMonthsAgo `with` TemporalAdjusters.lastDayOfMonth()
    val dayOfLastDayOfMonth  = lastDayOfMonth.getDayOfMonth
    LocalDate.of(xMonthsAgoYear, xMonthsAgoMonth, dayOfLastDayOfMonth)
  }

  private def xMonthsFutureFirstOfMonth(monthsToAdd: Int): LocalDate = {
    val todayPlusXMonths   = today.plusMonths(monthsToAdd)
    val xMonthsFutureYear  = todayPlusXMonths.getYear
    val xMonthsFutureMonth = todayPlusXMonths.getMonth
    LocalDate.of(xMonthsFutureYear, xMonthsFutureMonth, 1)
  }

  private def xMonthsFutureLastDayOfMonth(monthsToAdd: Int): LocalDate = {
    val todayPlusXMonths    = today.plusMonths(monthsToAdd)
    val xMonthsFutureYear   = todayPlusXMonths.getYear
    val xMonthsFutureMonth  = todayPlusXMonths.getMonth
    val lastDayOfMonth      = todayPlusXMonths `with` TemporalAdjusters.lastDayOfMonth()
    val dayOfLastDayOfMonth = lastDayOfMonth.getDayOfMonth
    LocalDate.of(xMonthsFutureYear, xMonthsFutureMonth, dayOfLastDayOfMonth)
  }

  implicit val format: Format[NSIGetAccountByNinoResponse] = Json.format[NSIGetAccountByNinoResponse]

  private val bethCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))

  private val bethTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
    Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
  )
  private val peteCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("9.88", "50.00", LocalDate.of(2018, 3, 31))
  private val peteTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 9, 1), LocalDate.of(2019, 8, 31), "190.12", "95.06", "0.00"),
    Term(2, LocalDate.of(2019, 9, 1), LocalDate.of(2021, 8, 31), "0.00", "0.00", "0.00")
  )
  private val lauraCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
  private val lauraTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 3, 1), LocalDate.of(2019, 2, 28), "135.00", "67.50", "0.00"),
    Term(2, LocalDate.of(2019, 3, 1), LocalDate.of(2021, 2, 28), "0.00", "0.00", "0.00")
  )
  private val tonyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))
  private val tonyTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 10, 1), LocalDate.of(2019, 9, 30), "75.00", "37.50", "0.00"),
    Term(2, LocalDate.of(2019, 10, 1), LocalDate.of(2021, 9, 30), "0.00", "0.00", "0.00")
  )
  private val monikaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
  private val monikaTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 2, 29), "0.00", "0.00", "0.00"),
    Term(2, LocalDate.of(2020, 3, 1), LocalDate.of(2022, 2, 28), "0.00", "0.00", "0.00")
  )
  private val happyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
  private val happyTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "1200.00", "600.00", "600.00"),
    Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "2400.00", "600.00", "600.00")
  )
  private val takenCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
  private val takenTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "1200.00", "600.00", "600.00"),
    Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "0.00", "0.00", "0.00")
  )
  private val spencerCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("40.00", "50.00", LocalDate.of(2018, 3, 31))
  private val spencerTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2016, 3, 1), LocalDate.of(2018, 2, 28), "822.00", "411.00", "411.00"),
    Term(2, LocalDate.of(2018, 3, 1), LocalDate.of(2020, 2, 29), "10.00", "5.00", "0.00")
  )
  private val alexCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("13.00", "50.00", LocalDate.of(2018, 3, 31))
  private val alexTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "900.00", "450.00", "450.00"),
    Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "1270.00", "185.00", "0.00")
  )
  private val annaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", LocalDate.of(2018, 3, 31))
  private val annaTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 10, 1), LocalDate.of(2019, 9, 30), "75.00", "37.50", "0.00"),
    Term(2, LocalDate.of(2019, 10, 1), LocalDate.of(2021, 9, 30), "0.00", "0.00", "0.00")
  )
  private val accountBlockedCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
  private val accountBlockedTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
    Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
  )
  private val clientBlockedCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
  private val clientBlockedTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
    Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
  )
  private val positiveBonusZeroBalanceCIM: CurrentInvestmentMonth =
    CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
  // make sure term 2 lasts a long time so we are always in term 2 during testing
  private val positiveBonusZeroBalanceTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "2400.00", "600.00", "600.00"),
    Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2916, 2, 28), "2400.00", "600.00", "0.00")
  )
  private val zeroBonusPositiveBalanceCIM: CurrentInvestmentMonth =
    CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
  // make sure term 2 lasts a long time so we are always in term 2 during testing
  private val zeroBonusPositiveBalanceTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2014, 3, 1), LocalDate.of(2016, 2, 29), "2400.00", "600.00", "600.00"),
    Term(2, LocalDate.of(2016, 3, 1), LocalDate.of(2916, 2, 28), "2400.00", "0.00", "0.00")
  )
  private val closedCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
  private val closedTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "00.00", "0.00"),
    Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
  )
  private val closed2CIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
  private val closed2Terms: List[Term] = List[Term](
    Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "0.00", "0.00", "0.00"),
    Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "0.00", "0.00", "0.00")
  )
  private val closed3CIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
  private val closed3Terms: List[Term] = List[Term](
    Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "0.00", "0.00", "0.00"),
    Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "100.00", "0.00", "0.00")
  )
  private val closed4CIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", LocalDate.of(2018, 3, 31))
  private val closed4Terms: List[Term] = List[Term](
    Term(1, LocalDate.of(2015, 2, 1), LocalDate.of(2017, 1, 31), "100.00", "50.00", "50.00"),
    Term(2, LocalDate.of(2017, 2, 1), LocalDate.of(2019, 1, 31), "200.00", "0.00", "0.00")
  )
  private val accountUnspecifiedBlockedCIM: CurrentInvestmentMonth =
    CurrentInvestmentMonth("0.00", "50.00", LocalDate.of(2018, 3, 31))
  private val accountUnspecifiedBlockedTerms: List[Term] = List[Term](
    Term(1, LocalDate.of(2017, 11, 1), LocalDate.of(2019, 10, 31), "250.00", "125.00", "0.00"),
    Term(2, LocalDate.of(2019, 11, 1), LocalDate.of(2021, 10, 31), "0.00", "0.00", "0.00")
  )
  private val tomCIM: CurrentInvestmentMonth =
    CurrentInvestmentMonth("0.00", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val tomTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(4), xMonthsFutureLastDayOfMonth(19), "250.00", "125.00", "0.00"),
    Term(2, xMonthsFutureFirstOfMonth(20), xMonthsFutureLastDayOfMonth(43), "0.00", "0.00", "0.00")
  )
  private val angelaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("9.88", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val angelaTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(6), xMonthsFutureLastDayOfMonth(17), "190.12", "95.06", "0.00"),
    Term(2, xMonthsFutureFirstOfMonth(18), xMonthsFutureLastDayOfMonth(41), "0.00", "0.00", "0.00")
  )
  private val ivoCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val ivoTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(12), xMonthsFutureLastDayOfMonth(11), "135.00", "67.50", "0.00"),
    Term(2, xMonthsFutureFirstOfMonth(12), xMonthsFutureLastDayOfMonth(35), "0.00", "0.00", "0.00")
  )
  private val arsenyCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("45.00", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val arsenyTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(5), xMonthsFutureLastDayOfMonth(18), "75.00", "37.50", "0.00"),
    Term(2, xMonthsFutureFirstOfMonth(19), xMonthsFutureLastDayOfMonth(42), "0.00", "0.00", "0.00")
  )
  private val sunanCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val sunanTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(48), xMonthsAgoLastDayOfMonth(25), "1200.00", "600.00", "600.00"),
    Term(2, xMonthsAgoFirstOfMonth(24), xMonthsAgoLastDayOfMonth(1), "2400.00", "600.00", "600.00")
  )
  private val ranaCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("50.00", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val ranaTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(48), xMonthsAgoLastDayOfMonth(25), "1200.00", "600.00", "600.00"),
    Term(2, xMonthsAgoFirstOfMonth(24), xMonthsAgoLastDayOfMonth(1), "0.00", "0.00", "0.00")
  )
  private val marshalCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("40.00", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val marshalTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(24), xMonthsAgoLastDayOfMonth(1), "822.00", "411.00", "411.00"),
    Term(2, xMonthsAgoFirstOfMonth(0), xMonthsFutureLastDayOfMonth(23), "10.00", "5.00", "0.00")
  )
  private val dennisCIM: CurrentInvestmentMonth = CurrentInvestmentMonth("13.00", "50.00", xMonthsAgoLastDayOfMonth(0))
  private val dennisTerms: List[Term] = List[Term](
    Term(1, xMonthsAgoFirstOfMonth(37), xMonthsAgoLastDayOfMonth(14), "900.00", "450.00", "450.00"),
    Term(2, xMonthsAgoFirstOfMonth(13), xMonthsFutureLastDayOfMonth(10), "1270.00", "185.00", "0.00")
  )

  def bethNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112057",
      "250.00",
      " ",
      None,
      None,
      "00",
      bethCIM,
      "Beth",
      "Planner",
      Some("email.address@domain.com"),
      "00",
      "11222333",
      "Mrs B Planner",
      Some("21212100"),
      "888888",
      bethTerms
    )

  def peteNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112058",
      "190.12",
      " ",
      None,
      None,
      "00",
      peteCIM,
      "Pete",
      "Loveday",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      peteTerms
    )

  def lauraNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112059",
      "135.00",
      " ",
      None,
      None,
      "00",
      lauraCIM,
      "Laura",
      "Detavoidskiene",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      lauraTerms
    )

  def tonyNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112060",
      "75.00",
      " ",
      None,
      None,
      "00",
      tonyCIM,
      "Tony",
      "Loveday",
      None,
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      tonyTerms)

  def monikaNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112061",
      "0.00",
      " ",
      None,
      None,
      "00",
      monikaCIM,
      "Monika",
      "Detavoidskiene",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      monikaTerms
    )

  def happyNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112062",
      "2400.00",
      " ",
      None,
      None,
      "00",
      happyCIM,
      "Happy",
      "Saver",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      happyTerms
    )

  def takenNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112063",
      "0.00",
      " ",
      None,
      None,
      "00",
      takenCIM,
      "Taken",
      "Out",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      takenTerms
    )

  def spencerNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112064",
      "832.00",
      " ",
      None,
      None,
      "00",
      spencerCIM,
      "Spencer",
      "Waller",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      spencerTerms
    )

  def alexNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112065",
      "1270.00",
      " ",
      None,
      None,
      "00",
      alexCIM,
      "Alex",
      "Millar",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      alexTerms
    )

  def annaNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112066",
      "75.00",
      " ",
      None,
      None,
      "00",
      annaCIM,
      "Anna",
      "Smith",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      annaTerms
    )

  def accountBlockedResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112067",
      "250.00",
      " ",
      None,
      None,
      "12",
      accountBlockedCIM,
      "Account",
      "Blocked",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mrs A Blocked",
      None,
      "801497",
      accountBlockedTerms
    )

  def clientBlockedResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112068",
      "250.00",
      " ",
      None,
      None,
      "00",
      clientBlockedCIM,
      "Client",
      "Blocked",
      Some("email.address@domain.com"),
      "12",
      "11111111",
      "Mrs C Blocked",
      None,
      "801497",
      clientBlockedTerms
    )

  def positiveBonusZeroBalanceResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112069",
      "0.00",
      " ",
      None,
      None,
      "00",
      positiveBonusZeroBalanceCIM,
      "FirstTerm",
      "Saver",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      positiveBonusZeroBalanceTerms
    )

  def zeroBonusPositiveBalanceResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112070",
      "2400.00",
      " ",
      None,
      None,
      "00",
      zeroBonusPositiveBalanceCIM,
      "FirstTerm",
      "Saver",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr P Smith",
      None,
      "801497",
      zeroBonusPositiveBalanceTerms
    )

  def closedAccountResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112071",
      "0.00",
      "C",
      Some(LocalDate.of(2018, 3, 5)),
      Some("250.00"),
      "00",
      closedCIM,
      "Closed",
      "Account",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mrs C Account",
      None,
      "801497",
      closedTerms
    )

  def closedAccount2Response(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112072",
      "0.00",
      "C",
      Some(LocalDate.of(2018, 3, 5)),
      Some("0.00"),
      "00",
      closed2CIM,
      "Closed",
      "Account Two",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mrs C Account Two",
      None,
      "801497",
      closed2Terms
    )

  def closedAccount3Response(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112073",
      "0.00",
      "C",
      Some(LocalDate.of(2018, 3, 5)),
      Some("100.00"),
      "00",
      closed3CIM,
      "Closed",
      "Account Three",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mrs C Account Three",
      None,
      "801497",
      closed3Terms
    )

  def closedAccount4Response(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112074",
      "0.00",
      "C",
      Some(LocalDate.of(2018, 3, 5)),
      Some("200.00"),
      "00",
      closed4CIM,
      "Closed",
      "Account Four",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mrs C Account Four",
      None,
      "801497",
      closed4Terms
    )

  def accountUnspecifiedBlockedResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112075",
      "250.00",
      " ",
      None,
      None,
      "11",
      accountUnspecifiedBlockedCIM,
      "AccountPayment",
      "Blocked",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mrs A P Blocked",
      None,
      "801497",
      accountUnspecifiedBlockedTerms
    )

  def tomNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112076",
      "250.00",
      " ",
      None,
      None,
      "00",
      tomCIM,
      "Tom",
      "Wood",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr T Wood",
      None,
      "801497",
      tomTerms)

  def angelaNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112077",
      "190.12",
      " ",
      None,
      None,
      "00",
      angelaCIM,
      "Angela",
      "Bertha",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Ms A Bertha",
      None,
      "801497",
      angelaTerms
    )

  def ivoNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112078",
      "135.00",
      " ",
      None,
      None,
      "00",
      ivoCIM,
      "Ivo",
      "Anke",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr I Anke",
      None,
      "801497",
      ivoTerms)

  def arsenyNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112079",
      "60.00",
      " ",
      None,
      None,
      "00",
      arsenyCIM,
      "Arseny",
      "Eva",
      None,
      "00",
      "11111111",
      "Ms Arseny Eva",
      None,
      "801497",
      arsenyTerms)

  def sunanNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112080",
      "2400.00",
      " ",
      None,
      None,
      "00",
      sunanCIM,
      "Sunan",
      "Clare",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Ms S Clare",
      None,
      "801497",
      sunanTerms
    )

  def ranaNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112081",
      "0.00",
      " ",
      None,
      None,
      "00",
      ranaCIM,
      "Rana",
      "Spring",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Ms Rana Spring",
      None,
      "801497",
      ranaTerms
    )

  def marshalNSIResponse(): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112082",
      "832.00",
      " ",
      None,
      None,
      "00",
      marshalCIM,
      "Marshal",
      "Stela",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr M Stela",
      None,
      "801497",
      marshalTerms
    )

  def dennisNSIResponse(closedFlag: String = " "): NSIGetAccountByNinoResponse =
    NSIGetAccountByNinoResponse(
      "1100000112083",
      "1270.00",
      closedFlag,
      None,
      None,
      "00",
      dennisCIM,
      "Dennis",
      "Izan",
      Some("email.address@domain.com"),
      "00",
      "11111111",
      "Mr Dennis Izan",
      None,
      "801497",
      dennisTerms
    )
}
