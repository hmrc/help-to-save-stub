/*
 * Copyright 2019 HM Revenue & Customs
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
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Environment, Play}
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.play.test.UnitSpec

trait TestSupport extends WordSpecLike with Matchers with UnitSpec with BeforeAndAfterAll {

  lazy val additionalConfig = Configuration()

  def buildFakeApplication(additionalConfig: Configuration): Application =
    new GuiceApplicationBuilder()
      .configure(
        Configuration(
          ConfigFactory.parseString("""
            | metrics.enabled       = false
          """.stripMargin)
        ) ++ additionalConfig)
      .build()

  lazy val fakeApplication: Application = buildFakeApplication(additionalConfig)

  override def beforeAll() {
    Play.start(fakeApplication)
    super.beforeAll()
  }

  override def afterAll() {
    Play.stop(fakeApplication)
    super.afterAll()
  }

  implicit lazy val configuration: Configuration = fakeApplication.injector.instanceOf[Configuration]

  implicit lazy val env: Environment = fakeApplication.injector.instanceOf[Environment]

  val testCC        = play.api.test.Helpers.stubControllerComponents()
  val testAppConfig = fakeApplication.injector.instanceOf[AppConfig]

  private val generator = new Generator(1)

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
