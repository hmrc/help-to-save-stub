package uk.gov.hmrc.helptosavestub.controllers

import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.helptosavestub.controllers.DWPController.UCDetails
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController

class DWPController extends BaseController with Logging {

  val wp01Json = UCDetails("Y", Some("Y"))
  val wp02Json = UCDetails("Y", Some("N"))
  val wp03Json = UCDetails("N", None)

  private def getHttpStatus(nino: String): Result = {
    val result = nino.substring(2, 4).toInt
    Status(result)
  }

  private def getECResponse(nino: String)(implicit request: Request[AnyContent]): Result = {
     if (nino.startsWith("WP01")) { Ok(Json.toJson(wp01Json)) }
     else if (nino.startsWith("WP02")) { Ok(Json.toJson(wp02Json)) }
     else if (nino.startsWith("WP03")) { Ok(Json.toJson(wp03Json)) }
     else if (nino.startsWith("WS")) { getHttpStatus(nino) }
     else if (nino.startsWith("WT")) { InternalServerError }
     else BadRequest
    }

  def dwpClaimantCheck(nino: String): Action[AnyContent] = Action {
    implicit request ⇒ {
      getECResponse(nino)
    }
  }

}

object DWPController {

  case class UCDetails(ucClaimant: String, withinThreshold: Option[String])

  object UCDetails {
     implicit val format: Format[UCDetails] =  Json.format[UCDetails]
  }
}