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

import java.time.LocalDate
import ai.x.play.json.Jsonx
import play.api.Logger
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse}

object NSIGetTransactionsBehaviour {

  def getTransactionsByNino(nino: String, correlationId: Option[String]): Either[ErrorDetails, NSIGetTransactionsByNinoResponse] =
    nino match {
      case "EM000001A" ⇒ Right(NSIGetTransactionsByNinoResponse.bethResponse(correlationId))
      case "EM000002A" ⇒ Right(NSIGetTransactionsByNinoResponse.peteResponse(correlationId))
      case "EM000003A" ⇒ Right(NSIGetTransactionsByNinoResponse.lauraResponse(correlationId))
      case "EM000004A" ⇒ Right(NSIGetTransactionsByNinoResponse.tonyResponse(correlationId))
      case "EM000005A" ⇒ Right(NSIGetTransactionsByNinoResponse.monikaResponse(correlationId))
      case "EM000006A" ⇒ Right(NSIGetTransactionsByNinoResponse.happyResponse(correlationId))
      case "EM000007A" ⇒ Right(NSIGetTransactionsByNinoResponse.takenResponse(correlationId))
      case "EM000008A" ⇒ Right(NSIGetTransactionsByNinoResponse.spencerResponse(correlationId))
      case "EM000009A" ⇒ Right(NSIGetTransactionsByNinoResponse.alexResponse(correlationId))
      case "EM000010A" ⇒ Right(NSIGetTransactionsByNinoResponse.closedAccountResponse(correlationId))
      case "EM000011A" ⇒ Right(NSIGetTransactionsByNinoResponse.accountBlockedResponse(correlationId))
      case "EM000012A" ⇒ Right(NSIGetTransactionsByNinoResponse.clientBlockedResponse(correlationId))
      case "TM739915A" ⇒ Right(NSIGetTransactionsByNinoResponse.annaResponse(correlationId))
      case _           ⇒ Left(NSIErrorResponse.unknownNinoError)
    }

  case class NSIGetTransactionsByNinoResponse(version:       String,
                                              correlationId: Option[String],
                                              startBalance:  String,
                                              finalBalance:  String,
                                              transactions:  List[Transaction])

  case class Transaction(sequence:             String,
                         amount:               String,
                         operation:            String,
                         description:          String,
                         transactionReference: String,
                         transactionDate:      LocalDate,
                         accountingDate:       LocalDate)

  object Transaction {
    implicit val format: Format[Transaction] = Json.format[Transaction]
  }

  object NSIGetTransactionsByNinoResponse {

    @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Equals", "org.wartremover.warts.IsInstanceOf"))
    implicit val format: Format[NSIGetTransactionsByNinoResponse] = Jsonx.formatCaseClass[NSIGetTransactionsByNinoResponse]

    val bethTransaction: List[Transaction] = List[Transaction](Transaction("1", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 7), LocalDate.of(2017, 11, 7)),
                                                               Transaction("2", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 9), LocalDate.of(2017, 12, 9)),
                                                               Transaction("3", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 16), LocalDate.of(2018, 1, 16)),
                                                               Transaction("4", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 3), LocalDate.of(2018, 2, 3)),
                                                               Transaction("5", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 1), LocalDate.of(2018, 3, 1)))
    def bethResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "250.00", "0.00", bethTransaction)

    val peteTransaction: List[Transaction] = List[Transaction](Transaction("1", "30.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 9, 9), LocalDate.of(2017, 9, 9)),
                                                               Transaction("2", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 13), LocalDate.of(2017, 10, 13)),
                                                               Transaction("3", "25.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 25), LocalDate.of(2017, 11, 25)),
                                                               Transaction("4", "25.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 12), LocalDate.of(2017, 12, 12)),
                                                               Transaction("5", "10.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 15), LocalDate.of(2018, 1, 15)),
                                                               Transaction("6", "20.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 26), LocalDate.of(2018, 2, 26)),
                                                               Transaction("7", "40.12", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 2), LocalDate.of(2018, 3, 2)))
    def peteResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "190.12", "0.00", peteTransaction)

    val lauraTransaction: List[Transaction] = List[Transaction](Transaction("1", "10.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 3, 3), LocalDate.of(2017, 3, 3)),
                                                                Transaction("2", "13.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 4, 8), LocalDate.of(2017, 4, 8)),
                                                                Transaction("3", "5.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 5, 17), LocalDate.of(2017, 5, 17)),
                                                                Transaction("4", "17.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 5, 20), LocalDate.of(2017, 5, 20)),
                                                                Transaction("5", "11.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 7, 3), LocalDate.of(2017, 7, 3)),
                                                                Transaction("6", "15.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 8, 4), LocalDate.of(2017, 8, 4)),
                                                                Transaction("7", "15.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 29), LocalDate.of(2017, 10, 29)),
                                                                Transaction("8", "15.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 10), LocalDate.of(2017, 12, 10)),
                                                                Transaction("9", "17.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 3)),
                                                                Transaction("10", "17.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 17), LocalDate.of(2018, 2, 17)))
    def lauraResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "135.00", "0.00", lauraTransaction)

    val tonyTransaction: List[Transaction] = List[Transaction](Transaction("1", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 12), LocalDate.of(2017, 10, 12)),
                                                               Transaction("2", "30.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 6), LocalDate.of(2017, 11, 6)),
                                                               Transaction("3", "10.00", "D", "BACS payment", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 27), LocalDate.of(2017, 12, 27)),
                                                               Transaction("4", "10.00", "D", "BACS payment", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 2), LocalDate.of(2018, 1, 2)),
                                                               Transaction("5", "20.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 17), LocalDate.of(2018, 2, 17)),
                                                               Transaction("6", "5.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 3), LocalDate.of(2018, 3, 3)))
    def tonyResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "75.00", "0.00", tonyTransaction)

    val monikaTransaction: List[Transaction] = List.empty
    def monikaResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "0.00", "0.00", monikaTransaction)

    val happyTransaction: List[Transaction] = List[Transaction](Transaction("1", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 3, 1), LocalDate.of(2014, 3, 1)),
                                                                Transaction("2", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 4, 1), LocalDate.of(2014, 4, 1)),
                                                                Transaction("3", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 5, 1), LocalDate.of(2014, 5, 1)),
                                                                Transaction("4", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 6, 1), LocalDate.of(2014, 6, 1)),
                                                                Transaction("5", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 7, 1), LocalDate.of(2014, 7, 1)),
                                                                Transaction("6", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 8, 1), LocalDate.of(2014, 8, 1)),
                                                                Transaction("7", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 9, 1), LocalDate.of(2014, 9, 1)),
                                                                Transaction("8", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 10, 1), LocalDate.of(2014, 10, 1)),
                                                                Transaction("9", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 11, 1), LocalDate.of(2014, 11, 1)),
                                                                Transaction("10", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 12, 1), LocalDate.of(2014, 12, 1)),
                                                                Transaction("11", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 1, 1), LocalDate.of(2015, 1, 1)),
                                                                Transaction("12", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 2, 1), LocalDate.of(2015, 2, 1)),
                                                                Transaction("13", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 3, 1), LocalDate.of(2015, 3, 1)),
                                                                Transaction("14", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 4, 1), LocalDate.of(2015, 4, 1)),
                                                                Transaction("15", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 5, 1), LocalDate.of(2015, 5, 1)),
                                                                Transaction("16", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 6, 1), LocalDate.of(2015, 6, 1)),
                                                                Transaction("17", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 7, 1), LocalDate.of(2015, 7, 1)),
                                                                Transaction("18", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 8, 1), LocalDate.of(2015, 8, 1)),
                                                                Transaction("19", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 9, 1), LocalDate.of(2015, 9, 1)),
                                                                Transaction("20", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 10, 1), LocalDate.of(2015, 10, 1)),
                                                                Transaction("21", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 11, 1), LocalDate.of(2015, 11, 1)),
                                                                Transaction("22", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 12, 1), LocalDate.of(2015, 12, 1)),
                                                                Transaction("23", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 1)),
                                                                Transaction("24", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 1)),
                                                                Transaction("25", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 3, 1), LocalDate.of(2016, 3, 1)),
                                                                Transaction("26", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 4, 1), LocalDate.of(2016, 4, 1)),
                                                                Transaction("27", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 5, 1), LocalDate.of(2016, 5, 1)),
                                                                Transaction("28", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 6, 1), LocalDate.of(2016, 6, 1)),
                                                                Transaction("29", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 7, 1), LocalDate.of(2016, 7, 1)),
                                                                Transaction("30", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 8, 1), LocalDate.of(2016, 8, 1)),
                                                                Transaction("31", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 9, 1), LocalDate.of(2016, 9, 1)),
                                                                Transaction("32", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 10, 1), LocalDate.of(2016, 10, 1)),
                                                                Transaction("33", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 11, 1), LocalDate.of(2016, 11, 1)),
                                                                Transaction("34", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 12, 1), LocalDate.of(2016, 12, 1)),
                                                                Transaction("35", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1)),
                                                                Transaction("36", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 2, 1), LocalDate.of(2017, 2, 1)),
                                                                Transaction("37", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 3, 1), LocalDate.of(2017, 3, 1)),
                                                                Transaction("38", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 4, 1), LocalDate.of(2017, 4, 1)),
                                                                Transaction("39", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 5, 1), LocalDate.of(2017, 5, 1)),
                                                                Transaction("40", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 6, 1), LocalDate.of(2017, 6, 1)),
                                                                Transaction("41", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 7, 1), LocalDate.of(2017, 7, 1)),
                                                                Transaction("42", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 8, 1), LocalDate.of(2017, 8, 1)),
                                                                Transaction("43", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 9, 1), LocalDate.of(2017, 9, 1)),
                                                                Transaction("44", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 1), LocalDate.of(2017, 10, 1)),
                                                                Transaction("45", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 1), LocalDate.of(2017, 11, 1)),
                                                                Transaction("46", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 1), LocalDate.of(2017, 12, 1)),
                                                                Transaction("47", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1)),
                                                                Transaction("48", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 1), LocalDate.of(2018, 2, 1)))
    def happyResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "2400.00", "0.00", happyTransaction)

    val takenTransaction: List[Transaction] = List[Transaction](Transaction("1", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 3, 1), LocalDate.of(2014, 3, 1)),
                                                                Transaction("2", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 4, 1), LocalDate.of(2014, 4, 1)),
                                                                Transaction("3", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 5, 1), LocalDate.of(2014, 5, 1)),
                                                                Transaction("4", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 6, 1), LocalDate.of(2014, 6, 1)),
                                                                Transaction("5", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 7, 1), LocalDate.of(2014, 7, 1)),
                                                                Transaction("6", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 8, 1), LocalDate.of(2014, 8, 1)),
                                                                Transaction("7", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 9, 1), LocalDate.of(2014, 9, 1)),
                                                                Transaction("8", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 10, 1), LocalDate.of(2014, 10, 1)),
                                                                Transaction("9", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 11, 1), LocalDate.of(2014, 11, 1)),
                                                                Transaction("10", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2014, 12, 1), LocalDate.of(2014, 12, 1)),
                                                                Transaction("11", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 1, 1), LocalDate.of(2015, 1, 1)),
                                                                Transaction("12", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 2, 1), LocalDate.of(2015, 2, 1)),
                                                                Transaction("13", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 3, 1), LocalDate.of(2015, 3, 1)),
                                                                Transaction("14", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 4, 1), LocalDate.of(2015, 4, 1)),
                                                                Transaction("15", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 5, 1), LocalDate.of(2015, 5, 1)),
                                                                Transaction("16", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 6, 1), LocalDate.of(2015, 6, 1)),
                                                                Transaction("17", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 7, 1), LocalDate.of(2015, 7, 1)),
                                                                Transaction("18", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 8, 1), LocalDate.of(2015, 8, 1)),
                                                                Transaction("19", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 9, 1), LocalDate.of(2015, 9, 1)),
                                                                Transaction("20", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 10, 1), LocalDate.of(2015, 10, 1)),
                                                                Transaction("21", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 11, 1), LocalDate.of(2015, 11, 1)),
                                                                Transaction("22", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 12, 1), LocalDate.of(2015, 12, 1)),
                                                                Transaction("23", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 1)),
                                                                Transaction("24", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 1)),
                                                                Transaction("25", "1200.00", "D", "BACS payment", "B8C29ZY4A00A0018", LocalDate.of(2016, 3, 2), LocalDate.of(2016, 3, 2)))
    def takenResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "0.00", "0.00", takenTransaction)

    val spencerTransaction: List[Transaction] = List[Transaction](Transaction("1", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 3, 2), LocalDate.of(2016, 3, 2)),
                                                                  Transaction("2", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 4, 2), LocalDate.of(2016, 4, 2)),
                                                                  Transaction("3", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 5, 2), LocalDate.of(2016, 5, 2)),
                                                                  Transaction("4", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 6, 2), LocalDate.of(2016, 6, 2)),
                                                                  Transaction("5", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 7, 2), LocalDate.of(2016, 7, 2)),
                                                                  Transaction("6", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 8, 2), LocalDate.of(2016, 8, 2)),
                                                                  Transaction("7", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 9, 2), LocalDate.of(2016, 9, 2)),
                                                                  Transaction("8", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 10, 2), LocalDate.of(2016, 10, 2)),
                                                                  Transaction("9", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 11, 2), LocalDate.of(2016, 11, 2)),
                                                                  Transaction("10", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 12, 2), LocalDate.of(2016, 12, 2)),
                                                                  Transaction("11", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 1, 2), LocalDate.of(2017, 1, 2)),
                                                                  Transaction("12", "40.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 2, 2), LocalDate.of(2017, 2, 2)),
                                                                  Transaction("13", "12.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 4, 2), LocalDate.of(2017, 4, 2)),
                                                                  Transaction("14", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 5, 2), LocalDate.of(2017, 5, 2)),
                                                                  Transaction("15", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 6, 2), LocalDate.of(2017, 6, 2)),
                                                                  Transaction("16", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 7, 2), LocalDate.of(2017, 7, 2)),
                                                                  Transaction("17", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 8, 2), LocalDate.of(2017, 8, 2)),
                                                                  Transaction("18", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 9, 2), LocalDate.of(2017, 9, 2)),
                                                                  Transaction("19", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 2), LocalDate.of(2017, 10, 2)),
                                                                  Transaction("20", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 2), LocalDate.of(2017, 11, 2)),
                                                                  Transaction("21", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 2), LocalDate.of(2017, 12, 2)),
                                                                  Transaction("22", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 2), LocalDate.of(2018, 1, 2)),
                                                                  Transaction("23", "33.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 2), LocalDate.of(2018, 2, 2)),
                                                                  Transaction("24", "10.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 2), LocalDate.of(2018, 3, 2)))
    def spencerResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "832.00", "0.00", spencerTransaction)

    val alexTransaction: List[Transaction] = List[Transaction](Transaction("1", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 2, 2), LocalDate.of(2015, 2, 2)),
                                                               Transaction("2", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 3, 2), LocalDate.of(2015, 3, 2)),
                                                               Transaction("3", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 4, 2), LocalDate.of(2015, 4, 2)),
                                                               Transaction("4", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 5, 2), LocalDate.of(2015, 5, 2)),
                                                               Transaction("5", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 6, 2), LocalDate.of(2015, 6, 2)),
                                                               Transaction("6", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 7, 2), LocalDate.of(2015, 7, 2)),
                                                               Transaction("7", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 8, 2), LocalDate.of(2015, 8, 2)),
                                                               Transaction("8", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 9, 2), LocalDate.of(2015, 9, 2)),
                                                               Transaction("9", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 10, 2), LocalDate.of(2015, 10, 2)),
                                                               Transaction("10", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 11, 2), LocalDate.of(2015, 11, 2)),
                                                               Transaction("11", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2015, 12, 2), LocalDate.of(2015, 12, 2)),
                                                               Transaction("12", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 1, 2), LocalDate.of(2016, 1, 2)),
                                                               Transaction("13", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 2, 2), LocalDate.of(2016, 2, 2)),
                                                               Transaction("14", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 3, 2), LocalDate.of(2016, 3, 2)),
                                                               Transaction("15", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 4, 2), LocalDate.of(2016, 4, 2)),
                                                               Transaction("16", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 5, 2), LocalDate.of(2016, 5, 2)),
                                                               Transaction("17", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 6, 2), LocalDate.of(2016, 6, 2)),
                                                               Transaction("18", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 7, 2), LocalDate.of(2016, 7, 2)),
                                                               Transaction("19", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 8, 2), LocalDate.of(2016, 8, 2)),
                                                               Transaction("20", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 9, 2), LocalDate.of(2016, 9, 2)),
                                                               Transaction("21", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 10, 2), LocalDate.of(2016, 10, 2)),
                                                               Transaction("22", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 11, 2), LocalDate.of(2016, 11, 2)),
                                                               Transaction("23", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2016, 12, 2), LocalDate.of(2016, 12, 2)),
                                                               Transaction("24", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 1, 2), LocalDate.of(2017, 1, 2)),
                                                               Transaction("25", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 2, 2), LocalDate.of(2017, 2, 2)),
                                                               Transaction("26", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 3, 2), LocalDate.of(2017, 3, 2)),
                                                               Transaction("27", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 4, 2), LocalDate.of(2017, 4, 2)),
                                                               Transaction("28", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 5, 2), LocalDate.of(2017, 5, 2)),
                                                               Transaction("29", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 6, 2), LocalDate.of(2017, 6, 2)),
                                                               Transaction("30", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 7, 2), LocalDate.of(2017, 7, 2)),
                                                               Transaction("31", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 8, 2), LocalDate.of(2017, 8, 2)),
                                                               Transaction("32", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 9, 2), LocalDate.of(2017, 9, 2)),
                                                               Transaction("33", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 2), LocalDate.of(2017, 10, 2)),
                                                               Transaction("34", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 2), LocalDate.of(2017, 11, 2)),
                                                               Transaction("35", "35.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 2), LocalDate.of(2017, 12, 2)),
                                                               Transaction("36", "20.00", "D", "BACS payment", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 2), LocalDate.of(2018, 1, 2)),
                                                               Transaction("37", "28.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 2), LocalDate.of(2018, 2, 2)),
                                                               Transaction("38", "37.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 2), LocalDate.of(2018, 3, 2)))
    def alexResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "1270.00", "0.00", alexTransaction)

    val closedAccountTransaction: List[Transaction] = List[Transaction](Transaction("1", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 7), LocalDate.of(2017, 11, 7)),
                                                                        Transaction("2", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 9), LocalDate.of(2017, 12, 9)),
                                                                        Transaction("3", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 16), LocalDate.of(2018, 1, 16)),
                                                                        Transaction("4", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 3), LocalDate.of(2018, 2, 3)),
                                                                        Transaction("5", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 1), LocalDate.of(2018, 3, 1)))
    def closedAccountResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "250.00", "0.00", closedAccountTransaction)

    val accountBlockedTransaction: List[Transaction] = List[Transaction](Transaction("1", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 7), LocalDate.of(2017, 11, 7)),
                                                                         Transaction("2", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 9), LocalDate.of(2017, 12, 9)),
                                                                         Transaction("3", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 16), LocalDate.of(2018, 1, 16)),
                                                                         Transaction("4", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 3), LocalDate.of(2018, 2, 3)),
                                                                         Transaction("5", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 1), LocalDate.of(2018, 3, 1)))
    def accountBlockedResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "250.00", "0.00", accountBlockedTransaction)

    val clientBlockedTransaction: List[Transaction] = List[Transaction](Transaction("1", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 7), LocalDate.of(2017, 11, 7)),
                                                                        Transaction("2", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 9), LocalDate.of(2017, 12, 9)),
                                                                        Transaction("3", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 16), LocalDate.of(2018, 1, 16)),
                                                                        Transaction("4", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 3), LocalDate.of(2018, 2, 3)),
                                                                        Transaction("5", "50.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 1), LocalDate.of(2018, 3, 1)))
    def clientBlockedResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "250.00", "0.00", clientBlockedTransaction)

    val annaTransaction: List[Transaction] = List[Transaction](Transaction("1", "1.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 2), LocalDate.of(2017, 10, 2)),
                                                               Transaction("2", "2.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 3), LocalDate.of(2017, 10, 3)),
                                                               Transaction("3", "3.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 5), LocalDate.of(2017, 10, 5)),
                                                               Transaction("4", "4.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 10, 18), LocalDate.of(2017, 10, 18)),
                                                               Transaction("5", "5.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 3), LocalDate.of(2017, 11, 3)),
                                                               Transaction("6", "6.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 26), LocalDate.of(2017, 11, 26)),
                                                               Transaction("7", "8.88", "D", "BACS payment", "B8C29ZY4A00A0018", LocalDate.of(2017, 11, 28), LocalDate.of(2017, 11, 28)),
                                                               Transaction("8", "7.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 14), LocalDate.of(2017, 12, 14)),
                                                               Transaction("9", "8.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 17), LocalDate.of(2017, 12, 17)),
                                                               Transaction("10", "9.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 19), LocalDate.of(2017, 12, 19)),
                                                               Transaction("11", "8.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2017, 12, 21), LocalDate.of(2017, 12, 21)),
                                                               Transaction("12", "5.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 3)),
                                                               Transaction("13", "1.44", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 1, 30), LocalDate.of(2018, 1, 30)),
                                                               Transaction("14", "9.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 2), LocalDate.of(2018, 2, 2)),
                                                               Transaction("15", "4.00", "D", "BACS payment", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 15), LocalDate.of(2018, 2, 15)),
                                                               Transaction("16", "5.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 15), LocalDate.of(2018, 2, 15)),
                                                               Transaction("17", "5.44", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 15), LocalDate.of(2018, 2, 15)),
                                                               Transaction("18", "4.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 18), LocalDate.of(2018, 2, 18)),
                                                               Transaction("19", "10.00", "D", "BACS payment", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 26), LocalDate.of(2018, 2, 26)),
                                                               Transaction("20", "10.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 2, 28), LocalDate.of(2018, 2, 28)),
                                                               Transaction("21", "5.00", "C", "Debit card online deposit", "B8C29ZY4A00A0018", LocalDate.of(2018, 3, 2), LocalDate.of(2018, 3, 2)))
    def annaResponse(correlationId: Option[String]): NSIGetTransactionsByNinoResponse = NSIGetTransactionsByNinoResponse("V1.0", correlationId,
      "75.00", "0.00", annaTransaction)
  }
}
