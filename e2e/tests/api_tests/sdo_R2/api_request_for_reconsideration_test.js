const config = require('../../../config.js');
const legalAdvUser = config.tribunalCaseworkerWithRegionId4;
// To use on local because the idam images are different
const judgeUser = config.judgeUserWithRegionId1;

async function prepareClaimSpec(api_spec_small) {
await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, false);
await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', true);
await api_spec_small.claimantResponse(config.applicantSolicitorUser, true);
}

Feature('Request for reconsideration - 1v1 - spec @api-specified @api-nightly-prod @api-r2-sdo'); // reinstate @api-nightly-prod tag when issue described on CIV-14871 is resolved

Scenario('1v1 spec request for reconsideration for uphold previous order', async ({api_spec_small}) => {
    await prepareClaimSpec(api_spec_small);
    await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
    await api_spec_small.requestForReconsideration(config.applicantSolicitorUser, 'Applicant');
    await api_spec_small.judgeDecisionOnReconsiderationRequest(judgeUser, 'YES');
}).tag('@api-nonprod');

Scenario('1v1 spec request for reconsideration for create new SDO', async ({api_spec_small}) => {
    await prepareClaimSpec(api_spec_small);
    await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
    await api_spec_small.requestForReconsideration(config.defendantSolicitorUser, 'Respondent1');
    await api_spec_small.judgeDecisionOnReconsiderationRequest(judgeUser, 'CREATE_SDO');
    // Create a new SDO again
    await api_spec_small.createSDO(judgeUser, 'CREATE_SMALL_NO_SUM');
});

Scenario('1v1 spec request for reconsideration for create general order', async ({api_spec_small}) => {
    await prepareClaimSpec(api_spec_small);
    await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
    await api_spec_small.requestForReconsideration(config.defendantSolicitorUser,'Respondent1');
    await api_spec_small.judgeDecisionOnReconsiderationRequest(judgeUser, 'CREATE_GENERAL_ORDER');
});

Scenario('1v2 spec request for reconsideration by defendant2 for create general order', async ({api_spec_small}) => {
    await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');
    await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_TWO_DIF_SOL',true);
    await api_spec_small.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_TWO_DIF_SOL',true);
    await api_spec_small.claimantResponse(config.applicantSolicitorUser, true);
    await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
    await api_spec_small.requestForReconsideration(config.secondDefendantSolicitorUser,'Respondent2');
    await api_spec_small.judgeDecisionOnReconsiderationRequest(judgeUser, 'CREATE_GENERAL_ORDER');
}).tag('@api-nonprod');

Scenario.skip('1v1 spec request for reconsideration when claim amount is greater than 1000', async ({api_spec}) => {
    await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
    await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE');
    await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
      'JUDICIAL_REFERRAL');
    await api_spec.createSDO(judgeUser, 'CREATE_FAST');
    //should throw 422 error as this event is not allowed for claim amount more than 1000
    await api_spec.requestForReconsideration(config.defendantSolicitorUser);
});

Scenario('1v1 spec request for reconsideration for create a new SDO', async ({api_spec_small}) => {
    await prepareClaimSpec(api_spec_small);
    await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
    await api_spec_small.requestForReconsideration(config.defendantSolicitorUser,'Respondent1');
    await api_spec_small.judgeDecisionOnReconsiderationRequest(judgeUser, 'CREATE_SDO');
    // await api_spec_small.notSuitableSdoChangeLocation(judgeUser, 'CHANGE_LOCATION');
});

AfterSuite(async ({api_spec_small, api_spec}) => {
  await api_spec_small.cleanUp();
  await api_spec.cleanUp();
});
