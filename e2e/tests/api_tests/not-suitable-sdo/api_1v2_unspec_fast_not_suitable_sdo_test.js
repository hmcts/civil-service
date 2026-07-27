const config = require('../../../config.js');

const mpScenario1v2 = 'ONE_V_TWO_TWO_LEGAL_REP';
const judgeUser = config.judgeUserWithRegionId1;
const caseWorkerUser = config.hearingCenterAdminWithRegionId1;
const fastClaimAmount = '11000';

Feature('Transfer Online Case 1v2 API test - fast claim - unspec').tag('@api-not-suitable-sdo @civil-service-nightly');

Scenario('1v2 full defence unspecified - not suitable SDO - Transfer Case)', async ({api}) => {
    await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario1v2, fastClaimAmount);
    await api.amendClaimDocuments(config.applicantSolicitorUser);
    await api.notifyClaim(config.applicantSolicitorUser, mpScenario1v2);
    await api.notifyClaimDetails(config.applicantSolicitorUser);
    await api.defendantResponse(config.defendantSolicitorUser, mpScenario1v2, 'solicitorOne');
    await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario1v2, 'solicitorTwo');
    await api.claimantResponse(config.applicantSolicitorUser, mpScenario1v2, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
    await api.notSuitableSDO(judgeUser, 'CHANGE_LOCATION');
    await api.transferCase(caseWorkerUser);
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});