const { I } = inject();

module.exports = {
  fields: {
    evidence: {
      id: '#speclistYourEvidenceList',
      type: '#speclistYourEvidenceList_0_evidenceType',
      other: '#speclistYourEvidenceList_0_otherEvidence',
    },
  },

  async addEvidence() {
    I.waitForElement(this.fields.evidence.id);
    await I.runAccessibilityTest();
    I.click('Add new');
    I.waitForElement(this.fields.evidence.type);
    I.selectOption(this.fields.evidence.type, 'other');
    I.waitForElement(this.fields.evidence.other);
    I.fillField(this.fields.evidence.other, 'evidence');
    await I.clickContinue();
  },
};
