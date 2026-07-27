const config = require('../../../config.js');
const mpScenario = 'ONE_V_ONE';

Feature('1v1 lip unspec api journey').tag('@civil-service-nightly @api-cos');

Scenario('Create claim where respondent is litigant in person and notify/notify details', async ({api}) => {
  await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimLip(config.applicantSolicitorUser);
  await api.notifyClaimDetailsLip(config.applicantSolicitorUser, mpScenario);
}).tag('@api-cos');

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});