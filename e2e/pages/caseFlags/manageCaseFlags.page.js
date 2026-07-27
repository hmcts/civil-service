const {I} = inject();

const fields = {
  flagLocationSelection: {
    id: 'fieldset[aria-describedby=\'manage-case-flag-heading\']',
    radioGroup: {
      id: '#conditional-radios-list',
    }
  },
  commentInput: {
    id: '#flagComment'
  },
  continue: {
    id: 'Continue'
  }
};

module.exports = {
  async selectFlagLocation(partyName, flagLabel) {
    await I.see(flagLabel);
    await I.waitForElement(fields.flagLocationSelection.id);
      await within(fields.flagLocationSelection.radioGroup.id, () => {
        I.click(partyName);
      });
    I.click(fields.continue.id);
  },

  async updateFlagComment(comment) {
    I.waitForElement(fields.commentInput.id);
    I.fillField(fields.commentInput.id, comment);
    I.click('Make inactive');
    I.doubleClick(fields.continue.id);
  }
};
