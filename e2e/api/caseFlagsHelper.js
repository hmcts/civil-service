const apiRequest = require('./apiRequest');
const {element} = require('./dataHelper');
const {
  PARTY_FLAGS, SUPPORT_WORKER_FLAG,
  LANGUAGE_INTERPRETER_FLAG,
  WHEELCHAIR_ACCESS_FLAG, DETAINED_INDIVIDUAL_FLAG,
  DISRUPTIVE_INDIVIDUAL
} = require('../fixtures/caseFlags');
const chai = require('chai');
const {expect} = chai;

const CREATE_FLAGS_EVENT = 'CREATE_CASE_FLAGS';
const MANAGE_FLAGS_EVENT = 'MANAGE_CASE_FLAGS';
const FLAG_LOCATIONS = [
  'applicant1',
  'applicant2',
  'applicant1LitigationFriend',
  'applicant2LitigationFriend',
  'respondent1',
  'respondent2',
  'respondent1LitigationFriend',
  'respondent2LitigationFriend',
  'respondent1Witnesses',
  'respondent1Experts',
  'respondent2Witnesses',
  'respondent2Experts',
  'applicantWitnesses',
  'applicantExperts'
];

const getPartyFlags = () => {
  return Object.keys(PARTY_FLAGS).map(key => PARTY_FLAGS[key]);
};

const getLanguageInterpreterFlag = () => {
  return Object.keys(LANGUAGE_INTERPRETER_FLAG).map(key => LANGUAGE_INTERPRETER_FLAG[key])[0];
};

const getRAWheelchairFlag = () => {
  return Object.keys(WHEELCHAIR_ACCESS_FLAG).map(key => WHEELCHAIR_ACCESS_FLAG[key])[0];
};

const getSupportWorkerFlag = () => {
  return Object.keys(SUPPORT_WORKER_FLAG).map(key => SUPPORT_WORKER_FLAG[key])[0];
};

const getDetainedIndividualFlag = () => {
  return Object.keys(DETAINED_INDIVIDUAL_FLAG).map(key => DETAINED_INDIVIDUAL_FLAG[key])[0];
};

const getDisruptiveIndividualFlag = () => {
  return Object.keys(DISRUPTIVE_INDIVIDUAL).map(key => DISRUPTIVE_INDIVIDUAL[key])[0];
};

const isCaseLevelFlag = (caseFlagLocation) => caseFlagLocation === 'caseFlags';

const getDefinedCaseFlagLocations = async(user, caseId) => {
  const {case_data} = await apiRequest.fetchCaseDetails(user, caseId);
  return FLAG_LOCATIONS.filter(flagLocation => case_data[flagLocation]);
};

const updateCaseDataWithFlag = (caseData, flagLocation, flag) => {
  return isCaseLevelFlag(flagLocation)
    ? {...caseData, caseFlags: {details: [flag]}}
    : {...caseData, [flagLocation]: insertFlags(caseData[flagLocation], [flag])};
};

const insertFlags = (targetField, newFlags) => {
  if (Array.isArray(targetField)) {
    const updated = insertFlags(targetField[0].value, newFlags);
    return [element(updated)];
  } else {
    if (targetField.flags  && targetField.flags.details) {
      return {
        ...targetField, flags: {...targetField.flags, details: targetField.flags.details && targetField.flags.details.length > 0 ? [...targetField.flags.details, ...newFlags] : newFlags}
      };
    } else {
      return {
        ...targetField, flags: {...targetField.flags, details: newFlags}
      };
    }
  }
};

const addCaseFlag = async (flagLocation, flag, caseId) => {
  console.log(`Adding [${flag.value.name}] flag to [${flagLocation}].`);
  const caseData = await apiRequest.startEvent(CREATE_FLAGS_EVENT, caseId);
  const updatedData = updateCaseDataWithFlag(caseData, flagLocation, flag);
  return apiRequest.submitEvent(CREATE_FLAGS_EVENT, updatedData, caseId);
};

const getFlagsField = (caseFlagLocation, caseData) => {
  if (caseFlagLocation === 'caseFlags') {
    return caseData[caseFlagLocation];
  } else {
    return Array.isArray(caseData[caseFlagLocation])
      ? caseData[caseFlagLocation][0].value.flags : caseData[caseFlagLocation].flags;
  }
};

const assertFlagAdded = (caseData, caseFlagLocation, expectedFlag) => {
  console.log(`Asserting [${caseFlagLocation}] has [${expectedFlag.value.name}] flag.`);
  const actual = getFlagsField(caseFlagLocation, caseData);
  expect(actual.details).to.deep.include(expectedFlag);
};
const addAndAssertCaseFlag = async (location, flag, caseId) => {
  const response = await addCaseFlag(location, flag, caseId);
  expect(response.status).equal(201);

  const {case_data} = await response.json();
  assertFlagAdded(case_data, location, flag);
};

const updateAndAssertCaseFlag = async (location, flag, caseId) => {
  const response = await updateCaseFlag(location, flag, caseId);
  expect(response.status).equal(201);

  const {case_data} = await response.json();
  assertFlagUpdated(case_data, location, flag);
};

const assertFlagUpdated = (caseData, caseFlagLocation, expectedFlag) => {
  console.log(`Asserting [${caseFlagLocation}] [${expectedFlag.value.name}] flag has been updated.`);
  const actual = getFlagsField(caseFlagLocation, caseData);
  expect(actual.details[0].value.flagComment).deep.equal('Updated Comment');
  expect(actual.details[0].value.status).deep.equal('Inactive');
};

const updateCaseFlag = async (flagLocation, flag, caseId) => {
  console.log(`Updating [${flag.value.name}] flag at [${flagLocation}].`);
  const caseData = await apiRequest.startEvent(MANAGE_FLAGS_EVENT, caseId);
  const updatedData = updateFlagDetails(caseData, flagLocation);
  return apiRequest.submitEvent(MANAGE_FLAGS_EVENT, updatedData, caseId);
};

const updateFlagDetails = (caseData, flagLocation) => {
  return {...caseData, [flagLocation]: updateFlag(caseData[flagLocation])};
};

const updateFlag = (targetField) => {
  if (Array.isArray(targetField)) {
    const updated = updateFlag(targetField[0].value);
    return [element(updated)];
  } else if (!targetField.flags) {
    return {
      ...targetField,
      details:
        [
          {
            ...targetField.details[0],
            value: {
              ...targetField.details[0].value,
              flagComment: 'Updated Comment',
              status: 'Inactive'
            }
          }
        ]
    };
  } else {
    return {
      ...targetField,
      flags: {
        ...targetField.flags,
        details: [
          {
            ...targetField.flags.details[0],
            value: {
              ...targetField.flags.details[0].value,
              flagComment: 'Updated Comment',
              status: 'Inactive'
            }
          }
        ]
      }
    };
  }
};

module.exports = {
  getPartyFlags,
  getDefinedCaseFlagLocations,
  addAndAssertCaseFlag,
  updateAndAssertCaseFlag,
  getLanguageInterpreterFlag,
  getRAWheelchairFlag,
  getSupportWorkerFlag,
  getDetainedIndividualFlag,
  getDisruptiveIndividualFlag
};
