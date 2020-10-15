const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      explainedToClient: {
        id: `#${party}DQFileDirectionsQuestionnaire_explainedToClient`,
        options: {
          confirm: `#${party}DQFileDirectionsQuestionnaire_explainedToClient-CONFIRM`
        }
      },
      oneMonthStay: {
        id: `#${party}DQFileDirectionsQuestionnaire_oneMonthStayRequested`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      reactionProtocolCompliedWith: {
        id: `#${party}DQFileDirectionsQuestionnaire_reactionProtocolCompliedWith`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      reactionProtocolNotCompliedWithReason: `#${party}DQFileDirectionsQuestionnaire_reactionProtocolNotCompliedWithReason`,
    };
  },

  async fileDirectionsQuestionnaire(party) {
    I.waitForElement(this.fields(party).explainedToClient.id);
    I.checkOption(this.fields(party).explainedToClient.options.confirm);

    await within(this.fields(party).oneMonthStay.id, () => {
      I.click(this.fields(party).oneMonthStay.options.no);
    });

    await within(this.fields(party).reactionProtocolCompliedWith.id, () => {
      I.click(this.fields(party).reactionProtocolCompliedWith.options.no);
    });

    I.fillField(this.fields(party).reactionProtocolNotCompliedWithReason, 'Reason for not complying');

    await I.clickContinue();
  }
};
