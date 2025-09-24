import { Party } from '../../../../../../../models/partys';

export const heading = 'Upload draft directions';

export const inputs = {
  uploadFile: {
    label: 'Upload File',
    hintText:
      'We accept documents sized 10MB or smaller, in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQDraftDirections`,
  },
};
