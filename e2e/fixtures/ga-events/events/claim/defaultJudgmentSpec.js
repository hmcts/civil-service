module.exports = {
  userInput: {
    defendantDetailsSpec: {
      defendantDetailsSpec: {
        value: {
          code: '62ff8ded-ab50-47a6-894e-c101fb56a89f',
          label: 'Sir John Doe'
        },
        list_items: [
          {
            code: '62ff8ded-ab50-47a6-894e-c101fb56a89f',
            label: 'Sir John Doe'
          }
        ]
      },
      bothDefendantsSpec: 'One',
      currentDefendant: 'Has Sir John Doe paid some of the amount owed?',
      currentDefendantName: 'Sir John Doe',
      businessProcess: {
        camundaEvent: 'CREATE_CLAIM_SPEC_AFTER_PAYMENT',
        status: 'FINISHED'
      }
    },
    showCertifyStatementSpec: {
      CPRAcceptance: {
        acceptance: ['CERTIFIED']
      }
    },
    claimPartialPayment : {
      partialPayment: 'No',
      showOldDJFixedCostsScreen: 'Yes',
      currentDefendantName: 'Sir John Doe'
    },
    paymentConfirmationSpec: {
      paymentConfirmationDecisionSpec: 'No',
      defaultJudgementOverallTotal: 1580,
    },
    paymentType: {
      currentDatebox: '25 August 2022',
      repaymentDue: '1580',
      paymentTypeSelection : 'IMMEDIATELY'
    },
    repaymentInformation: {
      repaymentDue: '1580.00',
      repaymentSuggestion: '3',
      repaymentDate: '2220-01-01'
    }
  }
};
