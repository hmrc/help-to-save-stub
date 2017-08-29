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

package uk.gov.hmrc.helptosavestub.models

import play.api.libs.json._

// Unfortunately, this is almost exactly the same as CreateAccount with the exception of the type of the birthdate.
// The ICD001 document specifies various errors that could be generated when the date is wrong and so it has to be
// parsed into a date during the validation process rather than the JSON parsing period - the latter either succeeds or
// fails.

object SquidModels {
  case class ContactDetails(address1:                String,
                            address2:                String,
                            address3:                Option[String],
                            address4:                Option[String],
                            address5:                Option[String],
                            postcode:                String,
                            countryCode:             Option[String],
                            email:                   Option[String],
                            phoneNumber:             Option[String],
                            communicationPreference: String)

  object ContactDetails {
    implicit val contactDetailsFormat: Format[ContactDetails] = Json.format[ContactDetails]
  }

  case class AccountCommand(forename:            String,
                            surname:             String,
                            dateOfBirth:         String,
                            nino:                String,
                            contactDetails:      ContactDetails,
                            registrationChannel: String)

  object AccountCommand {
    implicit val accountCommandFormat: Format[AccountCommand] = Json.format[AccountCommand]
  }

}
