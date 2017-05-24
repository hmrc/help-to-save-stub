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

import javax.inject.{Inject, Singleton}

import play.api.i18n.MessagesApi
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.helptosavestub.Constants._
import uk.gov.hmrc.helptosavestub.models.CreateAccount

@Singleton
class SquidController @Inject()(val messagesApi: MessagesApi) extends BaseController {

  private def errorJson(code: String, messageKey: String = "", detailKey: String = ""): JsValue = {
    val errorMap: Map[String, Map[String, String]] =
      Map("error" ->
        Map("errorMessageId" -> code,
          "errorMessage" -> messagesApi(messageKey),
          "errorDetail" -> messagesApi(detailKey)))
    Json.toJson(errorMap)
  }

  private def errorJsonWithTriple(triple: (String, String, String)): JsValue = {
    val errorMap: Map[String, Map[String, String]] =
      Map("error" ->
        Map("errorMessageId" -> triple._1,
          "errorMessage" -> triple._2,
          "errorDetail" -> triple._3))
    Json.toJson(errorMap)
  }

  private def validPostcode(postcode: String): Boolean = {
    val noSpacesPostcode = postcode.filter{_ != ' '}
    val row2Expr = """^[A-P|R-U|WYZ]\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row3Expr = """^[A-P|R-U|WYZ]\d\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row4Expr = """^[A-P|R-U|WYZ][A-H|K-Y]\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row5Expr = """^[A-P|R-U|WYZ][A-H|K-Y]\d\d\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row6Expr = """^[A-P|R-U|WYZ][A-H|K-Y]\d[ABEHMNPR|V-Y]\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    val row7Expr = """^[A-P|R-U|WYZ]\d[A-H|HJK|S-U|W]\d[AB|D-H|JLN|P-U|W-Z][AB|D-H|JLN|P-U|W-Z]$""".r
    (noSpacesPostcode == "GIR0AA") || (noSpacesPostcode match {
      case row2Expr() => true
      case row3Expr() => true
      case row4Expr() => true
      case row5Expr() => true
      case row6Expr() => true
      case row7Expr() => true
      case _  => false
    })
  }

  private def validateCreateAccount(json: JsValue): Either[(String, String, String), CreateAccount] = {
    //Convert incoming json to a case class
    val parseResult = Json.fromJson[CreateAccount]((json \ "createAccount").get)

    parseResult match {
      case JsSuccess(createAccount, _) => if (validPostcode(createAccount.postcode)) {
        Right(createAccount)
      } else {
        Left((INVALID_POSTCODE_ERROR_CODE, messagesApi("site.invalid-postcode"), messagesApi("site.invalid-postcode-detail")))
      }
      case JsError(errors) => Left((UNABLE_TO_PARSE_COMMAND_ERROR_CODE, messagesApi("site.unparsable-command"), errors.toString()))
    }
  }

  def processBody(request: Request[AnyContent]): Result = {
    request.body.asJson match {
      case None => BadRequest(Json.toJson(errorJson(NO_JSON_ERROR_CODE, "site.no-json", "site.no-json-detail")))

      case Some(json: JsValue) => {
        if (json.asInstanceOf[JsObject].fields.size != 1 || (json.asInstanceOf[JsObject].fields.head._1 != "createAccount")) {
          BadRequest(errorJson(NO_CREATEACCOUNTKEY_ERROR_CODE, "site.no-create-account-key", "site.no-create-account-key-detail"))
        } else {
          val nino = (json \ "createAccount" \ "NINO").get.asOpt[String]
          nino match {
            case Some(aNino) if aNino.startsWith("ER400") => BadRequest(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER401") => Unauthorized(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER403") => Forbidden(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER404") => NotFound(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER405") => MethodNotAllowed(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER415") => UnsupportedMediaType(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER500") => InternalServerError(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(aNino) if aNino.startsWith("ER503") => ServiceUnavailable(errorJson(PRECANNED_RESPONSE_ERROR_CODE, "site.pre-canned-error", "site.pre-canned-error-detail"))
            case Some(n) => {
              validateCreateAccount(json) match {
                case Right(_) => Ok
                case Left(errorTriple) => BadRequest(errorJsonWithTriple(errorTriple))
              }
            }
            case None => Ok
          }
        }
      }
    }

  }


  def createAccount(): Action[AnyContent] = Action { request =>
    val mimeType = request.headers.toSimpleMap.get(CONTENT_TYPE)

    mimeType match {
      case Some("application/json") => processBody(request)
      case _ => UnsupportedMediaType
    }
  }
}
