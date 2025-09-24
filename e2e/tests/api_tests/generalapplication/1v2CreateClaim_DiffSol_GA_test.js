const config = require('../../../config.js');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {waitForFinishedBusinessProcess} = require('../../../api/testingSupport');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
let caseNumber;

Feature('1v2 Different Solicitors General application creation @api-unspec @api-nightly-prod');

Scenario('Make a general application', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.informAgreedExtensionDate(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
    'AWAITING_APPLICANT_INTENTION');

  await api_spec.initiateGeneralApplication(caseNumber, config.applicantSolicitorUser, 'JUDICIAL_REFERRAL');
});

AfterSuite(async  () => {
  await unAssignAllUsers();
});
