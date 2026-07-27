const {I} = inject();

module.exports = {

  fields: {
    caseNoteType: {
      id: '#caseNoteType',
      options: {
        note: 'Note Only',
        documentAndNote: 'Document with a note',
        document: 'Document only'
      }
    },
  },

  async selectCaseNotes() {
    I.waitForElement(this.fields.caseNoteType.id);
    await I.runAccessibilityTest();
    await within(this.fields.caseNoteType.id, () => {
      I.click(this.fields.caseNoteType.options.documentAndNote);
    });
    await I.clickContinue();
  }
};

