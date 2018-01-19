package uk.gov.hmrc.helptosavestub.controllers

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.helptosavestub.models.NSIUserInfo
import uk.gov.hmrc.helptosavestub.util.Logging
import uk.gov.hmrc.play.microservice.controller.BaseController

object CreateDEAccountController extends BaseController with Logging {

  def createDEAccount()(implicit request: Request[AnyContent]): Result = {
    lazy val requestBodyText = request.body.asText.getOrElse("")

    request.body.asJson.map(_.validate[NSIUserInfo]) match {
      case None ⇒
        logger.error(s"No JSON found for /create-de-account request: $requestBodyText")
        BadRequest(Json.toJson(SubmissionFailureResponse(SubmissionFailure(None, "No JSON found", ""))))

      case Some(er: JsError) ⇒
        logger.error(s"Could not parse JSON found for /create-de-account request: $requestBodyText")
        BadRequest(Json.toJson(SubmissionFailureResponse(SubmissionFailure(None, "Invalid Json", er.toString))))

      case Some(JsSuccess(nsiUserInfo, _)) ⇒

        val status =
          if (nsiUserInfo.registrationChannel != "callCentre" ||
            nsiUserInfo.contactDetails.communicationPreference != "00") {
            BAD_REQUEST
          } else {
            CREATED
          }

        logger.info(s"Responding to /create-de-account with status $status")

        Status(status)
    }
  }
}
