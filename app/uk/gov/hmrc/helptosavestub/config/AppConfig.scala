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

package uk.gov.hmrc.helptosavestub.config

import com.google.inject.{Inject, Singleton}
import play.api.{ConfigLoader, Configuration, Environment, Mode}
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

@Singleton
class AppConfig @Inject()(
  val runModeConfiguration: Configuration,
  val environment: Environment,
  val servicesConfig: ServicesConfig) {

  protected def mode: Mode = environment.mode

 /* val desHeaders: String = s"Bearer ${servicesConfig.getString("microservice.expectedDESHeaders")}"
  val ifHeaders: String = s"Bearer ${servicesConfig.getString("microservice.expectedIFHeaders")}"
*/

  val expectedDESHeaders: Seq[String] =
    runModeConfiguration.underlying
      .getStringList("microservice.expectedDESHeaders").asScala
      .map(e => s"Bearer $e")
      .toSeq

  val expectedHeaders: Seq[String] =
    runModeConfiguration.underlying
      .getStringList("microservice.expectedIFHeaders")
      .asScala.map(e => s"Bearer $e")
      .toSeq



  def delayConfig(name: String): DelayConfig = DelayConfig(
    servicesConfig.getBoolean(s"delays.$name.enabled"),
    runModeConfiguration.get[FiniteDuration](s"delays.$name.mean-delay"),
    runModeConfiguration.get[FiniteDuration](s"delays.$name.standard-deviation"),
    runModeConfiguration.get[FiniteDuration](s"delays.$name.minimum-delay")
  )
}
