import StringHelper from '../../../../../../../helpers/string-helper';
import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  witnesses: `Witnesses`,
  partyWitnesses: (defendantParty: Party) =>
    `${StringHelper.capitalise(defendantParty.partyType)} ${defendantParty.number} witnesses`,
};

export const radioButtons = {
  witnessesRequired: {
    label: 'Are there any witnesses who should attend the hearing?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_witnessesToAppear_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_witnessesToAppear_No`,
    },
  },
};

export const buttons = {
  addNewWitness: {
    title: 'Add new',
    selector: (claimantDefendantParty: Party) =>
      `div[id='${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_details'] button[class='button write-collection-add-item__top']`,
  },
};

export const inputs = {
  witnessDetails: {
    label: 'Witness details',
    firstName: {
      label: 'First name',
      selector: (claimantDefendantParty: Party, claimantDefendantWitnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_details_${claimantDefendantWitnessParty.number - 1}_firstName`,
    },
    lastName: {
      label: 'Last name',
      selector: (claimantDefendantParty: Party, claimantDefendantWitnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_details_${claimantDefendantWitnessParty.number - 1}_lastName`,
    },
    phoneNumber: {
      label: 'Phone number (Optional)',
      selector: (claimantDefendantParty: Party, claimantDefendantWitnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_details_${claimantDefendantWitnessParty.number - 1}_phoneNumber`,
    },
    email: {
      label: 'Email address (Optional)',
      selector: (claimantDefendantParty: Party, claimantDefendantWitnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_details_${claimantDefendantWitnessParty.number - 1}_emailAddress`,
    },
    whatEvent: {
      label: 'What event did they witness?',
      selector: (claimantDefendantParty: Party, claimantDefendantWitnessParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQWitnessesSmallClaim_details_${claimantDefendantWitnessParty.number - 1}_reasonForWitness`,
    },
  },
  witnessNumber: {
    label: 'How many witnesses, including the claimant, will give evidence at the hearing?',
    selector: (claimantDefendantParty: Party) => `#${claimantDefendantParty.oldKey}ClaimWitnesses`,
  },
};
