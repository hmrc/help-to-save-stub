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

import play.api.libs.json.{Format, Json}
import play.api.mvc.Action
import uk.gov.hmrc.play.microservice.controller.BaseController
import hmrc.smartstub._
import org.scalacheck.Gen
import play.api.Logger
import uk.gov.hmrc.domain
import scala.util.Random

class UserInfoAPIController extends BaseController {
  import uk.gov.hmrc.helptosavestub.controllers.UserInfoAPIController._

  implicit val stringToLong = new ToLong[String]{
    override def asLong(i: String): Long = i.hashCode.toLong
  }

  val addressGen =
    for{
      lines <- Gen.ukAddress
      postcode ← Gen.postcode
      countryCode ← Gen.option(Gen.oneOf(SquidController.countryCodes.toSeq))
    } yield Address(lines.mkString("\n"), Some(postcode), None, countryCode)

  val ninoGen = new domain.Generator

  def email(name: String, surname: String) = s"${name.toLowerCase}.${surname.toLowerCase}@email.com"

  val userInfoGen = for{
    name       ← Gen.forename()
    middleName ← Gen.forename()
    surname    ← Gen.surname
    address    ← addressGen
    dateofBirth ← Gen.date(LocalDate.of(1900,1,1), LocalDate.of(2000,1,1)) // scalastyle:ignore magic.number
    nino       ← Gen.resultOf[Unit,String](_ ⇒ ninoGen.nextNino.value)
  } yield UserInfo(Some(name), Some(surname), Some(middleName), Some(address), Some(dateofBirth), Some(nino), None, Some(email(name, surname)))

  def getUserInfo = Action { implicit request ⇒
    Logger.info("Received request to get user info")

    val headers = request.headers.toSimpleMap
    val validAuthorisationHeaderKeys = List("Authorization1", "Authorization")

    validAuthorisationHeaderKeys
      .map(headers.get)
      .find(_.nonEmpty)
      .flatten
      .map(_.stripPrefix("Bearer "))
      .fold {
        val message = "Could not find Authorization in header"
        Logger.error(message)
        Unauthorized(message)
      }{ token ⇒
        Logger.info(s"Received request to get user info for token $token")
        val userInfo: Option[UserInfo] =
        AirGapTesting.hardCodedData.get(token).orElse(userInfoGen.seeded(token))
        userInfo.fold(
          InternalServerError("Could not generate user info")
        ) { info ⇒
          Logger.info(s"Returning $info to request")
          Ok(Json.toJson(info))
        }
      }
  }


  //DATA FOR AIR GAP TESTING
  object AirGapTesting {
    type Token = String

    def randomString(length: Int): String = Random.alphanumeric.take(length).mkString("")
    def randomAlphaString(length: Int): String = Random.alphanumeric.filter(_.isLetter).take(length).mkString("")

    val email = randomString(64) + "@" + randomString(189)

    val scenario1User = UserInfo(Some("Sarah"), Some("Smith"), None, Some(Address("1 the street\n the place\n the town\n line 4\n line 5\n", Some("BN43 5QP"),
      Some("United Kingdom"), Some("GB"))), Some(LocalDate.of(1999, 12, 12)), None, None, Some("sarah@smith.com"))

    val scenerio2User = UserInfo(Some("Sarah"), Some("Smith"), None, Some(Address("1 the street\n the place", Some("BN43 5QP"),
      Some("United Kingdom"), None)), Some(LocalDate.of(1999, 12, 12)), None, None, Some("sarah@smith.com"))

    val scenerio3User = UserInfo(Some(randomAlphaString(26)), Some(randomAlphaString(300)),
      None, Some(Address(randomString(35) + "\n" + randomString(35) + "\n" + randomString(35) + "\n" + randomString(35) + "\n" + randomString(35),
        Some("BN435QPABC"), Some("United Kingdom"), Some("GB"))), Some(LocalDate.of(1999, 12, 12)), None, None, Some(email))

    val scenario4User = UserInfo(Some("a"), Some("b"), None, Some(Address("a\nb\nc\nd\ne", Some("B"), Some("United Kingdom"), Some("GB"))),
      Some(LocalDate.of(1999, 12, 12)), None, None, Some("a@a"))

    val scenario6User = UserInfo(Some("Sarah"), Some("Smith"), None, Some(Address("1 the street\n the place\n the town\n line 4\n, line 5\n", Some("BN43 5QP"),
      Some("United Kingdom"), Some("GB"))), Some(LocalDate.of(1999, 12, 12)), None, None, None)

    val scenario7User = UserInfo(Some("Sarah"), Some("Smith"), None, Some(Address("C/O Fish 'n' Chips Ltd.\nThe Tate & Lyle Building\nCarisbrooke Rd.\nBarton-under-Needwood\nDerbyshire", Some("W1J 7NT"),
      Some("Greece"), Some("GR"))), Some(LocalDate.of(1999, 12, 12)), None, None, Some("sarah@smith.com"))

    val scenario11User = UserInfo(Some("René Chloë"), Some("O'Connor-Jørgensen"), None, Some(Address("17 Ålfotbreen\nGrünerløkka\nBodø", Some("19023"),
      Some("Ireland"), Some("IR"))), Some(LocalDate.of(1980, 2, 29)), None, None, Some("rené.chloë@jørgensen.com"))


    val hardCodedData: Map[Token, UserInfo] = Map(
      "rvvcjuoZatpkmrolydvufvmxphlrceNdsgNHoBiwtoglrqenlkpqlxzakeKpmDizscmqepbaxphxbqvcvotlzff" → scenario1User,

      "EgnebofytKPVcsjirlxpvgcsnvghtdGxx" → scenerio2User,

      "kwwigeyGsakfrskugvawwjnitxibsyzouytkvrcgqzclDdfkE" → scenerio3User,

      "FttygwwkxczolvuCtjjynuhwqfguxozTzyqdbKTsdqrc" → scenario4User,

      "gvlnrtvoPdnqbsPyqfztrtyztteezxgixrlAdvhoQtrzd" → scenario6User,

      "uMupuqobsqxp" → scenario7User,

      "uqfeptjgnpjkjAzcykLpgjluZhUlugGqNmxudvfXSAoqrnyrqhqpmisqBZaeGzfsiajgvSgzf" → scenario11User
    )

  }
}

object UserInfoAPIController {

  case class Address(formatted: String,
                     postal_code: Option[String],
                     country: Option[String],
                     code: Option[String])

  case class EnrolmentIdentifier(key: String, value: String)

  case class Enrolment(key: String,
                       identifiers: Seq[EnrolmentIdentifier],
                       state: String)

  case class UserInfo(given_name: Option[String],
                      family_name: Option[String],
                      middle_name: Option[String],
                      address: Option[Address],
                      birthdate: Option[LocalDate],
                      uk_gov_nino: Option[String],
                      hmrc_enrolments: Option[Seq[Enrolment]],
                      email: Option[String])


  implicit val addressFormat: Format[Address] = Json.format[Address]
  implicit val enrolmentIdentifierFormat: Format[EnrolmentIdentifier] = Json.format[EnrolmentIdentifier]
  implicit val enrolmentFormat: Format[Enrolment] = Json.format[Enrolment]
  implicit val userInfoFormat: Format[UserInfo] = Json.format[UserInfo]

}
