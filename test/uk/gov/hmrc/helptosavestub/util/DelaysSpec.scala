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

import com.github.pjfanning.pekko.scheduler.mock.VirtualTime
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.pekko.actor.Scheduler
import org.mockito.Mockito.*
import org.scalactic.Prettifier.default
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}

class DelaysSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar {

  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit lazy val mockAppConfig: AppConfig = mock[AppConfig]
  implicit lazy val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]


  def config(
    name: String,
    delayEnabled: String,
    meanDelay: String,
    standardDeviation: String,
    minimumDelay: String): Config =
    ConfigFactory.parseString(s"""
         |delays {
         |  $name {
         |    enabled: $delayEnabled,
         |    mean-delay: $meanDelay
         |    standard-deviation = $standardDeviation
         |    minimum-delay = $minimumDelay
         |  }
         |}
      """.stripMargin)

  class TestDelays(val delayConfig: DelayConfig) extends Delays {
    val time = new VirtualTime
    override val scheduler: Scheduler = time.scheduler
  }

  "Delays" must {

    "read parameters from config correctly" in  {
      val configWithTrueAndTimeInSeconds = config("test", "true", "1 second", "2 seconds", "0 seconds")
      when(mockAppConfig.delayConfig("test"))
        .thenReturn(
          DelayConfig(
            configWithTrueAndTimeInSeconds.getBoolean(s"delays.test.enabled"),
          (configWithTrueAndTimeInSeconds.getDuration(s"delays.test.mean-delay").toSeconds, SECONDS),
          (configWithTrueAndTimeInSeconds.getDuration(s"delays.test.standard-deviation").toSeconds, SECONDS),
          (configWithTrueAndTimeInSeconds.getDuration(s"delays.test.minimum-delay").toSeconds, SECONDS)
        ))

      Delays.config("test") shouldBe
        DelayConfig(true, 1.second, 2.seconds, 0.seconds)

      val configWithFalseAndTimeInMinutes =  config("test", "false", "1 minute", "2 minutes", "0 minutes")
      when(mockAppConfig.delayConfig("test"))
        .thenReturn(
          DelayConfig(
            configWithFalseAndTimeInMinutes.getBoolean(s"delays.test.enabled"),
            (configWithFalseAndTimeInMinutes.getDuration(s"delays.test.mean-delay").toMinutes, MINUTES),
            (configWithFalseAndTimeInMinutes.getDuration(s"delays.test.standard-deviation").toMinutes, MINUTES),
            (configWithFalseAndTimeInMinutes.getDuration(s"delays.test.minimum-delay").toMinutes, MINUTES)
          ))
      Delays.config("test") shouldBe DelayConfig(
        false,
        1.minute,
        2.minutes,
        0.minutes)
    }

    "delay actions if configured to do so" in new TestDelays(
      Delays.config("test"))
    {
      val configTest: Config = config("test", "true", "1 second", "0 seconds", "0 seconds")
      val result: Future[String] = withDelay(DelayConfig(
        configTest.getBoolean(s"delays.test.enabled"),
        (configTest.getDuration(s"delays.test.mean-delay").toSeconds, SECONDS),
        (configTest.getDuration(s"delays.test.standard-deviation").toSeconds, SECONDS),
        (configTest.getDuration(s"delays.test.minimum-delay").toSeconds, SECONDS)
      ))(() => "hello")

      time.advance(1.second - 1.millisecond)
      a[TimeoutException] shouldBe thrownBy(Await.result(result, 1.second))

      time.advance(1.millisecond)
      Await.result(result, 1.second) shouldBe "hello"
    }

    "not delay actions if configured to do so" in new TestDelays(
      Delays.config("test"))
        {
      val configTest: Config = config("test", "false", "1 second", "0 seconds", "0 seconds")
      val result: Future[String] = withDelay(DelayConfig(
        configTest.getBoolean(s"delays.test.enabled"),
        (configTest.getDuration(s"delays.test.mean-delay").toSeconds, SECONDS),
        (configTest.getDuration(s"delays.test.standard-deviation").toSeconds, SECONDS),
        (configTest.getDuration(s"delays.test.minimum-delay").toSeconds, SECONDS)
      ))(() => "hello")
      // shouldn't need to advance time for the future to complete
      Await.result(result, 1.second) shouldBe "hello"
    }

  }

}
