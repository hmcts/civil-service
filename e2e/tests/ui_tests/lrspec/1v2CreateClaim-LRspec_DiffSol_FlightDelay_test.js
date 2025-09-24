const config = require('../../../config.js');
const {addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;

const respondent1 = {
  represented: true,
  representativeRegistered: true,
  representativeOrgNumber: 2,
  partyType: 'Organisation'
};
const respondent2 = {
  represented: true,
  sameLegalRepresentativeAsRespondent1: false,
  representativeOrgNumber: 3,
  partyType: 'Organisation'
};

let caseNumber;

Feature('Claim creation 1v2 Diff Solicitor with flight delay @e2e-spec-fast @e2e-spec-1v2DS').tag('@e2e-nightly-prod');

Scenario.skip('Applicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for flight delay @create-claim-spec', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    console.log('AApplicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for flight delay @create-claim-spec');

    await LRspec.login(config.applicantSolicitorUser);
    //await LRspec.createCaseSpecified('1v2 Different LRs fast claim','Individual', null, respondent1, respondent2, 15450);
    await LRspec.createCaseSpecifiedForFlightDelay('1v2 Different LRs fast claim','Organisation', null, respondent1, respondent2, 15450);
    caseNumber = await LRspec.grabCaseNumber();
    await serviceRequest.openServiceRequestTab();
    await serviceRequest.payFee(caseId());

    addUserCaseMapping(caseId(), config.applicantSolicitorUser);
  }
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
