import { Party } from '../../../../../../../models/partys';

export const heading = 'Upload claim timeline template';

export const inputs = {
  upload: {
    label: 'Upload files',
    selector: (defendantParty: Party) => {
      if (defendantParty.number === 1) {
        return '#specResponseTimelineDocumentFiles';
      } else {
        return '#specResponsTimelineDocumentFiles2';
      }
    },
  },
};
