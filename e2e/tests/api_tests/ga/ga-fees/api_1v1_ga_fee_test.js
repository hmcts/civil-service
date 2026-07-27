const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
let civilCaseReference, gaCaseReference;

Feature('GA 1v1 GA Fee API tests').tag('@ga-fees');

Scenario.skip('MixTypesWithVary - 119 pounds', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGaWithTypes(
    config.applicantSolicitorUser, civilCaseReference, ['VARY_ORDER','EXTEND_TIME','STAY_THE_CLAIM'],
    '11900', 'FEE0443');
  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
              + gaCaseReference + ' ***');
  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);
  console.log('*** End Judge Request More Information and Uncloak Application on GA Case Reference: '
              + gaCaseReference + ' ***');
}).retry(1);

Scenario('MixTypesWithSetAside - 119 pounds', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGaWithTypes(
      config.applicantSolicitorUser, civilCaseReference, ['SET_ASIDE_JUDGEMENT','EXTEND_TIME','STAY_THE_CLAIM'],
      '11900', 'FEE0443');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
