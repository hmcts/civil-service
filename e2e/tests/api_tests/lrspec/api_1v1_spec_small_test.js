const config = require('../../../config.js');

Feature('CCD 1v1 API test @api-spec-small @api-specified @api-nightly-prod');

Scenario('1v1 FULL_DEFENCE claimant and defendant response small claim', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No');
  await api_spec_small.manageContactInformation(config.adminUser, true);
});

Scenario('1v1 FULL_ADMISSION claimant and defendant response small claim', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser);
});

Scenario('1v1 PART_ADMISSION claimant and defendant response small claim', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser);
});

Scenario('1v1 COUNTER_CLAIM claimant and defendant response small claim', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM');
  // counter claim defense brings the case offline
});

Scenario('1v1 FULL_DEFENCE claimant and defendant response small claim - CARM enabled', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, true);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', false, true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', true);
  await api_spec_small.manageContactInformation(config.adminUser, true);
});

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
});
