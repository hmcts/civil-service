import { Party } from '../../../../../../../models/partys';

export const subheadings = { welsh: 'Welsh language' };

export const paragraphs = {
  descriptionText:
    'Welsh is an official language of Wales. ' +
    'You can use Welsh in court hearings. ' +
    'Asking to speak in Welsh in your hearing will not delay the hearing or have any effect on proceedings or the outcome of a case.',
};

export const radioButtons = {
  courtLanguage: {
    label: 'What language will you, your experts or witnesses speak at the hearing?',
    welsh: {
      label: 'Welsh',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQLanguage_court-WELSH`,
    },
    english: {
      label: 'English',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQLanguage_court-ENGLISH`,
    },
    welshAndEnglish: {
      label: 'Welsh and English',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQLanguage_court-BOTH`,
    },
  },
  documentLanguage: {
    label: 'What language will documents be provided in?',
    welsh: {
      label: 'Welsh',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQLanguage_documents-WELSH`,
    },
    english: {
      label: 'English',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQLanguage_documents-ENGLISH`,
    },
    welshAndEnglish: {
      label: 'Welsh and English',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQLanguage_documents-BOTH`,
    },
  },
};
