const date = require('../../fragments/date');
const {I} = inject();
const servedDocuments = require('../../fragments/servedDocument');


module.exports = {
  fields:(party) => ( {
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
    supportingEvidenceDocumentFiles: {
      options: [
        `#cos${party}_cosEvidenceDocument`
      ]
    }

  }),

  async fillNotifyClaimDetailsCOSForm(cos, defendantName, party, file) {
    await I.waitInUrl('NOTIFY_DEFENDANT_OF_CLAIM_DETAILS/NOTIFY_DEFENDANT_OF_CLAIM_DETAILSCertificateOfService');
    await I.see(cos);
    await I.see(defendantName);
    await I.seeNumberOfVisibleElements('[id*="cosLabelFor"]', 2);
    await date.enterDate(this.fields(party).dateOfService.id, -1);
    await date.enterDateNoWeekends(this.fields(party).dateOfDeemedServed.id, 2);
    await I.fillField(this.fields(party).servedDocumentFilesTextArea, 'Test Document');
    await servedDocuments.upload(file, this.fields(party).supportingEvidenceDocumentFiles.options);
    await I.fillField(this.fields(party).cosRecipientTextArea, 'Test test');
    await I.selectOption(this.fields(party).cosRecipientServeTypeDropDown.id,
      this.fields(party).cosRecipientServeTypeDropDown.options['delivered']);
    await I.fillField(this.fields(party).serveLocationTextArea, 'Test test');
    await I.click(this.fields(party).serveLocationOwnerType.defendant);
    await I.selectOption(this.fields(party).cosRecipientServeLocationTypeDropDown.id,
      this.fields(party).cosRecipientServeLocationTypeDropDown.options['lastKnownResidence']);
    await I.see('Statement of truth');
    await I.seeNumberOfVisibleElements('[id="cosStatementOfTruthLabel"]', 1);
    await I.fillField(this.fields(party).senderName, 'Test Sender');
    await I.fillField(this.fields(party).firmName, 'Test Firm');
    await I.click(this.fields(party).certifiedCheckBox);
    await I.clickContinue();
  }
};

