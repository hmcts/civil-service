import partys from '../../../../../../../constants/partys';
import StringHelper from '../../../../../../../helpers/string-helper';
import { Party } from '../../../../../../../models/partys';

export const heading = "Defendant's legal representative's reference (optional)";

export const inputs = {
  defendant1DateOfBirth: {
    label: "Defendant's date of birth (Optional)",
  },
  defendant2DateOfBirth: {
    label: "Second defendant's date of birth (Optional)",
  },
};

export const tableHeadings = {
  organisation: 'Organisation',
  reference: 'Reference',
};

export const radioButtons = {
  address: {
    label: 'Is this address correct?',
    yes: {
      label: 'Yes',
      selector: (defendantParty: Party) =>
        `#specAoS${StringHelper.capitalise(defendantParty.oldPartyType)}${defendantParty === partys.DEFENDANT_1 ? '' : defendantParty.number}CorrespondenceAddressRequired_Yes`,
    },
    no: {
      label: 'No',
      selector: (defendantParty: Party) =>
        `#specAoS${StringHelper.capitalise(defendantParty.oldPartyType)}${defendantParty === partys.DEFENDANT_1 ? '' : defendantParty.number}CorrespondenceAddressRequired_No`,
    },
  },
};
