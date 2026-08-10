const {assignCaseToDefendant, unAssignUserFromCases} = require('./testingSupport');

let userCaseMappings = {};

const addUserCaseMapping = (caseId, user) => {
  if(process.env.UNASSIGN_CASES === 'true') {
    const userCase = userCaseMappings[`${user.email}`];
    userCaseMappings = {...userCaseMappings, [`${user.email}`]: [...(userCase ? userCase : []), {caseId, user}]};
  }
};

const assignCaseRoleToUser = async (caseId, role, user) => {
  await assignCaseToDefendant(caseId, role, user)
    .then(() => addUserCaseMapping(caseId, user));
};

const unAssignAllUsers = async () => {
  if(process.env.UNASSIGN_CASES === 'true') {
    console.log('Removing case role allocations...');
    for (const userRole of Object.values(userCaseMappings)) {
      await unAssignUserFromCases(userRole.map(({caseId}) => caseId), userRole[0].user);
    }
    userCaseMappings = {};
  }
};

module.exports = {
  addUserCaseMapping,
  assignCaseRoleToUser,
  unAssignAllUsers
};
