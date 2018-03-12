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

import java.util.UUID

import cats.instances.string._
import cats.syntax.eq._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.helptosavestub.models.NSIErrorResponse
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class NSIGetAccountController extends BaseController with NSIGetAccountBehaviour with Logging {

  def queryAccount(resource: String): Action[AnyContent] = Action {
    implicit request ⇒
      handleQuery(resource, request.rawQueryString)
  }

  private def handleQuery(resource: String, queryString: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Result = {
    extractParams(resource, queryString).fold(
      {
        error ⇒ BadRequest(Json.toJson(NSIErrorResponse.missingNinoError))
      }, {
        map ⇒ validateParams(map)
          if (nino.contains("401")) {
            Unauthorized
          } else if (nino.contains("500")) {
            InternalServerError
          } else if (nino.startsWith("EM") || nino.startsWith("TM")) {
            Ok(Json.toJson(getAccountByNino(nino)))
          } else {
            logger.info("error response") //we are here!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Ok(Json.toJson(getErrorResponse(nino)))
          }
      }
    )
  }

  private def validateParams(map: Map[String, String]): Either[String, String] = {

   val z =  for {
      nino ← map.get("nino")
      version ←  map.get("version")
      systemId ← map.get("systemId")

    } yield (nino, version, systemId)

    z.fold(
      Left(""))(
        x ⇒ {
          val ninox = x._1
          val versionx = x._2
          val systemIdx = x._3

          val validParams = for {
            nino ← ninox.matches(ninoRegex)
            
          }

          Right(ninox, versionx, systemIdx)
        }
      )

    val ninoRegex: Regex = """[A-Za-z]{2}[0-9]{6}[A-Za-z]{1}""".r



//    if(map.contains("nino")) {
//      val nino = map.get("nino")
//      nino.matches(ninoRegex)
//
//
//      ni
//      Right("AE123456C")
//    } else if (map.contains("version")) {
//      val version = map.get("version")
//
//      ver
//
//      UUID.fromString(corre)
//    }
//
//    else{
//      Left("sas")
//    }

  }

  private def getParams(rawQueryString: String): Map[String, String] = {
    rawQueryString
      .split("&")
      .map(keyValue ⇒ (keyValue.split("=")(0), keyValue.split("=")(1)))
      .toMap
  }

  private def extractParams(resource: String, rawQueryString: String): Either[String, Map[String, String]] = {
    if (resource === "account") {
      Try(getParams(rawQueryString)) match {
        case Success(map) ⇒ Right(map)
        case Failure(error) ⇒ Left(error.getMessage)
      }
    } else {
      Left("unknown resource")
    }
  }

}
