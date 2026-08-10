const {I} = inject();

module.exports = {

  fields: {
    confirmation: {
      id: '#confirmation-body'
    },
    applicationList: '#confirmation-body li'
  },

  async verifyFFConfirmationPage() {
    await I.waitInUrl('GENERATE_DIRECTIONS_ORDER/confirm');
    I.see('The order has been sent to:');
    I.see('Test Inc');
    I.see('Sir John Doe');
  },

  async verifyAOConfirmationPage() {
    await I.waitInUrl('GENERATE_DIRECTIONS_ORDER/confirm');
    I.see('The order has been sent to:');
    I.see('Test Inc');
    I.see('Sir John Doe');
    I.see('Dr Foo Bar');
  },

  async closeAndReturnToCaseDetails() {
    await I.click('Close and Return to case details');
  }
};

