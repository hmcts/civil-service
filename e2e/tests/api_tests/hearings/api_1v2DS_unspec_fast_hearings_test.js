const config = require('../../../config.js');
const {getSupportWorkerFlag, getDetainedIndividualFlag, getDisruptiveIndividualFlag
} = require('../../../api/caseFlagsHelper');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const fastClaimAmount = '11000';
const serviceId = 'AAA7';
const hmcTest = true;
let caseId;

Feature('CCD 1v2 Unspec fast hearings API test').tag('@civil-service-nightly @api-hearings @civil-wa-master @civil-wa-pr @civil-wa-nightly');

Scenario('01 1v2DS full defence defendant and claimant response', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, fastClaimAmount, false, hmcTest);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');

  caseId = await api.getCaseId();
});

Scenario('02 Listing officer adds case flags', async ({hearings}) => {
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'respondent1', getDetainedIndividualFlag());
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'respondent1', getDisruptiveIndividualFlag());
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'respondent2Witnesses', getSupportWorkerFlag());
});

Scenario.skip('06 Judge choose hearing in person', async ({api}) => {
  await api.createSDO(config.judgeUser2WithRegionId2, 'CREATE_FAST_IN_PERSON');
});

Scenario.skip('07 Hearing centre admin requests a hearing', async ({hearings}) => {
  await hearings.generateHearingsPayload(config.hearingCenterAdminWithRegionId2, caseId, serviceId);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

