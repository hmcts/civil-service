/* eslint-disable no-unused-vars */

const config = require('../config.js');
const testingSupport = require("../api/testingSupport");
let document, caseId;

Feature('CCD API tests @api-tests');

Scenario.skip('Create claim', async ({I, api}) => {
  console.log('start createClaimWithRepresentedRespondent');
  caseId = await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
});

Scenario.skip('HMCTS admin adds a case note to case', async ({I, api}) => {
  await api.addCaseNote(config.adminUser, caseId);
});

Scenario.skip('Amend claim documents', async ({I, api}) => {
  await api.amendClaimDocuments(config.applicantSolicitorUser, caseId);
});

Scenario.skip('Notify claim', async ({I, api}) => {
  await api.notifyClaim(config.applicantSolicitorUser, caseId);
});

Scenario.skip('Notify claim details', async ({I, api}) => {
  await api.notifyClaimDetails(config.applicantSolicitorUser, caseId);
});

Scenario.skip('Amend party details', async ({I, api}) => {
  await api.amendPartyDetails(config.adminUser, caseId);
});

Scenario.skip('Acknowledge claim', async ({I, api}) => {
  await api.acknowledgeClaim(config.defendantSolicitorUser, caseId);
});

Scenario.skip('Inform agreed extension date', async ({I, api}) => {
  await api.informAgreedExtension(config.defendantSolicitorUser, caseId);
});

Scenario.skip('Defendant response', async ({I, api}) => {
  await api.defendantResponse(config.defendantSolicitorUser, caseId);
});

Scenario.skip('Claimant response', async ({I, api}) => {
  await api.claimantResponse(config.applicantSolicitorUser, caseId);
});

Scenario('Create claim where respondent is litigant in person', async ({I, api}) => {
  caseId = await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser);
});

Scenario.skip('Create claim where respondent solicitor is not registered in my hmcts', async ({I, api}) => {
  await api.createClaimWithRespondentSolicitorFirmNotInMyHmcts(config.applicantSolicitorUser);
});

Scenario.skip('Create claim and move it to caseman', async ({I, api}) => {
  caseId = await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api.moveCaseToCaseman(config.adminUser, caseId);
});

// This will be enabled when PAY-3817 issue of two minutes is fixed
Scenario.skip('Resubmit claim after payment failure on PBA account ', async ({I, api}) => {
  await api.createClaimWithFailingPBAAccount(config.applicantSolicitorUser);
  await api.resubmitClaim(config.applicantSolicitorUser);
});
