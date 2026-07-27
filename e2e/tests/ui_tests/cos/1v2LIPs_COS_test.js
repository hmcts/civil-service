const config = require('../../../config.js');
let civilCaseReference;

Feature('CCD 1v2 2 LIPs COS UI test').tag('@civil-ccd-nightly @ui-cos');

Scenario('1v2 two respondents are LIP - notify/notify claim details journey', async ({I, api}) => {
  civilCaseReference = await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser,
    'ONE_V_TWO_LIPS');
  console.log('Case created for COS: ' + civilCaseReference);
  await I.login(config.applicantSolicitorUser);
  await I.fillNotifyClaimCOSForm(civilCaseReference, 'ONE_V_TWO_LIPS');
  await I.verifyCOSTabDetails(civilCaseReference);
  await I.navigateToTab(civilCaseReference, 'History');
  await I.see('Awaiting Claim Details Notification');
  await I.fillNotifyClaimDetailsCOSForm(civilCaseReference);
  await I.verifyCOSTabNotifyClaimDetails(civilCaseReference);
  await I.navigateToTab(civilCaseReference, 'History');
    await I.see('Awaiting Defendant Response');
}).retry(1);