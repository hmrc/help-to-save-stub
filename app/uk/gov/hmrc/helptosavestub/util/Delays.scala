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

import org.apache.pekko.actor.Scheduler
import org.apache.pekko.pattern.after
import uk.gov.hmrc.helptosavestub.config.AppConfig
import uk.gov.hmrc.helptosavestub.util.Delays.DelayConfig

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait Delays {

  val scheduler: Scheduler

  def withDelay[A](delayConfig: DelayConfig)(f: () => A)(implicit ec: ExecutionContext): Future[A] =
    if (delayConfig.enabled) {
      after(nextDelay(delayConfig), scheduler)(Future(f()))
    } else {
      Future(f())
    }

  // return a random delay based on a normal distribution floored with some configured minimum delay
  private def nextDelay(delayConfig: DelayConfig): FiniteDuration = {
    val nanos = (Random.nextGaussian() * delayConfig.standardDeviationNanos).toLong + delayConfig.meanDelayNanos
    nanos.max(delayConfig.minimumDelayNanos).nanos
  }

}

object Delays {

  case class DelayConfig(
    enabled: Boolean,
    meanDelay: FiniteDuration,
    standardDeviation: FiniteDuration,
    minimumDelay: FiniteDuration) {
    val meanDelayNanos: Long         = meanDelay.toNanos
    val standardDeviationNanos: Long = standardDeviation.toNanos
    val minimumDelayNanos: Long      = minimumDelay.toNanos
  }

  def config(name: String)(implicit appConfig: AppConfig): DelayConfig =    
    appConfig.delayConfig(name)
}
