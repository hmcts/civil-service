const apiRequest = require('../../api/apiRequest');
const config = require('../../config');
const chai = require('chai');
const {assert,expect} = chai;

const assertionText = 'Asserting case flag fields have been initialised for';

const assertPartyFlags = (flagsParent, roleOnCase) => {
  if(flagsParent) {
    console.log(`${assertionText} [${roleOnCase}]`);
    assert.deepEqual(flagsParent.flags, {
      partyName: flagsParent.partyName,
      roleOnCase
    });
  }
};

const assertLitigationFriendFlags = (flagsParent, roleOnCase) => {
  if(flagsParent) {
    console.log(`${assertionText} [${roleOnCase}]`);
    assert.deepEqual(flagsParent.flags, {
      partyName: `${flagsParent.firstName} ${flagsParent.lastName}`,
      roleOnCase
    });
  }
};

const assertRespondent1DQPartyFields = (caseDetails) => {
  let respondent1Experts = caseDetails.case_data.respondent1Experts;
  let respondent1Witnesses = caseDetails.case_data.respondent1Witnesses;

  if (respondent1Experts) {
    console.log(`${assertionText} [Respondent solicitor 1 expert]`);
    expect(respondent1Experts[0].value.flags).deep.equal({
      'partyName': 'John Doe',
      'roleOnCase': 'Defendant solicitor 1 expert'
    });
  }

  if (respondent1Witnesses) {
    console.log(`${assertionText} [Respondent solicitor 1 witness]`);
    expect(respondent1Witnesses[0].value.flags).deep.equal({
      'partyName': 'John Smith',
      'roleOnCase': 'Defendant solicitor 1 witness'
    });
  }
};

function assertRespondent2DQPartyFields(caseDetails) {
  let respondent2Experts = caseDetails.case_data.respondent2Experts;
  let respondent2Witnesses = caseDetails.case_data.respondent2Witnesses;

  if (respondent2Experts) {
    console.log(`${assertionText} [Respondent solicitor 2 expert]`);
    expect(respondent2Experts[0].value.flags).deep.equal({
      'partyName': 'John Doe',
      'roleOnCase': 'Defendant solicitor 2 expert'
    });
  }

  if (respondent2Experts) {
    console.log(`${assertionText} [Respondent solicitor 2 witness]`);
    expect(respondent2Witnesses[0].value.flags).deep.equal({
      'partyName': 'John Smith',
      'roleOnCase': 'Defendant solicitor 2 witness'
    });
  }
}

function assertApplicantDQPartyFields(caseDetails) {
  let applicantExperts = caseDetails.case_data.applicantExperts;
  let applicantWitnesses = caseDetails.case_data.applicantWitnesses;

  if (applicantExperts) {
    console.log(`${assertionText} [Applicant solicitor expert]`);
    expect(applicantExperts[0].value.flags).deep.equal({
      'partyName': 'John Doe',
      'roleOnCase': 'Claimant solicitor expert'
    });
  }

  if (applicantWitnesses) {
    console.log(`${assertionText} [Applicant solicitor witness]`);
    expect(applicantWitnesses[0].value.flags).deep.equal({
      'partyName': 'John Smith',
      'roleOnCase': 'Claimant solicitor witness'
    });
  }
}


module.exports = {
  assertFlagsInitialisedAfterCreateClaim: async (user, caseId) => {
    const {case_data} = await apiRequest.fetchCaseDetails(user, caseId);
    assertPartyFlags(case_data.applicant1, 'Claimant 1');
    assertPartyFlags(case_data.applicant2, 'Claimant 2');
    assertPartyFlags(case_data.respondent1, 'Defendant 1');
    assertPartyFlags(case_data.respondent2, 'Defendant 2');
    assertLitigationFriendFlags(case_data.applicant1LitigationFriend, 'Claimant 1 Litigation Friend');
    assertLitigationFriendFlags(case_data.applicant2LitigationFriend, 'Claimant 2 Litigation Friend');
  },
  assertFlagsInitialisedAfterAddLitigationFriend: async (user, caseId) => {
    const {case_data} = await apiRequest.fetchCaseDetails(user, caseId);
    assertLitigationFriendFlags(case_data.respondent1LitigationFriend, 'Defendant 1 Litigation Friend');
    assertLitigationFriendFlags(case_data.respondent2LitigationFriend, 'Defendant 2 Litigation Friend');
  },
  assertCaseFlags: async (caseId, user, response) => {
    if (['FULL_DEFENCE1', 'FULL_DEFENCE2', 'FULL_DEFENCE'].indexOf(response) > -1) {
      console.log('Asserting flags structure is available to admin user');
      const caseDetails = await apiRequest.fetchCaseDetails(config.adminUser, caseId);

      if (user === config.defendantSolicitorUser) {
        await assertRespondent1DQPartyFields(caseDetails);
      } else if (user === config.secondDefendantSolicitorUser) {
        await assertRespondent2DQPartyFields(caseDetails);
      } else {
        assertApplicantDQPartyFields(caseDetails);
      }
    }
  }
};
