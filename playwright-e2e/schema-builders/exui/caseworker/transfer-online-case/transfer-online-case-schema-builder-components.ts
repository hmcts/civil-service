import { z } from 'zod';

const nonEmptyString = z.string().min(1);

const transferOnlineCase = {
  transferCourtLocationList: z.strictObject({
    value: z.strictObject({
      code: nonEmptyString,
      label: nonEmptyString,
    }),
  }),
  reasonForTransfer: nonEmptyString,
};

const transferOnlineCaseSchemaBuilderComponents = {
  transferOnlineCase,
};

export default transferOnlineCaseSchemaBuilderComponents;
