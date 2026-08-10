import CaseDataHelper from '../../../../../helpers/case-data-helper';
import { UploadDocumentValue } from '../../../../../models/ccd-case-data';
import partys from '../../../../../constants/users/partys';

const defendantLitigationFriend = (certificateOfSuitability: UploadDocumentValue) => ({
  DefendantLitigationFriend: {
    isRespondent1: 'Yes',
    respondent1LitigationFriend: {
      ...CaseDataHelper.buildLitigationFriendData(partys.DEFENDANT_1_LITIGATION_FRIEND),
      certificateOfSuitability: [CaseDataHelper.setIdToData({ document: certificateOfSuitability })],
      partyName: undefined,
    },
  },
});

const addDefendantLitigationFriendDataComponents = {
  defendantLitigationFriend,
};

export default addDefendantLitigationFriendDataComponents;
