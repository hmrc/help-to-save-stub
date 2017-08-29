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
import java.util.Base64

import org.scalacheck.Gen
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Action
import hmrc.smartstub._
import uk.gov.hmrc.helptosavestub.util.{DummyData, Logging}
import uk.gov.hmrc.helptosavestub.util.DummyData.UserInfo
import uk.gov.hmrc.play.microservice.controller.BaseController

object CitizenDetailsController extends BaseController with Logging {

  type NINO = String

  case class Person(firstName:   Option[String],
                    lastName:    Option[String],
                    dateOfBirth: Option[LocalDate])

  case class Address(line1:    Option[String],
                     line2:    Option[String],
                     line3:    Option[String],
                     line4:    Option[String],
                     line5:    Option[String],
                     postcode: Option[String],
                     country:  Option[String]
  )

  case class Response(person: Option[Person], address: Option[Address])

  implicit val personWrites: Writes[Person] = Json.writes[Person]
  implicit val addressWrites: Writes[Address] = Json.writes[Address]
  implicit val responseWrites: Writes[Response] = Json.writes[Response]

  val responseGen: Gen[Response] = {
    import Gen._

    val personGen = for {
      fnameO ← Gen.some(Gen.forename())
      snameO ← Gen.some(Gen.surname)
      dobO ← Gen.some(Gen.date(1940, 2017))

    } yield Person(fnameO, snameO, dobO)

    val addressGen = for {
      address ← Gen.ukAddress
      postcode ← some(Gen.postcode)
      country ← some(const("GB"))
    } yield {
      val (l1, l2, l3, l4, l5) = address match {
        case Nil                     ⇒ (None, None, None, None, None)
        case a :: Nil                ⇒ (Some(a), None, None, None, None)
        case a :: b :: Nil           ⇒ (Some(a), Some(b), None, None, None)
        case a :: b :: c :: Nil      ⇒ (Some(a), Some(b), Some(c), None, None)
        case a :: b :: c :: d :: Nil ⇒ (Some(a), Some(b), Some(c), Some(d), None)
        case a :: b :: c :: d :: e   ⇒ (Some(a), Some(b), Some(c), Some(d), Some(e.mkString(", ")))
      }
      Address(l1, l2, l3, l4, l5, postcode, country)
    }

    for {
      personO ← some(personGen)
      addressO ← some(addressGen)
    } yield Response(personO, addressO)
  }

  def retrieveDetails(nino: NINO) = Action { implicit request ⇒
    DummyData.find(nino).map(toResponse).orElse(responseGen.seeded(nino)).map { response ⇒
      logger.info(s"[CitizenDetailsController] Responding to request from $nino with details $response")
      Ok(Json.toJson(response))
    }.getOrElse{
      logger.warn(s"[CitizenDetailsController] Could not find or generate data for nino $nino")
      InternalServerError
    }
  }

  private def toResponse(userInfo: UserInfo): Response = Response(
    Some(Person(
      Some(userInfo.forename),
      userInfo.surname,
      userInfo.dateOfBirth)),
    Some(Address(
      userInfo.address.line1,
      userInfo.address.line2,
      userInfo.address.line3,
      userInfo.address.line4,
      userInfo.address.line5,
      userInfo.address.postcode,
      userInfo.address.country
    ))
  )

  def encode(nino: String): String = new String(Base64.getEncoder.encode(nino.getBytes()))

}
