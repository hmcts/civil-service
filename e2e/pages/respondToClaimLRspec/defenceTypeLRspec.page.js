const {I} = inject();

module.exports = {
  fields: {
    DefenceType: {
      id: '#defenceRouteRequired',
      options: {
        hasPaid: 'Has paid the amount claimed',
        dispute: 'Disputes the claim'
      },
    },

   DefenceType2: {
          id: '#defenceRouteRequired2',
          options: {
            hasPaid: 'Has paid the amount claimed',
            dispute: 'Disputes the claim'
          },
    },
      howMuchWasPaid: '#respondToClaim_howMuchWasPaid',
      dayOfPayment: '#whenWasThisAmountPaid-day',
      monthOfPayment: '#whenWasThisAmountPaid-month',
      yearOfPayment: '#whenWasThisAmountPaid-year',
      HowWasThisAmountPaid: {
       id: '#respondToClaim_howWasThisAmountPaid',
       options: {
         creditCard: 'Credit card',
         cheque: 'Cheque',
         bacs: 'BACS',
         other: 'Other'
      },
   },
  },

  async selectDefenceType(twoDefendants,defenceType,amountPaid) {

   if (twoDefendants) {
      
     if (!this.fields.DefenceType2.options.hasOwnProperty(defenceType)) {
       throw new Error(`Response type: ${defenceType} does not exist`);
     }
     I.waitForElement(this.fields.DefenceType2.id);
     await I.runAccessibilityTest();
     await within(this.fields.DefenceType2.id, () => {
     I.click(this.fields.DefenceType2.options[defenceType]);
     });
    } else {
         
        if (!this.fields.DefenceType.options.hasOwnProperty(defenceType)) {
          throw new Error(`Response type: ${defenceType} does not exist`);
        }
        I.waitForElement(this.fields.DefenceType.id);
        await I.runAccessibilityTest();
        await within(this.fields.DefenceType.id, () => {
        I.click(this.fields.DefenceType.options[defenceType]);
        });
    }

   if ('hasPaid' === defenceType) {
      await I.fillField(this.fields.howMuchWasPaid,amountPaid);
      await I.fillField(this.fields.dayOfPayment, 1);
      await I.fillField(this.fields.monthOfPayment, 10);
      await I.fillField(this.fields.yearOfPayment, 2021);
      await I.click(this.fields.HowWasThisAmountPaid.options.creditCard);
    }

    await I.clickContinue();
  }
};
