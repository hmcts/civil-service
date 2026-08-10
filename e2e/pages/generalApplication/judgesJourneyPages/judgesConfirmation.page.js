const {I} = inject();

module.exports = {

  fields: {
    confirmation: {
      id: '#confirmation-body'
    },
    applicationList: '#confirmation-body li',
    selectedTab: 'div[aria-selected="true"] div[class*="content"]',
  },

  async verifyJudgesConfirmationPage() {
    I.seeInCurrentUrl('MAKE_DECISION/confirm');
    I.seeTextEquals('Your order has been made', '#confirmation-header h1');
  },

  async verifyReqMoreInfoConfirmationPage(infoType) {
    I.seeInCurrentUrl('MAKE_DECISION/confirm');
    switch (infoType) {
      case 'requestMoreInformation':
        I.seeTextEquals('You have requested more information', '#confirmation-header h1');
        await I.see('The applicant will be notified.');
        break;
      case 'sendApplicationToOtherParty':
        I.seeTextEquals('You have requested a response', '#confirmation-header h1');
        await I.see('The parties will be notified.');
        break;
    }
  },

  async closeAndReturnToCaseDetails() {
    await I.click('Close and Return to case details');
    await I.waitForText('Application', 15, this.fields.selectedTab);
  }
};

