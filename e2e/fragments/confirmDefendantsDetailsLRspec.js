const { I } = inject();

module.exports = {
  fields: {
    solicitor1Reference: {
      id: '#specAoSApplicantCorrespondenceAddressRequired_radio',
      options: {
        yes: '#specAoSApplicantCorrespondenceAddressRequired_Yes',
        no: '#specAoSApplicantCorrespondenceAddressRequired_No'
      }
    },
    solicitor2Reference: {
      id: '#specAoSRespondent2HomeAddressRequired_radio',
      options: {
        yes: '#specAoSRespondent2HomeAddressRequired_Yes',
        no: '#specAoSRespondent2HomeAddressRequired_No'
      }
    }
  },

 async confirmDetails(twoDefendants) {

    I.waitForElement(this.fields.solicitor1Reference.id);
    await I.runAccessibilityTest();
    const options = this.fields.solicitor1Reference.options;
    await I.click(options.yes);

    if(twoDefendants){
      I.waitForElement(this.fields.solicitor2Reference.id);
      await I.runAccessibilityTest();
      const options2 = this.fields.solicitor2Reference.options;
      await I.click(options2.yes);
    }
    await I.clickContinue();
  }
};
