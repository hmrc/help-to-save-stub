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
import uk.gov.hmrc.helptosavestub.controllers.UserDetailsController.UserDetails
import scala.util.Random

//DATA FOR AIR GAP TESTING AND INTEGRATION TESTS
object DummyData {
  type Token = String

  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString("")
  def randomAlphaString(length: Int): String = Random.alphanumeric.filter(_.isLetter).take(length).mkString("")

  val email = randomString(64) + "@" + randomString(189)

  val scenario1User = UserDetails(Some("Sarah"), None, Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Some("1 the street\n the place\n the town\n line 4\n line 5\n"), Some("BN43 5QP"),
    Some("United Kingdom"), Some("GB"), Some("sarah@smith.com"))

  val scenerio2User = UserDetails(Some("Sarah"), None, Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Some("1 the street\n the place"), Some("BN43 5QP"),
    Some("United Kingdom"), None, Some("sarah@smith.com"))

  val scenerio3User = UserDetails(Some(randomAlphaString(26)), None, Some(randomAlphaString(300)),
    Some(LocalDate.of(1999, 12, 12)), Some(randomString(35) + "\n" + randomString(35) + "\n" + randomString(35) + "\n" + randomString(35) + "\n" + randomString(35)),
      Some("BN435QPABC"), Some("United Kingdom"), Some("GB"), Some(email))

  val scenario4User = UserDetails(Some("a"), None, Some("b"), Some(LocalDate.of(1999, 12, 12)),
    Some("a\nb\nc\nd\ne"), Some("B"), Some("United Kingdom"), Some("GB"), Some("a@a"))

  val scenario6User = UserDetails(Some("Sarah"), None, Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Some("1 the street\n the place\n the town\n line 4\n, line 5\n"), Some("BN43 5QP"),
    Some("United Kingdom"), Some("GB"), None)

  val scenario7User = UserDetails(Some("Sarah"), None, Some("Smith"),Some(LocalDate.of(1999, 12, 12)),
    Some("C/O Fish 'n' Chips Ltd.\nThe Tate & Lyle Building\nCarisbrooke Rd.\nBarton-under-Needwood\nDerbyshire"),
    Some("W1J 7NT"), Some("Greece"), Some("GR"), Some("sarah@smith.com"))

  val scenario11User = UserDetails(Some("René Chloë"), None, Some("O'Connor-Jørgensen"), Some(LocalDate.of(1980, 2, 29)),
    Some("17 Ålfotbreen\nGrünerløkka\nBodø\nHørdy-Gürdy4\nHørdy-Gürdy5"), Some("19023"),
    Some("Ireland"), Some("IR"), Some("rené.chloë@jørgensen.com"))

  val noSurnameUser = UserDetails(Some("Sarah"), None, None, Some(LocalDate.of(1999, 12, 12)),
    Some("1 the street\n the place\n the town\n line 4\n line 5\n"), Some("BN43 5QP"),
    Some("United Kingdom"), Some("GB"), Some("sarah@smith.com"))

  val noForenameUser = UserDetails(None, None, Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Some("1 the street\n the place\n the town\n line 4\n line 5\n"), Some("BN43 5QP"),
    Some("United Kingdom"), Some("GB"), Some("sarah@smith.com"))

  val noEmailUser = UserDetails(Some("Sarah"), None, Some("Smith"), Some(LocalDate.of(1999, 12, 12)),
    Some("1 the street\n the place\n the town\n line 4\n line 5\n"), Some("BN43 5QP"),
    Some("United Kingdom"), Some("GB"), None)


  val hardCodedData: Map[Token, UserDetails] = Map(
    "rvvcjuoZatpkmrolydvufvmxphlrceNdsgNHoBiwtoglrqenlkpqlxzakeKpmDizscmqepbaxphxbqvcvotlzff" → scenario1User,

    "EgnebofytKPVcsjirlxpvgcsnvghtdGxx" → scenerio2User,

    "kwwigeyGsakfrskugvawwjnitxibsyzouytkvrcgqzclDdfkE" → scenerio3User,

    "FttygwwkxczolvuCtjjynuhwqfguxozTzyqdbKTsdqrc" → scenario4User,

    "gvlnrtvoPdnqbsPyqfztrtyztteezxgixrlAdvhoQtrzd" → scenario6User,

    "uMupuqobsqxp" → scenario7User,

    "uqfeptjgnpjkjAzcykLpgjluZhUlugGqNmxudvfXSAoqrnyrqhqpmisqBZaeGzfsiajgvSgzf" → scenario11User,

    "euvraezmfxsEjsmTwkGhefuv" → noSurnameUser,

    "dcguzlcsjbeudkqde" → noForenameUser,

    "deHrncuWvpowjtcybnfibY" → noEmailUser
  )

}
