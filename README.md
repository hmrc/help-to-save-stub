help-to-save-stub
=================
Provides endpoints for testing which mimic third party service endpoints which HTS depends on.

Table of Contents
=================
* [About Help to Save](#about-help-to-save)
* [Running and Testing](#running-and-testing)
   * [Running](#running)
   * [Unit tests](#unit-tests)
* [Endpoints](#endpoints)
   * [NS&amp;I stubs](#nsi-stubs)
   * [DES/ITMP stubs](#desitmp-stubs)
   * [Email verification stubs](#email-verification-stubs)
   * [DWP stubs](#dwp-stubs)
   * [GetPAYEPersonDetails stubs](#getpayepersondetails-stubs)
   * [Bank Account Reputation Service stubs](#bank-account-reputation-service-stubs)

About Help to Save
==================
Please click [here](https://github.com/hmrc/help-to-save#about-help-to-save) for more information.


Running and Testing
===================

Running
-------
Run `sbt run` on the terminal to start the service. The service runs on port 7002 by default.

Unit tests
----------

The unit tests can be run by running
```
sbt test
```

Endpoints
=========

NS&I stubs
----------
| Path                                                        | Method | Description  |
| ------------------------------------------------------------| ------ | ------------ |
| /nsi-services/account                                       | POST   | Mimics a call to create an account. Different responses are allowed for based on the NINO in the request |
| /nsi-services/account                                       | PUT    | If the nino: XX999999X is given, a health check response with an OK status is returned otherwise the request is treated as an update email|
| /nsi-services/account                                       | GET    | Gets account information using a predefined set of data mapped to certain NINOs |
| /nsi-services/transactions                                  | GET    | Gets transaction information using a predefined set of data mapped to certain NINOs|

DES/ITMP stubs
--------------
| Path                                                        | Method | Description  |
| ------------------------------------------------------------| ------ | ------------ |
| /help-to-save/eligibility-check/{NINO}                      | GET    | Mimics a call to check eligibility for HTS. Different responses are allowed for based on the NINO in the request  |
| /help-to-save/accounts/{NINO}                               | PUT    | Mimics a call to set the ITMP flag |
| /universal-credits/threshold-amount                         | GET    | Mimics call to get the HTS UC threshold value |

Email verification stubs
------------------------
| Path                                                        | Method | Description  |
| ------------------------------------------------------------| ------ | ------------ |
| /email-verification/verification-requests                   | POST   | Mimics call to verify an email address. When this endpoint is hit, a message will appear in the logs indicating where the continue URL is to simulate completing the email verification |  
 
 DWP stubs
 ---------
| Path                                                        | Method | Description  |
| ------------------------------------------------------------| ------ | ------------ |
| /hmrc/{NINO}                                                | GET    | Mimics a call to check the UC status. Different responses are allowed for based on the NINO in the request  |
| /hmrc-healthcheck                                           | GET    | Mimics the DWP health check endpoint |

  
GetPAYEPersonDetails stubs
--------------------------
| Path                                                        | Method | Description  |
| ------------------------------------------------------------| ------ | ------------ |
| /pay-as-you-earn/02.00.00/individuals/{NINO}                | GET    | Mimics a call to get user info via the GetPAYEPersonDetails API |

Bank Account Reputation Service stubs
-------------------------------------
| Path                                                        | Method | Description  |
| ------------------------------------------------------------| ------ | ------------ |
| /validateBankDetails                                        | GET    | Mimics a call to validate bank details. Different responses are allowed for based on the bank details being passed in |
  
  
License
=======
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html") 
* [License](#license)