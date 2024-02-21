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
import org.apache.pekko.actor.Scheduler
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig

import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}
import scala.concurrent.duration._

class DelaysSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
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

    "read parameters from config correctly" in {
      Delays.config(
        "test",
        config("test", "true", "1 second", "2 seconds", "0 seconds")
      ) shouldBe DelayConfig(true, 1.second, 2.seconds, 0.seconds)

      Delays.config("test", config("test", "false", "1 minute", "2 minutes", "0 minutes")) shouldBe DelayConfig(
        false,
        1.minute,
        2.minutes,
        0.minutes)
    }

    "delay actions if configured to do so" in new TestDelays(
      Delays.config("test", config("test", "true", "1 second", "0 seconds", "0 seconds"))) {
      val result: Future[String] = withDelay(delayConfig)(() => "hello")

      time.advance(1.second - 1.millisecond)
      a[TimeoutException] shouldBe thrownBy(Await.result(result, 1.second))

      time.advance(1.millisecond)
      Await.result(result, 1.second) shouldBe "hello"
    }

    "not delay actions if configured to do so" in new TestDelays(
      Delays.config("test", config("test", "false", "1 second", "0 seconds", "0 seconds"))) {
      val result: Future[String] = withDelay(delayConfig)(() => "hello")
      // shouldn't need to advance time for the future to complete
      Await.result(result, 1.second) shouldBe "hello"
    }

  }

}
