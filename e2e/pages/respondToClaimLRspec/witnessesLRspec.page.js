const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      witnessesToAppear: {
        id: `#${party}DQWitnessesRequiredSpec_radio`,
        options: {
          yes: `#${party}DQWitnessesRequiredSpec_Yes`,
          no: `#${party}DQWitnessesRequiredSpec_No`
        },
        id2: `#${party}DQWitnesses_witnessesToAppear_radio`,
        id2options: {
          yes: `#${party}DQWitnesses_witnessesToAppear_Yes`,
          no: `#${party}DQWitnesses_witnessesToAppear_No`
        },
        id3: `#${party}DQWitnesses_witnessesToAppear_radio`,
        id3options: {
          yes: `#${party}DQWitnesses_witnessesToAppear_Yes`,
          no: `#${party}DQWitnesses_witnessesToAppear_No`
        }
      },
      witnessDetails: {
        id: `#${party}DQWitnesses_details`,
        element: {
          name: `#${party}DQWitnessesDetailsSpec_0_name`,
          reasonForWitness: `#${party}DQWitnessesDetailsSpec_0_reasonForWitness`,
        }
      },
    };
  },

  async enterWitnessInformation(party) {

  if(party === 'applicant1'){
    I.waitForElement(this.fields(party).witnessesToAppear.id3);
    await I.runAccessibilityTest();
    await within(this.fields(party).witnessesToAppear.id3, () => {
      I.click(this.fields(party).witnessesToAppear.id3options.no);
    });

   } else if (party === 'respondent1'){
      I.waitForElement(this.fields(party).witnessesToAppear.id);
      await I.runAccessibilityTest();
      await within(this.fields(party).witnessesToAppear.id, () => {
        I.click(this.fields(party).witnessesToAppear.options.no);
      });

    } else if(party === 'respondent2'){
      I.waitForElement(this.fields(party).witnessesToAppear.id2);
      await I.runAccessibilityTest();
      await within(this.fields(party).witnessesToAppear.id2, () => {
        I.click(this.fields(party).witnessesToAppear.id2options.no);
      });

    }
   await I.clickContinue();
  },
};