const config = require('../../../config.js');
let civilCaseReference;

Feature('CCD 1v2LIPLR COS UI test').tag('@civil-ccd-nightly @ui-cos');

Scenario('1v2 - one LIP and one LR - notify/notify claim details journey', async ({I, api}) => {
  civilCaseReference = await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser,
    'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');
  console.log('Case created for COS: ' + civilCaseReference);
  await I.login(config.applicantSolicitorUser);
  await I.fillLRNotifyClaimCOSForm(civilCaseReference, 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');
  await I.navigateToTab(civilCaseReference, 'History');
  await I.see('Awaiting Claim Details Notification');
  await I.fillLRNotifyClaimDetailsCOSForm(civilCaseReference);
  await I.navigateToTab(civilCaseReference, 'History');
  await I.see('Awaiting Defendant Response');
}).retry(1);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
