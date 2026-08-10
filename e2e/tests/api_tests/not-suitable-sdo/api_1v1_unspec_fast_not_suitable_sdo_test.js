const config = require('../../../config.js');

const mpScenario1v1 = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const caseWorkerUser = config.hearingCenterAdminWithRegionId1;
const fastClaimAmount = '11000';

Feature('Transfer Online Case 1v1 API test - fast claim - unspec').tag('@civil-service-nightly');

Scenario('1v1 unspec full defence - not suitable SDO - Transfer Case', async ({api}) => {
    await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario1v1, fastClaimAmount);
    await api.amendClaimDocuments(config.applicantSolicitorUser);
    await api.notifyClaim(config.applicantSolicitorUser);
    await api.notifyClaimDetails(config.applicantSolicitorUser);
    await api.defendantResponse(config.defendantSolicitorUser, mpScenario1v1, null, 'SMALL_CLAIMS');
    await api.claimantResponse(config.applicantSolicitorUser, mpScenario1v1, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
    await api.notSuitableSDO(judgeUser, 'CHANGE_LOCATION');
    await api.transferCase(caseWorkerUser);
}).tag('@api-not-suitable-sdo');

Scenario('1v1 unspec full defence - not suitable SDO - Other Reasons', async ({api}) => {
    await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario1v1, fastClaimAmount);
    await api.amendClaimDocuments(config.applicantSolicitorUser);
    await api.notifyClaim(config.applicantSolicitorUser);
    await api.notifyClaimDetails(config.applicantSolicitorUser);
    await api.defendantResponse(config.defendantSolicitorUser, mpScenario1v1, null, 'SMALL_CLAIMS');
    await api.claimantResponse(config.applicantSolicitorUser, mpScenario1v1, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
    await api.notSuitableSDO(judgeUser, 'OTHER_REASONS');
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
