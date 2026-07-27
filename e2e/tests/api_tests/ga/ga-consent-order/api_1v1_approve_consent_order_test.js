const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
let civilCaseReference, civilCaseReferenceAfterSDO, gaCaseReference;
const genAppType = 'STAY_THE_CLAIM';
const claimAmountJudge = '11000';

Feature('GA 1v1 Consent Order API tests').tag('@civil-service-nightly');

BeforeSuite(async ({api_ga}) => {
 /* civilCaseReference = await api_ga.createUnspecifiedClaim(
    config.applicantSolicitorUser, mpScenario, 'SoleTrader');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  console.log('Civil Case created for general application: ' + civilCaseReference);*/


  civilCaseReferenceAfterSDO =await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', claimAmountJudge);
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReferenceAfterSDO);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReferenceAfterSDO);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReferenceAfterSDO, true);
  console.log('Civil Case After SDO created for general application: ' + civilCaseReferenceAfterSDO);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  console.log('Civil Case After SDO defendant responded: ' + civilCaseReferenceAfterSDO);
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case After SDO claimant responded: ' + civilCaseReferenceAfterSDO);
});

Scenario('Caseworker makes decision 1V1 - CONSENT ORDER', async ({api_ga}) => {

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateConsentGeneralApplication(config.applicantSolicitorUser, civilCaseReferenceAfterSDO, ['STAY_THE_CLAIM']);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponseConsentOrderApp(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start CaseWorker Approve Consent Order on GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.nbcAdminApproveConsentOrder(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  console.log('*** End CaseWorker Approve Consent Order on GA Case Reference: ' + gaCaseReference + ' ***');
}).retry(1).tag('@api-ga-consent-order');

Scenario('Judge Revisit 1V1 - consentOrder End Date Scheduler', async ({api_ga}) => {

  console.log('*** Triggering Judge Revisit Order Made Scheduler ***');
  await api_ga.judgeRevisitConsentScheduler(gaCaseReference, 'ORDER_MADE', genAppType);
  console.log('*** End of Judge Revisit Order Made Scheduler ***');

}).retry(1);

Scenario('Judge makes decision 1V1 - CONSENT ORDER - Uncloak Application', async ({api_ga}) => {

  console.log('Make a General Application for Consent order');
  gaCaseReference = await api_ga.initiateConsentGeneralApplication(config.applicantSolicitorUser, civilCaseReferenceAfterSDO, ['STRIKE_OUT']);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponseConsentOrderApp(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);
  console.log('*** End Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Start Callback for Additional Payment: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentSuccess(config.applicantSolicitorUser, gaCaseReference, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
  console.log('*** End uncloaking consent order: ' + gaCaseReference + ' ***');

}).retry(1);

Scenario('Legal Advisor decision 1V1 - CONSENT ORDER - Uncloak Application', async ({api_ga}) => {

  console.log('Make a General Application for Consent order');

  gaCaseReference = await api_ga.initiateConsentGeneralApplication(config.applicantSolicitorUser, civilCaseReferenceAfterSDO, ['EXTEND_TIME']);
  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponseConsentOrderApp(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);
  console.log('*** End Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Start Callback for Additional Payment: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentSuccess(config.applicantSolicitorUser, gaCaseReference, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
  console.log('*** End uncloaking consent order: ' + gaCaseReference + ' ***');

}).retry(1).tag('@api-ga-consent-order');

Scenario('Judge makes decision 1V1 - CONSENT ORDER - URGENT Uncloak Application', async ({api_ga}) => {

  console.log('Make a General Application for Consent order');
  gaCaseReference = await api_ga.initiateConsentUrgentGeneralApplication(config.applicantSolicitorUser, civilCaseReferenceAfterSDO, ['STRIKE_OUT']);

  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '+ gaCaseReference + ' ***');

  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);
  console.log('*** End Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Start Callback for Additional Payment: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentSuccess(config.applicantSolicitorUser, gaCaseReference, 'AWAITING_RESPONDENT_RESPONSE');
  console.log('*** End uncloaking consent order: ' + gaCaseReference + ' ***');

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponseConsentOrderApp(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

}).retry(1);

Scenario('After SDO - CONSENT ORDER -  CaseWorker Refer to Judge makes decision 1V1 - Uncloak Application', async ({ api_ga, I }) => {

  console.log('Make a Urgent General Application for Consent order');
  gaCaseReference = await api_ga.initiateConsentGeneralApplication(config.applicantSolicitorUser, civilCaseReferenceAfterSDO, ['STAY_THE_CLAIM']);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponseConsentOrderApp(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** NBC Admin Region1 Refer to Judge Process Start ***');
  await api_ga.nbcAdminReferToJudge(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  console.log('*** NBC Admin Region4 Refer to Judge Process End ***');

  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);

  console.log('*** Start Callback for Additional Payment: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentSuccess(config.applicantSolicitorUser,gaCaseReference , 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
  console.log('*** End uncloaking consent order: ' + gaCaseReference + ' ***');

}).retry(1).tag('@api-ga-consent-order');

Scenario('After SDO - CONSENT ORDER - CaseWorker Refer to Judge makes decision 1V1 -- URGENT - Uncloak Application', async ({ api_ga, I }) => {

  console.log('Make a Urgent General Application for Consent order');
  gaCaseReference = await api_ga.initiateConsentUrgentGeneralApplication(config.applicantSolicitorUser, civilCaseReferenceAfterSDO, ['STAY_THE_CLAIM']);

  console.log('*** NBC Admin Region1 Refer to Judge Process Start ***');
  await api_ga.nbcAdminReferToJudge(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  console.log('*** NBC Admin Region4 Refer to Judge Process End ***');

  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);

  console.log('*** Start Callback for Additional Payment: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentSuccess(config.applicantSolicitorUser, gaCaseReference, 'AWAITING_RESPONDENT_RESPONSE');
  console.log('*** End uncloaking consent order: ' + gaCaseReference + ' ***');

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponseConsentOrderApp(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

}).retry(1).tag('@api-ga-consent-order');

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
