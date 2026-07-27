import { z } from 'zod';
import NotSuitableSdoOption from '../../../../constants/ccd-events/non-suitable-sdo/not-suitable-sdo-option';

const nonEmptyString = z.string().min(1);

const notSuitableSdo = (notSuitableSdoOption: NotSuitableSdoOption) => {
  if (notSuitableSdoOption === NotSuitableSdoOption.CHANGE_LOCATION) {
    return {
      notSuitableSdoOptions: z.literal(NotSuitableSdoOption.CHANGE_LOCATION),
      tocTransferCaseReason: z.strictObject({
        reasonForCaseTransferJudgeTxt: nonEmptyString,
      }),
    };
  }

  if (notSuitableSdoOption === NotSuitableSdoOption.OTHER_REASONS) {
    return {
      notSuitableSdoOptions: z.literal(NotSuitableSdoOption.OTHER_REASONS),
      reasonNotSuitableSDO: z.strictObject({
        input: nonEmptyString,
      }),
    };
  }

  return {};
};

const notSuitableSdoSchemaBuilderComponents = {
  notSuitableSdo,
};

export default notSuitableSdoSchemaBuilderComponents;
