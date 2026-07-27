const {I} = inject();

module.exports = {

  fields: {
    informOtherPartyWithNotice: {
      id: '#generalAppInformOtherParty_isWithNotice',
      options: {
        yes: '#generalAppInformOtherParty_isWithNotice-Yes',
        no: '#generalAppInformOtherParty_isWithNotice-No'
      }
    },
    reasonsForWithoutNotice: '#generalAppInformOtherParty_reasonsForWithoutNotice'
  },

  async selectNotice(noticeCheck) {
    I.waitForElement(this.fields.informOtherPartyWithNotice.id);
    if ('no' === noticeCheck) {
      await within(this.fields.informOtherPartyWithNotice.id, () => {
        I.click(this.fields.informOtherPartyWithNotice.options[noticeCheck]);
      });
      await I.fillField(this.fields.reasonsForWithoutNotice, 'Test Reason for Without Notice');
    } else {
      await within(this.fields.informOtherPartyWithNotice.id, () => {
        I.click(this.fields.informOtherPartyWithNotice.options[noticeCheck]);
      });
    }
    await I.clickContinue();
  }
};

