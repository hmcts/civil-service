 

const config = require('../../../config.js');
Feature('CCD 1v2 2 Lips API test @api-unspec @api-multiparty @api-tests-1v2 @api-cos @api-nightly-prod');

Scenario('Create claim where one respondent is LIP one is LR and notify/notify details', async ({api}) => {
  await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser,
                                                      'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');
  await api.notifyClaimLip(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');
  await api.notifyClaimDetailsLip(config.applicantSolicitorUser,
                                  'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');
});

Scenario('Create claim where two respondents are LIP and notify/notify details', async ({api}) => {
  await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser,
                                                      'ONE_V_TWO_LIPS');
  await api.notifyClaimLip(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');
  await api.notifyClaimDetailsLip(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');
});

AfterSuite(async  ({api}) => {
    await api.cleanUp();
});
