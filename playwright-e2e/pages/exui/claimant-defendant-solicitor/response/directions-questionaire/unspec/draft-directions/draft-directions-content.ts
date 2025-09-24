import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  draft: 'Upload draft directions',
};

export const inputs = {
  uploadFile: {
    label: 'Upload file',
    hintText:
      'We accept documents sized 10MB or smaller, in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQDraftDirections`,
  },
};

export const buttons = {
  cancelUpload: {
    title: 'Cancel Upload',
    selector: 'button[aria-label="Cancel upload"]',
  },
};
