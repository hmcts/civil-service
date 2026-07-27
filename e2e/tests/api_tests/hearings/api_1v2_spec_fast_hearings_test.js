
const config = require('../../../config.js');
const {getLanguageInterpreterFlag, getRAWheelchairFlag} = require('../../../api/caseFlagsHelper');

const serviceId = 'AAA6';
const hmcTest = true;
let caseId;

Feature('CCD 1v2 Spec fast hearings API test').tag('@civil-service-nightly @api-hearings');


Scenario.skip('01 1v2 fast claim full defence', async ({api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', hmcTest);
  await api_spec_fast.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
  caseId = await api_spec_fast.getCaseId();
});

Scenario.skip('02 Listing officer adds case flags', async ({hearings}) => {
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'applicant1', getLanguageInterpreterFlag());
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'respondent1', getRAWheelchairFlag());
});

Scenario.skip('03 Judge choose hearing in person', async ({api_spec_fast}) => {
  await api_spec_fast.createSDO(config.judgeUser2WithRegionId2, 'CREATE_FAST');
});

Scenario.skip('04 Hearing centre admin requests a hearing', async ({hearings}) => {
  await hearings.generateHearingsPayload(config.hearingCenterAdminWithRegionId2, caseId, serviceId);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
