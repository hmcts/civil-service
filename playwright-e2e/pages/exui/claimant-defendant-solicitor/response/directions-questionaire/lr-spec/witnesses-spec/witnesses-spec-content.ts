import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  witnesses: 'Witnesses',
};

export const radioButtons = {
  witnessesRequired: {
    label: 'Are there any witnesses who should attend the hearing?',
    yes: {
      label: 'Yes',
      selector: (defendantParty: Party) => `#${defendantParty.oldKey}DQWitnessesRequiredSpec_Yes`,
    },
    no: {
      label: 'No',
      selector: (defendantParty: Party) => `#${defendantParty.oldKey}DQWitnessesRequiredSpec_No`,
    },
  },
};

export const buttons = {
  addNewWitness: {
    title: 'Add new',
    selector: (defendantParty: Party) =>
      `div[id='${defendantParty.oldKey}DQWitnessesDetailsSpec'] button[type='button']`,
  },
};

export const inputs = {
  witnessDetails: {
    firstName: {
      label: 'First name',
      selector: (defendantParty: Party, witnessParty: Party) =>
        `#${defendantParty.oldKey}DQWitnessesDetailsSpec_${witnessParty.number - 1}_firstName`,
    },
    lastName: {
      label: 'Last name',
      selector: (defendantParty: Party, witnessParty: Party) =>
        `#${defendantParty.oldKey}DQWitnessesDetailsSpec_${witnessParty.number - 1}_lastName`,
    },
    number: {
      label: 'Phone number (Optional)',
      selector: (defendantParty: Party, witnessParty: Party) =>
        `#${defendantParty.oldKey}DQWitnessesDetailsSpec_${witnessParty.number - 1}_phoneNumber`,
    },
    email: {
      label: 'Email address (Optional)',
      selector: (defendantParty: Party, witnessParty: Party) =>
        `#${defendantParty.oldKey}DQWitnessesDetailsSpec_${witnessParty.number - 1}_emailAddress`,
    },
    reasonForWitness: {
      label: 'What event did they witness?',
      selector: (defendantParty: Party, witnessParty: Party) =>
        `#${defendantParty.oldKey}DQWitnessesDetailsSpec_${witnessParty.number - 1}_reasonForWitness`,
    },
  },
};
