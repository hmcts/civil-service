const {date} = require('../../../api/dataHelper');

module.exports = {
  event: 'INITIATE_GENERAL_APPLICATION_COSC',
  caseDataUpdate: {
    generalAppType: {
      types: [
        'CONFIRM_CCJ_DEBT_PAID'
      ]
    },
    generalAppRespondentAgreement: {
      hasAgreed: 'No'
    },
    certOfSC: {
      debtPaymentEvidence: {
        provideDetails: 'details',
        debtPaymentOption: 'UNABLE_TO_PROVIDE_EVIDENCE_OF_FULL_PAYMENT'
      },
      defendantFinalPaymentDate: date()
    },
    generalAppStatementOfTruth: {
      name: 'test',
    },
    generalAppInformOtherParty: {
      isWithNotice: 'No',
      reasonsForWithoutNotice: 'reason'
    }
  }
};
