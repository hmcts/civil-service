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

Feature('Claim creation 1v2 Diff Solicitor with flight delay').tag('@civil-ccd-nightly @ui-flight-delay');

Scenario('Applicant solicitor creates 1v2DS specified claim defendant Different LRs for flight delay', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.createCaseSpecifiedForFlightDelay('1v2 Different LRs fast claim','Organisation', null, respondent1, respondent2, 15450);
  caseNumber = await LRspec.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);

  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(1);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
