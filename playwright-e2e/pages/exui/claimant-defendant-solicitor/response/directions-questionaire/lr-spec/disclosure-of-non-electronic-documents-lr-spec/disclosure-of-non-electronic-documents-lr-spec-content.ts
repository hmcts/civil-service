import StringHelper from '../../../../../../../helpers/string-helper';
import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  disclosureOfDocs: 'Disclosure of non-electronic documents (Optional)',
};

export const inputs = {
  bespokeDirections: {
    label: 'What directions are proposed for disclosure? (Optional)',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldKey)}DQDisclosureOfNonElectronicDocuments_bespokeDirections`,
  },
};
