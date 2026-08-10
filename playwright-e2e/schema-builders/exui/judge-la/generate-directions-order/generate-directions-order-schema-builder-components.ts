import { z } from 'zod';
import ClaimTrack from '../../../../constants/cases/claim-track';

const nonEmptyString = z.string().min(1);

const finalOrderDocumentCollection = (claimTrack: ClaimTrack) => {
  if (
    claimTrack === ClaimTrack.FAST_CLAIM ||
    claimTrack === ClaimTrack.SMALL_CLAIM
  ) {
    return {
      finalOrderDocumentCollection: z.array(
        z.looseObject({
          id: nonEmptyString,
          value: z.looseObject({
            createdBy: nonEmptyString,
            documentLink: z.looseObject({
              category_id: nonEmptyString,
              document_url: nonEmptyString,
              upload_timestamp: nonEmptyString,
              document_filename: nonEmptyString,
              document_binary_url: nonEmptyString,
            }),
            documentName: nonEmptyString,
            documentSize: z.number(),
            documentType: nonEmptyString,
            createdDatetime: nonEmptyString,
          }),
        }),
      ).min(1),
    };
  } else if (
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
    claimTrack === ClaimTrack.MULTI_CLAIM) {
      return {
        finalOrderDocumentCollection: z.array(
          z.looseObject({
            id: nonEmptyString,
            value: z.looseObject({
              documentLink: z.looseObject({
                category_id: nonEmptyString,
                document_url: nonEmptyString,
                upload_timestamp: nonEmptyString,
                document_filename: nonEmptyString,
                document_binary_url: nonEmptyString,
              }),
              documentName: nonEmptyString,
              documentSize: z.number(),
              documentType: nonEmptyString,
              createdDatetime: nonEmptyString,
            }),
          }),
        ).min(1),
      };
    }

  return {};
};

export default {
  finalOrderDocumentCollection,
};
