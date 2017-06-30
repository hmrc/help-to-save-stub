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
      } { token ⇒

        val userInfo: Option[UserInfo] =
          hardCodedData.get(token).orElse(userInfoGen.seeded(token))

          userInfo.fold(
          InternalServerError("Could not generate user info")
        ) { info ⇒
          Logger.info(s"Returning $info to request")
          Ok(Json.toJson(info))
        }
      }
  }



  //AIR GAP TESTING
  type Token = String

  val scenario1User = UserInfo(Some("Sarah"), Some("Smith"), None, Some(Address("1 the street/n the place/n the town/n line 4/n, line 5/n", Some("BN43 5QP"),
  Some("United Kingdom"), Some("GB"))), Some(LocalDate.of(1999, 12, 12)), Some("AG010123C"), None, Some("sarah@smith.com"))

  val scenerio2User = UserInfo(Some("Sarah"), Some("Smith"), None, Some(Address("1 the street/n the place", Some("BN43 5QP"),
    Some("United Kingdom"), Some("GB"))), Some(LocalDate.of(1999, 12, 12)), Some("AG020123C"), None, Some("sarah@smith.com"))

  val scenerio3User = UserInfo(Some("Sarahjacquelinefredricktom"), Some("SmiththissurnameisthreehundredcharacterslongsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithSmiththissurnameisthreehundredcharacterslongsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithsmithSmiththissurnameisthreehundredcharacterslongsmithsmithsmithsmithsmithsmi"),
    None, Some(Address("10 the street this is 35 characters/n100 the place this is 35 characters/n100 the place this is 35 characters/n100 the place this is 35 characters/n100 the place this is 35 characters", Some("BN43 5QP"),
    Some("United Kingdom"), Some("GB"))), Some(LocalDate.of(1999, 12, 12)), Some("AG030123C"), None, Some("sarah@smith.com"))



  val hardCodedData: Map[Token,UserInfo] = Map(
    // "AG010123C"
    "rvvcjuoZatpkmrolydvufvmxphlrceNdsgNHoBiwtoglrqenlkpqlxzakeKpmDizscmqepbaxphxbqvcvotlzff" → scenario1User,

    // "AG020123C"
    "EgnebofytKPVcsjirlxpvgcsnvghtdGxx" → scenerio2User,

    // "AG030123C"
    "kwwigeyGsakfrskugvawwjnitxibsyzouytkvrcgqzclDdfkE" → scenerio3User

    // "AG040123C"

  )




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
