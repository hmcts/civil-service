import preferredCourts from '../../../../config/preferred-courts';
import partys from '../../../../constants/users/partys';
import CaseDataHelper from '../../../../helpers/case-data-helper';

const transferOnlineCase = {
  TransferOnlineCase: {
    transferCourtLocationList: {
      value: CaseDataHelper.setCodeToData(preferredCourts.transfer.default),
      list_items: [
        CaseDataHelper.setCodeToData(preferredCourts[partys.CLAIMANT_1.key].default),
      ],
    },
    reasonForTransfer: 'Allocated court location is not appropriate',
  },
};

const transferOnlineCaseDataBuilderComponents = {
  transferOnlineCase,
};

export default transferOnlineCaseDataBuilderComponents;
