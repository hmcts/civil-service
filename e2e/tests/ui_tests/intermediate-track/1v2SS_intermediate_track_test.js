const config = require('../../../config.js');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');


const intermediateTrackClaimAmount = '99000';
const mintiEnabled = true;

const track = 'INTERMEDIATE_CLAIM';
const judgeUser = config.judgeUserWithRegionId1;
let civilCaseReference;

Feature('1v2SS unspec intermediate track journey').tag('@civil-ccd-nightly @ui-intermediate-track');

Scenario('1v2SS unspec intermediate track', async ({api, I}) => {
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

AfterSuite(async  () => {
  await unAssignAllUsers();
});