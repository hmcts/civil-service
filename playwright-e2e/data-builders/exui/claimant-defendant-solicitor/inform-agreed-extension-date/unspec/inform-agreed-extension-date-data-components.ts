import partys from '../../../../../constants/users/partys';
import DateHelper from '../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { Party } from '../../../../../models/users/partys';

const extensionDate = (
  ccdCaseData: CCDCaseData,
  defendantSolicitorParty: Party = partys.DEFENDANT_SOLICITOR_1,
) => {
  const responseDeadline = defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
    ? ccdCaseData.respondent2ResponseDeadline!
    : ccdCaseData.respondent1ResponseDeadline!;

  const agreedDeadlineExtension = DateHelper.formatDateToString(
    DateHelper.addToDate(responseDeadline, {
      days: 28,
      workingDay: true,
    }),
    { outputFormat: 'YYYY-MM-DD' },
  );

  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      ExtensionDate: {
        respondentSolicitor1AgreedDeadlineExtension: agreedDeadlineExtension,
      },
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      ExtensionDate: {
        respondentSolicitor2AgreedDeadlineExtension: agreedDeadlineExtension,
      },
    };
  }
}

const informAgreedExtensionDateDataComponents = {
  extensionDate,
};

export default informAgreedExtensionDateDataComponents;
