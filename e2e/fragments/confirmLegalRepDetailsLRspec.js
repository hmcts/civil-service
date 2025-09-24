const { I } = inject();

module.exports = {
  fields: {
    solicitor1Reference: {
      id: '#specAoSRespondentCorrespondenceAddressRequired_radio',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
    solicitor2Reference: {
      id: '#specAoSRespondent2CorrespondenceAddressRequired_radio',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    }
  },

  async confirmDetails(twoDefendants) {
  if(!twoDefendants){
    I.waitForElement(this.fields.solicitor1Reference.id);
    await I.runAccessibilityTest();
    const options = this.fields.solicitor1Reference.options;
    await I.click(options.yes);
  }else{
      I.waitForElement(this.fields.solicitor2Reference.id);
      await I.runAccessibilityTest();
      const options2 = this.fields.solicitor2Reference.options;
      await I.click(options2.yes);
    }
    await I.clickContinue();

  }
};