import DateHelper from '../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';

const extensionDate = (ccdCaseData: CCDCaseData) => {
  const extensionDate = DateHelper.formatDateToString(DateHelper.addToDate(ccdCaseData.respondent1ResponseDeadline!, {
    days: 28,
    workingDay: true,
  }), { outputFormat: 'YYYY-MM-DD' });

  return {
    ExtensionDate: {
      respondentSolicitor1AgreedDeadlineExtension: extensionDate,
    },
  };
};

const informAgreedExtensionDateSpecData = {
  extensionDate,
};

export default informAgreedExtensionDateSpecData;
