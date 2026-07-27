import { z } from 'zod';
import ClaimType from '../../../../../constants/cases/claim-type';
import partys from '../../../../../constants/users/partys';
import { Party } from '../../../../../models/users/partys';

const nonEmptyString = z.string().min(1);

const responseIntention = (
  claimType: ClaimType = ClaimType.ONE_VS_ONE,
  defendantSolicitorParty: Party = partys.DEFENDANT_SOLICITOR_1,
) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (claimType === ClaimType.TWO_VS_ONE) {
      return {
        respondent1ClaimResponseIntentionType: nonEmptyString,
        respondent1ClaimResponseIntentionTypeApplicant2: nonEmptyString,
      };
    }

    if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
      return {
        respondent1ClaimResponseIntentionType: nonEmptyString,
        respondent2ClaimResponseIntentionType: nonEmptyString,
      };
    }

    return {
      respondent1ClaimResponseIntentionType: nonEmptyString,
    };
  }

  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2ClaimResponseIntentionType: nonEmptyString,
    };
  }

  return {};
};

const responseDates = (defendantSolicitorParty: Party = partys.DEFENDANT_SOLICITOR_1) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2AcknowledgeNotificationDate: nonEmptyString,
      respondent2ResponseDeadline: nonEmptyString,
    };
  }

  return {
    respondent1AcknowledgeNotificationDate: nonEmptyString,
    respondent1ResponseDeadline: nonEmptyString,
  };
};

const solicitorReferences = (defendantSolicitorParty: Party = partys.DEFENDANT_SOLICITOR_1) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondentSolicitor2Reference: nonEmptyString,
    };
  }

  return {};
};

const acknowledgeClaimSchemaComponents = {
  responseIntention,
  responseDates,
  solicitorReferences,
};

export default acknowledgeClaimSchemaComponents;
