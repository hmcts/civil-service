import { z } from 'zod';

const nonEmptyString = z.string().min(1);

const servedDocumentFiles = {
  servedDocumentFiles: z.looseObject({
    particularsOfClaimDocument: z.array(
      z.looseObject({
        value: z.looseObject({
          document_url: nonEmptyString,
          document_binary_url: nonEmptyString,
          document_filename: nonEmptyString,
        }),
      }),
    ).min(1),
  }),
};

const addOrAmendClaimDocumentsSchemaBuilderComponents = {
  servedDocumentFiles,
};

export default addOrAmendClaimDocumentsSchemaBuilderComponents;
