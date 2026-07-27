const {I} = inject();

module.exports = {

  fields: {
    makeAnOrderForWrittenRepresentations: {
      id: '#judicialDecisionMakeAnOrderForWrittenRepresentations_makeAnOrderForWrittenRepresentations',
      options: {
        sequentialRep: 'Sequential representations',
        concurrentRep: 'Concurrent representations',
      }
    },
  },

  async selectWrittenRepresentations(representationsType) {
    I.waitForElement(this.fields.makeAnOrderForWrittenRepresentations.id);
    I.seeInCurrentUrl('MAKE_DECISION/MAKE_DECISIONGAJudicialMakeAnOrderForWrittenRepresentations');
    await within(this.fields.makeAnOrderForWrittenRepresentations.id, () => {
      I.click(this.fields.makeAnOrderForWrittenRepresentations.options[representationsType]);
    });
    switch (representationsType) {
      case 'sequentialRep':
        I.see('The claimant should upload any written responses or evidence in reply by 4pm on');
        I.see('The defendant should upload any written responses or evidence by 4pm on');
        break;
      case 'concurrentRep':
        I.see('The claimant and defendant should upload any written submissions and evidence by 4pm on');
        break;
    }
    await I.clickContinue();
  }
};

