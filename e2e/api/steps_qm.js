const apiRequest = require('./apiRequest');
const {waitForFinishedBusinessProcess} = require('./testingSupport');
const config = require('../config');
const {validateTaskInfo} = require('../helpers/assertions/waTaskAssertions');
const {fetchTasks} = require('./apiRequest');
const {
    followUpQueryMessage,
    queryResponseMessage,
    initialQueryMessage
} = require('../fixtures/queryMessages');
const chai = require('chai');
const {expect} = chai;
const isTestEnv = ['preview', 'demo'].includes(config.runningEnv);
const RAISE_QUERY_EVENT = 'queryManagementRaiseQuery';
const RESPOND_QUERY_EVENT = 'queryManagementRespondQuery';

const assertQueryMessage = (actualQueryMessage, expectedQueryMessage) => {
    expect(actualQueryMessage.id).equal(expectedQueryMessage.id);
    expect(new Date(actualQueryMessage.createdOn).toISOString()).equal(new Date(expectedQueryMessage.createdOn).toISOString());
    expect(actualQueryMessage.createdBy).equal(expectedQueryMessage.createdBy);
    expect(actualQueryMessage.body).equal(expectedQueryMessage.body);
    expect(actualQueryMessage.isHearingRelated).equal(expectedQueryMessage.isHearingRelated);
    expect(actualQueryMessage.hearingDate).equal(expectedQueryMessage.hearingDate);
};

const createQueryPayload = (caseData, queryType, newMessage) => {
    const queryPayload = {
        [queryType.collectionField]: caseData[queryType.collectionField] ? caseData[queryType.collectionField] : {
            partyName: queryType.partyName,
            caseMessages: []
        }
    };
    queryPayload[queryType.collectionField].caseMessages.push(newMessage);
    return queryPayload;
};

const triggerCaseworkerQueryEvent = async (caseId, event, queryType, newMessage) => {
    const updatedCaseData = await triggerCaseworkerEvent(caseId, event,
        (caseData) => createQueryPayload(caseData, queryType, newMessage));

    const actualQueryCollection = updatedCaseData[queryType.collectionField];
    const latestQueryMessage = actualQueryCollection.caseMessages[actualQueryCollection.caseMessages.length - 1].value;

    expect(actualQueryCollection.partyName).equal(queryType.partyName);
    assertQueryMessage(latestQueryMessage, newMessage.value);

    await waitForFinishedBusinessProcess(caseId);
    return latestQueryMessage;
};

const triggerCitizenQueryEvent = async (caseId, event, queryType, newMessage) => {
    const updatedCaseData = await triggerCitizenEvent(caseId, event,
        (caseData) => createQueryPayload(caseData, queryType, newMessage));

    const actualQueryCollection = updatedCaseData[queryType.collectionField];
    const latestQueryMessage = actualQueryCollection.caseMessages[actualQueryCollection.caseMessages.length - 1].value;

    expect(actualQueryCollection.partyName).equal(queryType.partyName);
    assertQueryMessage(latestQueryMessage, newMessage.value);

    await waitForFinishedBusinessProcess(caseId);
    return latestQueryMessage;
};

const triggerCaseworkerEvent = async (caseId, event, queryPayloadCallback) => {
    const preEventData = await apiRequest.startEvent(event, caseId);
    const payload = queryPayloadCallback(preEventData);
    const response = await apiRequest.submitEvent(event, payload, caseId);
    return (await response.json()).case_data;
};

const triggerCitizenEvent = async (caseId, event, queryPayloadCallback) => {
    const caseData = (await apiRequest.fetchCaseDetailsAsSystemUser(caseId)).case_data;
    const payload = queryPayloadCallback(caseData);
    return apiRequest.startEventForCitizen(event, caseId, {event, caseDataUpdate: payload});
};

const getWaTaskForQuery = async (user, caseId, queryId) => {
    return (await fetchTasks(user, caseId, tasks=> {
        console.log(`Attempting to retrieve wa task for query id: [${queryId}]`);
        return tasks.filter(task => task.description.includes(queryId));
    }))[0];
};

const completeQueryResponseTask = async (user, caseId, queryId) => {
  if (config.runWAApiTest) {
    const task = await getWaTaskForQuery(user, caseId, queryId);
    await apiRequest.taskActionByUser(user, task.id, 'complete');
  };
};

const findPartyNameForQueryFromUserConfig = async (user) => {
  const partyType = user.type;
  if (partyType.includes('applicant') || partyType.includes('claimant')) {
    return 'Claimant';
  } else if (partyType.includes('defendant')) {
    return 'Defendant';
  }
  return 'All queries';
};

module.exports = {
    raiseLRQuery: async (caseId, user, queryType, isHearingRelated= true) => {
        console.log(`Raising a query as: ${user.email}`);
        await apiRequest.setupTokens(user);
        const partyName = isTestEnv ? await findPartyNameForQueryFromUserConfig(user) : queryType.partyName;
        const newMessage = (await initialQueryMessage(partyName, apiRequest.getTokens().userId, isHearingRelated));
        return triggerCaseworkerQueryEvent(caseId, RAISE_QUERY_EVENT, queryType, newMessage);
    },
    respondToQuery: async (caseId, user, initialQueryMessage, queryType) => {
        console.log(`Responding to query as: ${user.email}`);
        await apiRequest.setupTokens(user);
        const newMessage = await queryResponseMessage(initialQueryMessage, apiRequest.getTokens().userId);
        await triggerCaseworkerQueryEvent(caseId, RESPOND_QUERY_EVENT, queryType, newMessage);
        await completeQueryResponseTask(user, caseId, initialQueryMessage.id);
    },
    followUpOnLRQuery: async (caseId, user, initialQueryMessage, queryType) => {
        console.log(`Following up on query as: ${user.email}`);
        await apiRequest.setupTokens(user);
        const newMessage = await followUpQueryMessage(initialQueryMessage, apiRequest.getTokens().userId);
        return triggerCaseworkerQueryEvent(caseId, RAISE_QUERY_EVENT, queryType, newMessage);
    },
    raiseLipQuery: async (caseId, user, queryType, isHearingRelated=true) => {
        console.log(`Raising a query as: ${user.email}`);
        await apiRequest.setupTokens(user);
        const partyName = isTestEnv ? await findPartyNameForQueryFromUserConfig(user) : queryType.partyName;
        const newMessage = await initialQueryMessage(partyName, apiRequest.getTokens().userId, isHearingRelated);
        const submittedMessage = await triggerCitizenQueryEvent(caseId, RAISE_QUERY_EVENT, queryType, newMessage);
        return submittedMessage;
    },
    followUpOnLipQuery: async (caseId, user, initialQueryMessage, queryType) => {
        console.log(`Following up on query as: ${user.email}`);
        await apiRequest.setupTokens(user);
        const newMessage = await followUpQueryMessage(initialQueryMessage, apiRequest.getTokens().userId);
        return await triggerCitizenQueryEvent(caseId, RAISE_QUERY_EVENT, queryType, newMessage);
    },
    validateQmResponseTask: async (caseId, user, expectedTask, queryId) => {
        if (config.runWAApiTest) {
            const task = await getWaTaskForQuery(user, caseId, queryId);
            validateTaskInfo(task, expectedTask);
        } else {
            console.log('WA API tests are not enabled - skipping WA test');
        };
    }
};
