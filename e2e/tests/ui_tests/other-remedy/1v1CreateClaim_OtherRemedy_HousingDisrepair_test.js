const config = require('../../../config.js');
const {assignCaseRoleToUser, unAssignAllUsers, addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {
  waitForFinishedBusinessProcess,
} = require('../../../api/testingSupport');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');

const claimant1 = {
  litigantInPerson: true
};

const respondent1 = {
  represented: true,
  representativeRegistered: true,
  representativeOrgNumber: 2
};

let caseNumber, validFastTrackDirectionsTask;

if (config.runWAApiTest) {
  validFastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
}

Feature('1v1 - Claim Journey with OtherRemedy type Housing Disrepair').tag('@civil-ccd-nightly @ui-other-remedy');

Scenario('01 Applicant solicitor creates claim with claim type as Housing disrepair', async ({I}) => {
  let claimType = 'Housing disrepair';
  await I.login(config.applicantSolicitorUser);
  await I.createCase(claimant1, null, respondent1, null, 25000, claimType);
  caseNumber = await I.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);
  await addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('02 Applicant solicitor notifies defendant solicitor of claim', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.notifyClaim();
  await assignCaseRoleToUser(caseNumber, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
}).retry(2);

Scenario('03 Applicant solicitor notifies defendant solicitor of claim details', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.notifyClaimDetails();
}).retry(2);

Scenario('04 Defendant solicitor acknowledges claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaim('fullDefence');
}).retry(2);

Scenario('05 Defendant solicitor requests deadline extension', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.informAgreedExtensionDate();
}).retry(2);

Scenario('06 Defendant solicitor adds defendant litigation friend', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.addDefendantLitigationFriend();
}).retry(2);

Scenario('07 Defendant solicitor responds to claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaim({defendant1Response: 'fullDefence', claimValue: 25000});
}).retry(2);


Scenario('08 Claimant solicitor responds to defence', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.respondToDefence('ONE_V_ONE', 25000);
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('09 Add case flags', async ({I}) => {
  const caseFlags = [{
    partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
    details: [PARTY_FLAGS.vulnerableUser.value]
  }, {
    partyName: 'John Smith', roleOnCase: 'Defendant solicitor 1 expert',
    details: [PARTY_FLAGS.unacceptableBehaviour.value]
  }];

  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.createCaseFlags(caseFlags);
  // await I.validateCaseFlags(caseFlags);
}).retry(2);

Scenario('10 Manage case flags', async ({I}) => {
  const caseFlags = [{
    partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
    flagType: 'Vulnerable user',
    flagComment: 'test comment'
  }, {
    partyName: 'John Smith', roleOnCase: 'Defendant solicitor 1 expert',
    flagType: 'Unacceptable/disruptive customer behaviour',
    flagComment: 'test comment'
  }];

  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.manageCaseFlags(caseFlags);
  // await I.validateUpdatedCaseFlags(caseFlags);
}).retry(2);

Scenario('11 Judge triggers SDO', async ({I, api, WA}) => {
  await I.login(config.judgeUserWithRegionId1);
  let taskId;
  if (config.runWAApiTest) {
    const fastTrackDirections = await api.retrieveTaskDetails(config.judgeUserWithRegionId1, caseNumber, config.waTaskIds.fastTrackDirections);
    console.log('fastTrackDirections...' , fastTrackDirections);
    WA.validateTaskInfo(fastTrackDirections, validFastTrackDirectionsTask);
    taskId = fastTrackDirections['id'];
    api.assignTaskToUser(config.judgeUserWithRegionId1, taskId);
  }
  await I.initiateSDOforOtherRemedy(null, null, 'fastTrack', null);
  if (config.runWAApiTest) {
    api.completeTaskByUser(config.judgeUserWithRegionId1, taskId);
  }
}).retry(2);

AfterSuite(async () => {
  await unAssignAllUsers();
});
