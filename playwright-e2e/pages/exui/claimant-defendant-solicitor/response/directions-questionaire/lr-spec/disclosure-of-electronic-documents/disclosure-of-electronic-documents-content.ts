import { Party } from '../../../../../../../models/partys';
import StringHelper from '../../../../../../../helpers/string-helper';

export const subheadings = { disclosureOfDocs: 'Disclosure of electronic documents' };

export const radioButtons = {
  disclosureOfElectronicDocs: {
    label:
      'Have you reached agreement, either using Electronic Documents Questionnaire in Practice Direction 31B or otherwise, about the scope and extent of disclosure of electronic documents on each side?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#spec${StringHelper.capitalise(claimantDefendantParty.oldKey)}DQDisclosureOfElectronicDocuments_reachedAgreement_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#spec${StringHelper.capitalise(claimantDefendantParty.oldKey)}DQDisclosureOfElectronicDocuments_reachedAgreement_No`,
    },
  },
  agreement: {
    label: 'Is such agreement likely?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#spec${StringHelper.capitalise(claimantDefendantParty.oldKey)}DQDisclosureOfElectronicDocuments_agreementLikely_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#spec${StringHelper.capitalise(claimantDefendantParty.oldKey)}DQDisclosureOfElectronicDocuments_agreementLikely_No`,
    },
  },
};

export const inputs = {
  disagreementReason: {
    label:
      'What are the issues about disclosure of electronic documents which the court needs to address, and should they be dealt with at the CMC or at a separate hearing?',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldKey)}DQDisclosureOfElectronicDocuments_reasonForNoAgreement`,
  },
};
