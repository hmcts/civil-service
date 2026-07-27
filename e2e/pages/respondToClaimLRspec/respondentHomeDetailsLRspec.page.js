const {I} = inject();

module.exports = {

  fields: {
    respondentHomeDetailsType: {
      id: '#respondent1DQHomeDetails_type',
      options: {
        mortgage: 'Home they own or pay a mortgage on',
        jointOwner: 'Jointly-owned home',
        private: 'Private rental',
        councilHouse: 'Council or housing association home',
        other: 'Other'
      }
    }
  },

  async selectRespondentHomeType() {
    console.log('selectRespondentHomeType');
    I.waitForElement(this.fields.respondentHomeDetailsType.id);
    await I.runAccessibilityTest();
    await within(this.fields.respondentHomeDetailsType.id, () => {
    I.click(this.fields.respondentHomeDetailsType.options['mortgage']);
    });

   await I.clickContinue();
  }
};

