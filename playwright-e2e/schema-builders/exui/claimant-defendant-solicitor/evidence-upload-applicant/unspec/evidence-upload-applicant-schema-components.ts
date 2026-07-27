import { z } from 'zod';
import ClaimTrack from '../../../../../constants/cases/claim-track';

const nonEmptyString = z.string().min(1);

const documentArray = z.array(
  z.looseObject({
    id: nonEmptyString,
    value: z.looseObject({
      createdDatetime: nonEmptyString,
    }),
  }),
).min(1);

const documentUpload = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM)
    return {
      documentDisclosureList: documentArray,
      documentWitnessSummary: documentArray,
      documentJointStatement: documentArray,
      documentEvidenceForTrial: documentArray,
    };
  else if (claimTrack === ClaimTrack.SMALL_CLAIM)
    return {
      documentExpertReport: documentArray,
      documentWitnessStatement: documentArray,
      documentAuthorities: documentArray,
    }
};

export default {
  documentUpload,
};
