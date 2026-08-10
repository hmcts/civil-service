import NotSuitableSdoOption from "../../../../constants/ccd-events/non-suitable-sdo/not-suitable-sdo-option";

const notSuitableSdo = (notSuitableSdoOption: NotSuitableSdoOption) => {
  if (notSuitableSdoOption === NotSuitableSdoOption.CHANGE_LOCATION) {
    return {
      NotSuitableSDO: {
        notSuitableSdoOptions: notSuitableSdoOption,
        tocTransferCaseReason: {
          reasonForCaseTransferJudgeTxt: 'Reason for transferring case',
        },
      }
    }
  }
  else if (notSuitableSdoOption === NotSuitableSdoOption.OTHER_REASONS) {
    return {
      NotSuitableSDO: {
        notSuitableSdoOptions: notSuitableSdoOption,
        reasonNotSuitableSDO: {
          input: 'Other reason for not suitable SDO',
        },
      }
    }
  }

  return {};
};

const notSuitableSdoDataBuilderComponents = {
  notSuitableSdo,
};

export default notSuitableSdoDataBuilderComponents;
