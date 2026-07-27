const config = require('../../../../config.js');
const { assert } = require('chai');
const { createAccount, deleteCitizenAccount } = require('../../../../api/idamHelper.js');
let civilCaseReference, gaCaseReference;


Feature('Create Lip v Lip claim -  Default Judgment');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('Spec Claimant create GA with single application type and HWF', async ({ api_ga }) => {
  civilCaseReference = await api_ga.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'SmallClaims', 'INDIVIDUAL');
  console.log(civilCaseReference);
  gaCaseReference = await api_ga.createGAApplicationWithUnrepresented(config.applicantCitizenUser, civilCaseReference, '', true);
});

Scenario('Spec Claimant create GA with multiple application types', async ({ api_ga }) => {
  civilCaseReference = await api_ga.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'SmallClaims', 'INDIVIDUAL');
  gaCaseReference = await api_ga.createGAApplicationWithUnrepresented(config.applicantCitizenUser, civilCaseReference, 'multiple', false);
});

Scenario('Spec Claimant create GA without notice judge make order', async ({ api_ga }) => {
  civilCaseReference = await api_ga.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'SmallClaims', 'INDIVIDUAL');
  gaCaseReference = await api_ga.createGAApplicationWithUnrepresented(config.applicantCitizenUser, civilCaseReference, '', false);
  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
              + gaCaseReference + ' ***');
  await api_ga.judgeMakesOrderDecisionUncloak(config.judgeUser2WithRegionId2, gaCaseReference);
});

Scenario('Spec Claimant create GA without notice', async ({ api_ga }) => {
  civilCaseReference = await api_ga.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'SmallClaims', 'INDIVIDUAL');
  gaCaseReference = await api_ga.createGAApplicationWithUnrepresentedWithout(config.applicantCitizenUser, civilCaseReference, 'multiple', false);
  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
              + gaCaseReference + ' ***');
  await api_ga.judgeMakesOrderDecisionUncloak(config.judgeUser2WithRegionId2, gaCaseReference);
});

Scenario('Spec Claimant create GA without notice judge make final order', async ({ api_ga }) => {
  civilCaseReference = await api_ga.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'SmallClaims', 'INDIVIDUAL');
  gaCaseReference = await api_ga.createGAApplicationWithUnrepresentedWithout(config.applicantCitizenUser, civilCaseReference, 'multiple', false);
  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');
  console.log('*** Start Judge List the application for hearing on GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge makes order application after hearing GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.judgeMakeFinalOrder(config.judgeUser2WithRegionId2, gaCaseReference, 'FREE_FORM_ORDER', false);
});

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
