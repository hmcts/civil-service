import { z } from 'zod';
import { Party } from '../../../../../models/users/partys';
import partys from '../../../../../constants/users/partys';

const uploadedDocument = z.looseObject({
  category_id: z.string(),
  document_url: z.string(),
  upload_timestamp: z.string(),
  document_binary_url: z.string(),
  document_filename: z.string(),
});

const mediationDocumentsReferred = (claimantDefendantSolicitorParty: Party) => {
  if (claimantDefendantSolicitorParty === partys.CLAIMANT_SOLICITOR_1)
    return {
      app1MediationDocumentsReferred: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            document: uploadedDocument,
            documentDate: z.string(),
            documentType: z.string(),
            documentUploadedDatetime: z.string(),
          }),
        }),
      ).min(1),
    };

  else if (claimantDefendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1)
    return {
      res1MediationDocumentsReferred: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            document: uploadedDocument,
            documentDate: z.string(),
            documentType: z.string(),
            documentUploadedDatetime: z.string(),
          }),
        }),
      ).min(1),
    };

  else if (claimantDefendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2)
    return {
      res2MediationDocumentsReferred: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            document: uploadedDocument,
            documentDate: z.string(),
            documentType: z.string(),
            documentUploadedDatetime: z.string(),
          }),
        }),
      ).min(1),
    };

  return {};
};

const mediationNonAttendanceDocs = (claimantDefendantSolicitorParty: Party) => {
  if (claimantDefendantSolicitorParty === partys.CLAIMANT_SOLICITOR_1)
    return {
      app1MediationNonAttendanceDocs: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            document: uploadedDocument,
            yourName: z.string(),
            documentDate: z.string(),
            documentUploadedDatetime: z.string(),
          }),
        }),
      ).min(1),
    };

  else if (claimantDefendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1)
    return {
      res1MediationNonAttendanceDocs: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            document: uploadedDocument,
            yourName: z.string(),
            documentDate: z.string(),
            documentUploadedDatetime: z.string(),
          }),
        }),
      ).min(1),
    };

  else if (claimantDefendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2)
    return {
      res2MediationNonAttendanceDocs: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            document: uploadedDocument,
            yourName: z.string(),
            documentDate: z.string(),
            documentUploadedDatetime: z.string(),
          }),
        }),
      ).min(1),
    };

  return {};
};

const uploadMediationDocumentsSchemaComponents = {
  mediationDocumentsReferred,
  mediationNonAttendanceDocs,
};

export default uploadMediationDocumentsSchemaComponents;
