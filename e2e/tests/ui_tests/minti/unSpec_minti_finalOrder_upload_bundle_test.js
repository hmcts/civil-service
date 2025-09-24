const config = require('../../../config.js');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');


const intermediateTrackClaimAmount = '99000';
const mintiEnabled = true;
const claimAmountMulti = '200001';

const track = 'INTERMEDIATE_CLAIM';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let civilCaseReference;

Feature('Intermediate and Multi tracks - Download order template Journey - Upload Bundle @e2e-nightly-prod @e2e-minti');

Scenario('1v2 Same Solicitor Int Track - Download order template - Upload Bundle', async ({api, I}) => {
  const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';
  civilCaseReference =  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, intermediateTrackClaimAmount, mintiEnabled);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, track);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL', 'FOR_SDO', track);

  await I.login(judgeUser);
  await I.initiateFinalOrder(civilCaseReference, 'Intermediate Track', 'Fix a date for CMC');

  await I.login(config.applicantSolicitorUser);
  await I.evidenceUpload(civilCaseReference, false, true);

  await I.login(config.defendantSolicitorUser);
  await I.evidenceUpload(civilCaseReference, true, true, true, mpScenario);

  await I.login(config.applicantSolicitorUser);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + civilCaseReference);
  await I.waitForText('Summary');
  await I.verifyBundleDetails(civilCaseReference);
}).retry(1);

Scenario('1v2 Different Solicitor Multi Track claim - Download order template - Upload Bundle', async ({api, I}) => {
  const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
  civilCaseReference = await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountMulti, mintiEnabled);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL', 'FOR_SDO', 'MULTI_CLAIM');
  await api.createFinalOrder(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'MULTI');
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  await I.login(judgeUser);
  await I.initiateFinalOrder(civilCaseReference, 'Multi Track', 'Fix a date for CCMC');

  await I.login(config.secondDefendantSolicitorUser);
  await I.evidenceUpload(civilCaseReference, true, true);
}).retry(1);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
