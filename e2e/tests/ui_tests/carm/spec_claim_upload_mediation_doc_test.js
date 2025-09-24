

const config = require('../../../config.js');
const legalAdvisorUser = config.tribunalCaseworkerWithRegionId4;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

let civilCaseReference;


Feature('SDO Carm - Upload mediation documents @e2e-nightly-prod @e2e-carm');

Scenario('2v1 claimant and defendant upload mediation documents', async ({api_spec, LRspec}) => {
  civilCaseReference = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', 'TWO_V_ONE',
    'JUDICIAL_REFERRAL');
  console.log('2v1 Spec small claims created : ' + civilCaseReference);
}).retry(2);

Scenario('2v1 claimant upload mediation docs', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Both Claimants', 'Both docs');
}).retry(2);

Scenario('2v1 defendant upload mediation docs', async ({LRspec}) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Defendant 1', 'Non-attendance');
}).retry(2);

Scenario('1v2 upload mediation documents in different SDO states', async ({api_spec, LRspec}) => {
  civilCaseReference = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL');
  await LRspec.wait(10);
  console.log('1v2 Spec claims created : ' + civilCaseReference);

  console.log('Create SDO');
  await api_spec.createSDO(legalAdvisorUser, 'CREATE_SMALL');

  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Claimant 1', 'Both docs');

  console.log('Schedule Hearing');
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Defendant 1', 'Non-attendance');

  console.log('Prepare for Hearing Conduct Hearing');
  await api_spec.amendHearingDueDate(config.systemupdate);
  await api_spec.hearingFeePaid(hearingCenterAdminToBeUsed);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Defendant 2', 'Both docs');
}).retry(2);

AfterSuite(async ({api_spec}) => {
  await api_spec.cleanUp();
});
