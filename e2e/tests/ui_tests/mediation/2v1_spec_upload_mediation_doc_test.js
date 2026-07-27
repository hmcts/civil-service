

const config = require('../../../config.js');

let civilCaseReference;

Feature('2v1 SDO Carm - Upload mediation documents').tag('@civil-ccd-nightly @ui-mediation');

Scenario('01 2v1 prepare claim up to claimant response', async ({api_spec}) => {
  civilCaseReference = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', 'TWO_V_ONE',
    'JUDICIAL_REFERRAL');
  console.log('2v1 Spec small claims created : ' + civilCaseReference);
}).retry(2);

Scenario('02 2v1 claimant upload mediation docs', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Both Claimants', 'Both docs');
}).retry(2);

Scenario('03 2v1 defendant upload mediation docs', async ({LRspec}) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.uploadMediationDocs(civilCaseReference, 'Defendant 1', 'Non-attendance');
}).retry(2);

AfterSuite(async ({api_spec}) => {
  await api_spec.cleanUp();
});
