const {I} = inject();

module.exports = {

  fields: {
    defendant1: {
      id: '#respondent1_respondent1',
      primaryAddress: '#respondent1_primaryAddress_primaryAddress',
      element: {
        addressLine1: '#respondent1_primaryAddress__detailAddressLine1',
        addressLine2: '#respondent1_primaryAddress__detailAddressLine2',
        addressLine3: '#respondent1_primaryAddress__detailAddressLine3',
        city: '#respondent1_primaryAddress__detailPostTown',
        postcode: '#respondent1_primaryAddress__detailPostCode',
        email: '#respondent1_partyEmail',
        phone: '#respondent1_partyPhone'
      }
    },
  },

  async editAddress() {
    I.waitForElement(this.fields.defendant1.id);
    await I.runAccessibilityTest();

    I.waitForElement(this.fields.defendant1.primaryAddress);
    I.fillField(this.fields.defendant1.element.addressLine1, 'Flat 6');
    I.fillField(this.fields.defendant1.element.addressLine2, 'Number 10');
    I.fillField(this.fields.defendant1.element.addressLine3, 'Upping Street');
    I.fillField(this.fields.defendant1.element.city, 'London');
    I.fillField(this.fields.defendant1.element.postcode, 'SW1A 2AA');
    I.fillField(this.fields.defendant1.element.email, 'updatedemail@newmail.com');
    I.fillField(this.fields.defendant1.element.phone, '07777777777');
    await I.clickContinue();
  },
};
