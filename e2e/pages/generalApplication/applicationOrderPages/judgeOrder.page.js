const {I} = inject();

module.exports = {

  fields: {
    judgeOrder: {
      id: '#finalOrderSelection',
      options: {
        assistedOrder: 'Assisted order',
        freeFromOrder: 'Free form order'
      }
    },
  },

  async selectOrderType(order) {
    await I.waitInUrl('/GENERATE_DIRECTIONS_ORDER/GENERATE_DIRECTIONS_ORDERFinalOrderSelect', 5);
    I.waitForElement(this.fields.judgeOrder.id);
    await within(this.fields.judgeOrder.id, () => {
      I.click(this.fields.judgeOrder.options[order]);
    });
    await I.see('What type of order do you wish to make?');
    await I.clickContinue();
  },

  async verifyErrorMessage() {
    await I.waitInUrl('/GENERATE_DIRECTIONS_ORDER/GENERATE_DIRECTIONS_ORDERFinalOrderSelect', 5);
    I.waitForElement(this.fields.judgeOrder.id);
    await I.click('Continue');
    await I.see('Field is required');
  }
};

