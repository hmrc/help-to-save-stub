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

import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import play.api.{Application, Configuration, Environment, Play}
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.util.UnitSpec

import scala.concurrent.ExecutionContext

trait TestSupport extends UnitSpec with BeforeAndAfterAll {

  lazy val additionalConfig: Configuration = Configuration()
  lazy val fakeApplication: Application = buildFakeApplication(additionalConfig)
  val testCC: ControllerComponents = play.api.test.Helpers.stubControllerComponents()
  lazy val testAppConfig: AppConfig = fakeApplication.injector.instanceOf[AppConfig]
  private val generator                 = new Generator(1)
  implicit lazy val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
  implicit lazy val appConfig: AppConfig = fakeApplication.injector.instanceOf[AppConfig]

  implicit lazy val configuration: Configuration = fakeApplication.injector.instanceOf[Configuration]

  implicit lazy val env: Environment = fakeApplication.injector.instanceOf[Environment]

  def buildFakeApplication(additionalConfig: Configuration): Application =
    new GuiceApplicationBuilder()
      .configure(
        Configuration(
          ConfigFactory.parseString("""
            | metrics.enabled       = false
          """.stripMargin)
        ).withFallback(additionalConfig))
      .build()

  override def beforeAll(): Unit = {
    Play.start(fakeApplication)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    Play.stop(fakeApplication)
    super.afterAll()
  }

  def randomNINO(): String = generator.nextNino.nino

}

object TestSupport {

  implicit class StringOps(val s: String) extends AnyVal {

    def withPrefixReplace(prefix: String): String =
      prefix + s.drop(prefix.length())

    def withSuffixReplace(suffix: String): String =
      s.dropRight(suffix.length()) + suffix

  }

}
