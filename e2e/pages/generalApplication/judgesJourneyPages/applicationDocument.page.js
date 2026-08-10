const {docFullDate} = require('../../generalAppCommons');
const {I} = inject();
const expect = require('chai').expect;

module.exports = {

  fields: {
    docLabel: 'ccd-read-fixed-list-field span',
    links: '.collection-field-table ccd-read-document-field button',
    appDocTable: '.Application.Documents',
    tab: 'div.mat-tab-label-content',
    docTitles: 'dl.complex-panel-title span'
  },

  async verifyUploadedFile(expectedLabel, uploadedDoc) {
    I.seeInCurrentUrl('Documents');
    I.see(uploadedDoc);
    I.see(expectedLabel);
    //  Concurrent written representations journey is now without notice to with notice hence added this logic
    if (expectedLabel !== 'Written representation concurrent document') {
      I.seeNumberOfVisibleElements(this.fields.links, 4);
    } else {
      I.seeNumberOfVisibleElements(this.fields.links, 4);
    }
  },

  async verifyUploadedDocumentPDF(documentType) {
    await I.seeInCurrentUrl('Documents');
    if (documentType === 'Hearing Notice') {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 3);
    } else if (documentType === 'Free From Order' || documentType === 'Assisted Order') {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 4);
    } else if (documentType === 'Consent Order'){
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 2);
    } else {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 3);
    }
    let docURL = await I.grabTextFrom(locate(this.fields.links).first());
    switch (documentType) {
      case 'General order':
      case 'Free From Order':
      case 'Assisted Order':
        expect(docURL).to.contains(`General_order_for_application_${docFullDate}`);
        break;
      case 'Directions order':
        expect(docURL).to.contains(`Directions_order_for_application_${docFullDate}`);
        break;
      case 'Dismissal order':
        expect(docURL).to.contains(`Dismissal_order_for_application_${docFullDate}`);
        break;
      case 'Request for information':
        expect(docURL).to.contains(`Request_for_information_for_application_${docFullDate}`);
        break;
      case 'Hearing order':
        expect(docURL).to.contains(`Hearing_order_for_application_${docFullDate}`);
        break;
      case 'Written representation sequential':
        expect(docURL).to.contains(`Order_Written_Representation_Sequential_for_application_${docFullDate}`);
        break;
      case 'Written representation concurrent':
        await I.see(`Order_Written_Representation_Concurrent_for_application_${docFullDate}`);
        break;
      case 'Hearing Notice':
        await I.see(`Application_Hearing_Notice_${docFullDate}`);
        break;
      case 'Consent Order':
        await I.see(`Consent_order_for_application_${docFullDate}`);
        break;
    }
    //  Concurrent written representations journey is now without notice to with notice hence added this logic
    if (documentType === 'Written representation concurrent') {
      await I.seeTextEquals(documentType, locate(this.fields.docLabel).first());
    } else if (documentType === 'Hearing Notice') {
      await I.seeTextEquals('Hearing order', locate(this.fields.docLabel).first());
      await I.seeTextEquals(documentType, locate(this.fields.docLabel).at(2));
    } else if (documentType === 'Free From Order' || documentType === 'Assisted Order') {
      await I.seeTextEquals('General order', locate(this.fields.docLabel).first());
      await I.seeTextEquals('Hearing order', locate(this.fields.docLabel).at(2));
      await I.seeTextEquals('Hearing Notice', locate(this.fields.docLabel).at(3));
    } else {
      await I.seeTextEquals(documentType, locate(this.fields.docLabel).first());
    }
  }
};
