const {checkCaseFlagsEnabled, checkCaseFlagsAndHmcEnabled, triggerCamundaProcess, waitForCompletedCamundaProcess} = require('./testingSupport');
const apiRequest = require('./apiRequest.js');
const {addAndAssertCaseFlag} = require('./caseFlagsHelper');
const {getHearingsPayload} = require('./apiRequest');
const chai = require('chai');
const {expect} = chai;
const {date} = require('../api/dataHelper');
const config = require('../config');
const {listedHearing} = require('./wiremock/data/hearings');
const {createUpdateStub} = require('./wiremock/wiremock');
const {hearingStubRequestBody, unnotifiedHearingStubRequestBody, getpartiesNotifiedStubRequestBody,
  putPartiesNotifiedStubRequestBody
} = require('./wiremock/requests/hearings');
const {
  AUTOMATED_HEARING_NOTICE,
  UNSPEC_AUTOMATED_HEARING_NOTICE_SCHEDULER,
  SPEC_AUTOMATED_HEARING_NOTICE_SCHEDULER
} = require('../fixtures/camundaProcesses');

const specServiceId = 'AAA6';
const unspecServiceId = 'AAA7';

const runningOnLocal = () => !['aat', 'demo', 'preview'].includes(config.runningEnv);
const locationId = () => runningOnLocal() ? '000000' : '424213';

const createHearingId = () => `${Math.floor(1000000000 + Math.random() * 9000000000)}`;
const getUILink = (process)=> `${process.links[0].href.replace('engine-rest', 'app/cockpit/default/#')}`;

const getExpectedPayload = (serviceId) => {
    if (serviceId === specServiceId) {
      return {
        'hmctsServiceID': 'AAA6',
        'hmctsInternalCaseName': '\'Test Inc\' v \'John Doe\', \'Second Defendant\'',
        'publicCaseName': '\'Test Inc\' v \'John Doe\', \'Second Defendant\'',
        'caseAdditionalSecurityFlag': false,
        'caseCategories': [
          {
            'categoryType': 'caseType',
            'categoryValue': 'AAA6-FAST_CLAIM'
          },
          {
            'categoryType': 'caseSubType',
            'categoryValue': 'AAA6-FAST_CLAIM',
            'categoryParent': 'AAA6-FAST_CLAIM'
          }
        ],
        'externalCaseReference': null,
        'caseManagementLocationCode': '424213',
        'caseSLAStartDate': date(),
        'autoListFlag': false,
        'hearingType': null,
        'hearingWindow': null,
        'duration': 0,
        'hearingPriorityType': 'Standard',
        'numberOfPhysicalAttendees': 0,
        'hearingInWelshFlag': false,
        'hearingLocations': [
          {
            'locationId': '424213',
            'locationType': 'court'
          }
        ],
        'facilitiesRequired': null,
        'listingComments': null,
        'hearingRequester': '',
        'privateHearingRequiredFlag': false,
        'caseInterpreterRequiredFlag': true,
        'panelRequirements': null,
        'leadJudgeContractType': '',
        'judiciary': {},
        'hearingIsLinkedFlag': false,
        'parties': [
          {
            'partyID': '',
            'partyType': 'ORG',
            'partyName': 'Test Inc',
            'partyRole': 'CLAI',
            'organisationDetails': {
              'name': 'Test Inc',
              'organisationType': 'ORG',
              'cftOrganisationID': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': 'Q1KOKP2',
            'partyType': 'ORG',
            'partyName': 'Civil - Organisation 1',
            'partyRole': 'LGRP',
            'organisationDetails': {
              'name': 'Civil - Organisation 1',
              'organisationType': 'ORG',
              'cftOrganisationID': 'Q1KOKP2'
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Doe',
            'partyRole': 'EXPR',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Doe',
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'john@doemail.com'
              ],
              'hearingChannelPhone': [
                '07111111111'
              ],
              'relatedParties': [],
              'custodyStatus': null,
              'otherReasonableAdjustmentDetails': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Smith',
            'partyRole': 'WITN',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Smith',
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'johnsmith@email.com'
              ],
              'hearingChannelPhone': [
                '07012345678'
              ],
              'relatedParties': [],
              'custodyStatus': null,
              'otherReasonableAdjustmentDetails': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'Sir John Doe',
            'partyRole': 'DEFE',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Doe',
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [
                'RA0019'
              ],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'seconddefendant@example.com'
              ],
              'hearingChannelPhone': [
                '07898678902'
              ],
              'relatedParties': [],
              'custodyStatus': null,
              'otherReasonableAdjustmentDetails': 'RA0019: Step free / wheelchair access: wheelchair'
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '79ZRSOU',
            'partyType': 'ORG',
            'partyName': 'Civil - Organisation 2',
            'partyRole': 'LGRP',
            'organisationDetails': {
              'name': 'Civil - Organisation 2',
              'organisationType': 'ORG',
              'cftOrganisationID': '79ZRSOU'
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Doe',
            'partyRole': 'EXPR',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Doe',
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'john@doemail.com'
              ],
              'hearingChannelPhone': [
                '07111111111'
              ],
              'relatedParties': [],
              'custodyStatus': null,
              'otherReasonableAdjustmentDetails': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'ORG',
            'partyName': 'Second Defendant',
            'partyRole': 'DEFE',
            'organisationDetails': {
              'name': 'Second Defendant',
              'organisationType': 'ORG',
              'cftOrganisationID': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          }
        ],
        'screenFlow': [
          {
            'screenName': 'hearing-requirements',
            'navigation': [
              {
                'resultValue': 'hearing-facilities'
              }
            ]
          },
          {
            'screenName': 'hearing-facilities',
            'navigation': [
              {
                'resultValue': 'hearing-stage'
              }
            ]
          },
          {
            'screenName': 'hearing-stage',
            'navigation': [
              {
                'resultValue': 'hearing-attendance'
              }
            ]
          },
          {
            'screenName': 'hearing-attendance',
            'navigation': [
              {
                'resultValue': 'hearing-venue'
              }
            ]
          },
          {
            'screenName': 'hearing-venue',
            'conditionKey': 'regionId',
            'navigation': [
              {
                'conditionOperator': 'INCLUDE',
                'conditionValue': '7',
                'resultValue': 'hearing-welsh'
              },
              {
                'conditionOperator': 'NOT INCLUDE',
                'conditionValue': '7',
                'resultValue': 'hearing-judge'
              }
            ]
          },
          {
            'screenName': 'hearing-welsh',
            'navigation': [
              {
                'resultValue': 'hearing-judge'
              }
            ]
          },
          {
            'screenName': 'hearing-judge',
            'navigation': [
              {
                'resultValue': 'hearing-timing'
              }
            ]
          },
          {
            'screenName': 'hearing-timing',
            'navigation': [
              {
                'resultValue': 'hearing-additional-instructions'
              }
            ]
          },
          {
            'screenName': 'hearing-additional-instructions',
            'navigation': [
              {
                'resultValue': 'hearing-create-edit-summary'
              }
            ]
          }
        ],
        'vocabulary': [
          {}
        ],
        'hearingChannels': [
          'INTER'
        ],
        'caseFlags': {
          'flags': [
            {
              'partyID': '',
              'partyName': 'Sir John Doe',
              'flagId': 'RA0019',
              'flagDescription': 'Step free / wheelchair access',
              'flagStatus': 'Active',
              'dateTimeCreated': ''
            },
            {
              'partyID': '',
              'partyName': 'Test Inc',
              'flagId': 'PF0015',
              'flagDescription': 'Language Interpreter',
              'flagStatus': 'Active',
              'dateTimeCreated': ''
            }
          ]
        },
        'caserestrictedFlag': false
      };
    }
    if (serviceId === unspecServiceId) {
      return {
        'hmctsServiceID': 'AAA7',
        'hmctsInternalCaseName': '\'Test Inc\' represented by \'Bob the litigant friend\' (litigation friend) v \'John Doe\', \'Foo Bar\'',
        'publicCaseName': '\'Test Inc\' represented by \'Bob the litigant friend\' (litigation friend) v \'John Doe\', \'Foo Bar\'',
        'caseAdditionalSecurityFlag': true,
        'caseCategories': [
          {
            'categoryType': 'caseType',
            'categoryValue': 'AAA7-FAST_CLAIM'
          },
          {
            'categoryType': 'caseSubType',
            'categoryValue': 'AAA7-FAST_CLAIM',
            'categoryParent': 'AAA7-FAST_CLAIM'
          }
        ],
        'externalCaseReference': null,
        'caseManagementLocationCode': locationId(),
        'caseSLAStartDate': date(),
        'autoListFlag': false,
        'hearingType': null,
        'hearingWindow': null,
        'duration': 0,
        'hearingPriorityType': 'Standard',
        'numberOfPhysicalAttendees': 0,
        'hearingInWelshFlag': false,
        'hearingLocations': [
          {
            'locationId': locationId(),
            'locationType': 'court'
          }
        ],
        'facilitiesRequired': [
          '11'
        ],
        'listingComments': null,
        'hearingRequester': '',
        'privateHearingRequiredFlag': false,
        'caseInterpreterRequiredFlag': false,
        'panelRequirements': null,
        'leadJudgeContractType': '',
        'judiciary': {},
        'hearingIsLinkedFlag': false,
        'parties': [
          {
            'partyID': '',
            'partyType': 'ORG',
            'partyName': 'Test Inc',
            'partyRole': 'CLAI',
            'organisationDetails': {
              'name': 'Test Inc',
              'organisationType': 'ORG',
              'cftOrganisationID': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': [
              {
                'unavailabilityType': 'All Day',
                'unavailableFromDate': date(10),
                'unavailableToDate': date(10)
              },
              {
                'unavailabilityType': 'All Day',
                'unavailableFromDate': date(30),
                'unavailableToDate': date(35)
              }
            ],
            'hearingSubChannel': null
          },
          {
            'partyID': 'Q1KOKP2',
            'partyType': 'ORG',
            'partyName': 'Civil - Organisation 1',
            'partyRole': 'LGRP',
            'organisationDetails': {
              'name': 'Civil - Organisation 1',
              'organisationType': 'ORG',
              'cftOrganisationID': 'Q1KOKP2'
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Doe',
            'partyRole': 'EXPR',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Doe',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'test@email.com'
              ],
              'hearingChannelPhone': [
                '07000111000'
              ],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Smith',
            'partyRole': 'WITN',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Smith',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'johnsmith@email.com'
              ],
              'hearingChannelPhone': [
                '07012345678'
              ],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'Bob the litigant friend',
            'partyRole': 'LIFR',
            'individualDetails': {
              'title': null,
              'firstName': 'Bob',
              'lastName': 'the litigant friend',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'bobthelitigant@litigants.com'
              ],
              'hearingChannelPhone': [
                '07123456789'
              ],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'Sir John Doe',
            'partyRole': 'DEFE',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Doe',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [],
              'hearingChannelPhone': [],
              'relatedParties': [],
              'custodyStatus': 'C'
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': [
              {
                'unavailabilityType': 'All Day',
                'unavailableFromDate': date(10),
                'unavailableToDate': date(10)
              },
              {
                'unavailabilityType': 'All Day',
                'unavailableFromDate': date(30),
                'unavailableToDate': date(35)
              }
            ],
            'hearingSubChannel': null
          },
          {
            'partyID': '79ZRSOU',
            'partyType': 'ORG',
            'partyName': 'Civil - Organisation 2',
            'partyRole': 'LGRP',
            'organisationDetails': {
              'name': 'Civil - Organisation 2',
              'organisationType': 'ORG',
              'cftOrganisationID': '79ZRSOU'
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Doe',
            'partyRole': 'EXPR',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Doe',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'john@doemail.com'
              ],
              'hearingChannelPhone': [
                '07111111111'
              ],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Smith',
            'partyRole': 'WITN',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Smith',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'johnsmith@email.com'
              ],
              'hearingChannelPhone': [
                '07012345678'
              ],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'Dr Foo Bar',
            'partyRole': 'DEFE',
            'individualDetails': {
              'title': null,
              'firstName': 'Foo',
              'lastName': 'Bar',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [],
              'hearingChannelPhone': [],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': [
              {
                'unavailabilityType': 'All Day',
                'unavailableFromDate': date(10),
                'unavailableToDate': date(10)
              },
              {
                'unavailabilityType': 'All Day',
                'unavailableFromDate': date(30),
                'unavailableToDate': date(35)
              }
            ], 'hearingSubChannel': null
          },
          {
            'partyID': 'H2156A0',
            'partyType': 'ORG',
            'partyName': 'Civil - Organisation 3',
            'partyRole': 'LGRP',
            'organisationDetails': {
              'name': 'Civil - Organisation 3',
              'organisationType': 'ORG',
              'cftOrganisationID': 'H2156A0'
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Doe',
            'partyRole': 'EXPR',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Doe',
              'otherReasonableAdjustmentDetails': null,
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [],
              'vulnerableFlag': false,
              'vulnerabilityDetails': null,
              'hearingChannelEmail': [
                'john@doemail.com'
              ],
              'hearingChannelPhone': [
                '07111111111'
              ],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          },
          {
            'partyID': '',
            'partyType': 'IND',
            'partyName': 'John Smith',
            'partyRole': 'WITN',
            'individualDetails': {
              'title': null,
              'firstName': 'John',
              'lastName': 'Smith',
              'otherReasonableAdjustmentDetails': 'RA0026: Support worker or carer with me: support worker comment',
              'preferredHearingChannel': null,
              'interpreterLanguage': null,
              'reasonableAdjustments': [
                'RA0026'
              ],
              'vulnerableFlag': true,
              'vulnerabilityDetails': 'Support worker or carer with me - support worker comment',
              'hearingChannelEmail': [
                'johnsmith@email.com'
              ],
              'hearingChannelPhone': [
                '07012345678'
              ],
              'relatedParties': [],
              'custodyStatus': null
            },
            'unavailabilityDOW': null,
            'unavailabilityRanges': null,
            'hearingSubChannel': null
          }
        ],
        'screenFlow': [
          {
            'screenName': 'hearing-requirements',
            'navigation': [
              {
                'resultValue': 'hearing-facilities'
              }
            ]
          },
          {
            'screenName': 'hearing-facilities',
            'navigation': [
              {
                'resultValue': 'hearing-stage'
              }
            ]
          },
          {
            'screenName': 'hearing-stage',
            'navigation': [
              {
                'resultValue': 'hearing-attendance'
              }
            ]
          },
          {
            'screenName': 'hearing-attendance',
            'navigation': [
              {
                'resultValue': 'hearing-venue'
              }
            ]
          },
          {
            'screenName': 'hearing-venue',
            'conditionKey': 'regionId',
            'navigation': [
              {
                'conditionOperator': 'INCLUDE',
                'conditionValue': '7',
                'resultValue': 'hearing-welsh'
              },
              {
                'conditionOperator': 'NOT INCLUDE',
                'conditionValue': '7',
                'resultValue': 'hearing-judge'
              }
            ]
          },
          {
            'screenName': 'hearing-welsh',
            'navigation': [
              {
                'resultValue': 'hearing-judge'
              }
            ]
          },
          {
            'screenName': 'hearing-judge',
            'navigation': [
              {
                'resultValue': 'hearing-timing'
              }
            ]
          },
          {
            'screenName': 'hearing-timing',
            'navigation': [
              {
                'resultValue': 'hearing-additional-instructions'
              }
            ]
          },
          {
            'screenName': 'hearing-additional-instructions',
            'navigation': [
              {
                'resultValue': 'hearing-create-edit-summary'
              }
            ]
          }
        ],
        'vocabulary': [
          {}
        ],
        'hearingChannels': null,
        'caseFlags': {
          'flags': [
            {
              'partyName': 'Sir John Doe',
              'flagId': 'PF0019',
              'flagDescription': 'Detained individual',
              'dateTimeCreated': '',
              'flagStatus': 'Active',
              'partyID': ''
            },
            {
              'partyName': 'Sir John Doe',
              'flagId': 'PF0007',
              'flagDescription': 'Unacceptable/disruptive customer behaviour',
              'dateTimeCreated': '',
              'flagStatus': 'Active',
              'partyID': ''

            },
            {
              'partyName': 'John Smith',
              'flagId': 'RA0026',
              'flagDescription': 'Support worker or carer with me',
              'dateTimeCreated': '',
              'flagStatus': 'Active',
              'partyID': ''
            }
          ]
        },
        'caserestrictedFlag': false
      };
    }
  }
;

const createHearing = async (caseId, hearingType, serviceCode) => {
  const hearingId = createHearingId();
  const hearing = listedHearing(caseId, hearingId, hearingType, serviceCode);
  await createUpdateStub(hearingStubRequestBody(hearing, hearingId));
  console.log(`Created new hearing mock: [${hearingId} - ${hearingType}]`);
  return hearingId;
};

const triggerHearingNoticeScheduler = async (expectedHearingId, definitionKey) => {
  //Update unnotified hearings stub
  await createUpdateStub(unnotifiedHearingStubRequestBody([expectedHearingId]));

  const process = await triggerCamundaProcess(definitionKey);
  console.log(`Started hearing notice scheduler process: ${getUILink(process)}`);

  // Wait for the hearing notice scheduler process
  await waitForCompletedCamundaProcess(null, process.id, null);

  // Wait for hearing notice process
  await waitForCompletedCamundaProcess(AUTOMATED_HEARING_NOTICE, null, `hearingId_eq_${expectedHearingId}`);
};

module.exports = {
  createCaseFlags: async (user, caseId, flagLocation, flag) => {
    if (!(await checkCaseFlagsEnabled())) {
      return;
    }

    await apiRequest.setupTokens(user);

    await addAndAssertCaseFlag(flagLocation, flag, caseId);
  },

  generateHearingsPayload: async (user, caseId, serviceId = 'AAA7') => {
    if (!(await checkCaseFlagsAndHmcEnabled())) {
      return;
    }

    await apiRequest.setupTokens(user);

    const payload = await getHearingsPayload(user, caseId);

    let {caseDeepLink, ...actualPayload} = payload;

    // remove uniquely generated partyID for all parties except legal rep
    actualPayload.parties = actualPayload.parties.map(function (party) {
      if (party.partyRole !== 'LGRP') {
        return {...party, partyID: ''};
      } else {
        return {...party};
      }
    });
    actualPayload.caseFlags.flags = actualPayload.caseFlags.flags.map(function (flag) {
      if (flag.partyRole !== 'LGRP') {
        return {...flag, partyID: '', dateTimeCreated: ''};
      } else {
        return {...flag};
      }
    });
    const expectedPayload = getExpectedPayload(serviceId);

    expect(actualPayload).deep.equal(expectedPayload);
    expect(caseDeepLink).deep.contain(`/cases/case-details/${caseId}`);
  },
  setupStaticMocks: async () => {
    await createUpdateStub(getpartiesNotifiedStubRequestBody());
    await createUpdateStub(putPartiesNotifiedStubRequestBody());
  },
  createUnspecTrialHearing: async (caseId) => createHearing(caseId, 'TRI', 'AAA7'),
  createUnspecDisposalHearing: async (caseId) => createHearing(caseId, 'DIS', 'AAA7'),
  createUnspecDisputeResolutionHearing: async (caseId) => createHearing(caseId, 'DRH', 'AAA7'),
  createSpecTrialHearing: async (caseId) => createHearing(caseId, 'TRI', 'AAA6'),
  createSpecDisposalHearing: async (caseId) => createHearing(caseId, 'DIS', 'AAA6'),
  createSpecDisputeResolutionHearing: async (caseId) => createHearing(caseId, 'DRH', 'AAA6'),
  triggerUnspecAutomatedHearingNoticeScheduler: async (expectedHearingId) => {
    return await triggerHearingNoticeScheduler(expectedHearingId, UNSPEC_AUTOMATED_HEARING_NOTICE_SCHEDULER);
  },
  triggerSpecAutomatedHearingNoticeScheduler: async (expectedHearingId) => {
    return await triggerHearingNoticeScheduler(expectedHearingId, SPEC_AUTOMATED_HEARING_NOTICE_SCHEDULER);
  }
};

