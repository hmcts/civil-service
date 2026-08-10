import { z } from 'zod';

const nonEmptyString = z.string().min(1);

const email = {
  respondentSolicitor1EmailAddress: nonEmptyString,
  applicantSolicitor1UserDetails: z.strictObject({
    email: nonEmptyString,
  }),
};

const amendPartyDetailsSchemaBuilderComponents = {
  email,
};

export default amendPartyDetailsSchemaBuilderComponents;
