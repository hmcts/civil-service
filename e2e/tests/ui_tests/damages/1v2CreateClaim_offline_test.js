const config = require('../../../config.js');

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

let caseNumber;


Feature('1v2 Create claim @e2e-unspec @e2e-multiparty');

Scenario('Claimant solicitor raise a claim against 2 defendants, one of who is without a solicitor (LiP) should progress case offline', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.createCase(
    claimant1,
    null,
    respondent1,
    respondent2,
    false
  );
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

