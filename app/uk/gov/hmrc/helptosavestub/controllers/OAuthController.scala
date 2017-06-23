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

import hmrc.smartstub._
import org.scalacheck.Gen
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.helptosavestub.controllers.OAuthController.Tokens
import uk.gov.hmrc.play.microservice.controller.BaseController

class OAuthController extends BaseController {

  implicit val stringToLong = new ToLong[String] {
    override def asLong(i: String): Long = i.hashCode.toLong
  }

  val authorisationCodeGen = Gen.alphaNumStr

  val tokenGenerator = for {
    accessToken ← Gen.alphaStr
    refreshToken ← Gen.alphaStr
    expiresIn ← Gen.choose(0L,100L)
  } yield Tokens(accessToken, refreshToken, expiresIn)


  def getAuthorisationCode(client_id: String,
                           scope: List[String],
                           response_type: String,
                           redirect_uri: String) = Action { implicit request ⇒
    authorisationCodeGen.seeded(client_id).fold {
      val message = "Could not generate authorisation code"
      Logger.error(message)
      Redirect(redirect_uri, Map("error" → Seq(message)), INTERNAL_SERVER_ERROR)
    } { code ⇒
      Logger.info(s"Received request to get authorisation code. Client ID was $client_id, " +
        s"scopes were [${scope.mkString(",")}], response type was $response_type," +
        s"redirect url was $redirect_uri. Redirecting with code $code")

      Redirect(redirect_uri, Map("code" → Seq(code)))
    }
  }

  def createOrRefreshToken = Action { implicit request ⇒
    if(request.queryString.isEmpty){
      grantTypeForm.bindFromRequest().fold(
        _ ⇒ BadRequest("Grant type not specified"),
        _ match {
          case "authorization_code" ⇒
            createTokenForm.bindFromRequest().fold(
              e ⇒ BadRequest(s"Could not read parameters to create tokens: [${errorString(e)}]"),
              (getToken _).tupled)

          case "refresh_token" ⇒
            refreshForm.bindFromRequest().fold(
              e ⇒ BadRequest(s"Could not read parameters to refresh token: [${errorString(e)}]"),
              (refreshToken _).tupled)

          case other ⇒
            BadRequest(s"Grant type not recognised: $other")
        })
    } else {
      BadRequest("Requests to this endpoint cannot be made with data in query parameters")
    }
  }

  private def errorString[A](form: Form[A]): String =
    form.errors.map(e ⇒ s"${e.key}: [${e.messages.mkString(", ")}]").mkString("; ")

  private def getToken(client_id: String,
                       client_secret: String,
                       redirect_uri: String,
                       code: String): Result =
    tokenGenerator.seeded(code).fold {
      val message = "Could not generate tokens"
      Logger.error(message)
      InternalServerError(message)
    } { tokens ⇒
      Logger.info(s"Received request for token: redirect was $redirect_uri, " +
        s"code was $code, client ID was $client_id, client secret was $client_secret. Responding with tokens: " +
        s"(access token: ${tokens.access_token}, refresh token: ${tokens.refresh_token})")

      Ok(Json.toJson(tokens))
    }

  private def refreshToken(client_id: String,
                           client_secret: String,
                           refresh_token: String): Result =
    tokenGenerator.seeded(refresh_token).fold {
      val message = "Could not generate tokens"
      Logger.error(message)
      InternalServerError(message)
    } { tokens ⇒
      Logger.info(s"Received request for token: refresh token was $refresh_token, " +
        s"client ID was $client_id, client secret was $client_secret " +
        s"Responding with tokens: (access token: ${tokens.access_token}, refresh token: ${tokens.refresh_token})")

      Ok(Json.toJson(tokens))
    }

  private val createTokenForm = Form(
    tuple(
      "client_id" -> nonEmptyText,
      "client_secret" -> nonEmptyText,
      "redirect_uri" -> nonEmptyText,
      "code" -> nonEmptyText
    )
  )

  private val refreshForm = Form(
    tuple(
      "client_id" -> nonEmptyText,
      "client_secret" -> nonEmptyText,
      "refresh_token" -> nonEmptyText
    )
  )

  private val grantTypeForm = Form("grant_type" -> nonEmptyText)
}


object OAuthController {

  case class Tokens(access_token: String, refresh_token: String, expires_in: Long)

  implicit val tokenWrites: Writes[Tokens] = Json.writes[Tokens]

}