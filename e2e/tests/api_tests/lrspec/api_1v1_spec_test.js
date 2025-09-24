

const config = require('../../../config.js');

Feature('CCD 1v1 API test @api-spec @api-spec-1v1 @api-specified @api-nightly-prod');

Scenario('Create claim spec 1v1', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
});

//Covered this scenario at line 43
Scenario('1v1 full admit', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION');
});

//Covered this scenario at line 50
Scenario('1v1 part admit', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION');
});

Scenario('1v1 counter claim @api-spec-counterclaim', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM');
});


Scenario('1v1 full defence claimant and defendant response @api-spec-full-defence', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.informAgreedExtensionDate(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

Scenario('1v1 full admit claimant and defendant response @api-spec-full-admit', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'FULL_ADMISSION');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', 'ONE_V_ONE',
    'All_FINAL_ORDERS_ISSUED');
});

Scenario('1v1 part admit defence claimant and defendant response @api-spec-part-admit', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'PART_ADMISSION');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'PART_ADMISSION', 'ONE_V_ONE',
    'All_FINAL_ORDERS_ISSUED');
});

Scenario('1v1 Settle claim - full defence claimant and defendant response', async ({I, api_spec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
    await api_spec.informAgreedExtensionDate(config.applicantSolicitorUser);
    await api_spec.defendantResponse(config.defendantSolicitorUser);
    await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
      'All_FINAL_ORDERS_ISSUED');
    await api_spec.settleClaim(config.applicantSolicitorUser, 'NO');
  }
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

