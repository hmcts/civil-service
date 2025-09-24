import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  uploadDefence: 'Upload defence',
};

export const inputs = {
  uploadDoc: {
    label: "Defendant's defence",
    selector: (defendantParty: Party) => `#${defendantParty.oldKey}ClaimResponseDocument_file`,
  },
};
