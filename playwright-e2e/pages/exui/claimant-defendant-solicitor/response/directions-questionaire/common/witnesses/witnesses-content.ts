import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  witnesses: 'Witnesses',
};

export const radioButtons = {
  witnessesRequired: {
    label: 'Are there any witnesses who should attend the hearing?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnesses_witnessesToAppear_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnesses_witnessesToAppear_No`,
    },
  },
};

export const buttons = {
  addNewWitness: {
    title: 'Add new',
    selector: (claimantDefendantParty: Party) =>
      `div[id='${claimantDefendantParty.oldKey}DQWitnesses_details'] button[class='button write-collection-add-item__top']`,
  },
};

export const inputs = {
  witnessDetails: {
    firstName: {
      label: 'First name',
      selector: (claimantDefendantParty: Party, witnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnesses_details_${witnessParty.number - 1}_firstName`,
    },
    lastName: {
      label: 'Last name',
      selector: (claimantDefendantParty: Party, witnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnesses_details_${witnessParty.number - 1}_lastName`,
    },
    number: {
      label: 'Phone number (Optional)',
      selector: (claimantDefendantParty: Party, witnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnesses_details_${witnessParty.number - 1}_phoneNumber`,
    },
    email: {
      label: 'Email address (Optional)',
      selector: (claimantDefendantParty: Party, witnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnesses_details_${witnessParty.number - 1}_emailAddress`,
    },
    whatEvent: {
      label: 'What event did they witness?',
      selector: (claimantDefendantParty: Party, witnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnesses_details_${witnessParty.number - 1}_reasonForWitness`,
    },
  },
};
