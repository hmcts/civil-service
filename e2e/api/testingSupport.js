const config = require('../config.js');
const idamHelper = require('./idamHelper');
const serviceAuthHelper = require('./serviceAuthorisationHelper');
const restHelper = require('./restHelper');
const {retry} = require('./retryHelper');


let incidentMessage;

const MAX_RETRIES = 30;
const RETRY_TIMEOUT_MS = 5000;

const checkFlagEnabled = async (flag) => {
  const authToken = await idamHelper.accessToken(config.applicantSolicitorUser);
  const s2sAuth = await serviceAuthHelper.civilServiceAuth();

  return await restHelper.request(
    `${config.url.civilService}/testing-support/feature-toggle/${flag}`,
    {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
      'ServiceAuthorization': s2sAuth
    }, null, 'GET')
    .then(async response =>  {
        if (response.status === 200) {
          const json = await response.json();
          return json.toggleEnabled;
        } else {
          throw new Error(`Error when checking toggle occurred with status : ${response.status}`);
        }
      }
    );
};

const checkHmcEnabled = async () => {
  return checkFlagEnabled('hmc');
};

const checkCaseFlagsEnabled = async () => {
  return checkFlagEnabled('case-flags');
};

const checkManageContactInformationEnabled = async () => {
  return checkFlagEnabled('update-contact-details');
};

const checkFastTrackUpliftsEnabled = async () => {
  return checkFlagEnabled('fast-track-uplifts');
};

const checkMintiToggleEnabled = async () => {
  return checkFlagEnabled('minti');
};

const checkLRQueryManagementEnabled = async () => {
  return checkFlagEnabled('query-management');
};

const checkLIPQueryManagementEnabled = async () => {
    return checkFlagEnabled('query-management-lips');
};

module.exports =  {
  waitForFinishedBusinessProcess: async caseId => {
    const authToken = await idamHelper.accessToken(config.applicantSolicitorUser);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    await retry(() => {
      return restHelper.request(
        `${config.url.civilService}/testing-support/case/${caseId}/business-process`,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`,
          'ServiceAuthorization': s2sAuth
        }, null, 'GET')
        .then(async response => await response.json()).then(response => {
          let businessProcess = response.businessProcess;
          if (response.incidentMessage) {
            incidentMessage = response.incidentMessage;
          } else if (businessProcess && businessProcess.status !== 'FINISHED') {
            throw new Error(`Ongoing business process: ${businessProcess.camundaEvent}, case id: ${caseId}, status: ${businessProcess.status},`
              + ` process instance: ${businessProcess.processInstanceId}, last finished activity: ${businessProcess.activityId}`);
          }
      });
    }, MAX_RETRIES, RETRY_TIMEOUT_MS);
    if (incidentMessage)
      throw new Error(`Business process failed for case: ${caseId}, incident message: ${incidentMessage}`);
  },
  waitForCompletedCamundaProcess: async (definitionKey, processInstanceId, variables) => {
    const authToken = await idamHelper.accessToken(config.systemUpdate2);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    await retry(() => {
      const params = {definitionKey, processInstanceId, variables};
      const url = new URL(`${config.url.civilService}/testing-support/camunda-processes`);
      Object.keys(params).forEach(key => {
        if (params[key]) {
          url.searchParams.append(key, params[key]);
        }
      });
      return restHelper.request(
        url.toString(),
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`,
          'ServiceAuthorization': s2sAuth
        }, null, 'GET')
        .then(async response => await response.json()).then(response => {
          // console.log(JSON.stringify(response));
          if (response && response.length > 0 && response[0].state === 'COMPLETED') {
            console.log(`${response[0].processDefinitionKey} process has completed!!`);
          } else {
            throw new Error('Waiting for camunda process to complete');
          }
        });
    }, 10, RETRY_TIMEOUT_MS);
  },

  assignCaseToDefendant: async (caseId, caseRole = 'RESPONDENTSOLICITORONE', user = config.defendantSolicitorUser) => {
    const authToken = await idamHelper.accessToken(user);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    await retry(() => {
      return restHelper.request(
        `${config.url.civilService}/testing-support/assign-case/${caseId}/${caseRole}`,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`,
          'ServiceAuthorization': s2sAuth
        },
        {},
        'POST')
        .then(response => {
          if (response.status === 200) {
            console.log( 'Role created successfully');
          } else if (response.status === 409) {
            console.log('Role already exists!');
          } else  {
            console.log('response..', response);
            throw new Error(`Error occurred with status : ${response.status}`);
          }
        });
    });
  },

  assignCaseToLRSpecDefendant: async (caseId, caseRole = 'RESPONDENTSOLICITORONE', user = config.defendantSolicitorUser) => {
    const authToken = await idamHelper.accessToken(user);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    await retry(() => {
      return restHelper.request(
        `${config.url.civilService}/testing-support/assign-case/${caseId}/${caseRole}`,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`,
          'ServiceAuthorization': s2sAuth
        },
        {},
        'POST')
        .then(response => {
          if (response.status === 200) {
            console.log('Role created successfully');
          } else if (response.status === 409) {
            console.log('Role already exists!');
          } else {
            throw new Error(`Error occurred with status : ${response.status}`);
          }
        });
    });
  },

  unAssignUserFromCases: async (caseIds, user) => {
    const authToken = await idamHelper.accessToken(user);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    await retry(() => {
      return restHelper.request(
        `${config.url.civilService}/testing-support/unassign-user`,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`,
          'ServiceAuthorization': s2sAuth
        },
        {
          caseIds
        },
        'POST')
        .then(response => {
          if (response.status === 200) {
            caseIds.forEach(caseId => console.log( `User unassigned from case [${caseId}] successfully`));
          }
          else  {
            console.log(`Error occurred with status : ${response.status}`);
            //throw new Error(`Error occurred with status : ${response.status}`);
          }
        });
    });
  },

  checkToggleEnabled: async (toggle) => {
    const authToken = await idamHelper.accessToken(config.applicantSolicitorUser);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    return await restHelper.request(
        `${config.url.civilService}/testing-support/feature-toggle/${toggle}`,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`,
          'ServiceAuthorization': s2sAuth
        }, null, 'GET')
        .then(async response =>  {
          if (response.status === 200) {
            const json = await response.json();
            return json.toggleEnabled;
          } else {
            throw new Error(`Error when checking toggle occurred with status : ${response.status}`);
          }
          }
        );
  },

  checkPBAv3IsEnabled: async () => {
    const authToken = await idamHelper.accessToken(config.applicantSolicitorUser);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    return await restHelper.request(
      `${config.url.civilService}/testing-support/feature-toggle/pba-version-3-ways-to-pay`,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': s2sAuth
      }, null, 'GET')
      .then(async response =>  {
          if (response.status === 200) {
            const json = await response.json();
            return json.toggleEnabled;
          } else {
            throw new Error(`Error when checking toggle occurred with status : ${response.status}`);
          }
        }
      );
  },

  updateCaseData: async (caseId, caseData, user = config.applicantSolicitorUser) => {
    const authToken = await idamHelper.accessToken(user);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    await restHelper.retriedRequest(
      `${config.url.civilService}/testing-support/case/${caseId}`,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': s2sAuth
      }, caseData, 'PUT');
  },

  uploadDocument: async () => {
    const authToken = await idamHelper.accessToken(config.applicantSolicitorUser);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    let response = await restHelper.request(
      `${config.url.civilService}/testing-support/upload/test-document`,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': s2sAuth
      },
      {},
      'POST');

    return await response.json();
  },
  triggerCamundaProcess: async (processName, variables = {}) => {
    const authToken = await idamHelper.accessToken(config.systemUpdate2);
    const s2sAuth = await serviceAuthHelper.civilServiceAuth();

    let response = await restHelper.request(
      `${config.url.civilService}/testing-support/trigger-camunda-process`,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': s2sAuth
      },
      {
        name: processName,
        variables
      },
      'POST');

    return await response.json();
  },
  checkCaseFlagsAndHmcEnabled: async () => {
    const caseFlagsEnabled = await checkCaseFlagsEnabled();
    console.log(`caseFlagsEnabled: ${caseFlagsEnabled}`);
    const hmcEnabled = await checkHmcEnabled();
    console.log(`hmcEnabled: ${hmcEnabled}`);

    return caseFlagsEnabled && hmcEnabled;
  },
  checkHmcEnabled,
  checkCaseFlagsEnabled,
  checkFastTrackUpliftsEnabled,
  checkManageContactInformationEnabled,
  checkMintiToggleEnabled,
  checkLRQueryManagementEnabled,
  checkLIPQueryManagementEnabled
};
