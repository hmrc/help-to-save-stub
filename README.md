[![Build Status](https://travis-ci.org/hmrc/help-to-save-stub.svg)](https://travis-ci.org/hmrc/help-to-save-stub) [ ![Download](https://api.bintray.com/packages/hmrc/releases/help-to-save-stub/images/download.svg) ](https://bintray.com/hmrc/releases/help-to-save-stub/_latestVersion)

## Help to Save Stub 

## About Help to Save

Please click [here](https://github.com/hmrc/help-to-save-frontend#product-repos) for more information

## How to run

Run the help-to-save-stub service on port 7002, e.g.:
```
sbt "run 7002"
```

## How to test

The unit tests can be run by running
```
sbt test
```

## How to deploy

This microservice is deployed as per all MDTP microservices via Jenkins into a Docker slug running on a Cloud Provider.

## Endpoints

# POST /nsihts/createaccount
 Given a nino of `STxyz...` this endpoint responds with a status of `xyz`, `xyz` being any of the possible HTTP responses.

# PUT /nsihts/createaccount
 If the nino: XX999999X is given, a health check response with an OK status is returned otherwise an update email response with an OK status is returned.

# PUT /help-to-save/accounts/:nino
 If the given nino starts with a C, 403 (Forbidden) response is returned, if the nino starts with an E, 500 (Internal Server Error) is returned, and 200 OK
 response is returned for all other ninos.

# GET /help-to-save/eligibility-check/:nino
 If the given nino starts with EL, an eligibility check result of eligible is returned, if the given nino starts with NE, a result of ineligible is returned,
 if the nino starts with AC, a result of an existing account holder is returned, if the nino starts with EE, an invalid result code is returned, with any other
 nino given, an eligible result is returned.
 If the nino starts with `ESxyz...` a response will be returned with status `xyz` and an error JSON response.

# POST /email-verification/verification-requests
 If a valid emailVerificationRequest is given in the request body as json, then a 200 OK response is returned. Also if successful the continue URL is logged,
 as this was the reason this endpoint was placed here in the stub. If invalid or no json is given in the request body then a 400 BAD REQUEST response is returned.


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html") 
