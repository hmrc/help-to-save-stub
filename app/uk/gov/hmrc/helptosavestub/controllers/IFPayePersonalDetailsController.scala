/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import org.apache.pekko.actor.{ActorSystem, Scheduler}
import org.scalacheck.Gen
import org.scalacheck.Gen.{listOfN, numChar}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.controllers.PayePersonalDetailsController._
import uk.gov.hmrc.helptosavestub.models.ErrorResponse
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.helptosavestub.util.{Delays, Logging}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.smartstub._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext
import scala.util.Try

@Singleton
class IFPayePersonalDetailsController @Inject()(actorSystem: ActorSystem, appConfig: AppConfig, cc: ControllerComponents, servicesConfig: ServicesConfig)(
  implicit ec: ExecutionContext)
    extends IFController(cc, appConfig, servicesConfig)
    with Logging
    with Delays {

  val scheduler: Scheduler = actorSystem.scheduler

  val getPayeDetailsDelayConfig: DelayConfig = Delays.config("get-paye-personal-details", actorSystem.settings.config)
  private val ninoStatusRegex                = """PY(\d{3}).*""".r
  private val telephoneNumberGen: Gen[TelephoneNumber] = for {
    callingCode <- Gen.choose(1, 250)
    telephoneType <- Gen.oneOf(1, 2, 7)
    dialingCode <- listOfN(5, numChar).map(_.mkString)
    convertedAreaDiallingCode <- listOfN(3, numChar).map(_.mkString)
    phoneNumber <- listOfN(6, numChar).map(_.mkString)
  } yield TelephoneNumber(callingCode, telephoneType, dialingCode, convertedAreaDiallingCode, phoneNumber)

  def getPayeDetails(nino: String): Action[AnyContent] = desAuthorisedAction { _ =>
    withDelay(getPayeDetailsDelayConfig) { () =>
     val code: String = nino match {
        case _ => nino.substring(5)
      }

      val response = code match {
        case "INVALID_NINO" =>  Status(400)(ErrorResponse.errorJson(code, "Submission has not passed validation. Invalid parameter nino."))
        case "INVALID_ORIGINATOR_ID" =>  Status(400)(ErrorResponse.errorJson(code, "Submission has not passed validation. Invalid header Originator-Id."))
        case "INVALID_CORRELATIONID" =>  Status(400)(ErrorResponse.errorJson(code, "Submission has not passed validation. Invalid header CorrelationId."))
        case "NOT_FOUND_NINO" =>  Status(404)(ErrorResponse.errorJson(code, "The remote endpoint has indicated that the nino cannot be found."))
        case "RESOURCE_NOT_FOUND" =>  Status(404)(ErrorResponse.errorJson(code, "The remote endpoint has indicated that the PAYE taxpayer details not found."))
        case "SERVER_ERROR" =>  Status(500)(ErrorResponse.errorJson(code, "IF is currently experiencing problems that require live service intervention."))
        case "SERVICE_UNAVAILABLE" =>  Status(503)(ErrorResponse.errorJson(code, "IF is currently experiencing problems that require live service intervention."))

        case _ =>
          payeDetails(nino)
            .seeded(nino)
            .fold[Result] {
              logger.warn(s"Could not generate PayeDetails for NINO $nino")
              InternalServerError
            } { s =>
              logger.info(s"Returning PayePersonalDetails for NINO $nino:\n$s")
              Ok(Json.parse(s))
            }
      }

      withIfCorrelationID(response)
    }
  }

  private[controllers] def payeDetails(nino: String) =
    for { // scalastyle:ignore
      sex <- Gen.gender
      name <- Gen.forename(sex)
      surname <- Gen.surname
      date <- Gen.choose(100L, 1000L).map(LocalDate.ofEpochDay)
      address <- Gen.ukAddress
      postcode <- Gen.postcode
      countryCode <- Gen.choose(1, 250)
      telephone1 <- telephoneNumberGen
      telephone2 <- Gen.option(telephoneNumberGen)
    } yield
      s"""{
       |  "nino": "${nino.dropRight(1)}",
       |  "ninoSuffix": "${nino.takeRight(1)}",
       |  "names": {
       |    "1": {
       |      "sequenceNumber": 12345,
       |      "firstForenameOrInitial": "$name",
       |      "surname": "$surname",
       |      "startDate": "2000-01-01"
       |    }
       |  },
       |  "sex": "${sex.fold("F", "M")}",
       |  "dateOfBirth": "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
       |  "deceased": false,
       |  "addresses": {
       |    "1": {
       |      "line1": "${address.headOption.getOrElse("1 the road")}",
       |      "line2": "${address.drop(1).headOption.getOrElse("The Place")}",
       |      "line3": "Sometown",
       |      "line4": "Anyshire",
       |      "line5": "Line 5",
       |      "countryCode": $countryCode,
       |      "postcode": "$postcode",
       |      "sequenceNumber": 1,
       |      "startDate": "2000-01-01"
       |    }
       |  },
       |  "phoneNumbers": {
       |    "${telephone1.telephoneType}": ${Json.toJson(telephone1)}${telephone2
           .map { t =>
             s""",
           |    "${t.telephoneType}": ${Json.toJson(t)}""".stripMargin
           }
           .getOrElse("")}
       |  }
       |}""".stripMargin

}

object IFPayePersonalDetailsController {

  implicit class GenderOps(val gender: Gender) extends AnyVal {
    def fold[A](female: => A, male: => A): A = gender match {
      case Female => female
      case Male => male
    }
  }

  case class TelephoneNumber(
    callingCode: Int,
    telephoneType: Int,
    areaDiallingCode: String,
    convertedAreaDiallingCode: String,
    telephoneNumber: String)

  object TelephoneNumber {
    implicit val telephoneNumberWrites: Writes[TelephoneNumber] = Json.writes[TelephoneNumber]
  }

}
