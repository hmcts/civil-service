import { z } from 'zod';
import partys from '../../../../../constants/users/partys';
import { Party } from '../../../../../models/users/partys';

const nonEmptyString = z.string().min(1);

const extensionDate = (defendantSolicitorParty: Party = partys.DEFENDANT_SOLICITOR_1) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondentSolicitor1AgreedDeadlineExtension: nonEmptyString,
    };
  }
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondentSolicitor2AgreedDeadlineExtension: nonEmptyString,
    };
  }

  return {};
};

const undefine = {
  isRespondent1: z.undefined().optional(),
}

const informAgreedExtensionDateSchemaComponents = {
  extensionDate,
  undefine,
};

export default informAgreedExtensionDateSchemaComponents;
