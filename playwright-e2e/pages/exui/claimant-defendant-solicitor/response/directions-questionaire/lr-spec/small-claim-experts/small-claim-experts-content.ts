import ClaimType from '../../../../../../../enums/claim-type';
import PartyType from '../../../../../../../enums/party-types';
import { Party } from '../../../../../../../models/partys';

export const subheadings = { experts: 'Use of experts in court' };

export const radioButtons = {
  expertsRequired: {
    label: 'Do you want to use an expert?',
    yes: {
      selector2v1: '#applicantMPClaimExpertSpecRequired_Yes',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT) {
          return `#${claimantDefendantParty.oldKey}ClaimExpertSpecRequired_Yes`;
        }
        return `#responseClaimExpertSpecRequired${claimantDefendantParty.number === 1 ? '' : '2'}_Yes`;
      },
    },
    no: {
      selector2v1: '#applicantMPClaimExpertSpecRequired_No',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}ClaimExpertSpecRequired_No`;
        return `#responseClaimExpertSpecRequired${claimantDefendantParty.number === 1 ? '' : '2'}_No`;
      },
    },
  },
};

export const inputs = {
  expert: {
    firstName: {
      label: 'First name',
      hintText:
        'If the name is unknown at this time please add TBC to both the first name and last name lines.' +
        'Then use the Manage Contact Information event to provide the name when known',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}RespondToClaimExperts_firstName`;
        return `#respondToClaimExperts${claimantDefendantParty.number === 1 ? '' : '2'}_firstName`;
      },
    },
    lastName: {
      label: 'Last name',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}RespondToClaimExperts_lastName`;
        return `#respondToClaimExperts${claimantDefendantParty.number === 1 ? '' : '2'}_lastName`;
      },
    },
    phoneNumber: {
      label: 'Phone number (Optional)',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}RespondToClaimExperts_phoneNumber`;
        return `#respondToClaimExperts${claimantDefendantParty.number === 1 ? '' : '2'}_phoneNumber`;
      },
    },
    email: {
      label: 'Email address (Optional)',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}RespondToClaimExperts_emailAddress`;
        return `#respondToClaimExperts${claimantDefendantParty.number === 1 ? '' : '2'}_emailAddress`;
      },
    },
    expertise: {
      label: 'Field of expertise',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}RespondToClaimExperts_fieldofExpertise`;
        return `#respondToClaimExperts${claimantDefendantParty.number === 1 ? '' : '2'}_fieldofExpertise`;
      },
    },
    whyRequired: {
      label: 'Why do you need this expert?',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}RespondToClaimExperts_whyRequired`;
        return `#respondToClaimExperts${claimantDefendantParty.number === 1 ? '' : '2'}_whyRequired`;
      },
    },
    estimatedCost: {
      label: 'Estimated cost',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty.partyType === PartyType.CLAIMANT)
          return `#${claimantDefendantParty.oldKey}RespondToClaimExperts_estimatedCost`;
        return `#respondToClaimExperts${claimantDefendantParty.number === 1 ? '' : '2'}_estimatedCost`;
      },
    },
  },
};
