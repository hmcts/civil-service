const { waitForFinishedBusinessProcess } = require('../../api/testingSupport');

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
    reviewLinks: '.govuk-table__body td a'
  },

  async verifyAdditionalPayment(caseNumber) {
    I.waitInUrl(caseNumber);
    I.seeNumberOfVisibleElements(this.fields.reviewLinks, 2);
    I.click(locate(this.fields.reviewLinks).last());
    I.see('Paid');
    I.see('General application (on notice)');
    I.see('Total fees to pay: £167.00');
  },

  async payFee(caseNumber, hearingFee = false) {
    let feePaymentXPath;
    await I.waitInUrl(caseNumber);
    await I.waitForText('Not paid', 30);
    await I.see('Not paid');
    if (hearingFee) {
      feePaymentXPath = '//table/tbody/tr[position()=2]/td[position()=5]/a[contains(text(), \'Pay now\')]';
    } else {
      feePaymentXPath = '//table/tbody/tr[position()=1]/td[position()=5]/a[contains(text(), \'Pay now\')]';
    }
    await I.forceClick(feePaymentXPath);
    await I.waitForElement('#pbaAccount');
    await I.click({css: 'input#pbaAccount'});
    await I.waitForElement(this.fields.pbaNumber.id);
    await I.selectOption(this.fields.pbaNumber.id, this.fields.pbaNumber.options['activeAccount2']);
    await I.fillField('#pbaAccountRef', 'Test Test');
    await I.click({css: 'div.govuk-form-group span'});
    await I.click('Confirm payment');
    await I.waitForText('Payment successful');
    await I.click('View service requests');
  },

  async openServiceRequestTab() {
    let urlBefore = await I.grabCurrentUrl();
    console.log('openServiceRequestTab urlBefore ..', urlBefore);
    await I.refreshPage();
    await I.waitForVisible(locate('div.mat-tab-label-content').withText('Service Request'), 60);

    await I.retryUntilUrlChanges(async () => {
      await I.forceClick(locate('div.mat-tab-label-content').withText('Service Request'));
      await I.waitForInvisible(locate(this.fields.spinner).withText('Loading'), 30);
    }, urlBefore);
  }
};
