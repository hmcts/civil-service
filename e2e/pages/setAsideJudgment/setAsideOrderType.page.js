const dataHelper = require('../../api/dataHelper');
const {I} = inject();

module.exports = {
  fields: {
    joSetAsideOrderDate: {
      id: '#joSetAsideOrderDate',
      day: '#joSetAsideOrderDate-day',
      month: '#joSetAsideOrderDate-month',
      year: '#joSetAsideOrderDate-year', 
    },
    joSetAsideOrderType: {
      id: '#joSetAsideOrderType',
      options: {
        orderAfterApp: '#joSetAsideOrderType-ORDER_AFTER_APPLICATION',
        orderAfterDef: '#joSetAsideOrderType-ORDER_AFTER_DEFENCE',
      },
    },
    joSetAsideApplicationDate: {
      id: '#joSetAsideApplicationDate',
      day: '#joSetAsideApplicationDate-day',
      month: '#joSetAsideApplicationDate-month',
      year: '#joSetAsideApplicationDate-year', 
    },
    joSetAsideDefenceReceivedDate: {
      id: '#joSetAsideDefenceReceivedDate',
      day: '#joSetAsideDefenceReceivedDate-day',
      month: '#joSetAsideDefenceReceivedDate-month',
      year: '#joSetAsideDefenceReceivedDate-year', 
    },
  },

  async orderAfterApplication() {
    const orderAsideDate = dataHelper.decrementDate(new Date(), 0, 1, 0);
    await I.waitForSelector(this.fields.joSetAsideOrderDate.day);
    await I.fillField(this.fields.joSetAsideOrderDate.day,   String(orderAsideDate.getDate()).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideOrderDate.month, String(orderAsideDate.getMonth() + 1).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideOrderDate.year,  String(orderAsideDate.getFullYear()));
    await I.click(this.fields.joSetAsideOrderType.options.orderAfterApp);

    const applicationAsideDate = dataHelper.decrementDate(new Date(), 0, 1, 0);
    await I.waitForText('Enter the date of the application to set aside');
    await I.fillField(this.fields.joSetAsideApplicationDate.day, String(applicationAsideDate.getDate()).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideApplicationDate.month, String(applicationAsideDate.getMonth() + 1).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideApplicationDate.year, String(applicationAsideDate.getFullYear()));

    await I.clickContinue();
  },

  async orderAfterDefence() {
    const orderAsideDate = dataHelper.decrementDate(new Date(), 0, 1, 0);
    await I.waitForSelector(this.fields.joSetAsideOrderDate.day);
    await I.fillField(this.fields.joSetAsideOrderDate.day,   String(orderAsideDate.getDate()).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideOrderDate.month, String(orderAsideDate.getMonth() + 1).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideOrderDate.year,  String(orderAsideDate.getFullYear()));
    await I.click(this.fields.joSetAsideOrderType.options.orderAfterDef);

    const defenceReceivedAsideDate = dataHelper.decrementDate(new Date(), 0, 1, 0);
    await I.waitForText('Enter the date of the defence was received');
    await I.fillField(this.fields.joSetAsideDefenceReceivedDate.day, String(defenceReceivedAsideDate.getDate()).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideDefenceReceivedDate.month, String(defenceReceivedAsideDate.getMonth() + 1).padStart(2,'0'));
    await I.fillField(this.fields.joSetAsideDefenceReceivedDate.year, String(defenceReceivedAsideDate.getFullYear()));

    await I.clickContinue();
  },
};