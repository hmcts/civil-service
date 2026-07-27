const config = require('../config.js');
const {updateActiveOrganisationUsersMocks} = require('../helpers/activeOrganisationUsers');
const {getNocQuestions, validateNocAnswers, submitNocRequest} = require('./caseAssignment');
const {waitForFinishedBusinessProcess} = require('../api/testingSupport');
const {fetchCaseDetails} = require('./apiRequest');
const chai = require('chai');
const {assert} = chai;



const requestNoticeOfChange = async (caseId, newSolicitor, orgPolicyTag, answers) => {
  if (config.localNoCTests === true) {
    await updateActiveOrganisationUsersMocks(newSolicitor);
  }

  console.log('Validating noc questions');
  const getNotQuestionsResponse = await getNocQuestions(caseId, newSolicitor);
  assert.equal(getNotQuestionsResponse.status, 200,
    'Expected getNotQuestionsResponse api call to return successful status');

  console.log('Validating noc answers');
  const validateAnswersResponse = await validateNocAnswers(caseId, answers, newSolicitor);
  assert.equal(validateAnswersResponse.status, 200,
    'Expected validateNocAnswers api call to return successful status');

  console.log(`Submitting notice of change request for case [${caseId}]`);
  const submitNocRequestResponse = await submitNocRequest(caseId, answers, newSolicitor);
  assert.equal(submitNocRequestResponse.status, 201,
    'Expected submitNocRequestResponse api call to return created status');

  await waitForFinishedBusinessProcess(caseId);

  const caseData = await fetchCaseDetails(config.adminUser, caseId, 200);
  const actualOrgId = caseData.case_data[`${orgPolicyTag}`].Organisation.OrganisationID;

  console.log(`Checking case data [${orgPolicyTag}] for new solicitors organisation Id [${newSolicitor.orgId}].`);
  assert.equal(actualOrgId, newSolicitor.orgId, 'Should have the new solicitors organisation id.');
};

const buildNocAnswers = (clientName) => ([
  {question_id: 'clientName', value: `${clientName}`}
]);

module.exports = {
  requestNoticeOfChangeForRespondent1Solicitor: async (caseId, newSolicitor) => {
    await requestNoticeOfChange(caseId, newSolicitor, 'respondent1OrganisationPolicy',
      buildNocAnswers('Sir John Doe'));
  },
  requestNoticeOfChangeForRespondent2Solicitor: async (caseId, newSolicitor) => {
    await requestNoticeOfChange(caseId, newSolicitor, 'respondent2OrganisationPolicy',
      buildNocAnswers('Dr Foo bar')
    );
  },
  requestNoticeOfChangeForApplicant1Solicitor: async (caseId, newSolicitor) => {
    await requestNoticeOfChange(caseId, newSolicitor, 'applicant1OrganisationPolicy',
        buildNocAnswers('Test Inc')
    );
  },
  requestNoticeOfChangeForApplicant2Solicitor: async (caseId, newSolicitor) => {
    await requestNoticeOfChange(caseId, newSolicitor, 'applicant1OrganisationPolicy',
      buildNocAnswers('Dr Jane Doe')
    );
  },
  requestNoticeOfChangeForRespondent2SolicitorSpec: async (caseId, newSolicitor) => {
    await requestNoticeOfChange(caseId, newSolicitor, 'respondent2OrganisationPolicy',
      buildNocAnswers('Second Defendant')
    );
  },
};
