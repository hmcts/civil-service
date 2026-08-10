 

const config = require('../../../config.js');
const mpScenario = 'ONE_V_ONE';

Feature('Bulk claim 1v1 SDT user API test').tag('@api-bulk-claim');

/* Scenario('1v1 with No interest - Create claim via SDT', async ({bulks}) => {
    await bulks.createClaimFromSDTRequest(config.applicantSolicitorUser, mpScenario, false);
}); */

Scenario('Create claim - 1v1 with No interest', async ({bulks}) => {
  // create claim directly via civil service, using caseworker case creation endpoint
  await bulks.createNewClaimWithCaseworkerCivilService(config.applicantSolicitorUser, 'ONE_V_ONE', false);
});

Scenario('Create claim - 1v2 with No interest', async ({bulks}) => {
  // create claim directly via civil service, using caseworker case creation endpoint
  await bulks.createNewClaimWithCaseworkerCivilService(config.applicantSolicitorUser, 'ONE_V_TWO', false);
});

Scenario('Create claim - 1v1 with interest', async ({bulks}) => {
  // create claim directly via civil service, using caseworker case creation endpoint
  await bulks.createNewClaimWithCaseworkerCivilService(config.applicantSolicitorUser, 'ONE_V_ONE', true);
});

Scenario('1v1 with No interest - Create claim via SDT - Claim Created - valid success sync response', async ({bulks}) => {
  await bulks.createClaimFromSDTRequestValidSuccessSyncResponse(config.applicantSolicitorUserForBulkClaim, mpScenario, false);
});

Scenario('1v1 with No interest - Create claim via SDT - Postcode Negative Validation', async ({bulks}) => {
  await bulks.createClaimFromSDTRequestForPostCodeNegative(config.applicantSolicitorUserForBulkClaim, mpScenario, false);
});

Scenario('1v1 with No interest - Create claim via SDT - Postcode Positive Validation', async ({bulks}) => {
  await bulks.createClaimFromSDTRequestForPostCodePositive(config.applicantSolicitorUserForBulkClaim, mpScenario, false);
});

Scenario('1v1 with No interest - Create claim via SDT - Duplicate case check call', async ({bulks}) => {
  await bulks.createClaimFromSDTRequestForDuplicateCaseCheckCall(config.applicantSolicitorUserForBulkClaim, mpScenario, false);
});

AfterSuite(async ({api}) => {
    await api.cleanUp();
});
