const {I} = inject();

module.exports = {

  fields: {
    pbaNumber: {
      id: '#pbaAccountNumber',
      options: {
        activeAccount1: 'PBA0088192',
        activeAccount2: 'PBA0078095'
      }
    },
    reviewLinks: '.govuk-table__body td a',
    serviceRequestTable: 'table.govuk-table',
  },

  async verifyPaymentDetails() {
    await I.waitInUrl('#Service', 5);
    I.waitForVisible(this.fields.reviewLinks, 8);
    I.click(this.fields.reviewLinks);
    I.see('Paid');
    I.see('General application');
    I.see('Total fees to pay');
  },

  async payGAAmount() {
    await I.wait(25);
    await I.waitInUrl('#Service', 5);
    await I.waitForClickable(locate('.ServiceRequestTab .govuk-table a').first(), 30);
    await I.seeTextEquals('Not paid', locate('td.govuk-table__cell').first());
    I.click('Pay now');
    I.click({css: 'input#pbaAccount'});
    I.waitForElement(this.fields.pbaNumber.id);
    I.selectOption(this.fields.pbaNumber.id, this.fields.pbaNumber.options['activeAccount1']);
    I.fillField('pbaAccountRef', 'Test Test');
    I.click({css: 'div.govuk-form-group span'});
    I.click('Confirm payment');
    await I.waitForText('Payment successful');
    I.wait(5);
    I.click('View service requests');
  }
};
