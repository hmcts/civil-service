
module.exports = {
  userInput: {
    defendantDetailsSpec: {
      defendantDetailsSpec: {
        value: {
          code: '62ff8ded-ab50-47a6-894e-c101fb56a89f',
          label: 'Both defendants'
        },
        list_items: [
          {
            code: '62ff8ded-ab50-47a6-894e-c101fb56a89f',
            label: 'Sir John Doe'
          },
          {
            code: '62ff8ded-ab50-47a6-894e-c101fb56a89f',
            label: 'Dr Foo Bar'
          },
          {
            code: '62ff8ded-ab50-47a6-894e-c101fb56a89f',
            label: 'Both defendants'
          }
        ]
      },
      bothDefendantsSpec: 'Both defendants',
      currentDefendant: 'Have the defendants paid some of the amount owed?',
      currentDefendantName: 'both defendants',
      businessProcess: {
        camundaEvent: 'CREATE_CLAIM_SPEC',
        status: 'FINISHED'
      }
    },
    paymentConfirmationSpec: {
      repaymentSummaryObject: 'The judgment will order the defendants to pay £1580.00, including the claim fee and interest, if applicable, as shown:\n### Claim amount \n £1500.00\n### Claim fee amount \n £80.00\n ## Subtotal \n £1580.00\n\n ## Total still owed \n £1580.00'
    },
    paymentType: {
      currentDatebox: '25 August 2022',
      repaymentDue: '1580.00',
      paymentTypeSelection : 'IMMEDIATELY'
    },
    paymentSetDate: {
    },
    repaymentInformation: {
      repaymentDue: '1580.00',
      repaymentSuggestion: '3',
      repaymentDate: '2220-01-01'
    }


  }
};
