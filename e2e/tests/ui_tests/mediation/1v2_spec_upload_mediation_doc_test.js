
const config = require('../../../config.js');

const legalAdvisorUser = config.tribunalCaseworkerWithRegionId4;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

let civilCaseReference;

Feature('1v2SS SDO Carm - Upload mediation documents').tag('@civil-ccd-nightly @ui-mediation');

Scenario('01 1v2SS prepare claim up to SDO', async ({api_spec, LRspec}) => {
  civilCaseReference = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL');
  await LRspec.wait(10);
  console.log('1v2 Spec claims created : ' + civilCaseReference);

  console.log('Create SDO');
  await api_spec.createSDO(legalAdvisorUser, 'CREATE_SMALL');
}).retry(2);

Scenario('02 Claimant upload mediation document', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Claimant 1', 'Both docs');
}).retry(2);

Scenario('03 Schedule a hearing', async ({api_spec}) => {
  console.log('Schedule Hearing');
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
});

Scenario('04 Defendant 1 uploads mediation doc', async ({LRspec}) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Defendant 1', 'Non-attendance');
}).retry(2);

Scenario('05 Prepare for hearing conduct', async ({api_spec}) => {
  console.log('Prepare for Hearing Conduct Hearing');
  await api_spec.amendHearingDueDate(config.systemupdate);
  await api_spec.hearingFeePaid(hearingCenterAdminToBeUsed);
});

Scenario('06 Defendant 2 uploads mediation doc', async ({LRspec}) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Defendant 2', 'Both docs');
}).retry(2);

AfterSuite(async ({api_spec}) => {
  await api_spec.cleanUp();
});
