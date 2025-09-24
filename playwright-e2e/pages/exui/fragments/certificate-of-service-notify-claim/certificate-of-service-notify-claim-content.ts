import { Party } from '../../../../models/partys';
import StringHelper from '../../../../helpers/string-helper';

export const heading = (defendantParty: Party) => `Certificate of Service [${defendantParty.key}]`;

export const inputs = {
  dateOfService: {
    label: 'On what day did you serve?',
    selectorKey: 'cosDateOfServiceForDefendant',
  },
  dateDeemedServed: {
    label: 'The date of service is',
    selectorKey: 'cosDateDeemedServedForDefendant',
  },
  documentsServed: {
    label: 'What documents did you serve?',
    selector: (defendantParty: Party) =>
      `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosServedDocumentFiles`,
  },
  documentsServedLocation: {
    label: 'Where did you serve the documunts',
    selector: (defendantParty: Party) =>
      `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipientServeLocation`,
  },
  notifyClaimRecipient: {
    label: 'Who did you serve the claim to?',
    selector: (defendantParty: Party) =>
      `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipient`,
  },
  statementOfTruth: {
    name: {
      label: 'Your name',
      selector: (defendantParty: Party) =>
        `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosSender`,
    },
    firm: {
      label: 'Your firm',
      selector: (defendantParty: Party) =>
        `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosSenderFirm`,
    },
  },
  evidenceDocument: {
    label: 'Supporting evidence',
    selector: (defendantParty: Party) =>
      `#cosNotifyClaimDetails${StringHelper.capitalise(defendantParty.number.toString())}_cosEvidenceDocument_value`,
  },
};

export const dropdowns = {
  serveType: {
    label: 'How did you serve the documents?',
    selector: (defendantParty: Party) =>
      `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipientServeType`,
    options: [
      'Personally handed it to or left it with',
      'Delivered to or left at permitted place',
      'Sent by first class post or another service which delivers on the next business day',
      'Other means permitted by the court',
    ],
  },
  locationType: {
    label: 'Select the type of location where you served the documents',
    selector: (defendantParty: Party) =>
      `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipientServeLocationType`,
    options: [
      'Usual Residence',
      'Last known residence',
      'Place of business of the partnership/company/corporation within the jurisdiction with a connection to the claim',
      'Principal office of the company',
      'Principal office of the coropration',
      'Principal office of the partnership',
      'Last known principal place of business',
      'Principal place of business',
      'Place of business',
      'Email',
      'Other',
    ],
  },
};

export const radioButtons = {
  docsServed: {
    label: 'The location where you served the documents was the:',
    litigationFriend: {
      label: "litigation friend's",
      selector: (defendantParty: Party) =>
        `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipientServeLocationOwnerType-FRIEND`,
    },
    solicitor: {
      label: "solicitor's",
      selector: (defendantParty: Party) =>
        `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipientServeLocationOwnerType-SOLICITOR`,
    },
    defendant: {
      label: "defendant's",
      selector: (defendantParty: Party) =>
        `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipientServeLocationOwnerType-DEFENDANT`,
    },
    claimant: {
      label: "claimant's",
      selector: (defendantParty: Party) =>
        `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosRecipientServeLocationOwnerType-CLAIMANT`,
    },
  },
};

export const checkboxes = {
  signedTrue: {
    label: 'I believe that the facts stated in the certificate are true',
    selector: (defendantParty: Party) =>
      `#cosNotifyClaim${StringHelper.capitalise(defendantParty.key)}_cosUISenderStatementOfTruthLabel-CERTIFIED`,
  },
};

export const buttons = {
  addNewSupportingEvidence: {
    title: 'Add new',
    selector: (defendantParty: Party) =>
      `div[id='cosNotifyClaimDetails${defendantParty.number}_cosEvidenceDocument'] button[class='button write-collection-add-item__top']`,
  },
};
