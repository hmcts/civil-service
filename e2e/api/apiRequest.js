const config = require('../config.js');

const idamHelper = require('./idamHelper');
const restHelper = require('./restHelper.js');
const {retry} = require('./retryHelper');
const {TOTP} = require('totp-generator');

const TASK_MAX_RETRIES = 30;
const TASK_RETRY_TIMEOUT_MS = 5000;

const tokens = {};
const getCcdDataStoreBaseUrl = () => `${config.url.ccdDataStore}/caseworkers/${tokens.userId}/jurisdictions/${config.definition.jurisdiction}/case-types/${config.definition.caseType}`;
const getCcdDataStoreGABaseUrl = () => `${config.url.ccdDataStore}/caseworkers/${tokens.userId}/jurisdictions/${config.definition.jurisdiction}/case-types/${config.definition.caseTypeGA}`;
const getCcdCaseUrl = (userId, caseId) => `${config.url.ccdDataStore}/aggregated/caseworkers/${userId}/jurisdictions/${config.definition.jurisdiction}/case-types/${config.definition.caseType}/cases/${caseId}`;
const getCaseDetailsUrl = (userId, caseId) => `${config.url.ccdDataStore}/caseworkers/${userId}/jurisdictions/${config.definition.jurisdiction}/case-types/${config.definition.caseType}/cases/${caseId}`;
const getCivilServiceUrl = () => `${config.url.civilService}`;
const getPaymentCallbackUrl = () => `${config.url.civilService}/service-request-update`;
const getJudgeRevisitTaskHandlerUrl = (state, genAppType) => `${config.url.civilService}/testing-support/trigger-judge-revisit-process-event/${state}/${genAppType}`;
const getCaseDismissalTaskHandlerUrl = () => `${config.url.civilService}/testing-support/trigger-case-dismissal-scheduler`;
const getGaCaseDataUrl = (caseId) => `${config.url.civilService}/testing-support/case/${caseId}`;
const getMainCivilServiceCaseDataUrl = () => `${config.url.civilService}/testing-support/case/`;
const getCivilServiceCaseDataUrl = () => `${config.url.civilService}/testing-support/case/`;
const getHearingFeePaidUrl = (caseId) => `${config.url.civilService}/testing-support/${caseId}/trigger-hearing-fee-paid`;
const getHearingFeeUnpaidUrl = (caseId) => `${config.url.civilService}/testing-support/${caseId}/trigger-hearing-fee-unpaid`;
const getBundleTriggerUrl = (caseId) => `${config.url.civilService}/testing-support/${caseId}/trigger-trial-bundle`;
const getBulkClaimServiceUrl = () => `${config.url.orchestratorService}/createSDTClaim`;
const getPaymentAPIBaseUrl = () => `${config.url.paymentApi}`;
const getGeneralApplicationBaseUrl = () => `${config.url.civilService}/testing-support/case/`;

const getRequestHeaders = (userAuth) => {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${userAuth}`,
    'ServiceAuthorization': tokens.s2sAuth
  };
};
const getRequestHeadersPayment = () => {
  return {
    'Content-Type': 'application/json',
    'ServiceAuthorization': tokens.s2sAuth
  };
};
const getCivilServiceCaseworkerSubmitNewClaimUrl = () => `${config.url.civilService}/cases/caseworkers/create-case/${tokens.userId}`;

const fetchCaseDetails = async (user, caseId, response = 200) => {
  let eventUserAuth = await idamHelper.accessToken(user);
  let eventUserId = await idamHelper.userId(eventUserAuth);
  let url = getCaseDetailsUrl(eventUserId, caseId);

  return await restHelper.retriedRequest(url, getRequestHeaders(eventUserAuth), null, 'GET', response)
      .then(response => response.json());
};

const fetchSentNotifications = async (user, caseId, templateId, recipientEmail, response = 200) => {
  let eventUserAuth = await idamHelper.accessToken(user);
  const params = new URLSearchParams();
  if (caseId) params.append('caseId', caseId);
  if (templateId) params.append('templateId', templateId);
  if (recipientEmail) params.append('recipientEmail', recipientEmail);
  const url = `${config.url.civilService}/testing-support/notifications/sent?${params.toString()}`;
  return await restHelper.retriedRequest(url, {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${eventUserAuth}`
  }, null, 'GET', response)
    .then(r => r.json());
};

const triggerScheduler = async (user, schedulerName, response = 202) => {
  let eventUserAuth = await idamHelper.accessToken(user);
  const url = `${config.url.civilService}/testing-support/run-scheduler/${schedulerName}`;
  return await restHelper.retriedRequest(url, {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${eventUserAuth}`
  }, null, 'GET', response)
    .then(r => r.text());
};

const listSchedulers = async (user, response = 200) => {
  let eventUserAuth = await idamHelper.accessToken(user);
  const url = `${config.url.civilService}/testing-support/schedulers`;
  return await restHelper.retriedRequest(url, {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${eventUserAuth}`
  }, null, 'GET', response)
    .then(r => r.json());
};

const fetchWaTasks = async (user, caseNumber, expectedStatus = 200) => {
  const userToken = await idamHelper.accessToken(user);
  const s2sToken = await restHelper.retriedRequest(
      `${config.url.authProviderApi}/testing-support/lease`,
      {'Content-Type': 'application/json'},
      {
        microservice: config.s2sForXUI.microservice,
        oneTimePassword: TOTP.generate(config.s2sForXUI.secret)
      })
      .then(response => response.text());

    const inputData = {
        'search_parameters': [
            {'key': 'caseId', 'operator': 'IN', 'values': [caseNumber]},
            {'key': 'jurisdiction', 'operator': 'IN', 'values': ['CIVIL']},
            {'key': 'state', 'operator': 'IN', 'values': ['assigned', 'unassigned']}
        ],
        'sorting_parameters': [{'sort_by': 'dueDate', 'sort_order': 'asc'}]
    };

  return restHelper.request(`${config.url.waTaskMgmtApi}/task`,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userToken}`,
        'ServiceAuthorization': `Bearer ${s2sToken}`
      }, inputData, 'POST', expectedStatus)
      .then(async response => {
        if (response.status !== expectedStatus) {
          throw new Error(`There was an issue retrieving wa tasks. Expected status: ${expectedStatus}, actual status: ${response.status}, `
            + `message: ${response.statusText}, url: ${response.url}`);
        }
        return await response.json();
      });
};

module.exports = {
  setupTokens: async (user) => {
    tokens.userAuth = await idamHelper.accessToken(user);
    tokens.userId = await idamHelper.userId(tokens.userAuth);
    tokens.s2sAuth = await restHelper.retriedRequest(
      `${config.url.authProviderApi}/lease`,
      {'Content-Type': 'application/json'},
      {
        microservice: config.s2s.microservice,
        oneTimePassword: TOTP.generate(config.s2s.secret).otp
      })
      .then(response => response.text());
  },

  getTokens: () => tokens,

  getUserFullName: async (user) => {
    let authToken = await idamHelper.accessToken(user);
    return await idamHelper.getGivenName(authToken) + ' ' + await idamHelper.getFamilyName(authToken);
  },

  fetchCaseDetails,
  fetchSentNotifications,
  triggerScheduler,
  listSchedulers,
  fetchCaseDetailsAsSystemUser: async (caseId) => {
    const { userAuth, userId } = tokens;
    const details = await fetchCaseDetails(config.systemUpdate2, caseId, 200);
    // Reset auth and id back to original user.
    tokens.userAuth = userAuth;
    tokens.userId = userId;
    return details;
  },
  fetchCaseForDisplay: async (user, caseId, response = 200) => {
    let eventUserAuth = await idamHelper.accessToken(user);
    let eventUserId = await idamHelper.userId(eventUserAuth);
    let url = getCcdCaseUrl(eventUserId, caseId);

    return await restHelper.retriedRequest(url, getRequestHeaders(eventUserAuth), null, 'GET', response)
      .then(response => response.json());
  },

  paymentApiRequestUpdateServiceCallback: async (serviceRequestUpdateDto) => {
    let url = getPaymentCallbackUrl();
    let response = await restHelper.retriedRequest(url, getRequestHeadersPayment(),
      serviceRequestUpdateDto, 'PUT');
    return response || {};
  },

  gaOrderMadeSchedulerTaskHandler: async (state, genAppType) => {
    const authToken = await idamHelper.accessToken(config.systemupdate);
    let url = getJudgeRevisitTaskHandlerUrl(state, genAppType);
    let response_msg =  await restHelper.retriedRequest(url, {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null,
      'GET');
    return response_msg || {};
  },

  civilCaseDismissalHandler: async() => {
    const authToken = await idamHelper.accessToken(config.systemupdate);
    let url = getCaseDismissalTaskHandlerUrl();
    let response_msg =  await restHelper.retriedRequest(url, {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      }, null,
      'GET');
    return response_msg || {};
  },

  fetchGaCaseData: async (caseId) => {
    const authToken = await idamHelper.accessToken(config.systemupdate);
    let url = getGaCaseDataUrl(caseId);
    console.log('*** GA Case Reference: '  + caseId + ' ***');

    return await restHelper.retriedRequest(url,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null, 'GET');
  },

  startEvent: async (eventName, caseId) => {
    let url = getCcdDataStoreBaseUrl();
    if (caseId) {
      url += `/cases/${caseId}`;
    }
    url += `/event-triggers/${eventName}/token`;

    let response = await restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth), null, 'GET')
      .then(response => response.json());
    tokens.ccdEvent = response.token;
    return response.case_details.case_data || {};
  },

  startGAEvent: async (eventName, caseId) => {
    let url = getCcdDataStoreGABaseUrl();
    if (caseId) {
      url += `/cases/${caseId}`;
    }
    url += `/event-triggers/${eventName}/token`;

    let response = await restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth), null, 'GET')
      .then(response => response.json());
    tokens.ccdEvent = response.token;

    return response.case_details.case_data || {};
  },

  startEventForCallbackError: async (eventName, caseId) => {
    let url = getCcdDataStoreBaseUrl();
    if (caseId) {
      url += `/cases/${caseId}`;
    }
    url += `/event-triggers/${eventName}/token`;

    let response = await restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth), null, 'GET', 422)
      .then(response => response.json());
    tokens.ccdEvent = response.token;
    return response.callbackErrors[0];
  },

  fetchUserId: async () => {
    return await idamHelper.userId(tokens.userAuth);
  },

  startCreateCaseForCitizen: async (payload, caseId = 'draft') => {
    let url = getCivilServiceUrl();
    const userId = await idamHelper.userId(tokens.userAuth);
    url += `/cases/${caseId}/citizen/${userId}/event`;

    let response = await restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth), payload, 'POST',200)
      .then(response => response.json());
    tokens.ccdEvent = response.token;
    return response.id;
  },

  startEventForCitizen: async (eventName, caseId, payload) => {
    let url = getCivilServiceUrl();
    const userId = await idamHelper.userId(tokens.userAuth);
    if (caseId) {
      url += `/cases/${caseId}`;
    }
    url += `/citizen/${userId}/event`;

    let response = await restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth), payload, 'POST', 200)
      .then(response => response.json());
    tokens.ccdEvent = response.token;
    return response.case_data;
  },

  startEventNotAllowed: async (eventName, caseId) => {
    let url = getCcdDataStoreBaseUrl();
    if (caseId) {
      url += `/cases/${caseId}`;
    }
    url += `/event-triggers/${eventName}/token`;

    let response = await restHelper.request(url, getRequestHeaders(tokens.userAuth), null, 'GET');
    //.then(response => response.json());
    tokens.ccdEvent = response.token;
    return response;
  },
  validatePage: async (eventName, pageId, caseData, caseId, expectedStatus = 200) => {
    return restHelper.retriedJsonRequest(`${getCcdDataStoreBaseUrl()}/validate?pageId=${eventName}${pageId}`, getRequestHeaders(tokens.userAuth),
      {
        case_reference: caseId,
        data: caseData,
        event: {id: eventName},
        event_data: caseData,
        event_token: tokens.ccdEvent
      }, 'POST', expectedStatus);
  },

  validateGAPage: async (eventName, pageId, caseData, caseId) => {
    return restHelper.request(`${getCcdDataStoreGABaseUrl()}/validate?pageId=${eventName}${pageId}`, getRequestHeaders(tokens.userAuth),
      {
        case_reference: caseId,
        data: caseData,
        event: {id: eventName},
        event_data: caseData,
        event_token: tokens.ccdEvent
      }, 'POST');
  },

  submitEvent: async (eventName, caseData, caseId) => {
    let url = `${getCcdDataStoreBaseUrl()}/cases`;
    if (caseId) {
      url += `/${caseId}/events`;
    }
    return restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth),
      {
        data: caseData,
        event: {id: eventName},
        event_data: caseData,
        event_token: tokens.ccdEvent
      }, 'POST', 201);
  },

  submitGAEvent: async (eventName, caseData, caseId) => {
    let url = `${getCcdDataStoreGABaseUrl()}/cases`;
    if (caseId) {
      url += `/${caseId}/events`;
    }

    return restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth),
      {
        data: caseData,
        event: {id: eventName},
        event_data: caseData,
        event_token: tokens.ccdEvent
      }, 'POST', 201);
  },

  submitNewClaimAsCaseworker: async (eventName, caseData) => {
    let url = getCivilServiceCaseworkerSubmitNewClaimUrl();

    return restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth),
      {
        data: caseData,
        event: eventName,
      }, 'POST', 201);
  },

  fetchUpdatedCivilCaseData: async (caseId, user) => {
    const authToken = await idamHelper.accessToken(user);
    let url = getCivilServiceCaseDataUrl();
    console.log('*** Civil Case Reference: '  + caseId + ' ***');
    if (caseId) {
      url += `${caseId}`;
    }
    return await restHelper.retriedRequest(url,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null, 'GET');
  },

  fetchMainCivilCaseData: async (caseId, user) => {
    const authToken = await idamHelper.accessToken(user);
    let url = getMainCivilServiceCaseDataUrl();
    console.log('*** Civil Case Reference: '  + caseId + ' ***');
    if (caseId) {
      url += `${caseId}`;
    }
    return await restHelper.retriedRequest(url,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null, 'GET');
  },

  fetchUpdatedCaseData: async (caseId, user) => {
    const authToken = await idamHelper.accessToken(user);
    let url = getGeneralApplicationBaseUrl();
    console.log('*** Civil Case Reference: '  + caseId + ' ***');
    if (caseId) {
      url += `${caseId}`;
    }

    return await restHelper.retriedRequest(url,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null, 'GET');
  },

  fetchUpdatedGABusinessProcessData: async (caseId, user) => {
    const authToken = await idamHelper.accessToken(user);
    let url = getGeneralApplicationBaseUrl();
    console.log('*** GA Case Reference: '  + caseId + ' ***');
    if (caseId) {
      url += `${caseId}/business-process`;
    }

    return await restHelper.retriedRequest(url,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null, 'GET');
  },

  taskActionByUser: async function (user, taskId, url, expectedStatus = 204) {
    const userToken = await idamHelper.accessToken(user);
    const s2sToken = await restHelper.retriedRequest(
      `${config.url.authProviderApi}/testing-support/lease`,
      {'Content-Type': 'application/json'},
      {
        microservice: config.s2sForXUI.microservice,
        oneTimePassword: TOTP.generate(config.s2sForXUI.secret)
      })
      .then(response => response.text());

    return retry(() => {
      return restHelper.request(`${config.url.waTaskMgmtApi}/task/${taskId}/${url}`,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userToken}`,
          'ServiceAuthorization': `Bearer ${s2sToken}`
        }, '', 'POST', expectedStatus);
    }, 2, TASK_RETRY_TIMEOUT_MS);
  },

  fetchTaskDetails: async (user, caseNumber, taskId, expectedStatus = 200) => {
    let taskDetails;
    return retry(() => {
      return fetchWaTasks(user, caseNumber, expectedStatus)
        .then(jsonResponse => {
          let availableTaskDetails = jsonResponse['tasks'];
          availableTaskDetails.forEach((taskInfo) => {
            if (taskInfo['type'] == taskId) {
              console.log('Found taskInfo with id ...', taskId);
              console.log('Task details are ...', taskInfo);
              taskDetails = taskInfo;
            }
          });
          if (!taskDetails) {
            throw new Error(`Ongoing task retrieval process for case id: ${caseNumber}`);
          } else {
            return taskDetails;
          }
        });
    }, TASK_MAX_RETRIES, TASK_RETRY_TIMEOUT_MS);
  },

  fetchTasks: async (user, caseNumber, filterCallback, expectedStatus = 200) => {
    return retry(() => {
      return fetchWaTasks(user, caseNumber, expectedStatus)
          .then(jsonResponse => {
            const tasks = filterCallback(jsonResponse['tasks']);
            if (tasks.length > 0) {
              console.log('Tasks ...');
              tasks.forEach(taskInfo => console.log(taskInfo));
              return tasks;
            } else {
              throw new Error(`Ongoing task retrieval process for case id: ${caseNumber}`);
            }
          });
    }, TASK_MAX_RETRIES, TASK_RETRY_TIMEOUT_MS);
  },

  paymentUpdate: async (caseId, endpoint, serviceRequestUpdateDto) => {
    let endpointURL = getCivilServiceUrl() + endpoint;

    let response = await restHelper.retriedRequest(endpointURL, getRequestHeaders(tokens.userAuth),
      serviceRequestUpdateDto, 'PUT');

    return response || {};
  },

  createBulkClaim: async (sdtRequestId, claimData) => {
    let sdtClaimURL = getBulkClaimServiceUrl();

    let response = await restHelper.retriedRequestFor400(sdtClaimURL,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${tokens.userAuth}`,
        'SdtRequestId': `${sdtRequestId}`
      },
      claimData,
      'POST')
      .then(response => response.json());

    return response || {};
  },

  createBulkClaimForStatusCode201: async (sdtRequestId, claimData) => {
    let sdtClaimURL = getBulkClaimServiceUrl();

    let response = await restHelper.retriedRequestFor201(sdtClaimURL,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${tokens.userAuth}`,
        'SdtRequestId': `${sdtRequestId}`
      },
      claimData,
      'POST')
      .then(response => response.json());

    return response || {};
  },

  hearingFeePaidEvent: async (caseId) => {
    const authToken = await idamHelper.accessToken(config.systemupdate);
    let url = getHearingFeePaidUrl(caseId);
    let response_msg = await restHelper.retriedRequest(url, {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null,
      'GET');
    return response_msg || {};
  },

  bundleTriggerEvent: async (caseId) => {
    const authToken = await idamHelper.accessToken(config.systemupdate);
    let url = getBundleTriggerUrl(caseId);
    let response_msg = await restHelper.retriedRequest(url, {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null,
      'GET');
    return response_msg || {};
  },

  hearingFeeUnpaidEvent: async (caseId) => {
    const authToken = await idamHelper.accessToken(config.systemupdate);
    let url = getHearingFeeUnpaidUrl(caseId);
    let response_msg = await restHelper.retriedRequest(url, {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': tokens.s2sAuth
      }, null,
      'GET');
    return response_msg || {};
  },

  fetchCaseState: async (caseId, eventName) => {
    let url = getCcdDataStoreBaseUrl();
    url += `/cases/${caseId}`;

    url += `/event-triggers/${eventName}/token`;

    let response = await restHelper.retriedRequest(url, getRequestHeaders(tokens.userAuth), null, 'GET')
      .then(response => response.json());
    return response.case_details.state || {};
  },

  getHearingsPayload: async (user, caseId) => {
    return restHelper.request(
      `${config.url.civilService}/serviceHearingValues`, getRequestHeaders(tokens.userAuth), {
        caseReference: caseId,
        hearingId: 'HER123123123'
      }, 'POST')
      .then(
        async response =>
          await response.json()
      );
  },

  createAPBAPayment: async function (user, ccdCaseNumber, amount, feeCode, version, volume) {

    const creditAccountPaymentEndPoint = '/credit-account-payments';
    const userToken = await idamHelper.accessToken(user);
    const s2sToken = await restHelper.retriedRequest(
      `${config.url.authProviderApi}/lease`,
      {'Content-Type': 'application/json'},
      {
        microservice: config.s2s.microservice,
        oneTimePassword: TOTP.generate(config.s2s.secret).otp
      })
      .then(response => response.text());
    const accountNumber = 'PBA0088192';

    const saveBody = {
      account_number: `${accountNumber}`,
      amount: amount,
      case_reference: `${ccdCaseNumber}`,
      ccd_case_number: `${ccdCaseNumber}`,
      currency: 'GBP',
      customer_reference: 'string',
      description: 'string',
      fees: [
        {
          calculated_amount: amount,
          code: `${feeCode}`,
          fee_amount: amount,
          version: version,
          volume: volume
        }
      ],
      organisation_name: 'string',
      service: 'CIVIL',
      site_id: 'AAA7'
    };

    return retry(() => {
      return restHelper.retriedRequest(getPaymentAPIBaseUrl() + creditAccountPaymentEndPoint,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userToken}`,
          'ServiceAuthorization': `Bearer ${s2sToken}`
        }, saveBody, 'POST', 201);
    }, 2, 30).then(response => response.text).catch(error => {
      console.log(error);
    });

  },

  rollbackPaymentDate: async function (user, ccdCaseNumber, expectedStatus = 204) {
    const userToken = await idamHelper.accessToken(user);
    const s2sToken = await restHelper.retriedRequest(
      `${config.url.authProviderApi}/lease`,
      {'Content-Type': 'application/json'},
      {
        microservice: config.s2s.microservice,
        oneTimePassword: TOTP.generate(config.s2s.secret).otp
      })
      .then(response => response.text());
    const rollbackPaymentDateByCCDNumberEndPoint = `/payments/ccd_case_reference/${ccdCaseNumber}/lag_time/25`;
    return retry(() => {
      return restHelper.retriedRequest(getPaymentAPIBaseUrl() + rollbackPaymentDateByCCDNumberEndPoint,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userToken}`,
          'ServiceAuthorization': `Bearer ${s2sToken}`
        }, '', 'PATCH', expectedStatus);
    }, 2, 30).then(response => response.status);
  },
};
