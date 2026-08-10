const {I} = inject();

module.exports = {
  fields: {
    partAdmitType: {
      id: '#specDefenceAdmittedRequired_radio',
      options: {
        no: '#specDefenceAdmittedRequired_no',
        yes: '#specDefenceAdmittedRequired_yes'
      },
    },
      claimOwingAmount: '#respondToAdmittedClaimOwingAmount',
      howMuchWasPaid: '#respondToAdmittedClaim_howMuchWasPaid',
      dayOfPayment: '#whenWasThisAmountPaid-day',
      monthOfPayment: '#whenWasThisAmountPaid-month',
      yearOfPayment: '#whenWasThisAmountPaid-year',
      HowWasThisAmountPaid: {
       id: '#respondToAdmittedClaim_howWasThisAmountPaid-CREDIT_CARD',
       options: {
         creditCard: 'Credit card',
         cheque: 'Cheque',
         bacs: 'BACS',
         other: 'Other'
      },
   },
  },

  async selectAdmitType(hasPaid) {
     
    I.waitForElement(this.fields.partAdmitType.id);
    await I.runAccessibilityTest();
    await within(this.fields.partAdmitType.id, () => {
    I.click(this.fields.partAdmitType.options[hasPaid]);
    //await I.fillField(this.fields.claimOwingAmount, 100);
    });
    if ('yes' === hasPaid) {
      await I.fillField(this.fields.howMuchWasPaid,10);
      await I.fillField(this.fields.dayOfPayment, 14);
      await I.fillField(this.fields.monthOfPayment, 2);
      await I.fillField(this.fields.yearOfPayment, 2023);
      await I.click(this.fields.HowWasThisAmountPaid.options.creditCard);
    }

    await I.clickContinue();
  }
};
