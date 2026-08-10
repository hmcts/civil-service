const DEFAULT_JUDGEMENT_TOTAL = 1702;
const DEFAULT_JUDGEMENT_TOTAL_POUNDS = DEFAULT_JUDGEMENT_TOTAL.toFixed(2);
const DEFAULT_JUDGEMENT_SUMMARY = `The judgment will order Sir John Doe to pay £${DEFAULT_JUDGEMENT_TOTAL_POUNDS}, including the claim fee and interest, if applicable, as shown:\n### Claim amount \n £1500.00\n ### Fixed cost amount \n£122.00\n### Claim fee amount \n £80.00\n ## Subtotal \n £${DEFAULT_JUDGEMENT_TOTAL_POUNDS}\n\n ## Total still owed \n £${DEFAULT_JUDGEMENT_TOTAL_POUNDS}`;

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
      defaultJudgementOverallTotal: DEFAULT_JUDGEMENT_TOTAL,
      businessProcess: {
        camundaEvent: 'CREATE_CLAIM_SPEC_AFTER_PAYMENT',
        status: 'FINISHED'
      }
    },
    claimPartialPayment : {
      partialPayment: 'No'
    },
    fixedCostsOnEntry: {
      claimFixedCostsOnEntryDJ: 'Yes',
      repaymentSummaryObject: DEFAULT_JUDGEMENT_SUMMARY
    },
    paymentConfirmationSpec: {
      repaymentSummaryObject: DEFAULT_JUDGEMENT_SUMMARY
    },
    paymentType: {
      currentDatebox: '25 August 2022',
      repaymentDue: DEFAULT_JUDGEMENT_TOTAL_POUNDS,
      paymentTypeSelection : 'IMMEDIATELY'
    },
    paymentSetDate: {
    },
    repaymentInformation: {
      repaymentDue: DEFAULT_JUDGEMENT_TOTAL_POUNDS,
      repaymentSuggestion: '3',
      repaymentDate: '2220-01-01'
    }
  }
};
