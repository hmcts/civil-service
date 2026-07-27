const config = require('../../../config.js');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');

const claimant1 = {
  litigantInPerson: false
};
const respondent1 = {
  represented: false
};
const respondent2 = {
  sameLegalRepresentativeAsRespondent1: false,
  represented: true,
  representativeRegistered: true,
  representativeOrgNumber: 2
};

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//let caseNumber;


Feature('1v2 Create claim, then automatically goes offline').tag('@civil-ccd-nightly @ui-case-offline');

Scenario.skip('Claimant solicitor raise a claim against 2 defendants, one of who is without a solicitor (LiP) should progress case offline', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.createCase(
    claimant1,
    null,
    respondent1,
    respondent2,
    30000,
    false
  );

  // Reinstate the lines below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //caseNumber = await I.grabCaseNumber();
  //await I.see(`Case ${caseNumber} has been created.`);
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
