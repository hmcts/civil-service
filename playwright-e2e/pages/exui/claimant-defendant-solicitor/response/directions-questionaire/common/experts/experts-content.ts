import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  experts: 'Experts',
};

export const radioButtons = {
  expertsRequired: {
    label: 'Do you want to use an expert?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_expertRequired_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_expertRequired_No`,
    },
  },
  expertReports: {
    label: 'Have you already sent expert reports or similar to other parties?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_expertReportsSent-YES`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_expertReportsSent-NO`,
    },
    notObtained: {
      label: 'Not yet obtained',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_expertReportsSent-NOT_OBTAINED`,
    },
  },
  jointExpert: {
    label: 'Do you think the case is suitable for a joint expert?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_jointExpertSuitable_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_jointExpertSuitable_No`,
    },
  },
};

export const inputs = {
  expertDetails: {
    firstName: {
      label: 'First name',
      selector: (claimantDefendantParty: Party, expertParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_details_${expertParty.number - 1}_firstName`,
    },
    lastName: {
      label: 'Last name',
      selector: (claimantDefendantParty: Party, expertParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_details_${expertParty.number - 1}_lastName`,
    },
    emailAddress: {
      label: 'Email Address',
      selector: (claimantDefendantParty: Party, expertParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_details_${expertParty.number - 1}_emailAddress`,
    },
    phoneNumber: {
      label: 'Phone Number',
      selector: (claimantDefendantParty: Party, expertParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_details_${expertParty.number - 1}_phoneNumber`,
    },
    expertise: {
      label: 'Field of expertise',
      selector: (claimantDefendantParty: Party, expertParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_details_${expertParty.number - 1}_fieldOfExpertise`,
    },
    whyRequired: {
      label: 'Why do you need this expert?',
      selector: (claimantDefendantParty: Party, expertParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_details_${expertParty.number - 1}_whyRequired`,
    },
    estimatedCost: {
      label: 'Estimated cost',
      selector: (claimantDefendantParty: Party, expertParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQExperts_details_${expertParty.number - 1}_estimatedCost`,
    },
  },
};

export const buttons = {
  addNew: {
    title: 'Add new',
    selector: (claimantDefendantParty: Party) =>
      `div[id='${claimantDefendantParty.oldKey}DQExperts_details'] button[type='button']`,
  },
};
