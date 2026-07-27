import { z } from 'zod';
import partys from '../../../../constants/users/partys';
import { Party } from '../../../../models/users/partys';

const nonEmptyString = z.string().min(1);

const claimant1Party = () => ({});

const respondent1LRIndividuals = (party: Party) => {
  if (party === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1LRIndividuals: z.array(
        z.strictObject({
          id: nonEmptyString,
          value: z.strictObject({
            email: nonEmptyString,
            flags: z.strictObject({
              partyName: nonEmptyString,
              roleOnCase: nonEmptyString,
            }),
            phone: nonEmptyString,
            partyID: nonEmptyString,
            lastName: nonEmptyString,
            firstName: nonEmptyString,
          }),
        }),
      ).min(1),
    };
  }

  return {};
};

const manageContactInformationSchemaComponents = {
  claimant1Party,
  respondent1LRIndividuals,
};

export default manageContactInformationSchemaComponents;
