const {I} = inject();

module.exports = {

  async verifyBundleDetails() {
    I.waitInUrl('#Bundles', 10);
    I.see('Bundle name');
    I.see('Document Uploaded DateTime',);
    I.see('Hearing date');
    I.see('Upload a file');
    I.seeNumberOfElements('.complex-panel-table tbody .new-table-row', 2);
    I.see('Test bundle name');
    I.see('1 Jan 2026',);
    I.see('01-01-2026-Test bundle name.pdf');
    I.see('Test bundle name 1');
    I.see('10 Oct 2027');
    I.see('10-10-2027-Test bundle name 1.pdf');
  },
};
