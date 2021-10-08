/* eslint-disable no-unused-vars */

const config = require('../config.js');

Feature('CCD API tests @api-tests');

Scenario('Create claim', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
});

Scenario('HMCTS admin adds a case note to case', async ({I, api}) => {
  await api.addCaseNote(config.adminUser);
});

Scenario('Amend claim documents', async ({I, api}) => {
  await api.amendClaimDocuments(config.applicantSolicitorUser);
});

Scenario('Notify claim', async ({I, api}) => {
  await api.notifyClaim(config.applicantSolicitorUser);
});

Scenario('Notify claim details', async ({I, api}) => {
  await api.notifyClaimDetails(config.applicantSolicitorUser);
});

Scenario('Amend party details', async ({I, api}) => {
  await api.amendPartyDetails(config.adminUser);
});

Scenario('Acknowledge claim', async ({I, api}) => {
  await api.acknowledgeClaim(config.defendantSolicitorUser);
});

Scenario('Inform agreed extension date', async ({I, api}) => {
  await api.informAgreedExtension(config.defendantSolicitorUser);
});

Scenario('Defendant response', async ({I, api}) => {
  await api.defendantResponse(config.defendantSolicitorUser);
});

Scenario('Claimant response', async ({I, api}) => {
  await api.claimantResponse(config.applicantSolicitorUser);
});

Scenario('Create claim where respondent is litigant in person', async ({I, api}) => {
  await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser);
});

Scenario('Create claim where respondent solicitor is not registered in my hmcts', async ({I, api}) => {
  await api.createClaimWithRespondentSolicitorFirmNotInMyHmcts(config.applicantSolicitorUser);
});

Scenario('Create claim and move it to caseman', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api.moveCaseToCaseman(config.adminUser);
});

// This will be enabled when PAY-3817 issue of two minutes is fixed
Scenario.skip('Resubmit claim after payment failure on PBA account ', async ({I, api}) => {
  await api.createClaimWithFailingPBAAccount(config.applicantSolicitorUser);
  await api.resubmitClaim(config.applicantSolicitorUser);
});
