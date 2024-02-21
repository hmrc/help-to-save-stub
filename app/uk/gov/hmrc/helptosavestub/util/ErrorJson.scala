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

import org.apache.pekko.http.scaladsl.model.{StatusCode, StatusCodes}
import play.api.libs.json.{JsValue, Json}

object ErrorJson {

  def errorJson(status: Int): JsValue = {
    val reason = "intentional error"
    val code = (if ((100 to 599 contains status)) {
                  StatusCode.int2StatusCode(status)
                } else {
                  StatusCodes.custom(status, reason, "", false, false)
                }).reason()

    Json.parse(s"""
        {
          "code":   "$code",
          "reason": "$reason"
        }
       """.stripMargin)
  }
}
