package uk.gov.hmrc.helptosavestub.controllers

import java.time.LocalDate

import play.api.libs.json.{Format, Json}

/**
  * Created by jackie on 03/08/17.
  */
class UserDetailsController {








}


object UserDetailsController{
  case class UserDetails(
                          givenName: Option[String],
                          middleName: Option[String],
                          familyName: Option[String],
                          birthdate: Option[LocalDate],
                          formattedAddress: Option[String],
                          postCode: Option[String],
                          countryNme: Option[String],
                          countryCode: Option[String],
                          email: Option[String]
                        )

  object UserDetails {
    implicit val userDetailFormat: Format[UserDetails] = Json.format[UserDetails]
  }
}
