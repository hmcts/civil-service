import exuiUsers from "../../../../config/users/exui-users";

const email = {
  Email: {
    respondentSolicitor1EmailAddress: exuiUsers.defendantSolicitor1User.email,
    applicantSolicitor1UserDetails: {
      email: exuiUsers.claimantSolicitorUser.email,
    },
    specRespondentSolicitor1EmailAddress: exuiUsers.defendantSolicitor1User.email
  },
};

const amendPartyDetailsDataBuilderComponents = {
  email,
};

export default amendPartyDetailsDataBuilderComponents;
