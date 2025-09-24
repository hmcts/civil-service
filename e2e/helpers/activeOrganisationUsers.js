const {updateStubResponseFileByRequestUrl} = require('../api/wiremock/wiremock');

const activeOrganisationUsersRequestUrl = '/refdata/external/v1/organisations/users';

const getResponseFileLocationByUser = (user) => {
  const userEmail = `${user.email}`;

  if (userEmail.startsWith('hmcts.civil+organisation.1')) {
    return 'prd/organisation1UsersWithoutRoles.json';
  } else if (userEmail.startsWith('hmcts.civil+organisation.2')) {
    return 'prd/organisation2UsersWithoutRoles.json';
  } else if (userEmail.startsWith('hmcts.civil+organisation.3')) {
    return 'prd/organisation3UsersWithoutRoles.json';
  } else if(userEmail.startsWith('civil.damages.claims+organisation.1')) {
    return 'prd/civilDamagesClaimsOrganisation1UsersWithoutRoles.json';
  }else if(userEmail.startsWith('civil.damages.claims+organisation.2')) {
    return 'prd/civilDamagesClaimsOrganisation2UsersWithoutRoles.json';
  }
  throw new Error(`Response mapping does not exist for user: ${userEmail}`);
};

module.exports = {
  updateActiveOrganisationUsersMocks: async(userFromOrganisation) => {
    const responseFileLocation = getResponseFileLocationByUser(userFromOrganisation);
    console.log(`Setting active organisation users response file: ${responseFileLocation}`);
    await updateStubResponseFileByRequestUrl(activeOrganisationUsersRequestUrl, responseFileLocation);
  }
};
