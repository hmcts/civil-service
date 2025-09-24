const { I } = inject();

module.exports = {

  fields: {
    documentAndNoteToAdd: {
      id: '#documentAndNoteToAdd',
      fragment: {
        button: 'Add new',
        name: '#documentAndNoteToAdd_0_documentName',
        document: '#documentAndNoteToAdd_0_document',
        note: '#documentAndNoteToAdd_0_documentNote'
      }
    },
  },

  async addDocumentAndNotes (file) {
    await I.runAccessibilityTest();
    I.waitForElement(this.fields.documentAndNoteToAdd.id);
    await within(this.fields.documentAndNoteToAdd.id, () => {
      I.click(this.fields.documentAndNoteToAdd.fragment.button);
      I.fillField(this.fields.documentAndNoteToAdd.fragment.name, 'Doc 1');
      I.attachFile(this.fields.documentAndNoteToAdd.fragment.document, file);
      I.fillField(this.fields.documentAndNoteToAdd.fragment.note, 'Test Note');
    });
    await I.clickContinue();
  },
};
