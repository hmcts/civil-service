const I = actor();
const expect = require('chai').expect;
const {docFullDate} = require('../generalAppCommons');

module.exports = {

  fields: {
    appFolder: 'button[aria-label*="Applications"] .node__count',
    orderFolder: 'button[aria-label*="Orders"] .node__count',
    expandedFolder: '[aria-expanded="true"] .node-name-document',
    nameFolder: 'button[aria-label*="Applications"] span',
  },

  async verifyCaseFileAppDocument(documentType) {
    await I.seeInCurrentUrl('File');
    await I.waitNumberOfVisibleElements('.cdk-tree', 1, 20);
    await I.waitForText('Applications', 20, this.fields.nameFolder);
    if ('No document' !== documentType) {
      await I.forceClick(locate(this.fields.appFolder));
      await I.waitForSelector(this.fields.expandedFolder, 30);
    }
    let docs = await I.grabTextFromAll(locate(this.fields.expandedFolder));
    let appCount = await I.grabTextFrom(locate(this.fields.appFolder));
    switch (documentType) {
      case 'Hearing Notice':
        expect(docs.toString()).to.contains(`Hearing_order_for_application_${docFullDate}`);
        expect(docs.toString()).to.contains(`Application_Hearing_Notice_${docFullDate}`);
        expect(appCount).to.be.oneOf(['2', '3']);
        break;
      case 'Applicant Evidence':
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(docs.toString()).to.contains(`Draft_application_${docFullDate}`);
        expect(appCount).equals('2');
        break;
      case 'N245 Evidence':
        expect(docs.toString()).to.contains(`Hearing_order_for_application_${docFullDate}`);
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(docs.toString()).to.contains(`Draft_application_${docFullDate}`);
        expect(appCount).equals('4');
        break;
      case 'Sequential order document':
        expect(docs.toString()).to.contains(`Order_Written_Representation_Sequential_for_application_${docFullDate}`);
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(docs.toString()).to.contains(`Draft_application_${docFullDate}`);
        expect(appCount).equals('4');
        break;
      case 'Request more info order':
        expect(docs.toString()).to.contains(`Request_for_information_for_application_${docFullDate}`);
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(docs.toString()).to.contains(`Draft_application_${docFullDate}`);
        expect(appCount).equals('7');
        break;
      case 'Concurrent order document':
        expect(docs.toString()).to.contains(`Order_Written_Representation_Concurrent_for_application_${docFullDate}`);
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(docs.toString()).to.contains(`Draft_application_${docFullDate}`);
        expect(appCount).equals('4');
        break;
      case 'Consent Order':
        expect(docs.toString()).to.contains(`Draft_application_${docFullDate}`);
        expect(appCount).equals('1');
        break;
      case 'Supporting evidence':
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(appCount).equals('1');
      break;
      case 'N245 and supporting evidence':
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(appCount).equals('2');
      break;
      case 'No document':
        expect(appCount).equals('0');
        break;
      case 'Hearing order':
        expect(docs.toString()).to.contains(`Hearing_order_for_application_${docFullDate}`);
        expect(docs.toString()).to.contains('examplePDF.pdf');
        expect(docs.toString()).to.contains(`Draft_application_${docFullDate}`);
        expect(appCount).equals('3');
        break;
    }
    await I.forceClick(locate(this.fields.appFolder));
  },

  async verifyCaseFileOrderDocument(documentType) {
    await I.seeInCurrentUrl('File');
    await I.waitNumberOfVisibleElements('.cdk-tree', 1, 20);
    await I.waitForText('Applications', 20, this.fields.nameFolder);
    await I.forceClick(locate(this.fields.orderFolder).first());
    await I.waitForText('Orders made on applications');
    await I.forceClick('Orders made on applications');
    let docs = await I.grabTextFromAll(locate(this.fields.expandedFolder));
    switch (documentType) {
      case 'General order document':
        expect(docs.toString()).to.contains(`General_order_for_application_${docFullDate}`);
        break;
      case 'Directions order document':
        expect(docs.toString()).to.contains(`Directions_order_for_application_${docFullDate}`);
        break;
      case 'Dismissal order document':
        expect(docs.toString()).to.contains(`Dismissal_order_for_application_${docFullDate}`);
        break;
      case 'Consent Order':
        expect(docs.toString()).to.contains(`Consent_order_for_application_${docFullDate}`);
        break;
    }
    await I.forceClick(locate(this.fields.orderFolder).first());
  }
};
