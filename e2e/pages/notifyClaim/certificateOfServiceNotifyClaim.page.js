const date = require('../../fragments/date');
const {I} = inject();


module.exports = {
  fields: {
    dateOfService: {
      id: 'cosDateOfServiceForDefendant'
    },
    dateOfDeemedServed: {
      id: 'cosDateDeemedServedForDefendant'
    },
    servedDocumentFilesTextArea: 'textarea[id*="cosServedDocumentFiles"]',
    cosRecipientTextArea: 'textarea[id$="cosRecipient"]',
    cosRecipientServeTypeDropDown: {
      id: 'select[id*="cosRecipientServeType"]',
      options: {
        handed: 'Personally handed it to or left it with',
        delivered: 'Delivered to or left at permitted place',
        posted: 'Sent by first class post or another service which delivers on the next business day',
        other: 'Other means permitted by the court',
      }
    },
    serveLocationTextArea: 'textarea[id$="cosRecipientServeLocation"]',
    serveLocationOwnerType: {
      friend: 'input[id*="FRIEND"]',
      solicitor: 'input[id*="SOLICITOR"]',
      defendant: 'input[id*="DEFENDANT"]',
      claimant: 'input[id*="CLAIMANT"]',
    },
    cosRecipientServeLocationTypeDropDown: {
      id: 'select[id*="cosRecipientServeLocationType"]',
      options: {
        residence: 'Usual Residence',
        lastKnownResidence: 'Last known residence'
      }
    },
    senderName: 'input[id$="cosSender"]',
    firmName: 'input[id*="cosSenderFirm"]',
    certifiedCheckBox: 'input[id*="CERTIFIED"]',
  },

  async fillNotifyClaimCOSForm(cos, defendantName) {
    await I.waitInUrl('NOTIFY_DEFENDANT_OF_CLAIM/NOTIFY_DEFENDANT_OF_CLAIMCertificateOfService');
    await I.see(cos);
    await I.see(defendantName);
    await I.seeNumberOfVisibleElements('[id*="cosLabelFor"]', 2);
    await date.enterDate(this.fields.dateOfService.id, -1);
    await date.enterDateNoWeekends(this.fields.dateOfDeemedServed.id, 2);
    await I.fillField(this.fields.servedDocumentFilesTextArea, 'Test Document');
    await I.fillField(this.fields.cosRecipientTextArea, 'Test test');
    await I.selectOption(this.fields.cosRecipientServeTypeDropDown.id,
      this.fields.cosRecipientServeTypeDropDown.options['delivered']);
    await I.fillField(this.fields.serveLocationTextArea, 'Test test');
    await I.click(this.fields.serveLocationOwnerType.defendant);
    await I.selectOption(this.fields.cosRecipientServeLocationTypeDropDown.id,
      this.fields.cosRecipientServeLocationTypeDropDown.options['lastKnownResidence']);
    await I.see('Statement of truth');
    await I.seeNumberOfVisibleElements('[id="cosStatementOfTruthLabel"]', 1);
    await I.fillField(this.fields.senderName, 'Test Sender');
    await I.fillField(this.fields.firmName, 'Test Firm');
    await I.click(this.fields.certifiedCheckBox);
    await I.clickContinue();
  }
};
