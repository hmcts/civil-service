const {checkCaseFlagsEnabled} = require('../api/testingSupport');

module.exports = {
  removeFlagsFieldsFromFixture: async (data) => {
    if (!(await checkCaseFlagsEnabled())) {
      ['ConfirmNameAddress', 'ConfirmDetails'].forEach(pageId =>
        ['respondent1', 'respondent2'].forEach(field => {
          if (data.valid[pageId] && data.valid[pageId][field]) {
            delete data.valid[pageId][field]['flags'];
          }
        })
      );
    }
  },

  addFlagsToFixture: async (data) => {
    if (await checkCaseFlagsEnabled()) {
      ['respondent1', 'respondent2', 'applicant1', 'applicant2', 'applicant1LitigationFriend',
        'respondent1LitigationFriend', 'respondent2LitigationFriend'].forEach(field => {
        if (data) {
          if (field === 'respondent1' && data.respondent1 && !data.respondent1.flags) {
            data = {
              ...data,
              respondent1: {
                ...data.respondent1,
                flags: {
                  partyName: 'Sir John Doe',
                  roleOnCase: 'Defendant 1'
                }
              }
            };
          }
          if (field === 'respondent2' && data.respondent2 && !data.respondent2.flags) {
            data = {
              ...data,
              respondent2: {
                ...data.respondent2,
                flags: {
                  partyName: 'Dr Foo Bar',
                  roleOnCase: 'Defendant 2'
                }
              }
            };
          }
          if (field === 'applicant1' && data.applicant1 && !data.applicant1.flags) {
            data = {
              ...data,
              applicant1: {
                ...data.applicant1,
                flags: {
                  partyName: 'Test Inc',
                  roleOnCase: 'Claimant 1'
                }
              }
            };
          }
          if (field === 'applicant2' && data.applicant2 && !data.applicant2.flags) {
            data = {
              ...data,
              applicant2: {
                ...data.applicant2,
                flags: {
                  partyName: 'Dr Jane Doe',
                  roleOnCase: 'Claimant 2'
                }
              }
            };
          }
          if (field === 'applicant1LitigationFriend' && data.applicant1LitigationFriend && !data.applicant1LitigationFriend.flags) {
            data = {
              ...data,
              applicant1LitigationFriend: {
                ...data.applicant1LitigationFriend,
                flags: {
                  partyName: 'Bob the litigant friend',
                  roleOnCase: 'Claimant 1 Litigation Friend'
                }
              }
            };
          }
          if (field === 'respondent1LitigationFriend' && data.respondent1LitigationFriend && !data.respondent1LitigationFriend.flags) {
            data = {
              ...data,
              respondent1LitigationFriend: {
                ...data.respondent1LitigationFriend,
                flags: {
                  partyName: 'Bob the litigant friend',
                  roleOnCase: 'Defendant 1 Litigation Friend'
                }
              }
            };
          }
          if (field === 'respondent2LitigationFriend' && data.respondent2LitigationFriend && !data.respondent2LitigationFriend.flags) {
            data = {
              ...data,
              respondent2LitigationFriend: {
                ...data.respondent2LitigationFriend,
                flags: {
                  partyName: 'David the litigant friend',
                  roleOnCase: 'Defendant 2 Litigation Friend'
                }
              }
            };
          }
        }
      });
    }
    return data;
  }
};
