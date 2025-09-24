import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  uploadEvidence: 'Upload supporting evidence (optional)',
};

export const inputs = {
  disputeReason: {
    label: '',
    selector: (defendantParty: Party) =>
      `#detailsOfWhyDoesYouDisputeTheClaim${defendantParty.number === 1 ? '' : defendantParty.number}`,
  },
  uploadEvidence: {
    label: '',
    selector: (defendantParty: Party) =>
      `#${defendantParty.oldKey}SpecDefenceResponseDocument_file`,
  },
};
