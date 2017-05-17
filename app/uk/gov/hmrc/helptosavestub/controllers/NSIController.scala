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

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Headers}
import uk.gov.hmrc.helptosavestub.models.{CreateAccount, NSIUserInfo}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.controllers.SubmissionFailure._

import scala.concurrent.Future
import scala.util.{Success, Try}

object NSIController extends BaseController {

  val testAuthHeader = "test.user:test123"

  /**
    * Both HMRC and NS&I want a header wit the key "Authorisation". Try to decode
    * all header values for the key "Authorisation" and see if any of them match the
    * expected username/password combo
    */

  def isAuthorised(headers: Headers): Boolean = {
    val decoded = headers.headers.filter(_._1 == "Authorization")
      .map(h ⇒ Try(BaseEncoding.base64().decode(h._2)))
      .collect { case Success(bytes) ⇒ new String(bytes, Charsets.UTF_8)}

    decoded.contains(testAuthHeader)
  }

  def createAccount() = Action.async { implicit request =>
    if(isAuthorised(request.headers)) {
      lazy val requestBodyText = request.body.asText.getOrElse("")

      request.body.asJson.map(_.validate[CreateAccount]) match {
        case None ⇒
          Logger.error(s"No JSON found for create-account request: $requestBodyText")
          Future.successful(BadRequest(
            Json.toJson(SubmissionFailure(None,"No JSON found",""))))

        case Some(er: JsError) ⇒
          Logger.error(s"Could not parse JSON found for create-account request: $requestBodyText")
          Future.successful(BadRequest(
            Json.toJson(SubmissionFailure(None,"Invalid Json", er.toString))))

        case Some(JsSuccess(createAccount, _)) ⇒
          NSIUserInfo(createAccount).fold(
            errors  ⇒ {
              Logger.error(s"User details not valid for create-account request: $requestBodyText")
              Future.successful(BadRequest(
                Json.toJson(SubmissionFailure(None, "Invalid user details", errors.toList.mkString(",")))))
            },
            _ ⇒ Future.successful(Created)
          )
      }
    } else {
      Logger.error("No authorisation data found in header")
      Future.successful(Unauthorized)
    }
  }
}

case class SubmissionFailure(errorMessageId:Option[String], errorMessage:String, errorDetail:String)

object SubmissionFailure{
  implicit val submissionFailureFormat: Writes[SubmissionFailure] = Json.writes[SubmissionFailure]
}
