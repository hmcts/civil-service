const {I} = inject();

module.exports = {

  fields: (party) => (
    {
      determinationWithoutHearing: {
        id: `#deterWithoutHearing${party}_deterWithoutHearingYesNo_radio`,
        options: {
          yes: `#deterWithoutHearing${party}_deterWithoutHearingYesNo_Yes`,
          no: `#deterWithoutHearing${party}_deterWithoutHearingYesNo_No`,
        },
        whyNot: `#deterWithoutHearing${party}_deterWithoutHearingWhyNot`
      },
    }),

    async selectNo(party) {
      await I.see('Determination without hearing?');
      await I.click(this.fields(party).determinationWithoutHearing.options.no);
      await I.fillField(this.fields(party).determinationWithoutHearing.whyNot, 'I do not require determination without hearing questions');
      await I.clickContinue();
    },
  };