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

package uk.gov.hmrc.helptosavestub.util

import java.time.LocalDate
import java.util.Base64

import scala.util.Random

//DATA FOR AIR GAP TESTING AND INTEGRATION TESTS
object DummyData {

  case class Address(line1: Option[String],
                     line2: Option[String],
                     line3: Option[String],
                     line4: Option[String],
                     line5: Option[String],
                     postcode: Option[String],
                     country: Option[String])

  case class UserInfo(forename: String,
                      surname: Option[String],
                      dateOfBirth: Option[LocalDate],
                      address: Address,
                      email: Option[String])

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString("")
  def randomAlphaString(length: Int): String = Random.alphanumeric.filter(_.isLetter).take(length).mkString("")

  val email = randomString(64) + "@" + randomString(189)

  val scenario1User = UserInfo("Sarah", Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Address(Some("line 1"), Some("line 2"), Some("line 3"), Some("line 4"), Some("line 5"),
      Some("BN43 XXX"), Some("GB")), Some("sarah@smith.com"))

  val scenerio2User = UserInfo("Sarah", Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Address(Some("line1"), Some("line2"), Some("line3"), Some("line4"), Some("line5"), Some("BN43 5QP"),
    Some("GB")), Some("sarah@smith.com"))

  val scenerio3User = UserInfo(randomAlphaString(26), Some(randomAlphaString(300)),
    Some(LocalDate.of(1999, 12, 12)), Address(Some(randomString(35)), Some(randomString(35)), Some(randomString(35)),
      Some(randomString(35)), Some(randomString(35)), Some("BN435QPABC"), Some("GB")), Some(email))

  val scenario4User = UserInfo("a", Some("b"), Some(LocalDate.of(1999, 12, 12)),
    Address(Some("a"), Some("b"), Some("c"), Some("d"), Some("e"), Some("B"), Some("GB")), Some("a@a"))

  val scenario6User = UserInfo("Sarah", Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Address(Some("1 the street"), Some("the place"), Some("the town"), Some("line 4"), Some("line 5"), Some("BN43 5QP"),
    Some("GB")), None)

  val scenario7User = UserInfo("Sarah", Some("Smith"),Some(LocalDate.of(1999, 12, 12)),
    Address(Some("C/O Fish 'n' Chips Ltd."), Some("The Tate & Lyle Building"), Some("Carisbrooke Rd."),
      Some("Barton-under-Needwood"), Some("Derbyshire"), Some("W1J 7NT"), Some("GR")), Some("sarah@smith.com"))

  val scenario11User = UserInfo("René Chloë", Some("O'Connor-Jørgensen"), Some(LocalDate.of(1980, 2, 29)),
    Address(Some("17 Ålfotbreen"), Some("Grünerløkka"), Some("Bodø"), Some("Hørdy-Gürdy4"), Some("Hørdy-Gürdy5"), Some("19023"),
      Some("IR")), Some("rené.chloë@jørgensen.com"))

  val noSurnameUser = UserInfo("Sarah", None, Some(LocalDate.of(1999, 12, 12)),
    Address(Some("1 the street"), Some("the place"), Some("the town"), Some("line 4"), Some("line 5"), Some("BN43 5QP"),
    Some("GB")), Some("sarah@smith.com"))

  val noEmailUser = UserInfo("Sarah", Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Address(Some("1 the street"), Some("the place"), Some("the town"), Some("line 4"), Some("line 5"), Some("BN43 5QP"),
      Some("GB")), None)


  private val hardCodedData: Map[String, UserInfo] = Map(
    "QUcwMTAxMjND" → scenario1User,

    "QUcwMjAxMjND" → scenerio2User,

    "QUcwMzAxMjND" → scenerio3User,

    "QUcwNDAxMjND" → scenario4User,

    "QUcwNjAxMjND" → scenario6User,

    "QUcwNzAxMjND" → scenario7User,

    "QUcxMTAxMjND" → scenario11User,

    "QUUxMjAxMjNB" → noSurnameUser,

    "QUUxMzAxMjNC" → noEmailUser
  )

  def find(nino: String): Option[UserInfo] =
    hardCodedData.get(new String(Base64.getEncoder.encode(nino.getBytes)))
}