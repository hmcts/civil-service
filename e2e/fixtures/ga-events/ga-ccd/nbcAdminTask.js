const {date} = require('../../../api/dataHelper');
module.exports = {
  nbcAdminReferToJudgeData: () => {
    return {
      referToJudge: {
        judgeReferEventDescription: 'Testing refer to Judge',
        judgeReferAdditionalInfo: 'sample data'
      }
    };
  },
  nbcAdminReferToLegalAdvisorData: () => {
    return {
      referToLegalAdvisor: {
        legalAdvisorEventDescription: 'Testing refer to LegalAdvisor',
        legalAdvisorAdditionalInfo: 'sample data'
      }
    };
  },
  nbcAdminApproveConsentOrderData: () => {
    return {
      approveConsentOrder: {
        consentOrderDescription: 'Testing Approve Consent Order',
        showConsentOrderDate: 'No',
        isOrderProcessedByStayScheduler: 'No',
        consentOrderDateToEnd: date(0),
      }
    };
  }
};
