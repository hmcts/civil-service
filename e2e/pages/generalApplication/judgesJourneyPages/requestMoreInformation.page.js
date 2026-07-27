const date = require('../../../fragments/date');
const {I} = inject();

module.exports = {

  fields: {
    requestMoreInfo: {
      id: '#judicialDecisionRequestMoreInfo_requestMoreInfoOption',
      options: {
        requestMoreInformation: 'Request more information',
        sendApplicationToOtherParty: 'Send application to other party and request hearing details'
      }
    },
    judgeRequestMoreInfoTextArea: '#judicialDecisionRequestMoreInfo_judgeRequestMoreInfoText',
    judgeRequestMoreInfoRecitalTextArea: '#judicialDecisionRequestMoreInfo_judgeRecitalText',
    judgeRequestMoreInfoDate: 'judgeRequestMoreInfoByDate',
    requestInfoRadioButton: '#judicialDecisionRequestMoreInfo_requestMoreInfoOption input',
  },

  async requestMoreInfoOrder(info, withoutNotice) {
    await I.waitForElement(this.fields.requestMoreInfo.id);
    I.seeInCurrentUrl('MAKE_DECISIONGAJudicialRequestMoreInfoScreen');
    if (withoutNotice === 'no') {
      I.seeNumberOfVisibleElements(this.fields.requestInfoRadioButton, 2);
      await within(this.fields.requestMoreInfo.id, () => {
        I.click(this.fields.requestMoreInfo.options[info]);
      });
    } else if (withoutNotice === 'yes') {
      I.dontSee('Send application to other party and request hearing details');
    }
    if ('requestMoreInformation' === info) {
      I.fillField(this.fields.judgeRequestMoreInfoTextArea, 'Judges request more information');
      I.see('When should this application be referred to a Judge again?');
      await date.enterDate(this.fields.judgeRequestMoreInfoDate, +2);
      I.fillField(this.fields.judgeRequestMoreInfoRecitalTextArea, 'Request more Info - Judge recital text');
    }
    await I.clickContinue();
  }
};
