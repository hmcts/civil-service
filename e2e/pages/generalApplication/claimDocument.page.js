const {I} = inject();
const expect = require('chai').expect;
const {docFullDate} = require('../generalAppCommons');

module.exports = {

  fields: {
    docLabel: 'div.case-viewer-label',
    links: '.collection-field-table ccd-read-document-field button',
    docTitles: 'ccd-read-complex-field-collection-table .complex-panel .complex-panel-title'
  },

  async verifyHearingNoticeDocNotAvailable() {
    await I.dontSee('Hearing Notice', locate(this.fields.docLabel).last());
  },

  async verifyUploadedDocument(documentType) {
    await I.seeInCurrentUrl('documents');
    console.log('The Document Type ' + documentType);
    if (documentType === 'After SDO - Hearing Notice' || documentType === 'Hearing Notice') {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 6);
    } else if (documentType === 'Free From Order' || documentType === 'Assisted Order') {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 7);
    } else if (documentType === 'Directions order document') {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 5);
    } else if(documentType === 'Dismissal order document') {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 3);
    } else {
      await I.seeNumberOfVisibleElements(this.fields.docTitles, 5);
    }
    let links = await I.grabTextFromAll(this.fields.links);
    expect(links.some(link => link.includes(`Draft_application_${docFullDate}`))).to.be.true;

    switch (documentType) {
      case 'General order document':
        I.see('Upload documents');
        I.see(`General_order_for_application_${docFullDate}`);
        break;
      case 'Free From Order':
      case 'Assisted Order':
        I.see('Upload documents');
        I.see(`General_order_for_application_${docFullDate}`);
        I.see(`Application_Hearing_Notice_${docFullDate}`);
        break;
      case 'Directions order document':
        I.see('Upload documents');
        I.see(`Directions_order_for_application_${docFullDate}`);
        break;
      case 'Dismissal order document':
        I.see(`Dismissal_order_for_application_${docFullDate}`);
        break;
      case 'Hearing Notice':
        I.see(`Application_Hearing_Notice_${docFullDate}`);
        break;
      case 'Consent order document':
        await I.see(`Consent_order_for_application_${docFullDate}`);
        break;
    }
    I.see('System generated Case Documents');
    await I.see('Type');
    await I.see('Uploaded on');
    await I.see('Document URL');
    let docType = await I.grabTextFrom(locate(this.fields.docLabel).last());
    if (documentType === 'After SDO - Hearing Notice') {
      await I.seeTextEquals('Hearing Notice', locate(this.fields.docLabel).at(5));
      await I.seeTextEquals('Draft Application document', locate(this.fields.docLabel).at(6));
    } else if (documentType === 'Hearing Notice') {
      await I.seeTextEquals('Hearing Notice', locate(this.fields.docLabel).at(5));
      await I.seeTextEquals('Draft Application document', locate(this.fields.docLabel).at(6));
    } else if (documentType === 'Free From Order' || documentType === 'Assisted Order') {
      await I.seeTextEquals('General order document', locate(this.fields.docLabel).at(5));
      await I.seeTextEquals('Hearing Notice', locate(this.fields.docLabel).at(6));
      await I.seeTextEquals('Draft Application document', locate(this.fields.docLabel).at(7));
    } else {
      expect(docType).to.equals(' Draft Application document');
    }
  }
};
