const config = require('../../../config.js');
const legalAdvUser = config.tribunalCaseworkerWithRegionId4;
// To use on local because the idam images are different
const judgeUser = config.judgeUserWithRegionId1;

Feature('Request for reconsideration - 1v1 - spec').tag('@civil-service-nightly'); // reinstate @civil-service-nightly tag when issue described on CIV-14871 is resolved

Scenario('1v2 spec request for reconsideration by defendant 2 for create general order', async ({api_spec_small}) => {
    await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');
    await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_TWO_DIF_SOL',true);
    await api_spec_small.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_TWO_DIF_SOL',true);
    await api_spec_small.claimantResponse(config.applicantSolicitorUser, true);
    await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
    await api_spec_small.requestForReconsideration(config.secondDefendantSolicitorUser,'Respondent2');
    await api_spec_small.judgeDecisionOnReconsiderationRequest(judgeUser, 'CREATE_GENERAL_ORDER');
}).tag('@api-rfr');;

AfterSuite(async ({api_spec_small, api_spec}) => {
  await api_spec_small.cleanUp();
  await api_spec.cleanUp();
});
