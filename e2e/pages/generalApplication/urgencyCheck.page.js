const {nextYear} = require('../../api/dataHelper');
const {I} = inject();

module.exports = {

  fields: {
    generalAppUrgencyRequirement: {
      id: '#generalAppUrgencyRequirement_generalAppUrgency_radio',
      options: {
        yes: '#generalAppUrgencyRequirement_generalAppUrgency_Yes',
        no: '#generalAppUrgencyRequirement_generalAppUrgency_No'
      }
    },
    considerationDay: '#urgentAppConsiderationDate-day',
    considerationMonth: '#urgentAppConsiderationDate-month',
    considerationYear: '#urgentAppConsiderationDate-year',
    reasonsForUrgency: '#generalAppUrgencyRequirement_reasonsForUrgency',
    consentAgreementCheckBox: '#generalAppUrgencyRequirement_ConsentAgreementCheckBox-ConsentAgreementCheckBox'
  },

  async selectUrgencyRequirement(urgencyCheck) {
    await I.waitForElement(this.fields.generalAppUrgencyRequirement.id);
    await I.waitInUrl('INITIATE_GENERAL_APPLICATIONGAUrgencyRecordPage');
    if ('yes' === urgencyCheck) {
      await within(this.fields.generalAppUrgencyRequirement.id, () => {
        I.click(this.fields.generalAppUrgencyRequirement.options[urgencyCheck]);
      });
      await I.fillField(this.fields.considerationDay, 1);
      await I.fillField(this.fields.considerationMonth, 10);
      await I.fillField(this.fields.considerationYear, nextYear());
      await I.fillField(this.fields.reasonsForUrgency, 'Test Reason for Urgency');
      await I.click(this.fields.consentAgreementCheckBox);
    } else {
      await within(this.fields.generalAppUrgencyRequirement.id, () => {
        I.click(this.fields.generalAppUrgencyRequirement.options[urgencyCheck]);
      });
    }
    await I.clickContinue();
  }
};

