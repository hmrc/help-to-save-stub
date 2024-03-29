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

package uk.gov.hmrc.helptosavestub.util

import java.nio.charset.Charset
import org.apache.pekko.stream.Materializer
import org.apache.pekko.util.ByteString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

trait UnitSpec extends AnyWordSpec with Matchers {

  implicit val defaultTimeout: FiniteDuration = 5 seconds

  def bindModules: Seq[GuiceableModule] = Seq()

  def status(of: Future[play.api.mvc.Result])(implicit timeout: Duration): Int = status(Await.result(of, timeout))

  def status(of: play.api.mvc.Result): Int = of.header.status

  def jsonBodyOf(
    resultF: Future[play.api.mvc.Result])(implicit mat: Materializer, ec: ExecutionContext): Future[JsValue] =
    resultF.map(jsonBodyOf)

  def jsonBodyOf(result: play.api.mvc.Result)(implicit mat: Materializer): JsValue = Json.parse(bodyOf(result))

  def bodyOf(result: play.api.mvc.Result)(implicit mat: Materializer): String = {
    val bodyBytes: ByteString = await(result.body.consumeData)
    bodyBytes.decodeString(Charset.defaultCharset().name)
  }

  def await[A](future: Future[A])(implicit timeout: Duration): A =
    Await.result(future, timeout)

  def bodyOf(resultF: Future[play.api.mvc.Result])(implicit mat: Materializer, ec: ExecutionContext): Future[String] =
    resultF.map(bodyOf)

  case class ExternalService(
    serviceName: String,
    runFrom: String            = "SNAPSHOT_JAR",
    classifier: Option[String] = None,
    version: Option[String]    = None)

}
