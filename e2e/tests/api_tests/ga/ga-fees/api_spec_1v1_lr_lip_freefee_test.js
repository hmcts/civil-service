const {createAccount, deleteCitizenAccount} = require('../../../../api/idamHelper.js');
const config = require('../../../../config.js');

let civilCaseReference, gaCaseReference;
const mpScenario = 'ONE_V_ONE';

// this test is skipped until its fixed but comment changes as spec claim ga works now in non prod env
Feature('General Application LR vs LIP 1V1 Application').tag('@ga-fees');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario.skip('GA 1v1 Free Fee  - LR initiates GA vs LIP', async ({ api_ga, I }) => {

  civilCaseReference = await api_ga.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  let hearingDate = await api_ga.createDateString(15);
  gaCaseReference = await api_ga.initiateAdjournVacateGeneralApplication(
    config.applicantSolicitorUser, civilCaseReference, 'No',
    'Yes', hearingDate, '0', 'FREE', '1');
});

Scenario.skip('GA 1v1 Without Notice  - LR initiates GA vs LIP', async ({ api_ga, I }) => {

  civilCaseReference = await api_ga.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser,
    civilCaseReference);
}).retry(1);


AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
