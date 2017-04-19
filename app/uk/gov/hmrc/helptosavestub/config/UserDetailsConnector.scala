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

package uk.gov.hmrc.helptosavestub.config

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import play.api.libs.json._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Email(email: String) extends AnyVal

object Email {
  implicit val emailFormat: Format[Email] = Json.format[Email]
}

@ImplementedBy(classOf[UserDetailsConnectorImpl])
trait UserDetailsConnector {
  def getEmail(userDetailsId: String)(implicit hc: HeaderCarrier): Future[Email]
}

@Singleton
class UserDetailsConnectorImpl extends UserDetailsConnector with ServicesConfig {

  private val userDetailsRoot = baseUrl("user-details")
  private val serviceURL = "user-details/id"
  private val http = WSHttp

  case class EmailException(id: String) extends Exception(s"Could not find email in the user details for id: $id ")

  override def getEmail(userDetailsId: String)(implicit hc: HeaderCarrier): Future[Email] = {

    http.GET(s"$userDetailsRoot/$serviceURL/$userDetailsId").flatMap {
      _.json.\("email").validate[String] match {
        case JsSuccess(email, _) => Future.successful(Email(email))
        case _ ⇒ Future.failed[Email](EmailException(userDetailsId))
      }
    }
  }
}
