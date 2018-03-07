package uk.gov.hmrc.helptosavestub.controllers

import play.api.libs.json.JsValue
import uk.gov.hmrc.helptosavestub.models.NSIErrorResponse

trait NSIGetAccountBehaviour {

  //Need to update NINOs when they have been finalized
  def getAccount(nino: String): Option[JsValue] =
    if (nino.startsWith("EC01")){
      Some(NSIErrorResponse.missingVersionResponse)
    } else if (nino.startsWith("EC02")){
      Some(NSIErrorResponse.unsupportedVersion)
    } else if (nino.startsWith("EC03")){
      Some(NSIErrorResponse.missingNino)
    } else if (nino.startsWith("EC04")){
      Some(NSIErrorResponse.badNino)
    } else if (nino.startsWith("EC05")){
      Some(NSIErrorResponse.unknownNino)
    } else {
      None
    }



}