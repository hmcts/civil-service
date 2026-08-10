const {I} = inject();
const {incrementDate, formatDateToString} = require('../api/dataHelper');

module.exports = {

  async verifyBundleDetails() {
    const futureDate = incrementDate(new Date(), 0, 1, 0);
    I.waitInUrl('#Bundles', 10);
    I.see('Bundle name');
    I.see('Document Uploaded DateTime',);
    I.see('Hearing date');
    I.see('Upload a file');
    I.seeNumberOfElements('.complex-panel-table tbody .new-table-row', 2);
    I.see('Test bundle name');
    I.see(formatDateToString(futureDate, 'DD Mon YYYY'));
    I.see(`${formatDateToString(futureDate, 'DD-MM-YYYY')}-Test bundle name.pdf`);
    I.see('Test bundle name 1');
    I.see(formatDateToString(futureDate, 'DD Mon YYYY'));
    I.see(`${formatDateToString(futureDate, 'DD-MM-YYYY')}-Test bundle name 1.pdf`);
  },
};
