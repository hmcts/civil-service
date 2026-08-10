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
      documentDisclosureListRes: documentArray,
      documentWitnessSummaryRes: documentArray,
      documentQuestionsRes: documentArray,
      documentAuthoritiesRes: documentArray,
    };
  else if (claimTrack === ClaimTrack.SMALL_CLAIM)
    return {
      documentExpertReportRes: documentArray,
      documentWitnessStatementRes: documentArray,
      documentAuthoritiesRes: documentArray,
    }
};

export default {
  documentUpload,
};
