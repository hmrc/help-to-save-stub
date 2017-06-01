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

package uk.gov.hmrc.helptosavestub

object Constants {
  val NO_JSON_ERROR_CODE = "AAAA0002"
  val NO_CREATEACCOUNTKEY_ERROR_CODE = "AAAA0003"
  val PRECANNED_RESPONSE_ERROR_CODE = "AAAA0004"
  val UNABLE_TO_PARSE_COMMAND_ERROR_CODE = "AAAAA0005"
  val INVALID_POSTCODE_ERROR_CODE = "ZYRC0506"
  val LEADING_SPACES_ERROR_CODE = "ZYRA0703"
  val NUMERIC_CHARS_ERROR_CODE = "ZYRA0705"
  val DISALLOWED_CHARS_ERROR_CODE = "ZYRA0711"
  val FIRST_CHAR_SPECIAL_ERROR_CODE = "ZYRA0712"
  val LAST_CHAR_SPECIAL_ERROR_CODE = "ZYRA0713"
  val TOO_FEW_INITIAL_ALPHA_ERROR_CODE = "ZYRA0714"
  val TOO_FEW_CONSECUTIVE_ALPHA_ERROR_CODE = "ZYRA0715"
  val TOO_MANY_CONSECUTIVE_SPECIAL_ERROR_CODE = "ZYRA0716"
  val UNPARSABLE_DATE_ERROR_CODE = "CWFDAT02"
  val BAD_DAY_DATE_ERROR_CODE = "CWDAT03"
  val BAD_MONTH_DATE_ERROR_CODE = "CWFDAT04"
  val BAD_CENTURY_DATE_ERROR_CODE = "CWDAT06"
  val UNKNOWN_COUNTRY_CODE_ERROR_CODE = "TAR10005"
  val BAD_NINO_ERROR_CODE = "ZYRC0508"
}
