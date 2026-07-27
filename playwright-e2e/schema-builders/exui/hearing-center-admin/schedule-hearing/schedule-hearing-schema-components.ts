import { z } from 'zod';

const nonEmptyString = z.string().min(1);

const hearingNoticeSelect = {
  hearingNoticeList: nonEmptyString,
};

const listingOrRelisting = {
  listingOrRelisting: nonEmptyString,
};

const hearingDetails = {
  hearingLocation: z.looseObject({
    value: z.looseObject({ code: nonEmptyString, label: nonEmptyString }),
  }),
  channel: nonEmptyString,
  hearingDate: nonEmptyString,
  hearingTimeHourMinute: nonEmptyString,
  hearingDuration: nonEmptyString,
};

const hearingInformation = {
  information: nonEmptyString,
};

const hearingDocuments = {
  hearingDocuments: z.array(
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

const hearingFee = {
  hearingFee: z.looseObject({
    code: nonEmptyString,
    version: nonEmptyString,
    calculatedAmountInPence: nonEmptyString,
  }),
};

const hearingFeePBADetails = {
  hearingFeePBADetails: z.looseObject({
    fee: z.looseObject({
      code: nonEmptyString,
      version: nonEmptyString,
      calculatedAmountInPence: nonEmptyString,
    }),
    applicantsPbaAccounts: z.looseObject({
      list_items: z.array(z.looseObject({ code: nonEmptyString, label: nonEmptyString })).min(1),
    }),
    serviceRequestReference: nonEmptyString,
  }),
};

const hearingReferenceNumber = {
  hearingReferenceNumber: nonEmptyString,
  hearingDueDate: nonEmptyString,
};

export default {
  hearingNoticeSelect,
  listingOrRelisting,
  hearingDetails,
  hearingInformation,
  hearingDocuments,
  hearingFee,
  hearingFeePBADetails,
  hearingReferenceNumber,
};
