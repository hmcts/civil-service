import { UploadDocumentValue } from '../../../../../models/ccd-case-data';
import CaseDataHelper from '../../../../../helpers/case-data-helper';

const upload = (particularsOfClaimDocument: UploadDocumentValue) => ({
  Upload: {
    servedDocumentFiles: {
      particularsOfClaimDocument: [
        CaseDataHelper.setIdToData(particularsOfClaimDocument)
      ],
    },
  },
});

const addOrAmendClaimDocumentsDataBuilderComponents = {
  upload,
};

export default addOrAmendClaimDocumentsDataBuilderComponents;
