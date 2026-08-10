const expect = require('chai').expect;
const {I} = inject();
const dateFrag = require('../fragments/date');

const month = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
const date = new Date();
const twoDigitDate = ((date.getDate()) >= 10) ? (date.getDate()) : '0' + (date.getDate());
const ownInitiativeOrder = 'Order on court\'s own initiative';
const withOutNoticeOrder = 'Order without notice';
const noneOrder = 'Not applicable';
const initiativeOrderText = 'As this order was made on the court\'s own initiative, any party affected ' +
  'by the order may apply to set aside, vary, or stay the order. Any such application must be made by 4pm on';
const withOutNoticeOrderText = 'If you were not notified of the application before this order was made, ' +
  'you may apply to set aside, vary, or stay the order. Any such application must be made by 4pm on';

let fullDate = date.getDate() + ' ' + month[date.getMonth()] + ' ' + date.getFullYear().toString();
let docMonth = ((date.getMonth() + 1) >= 10) ? (date.getMonth() + 1) : '0' + (date.getMonth() + 1);
let docFullDate = date.getFullYear().toString() + '-' + docMonth + '-' + twoDigitDate;

module.exports = {

  async selectCourtsOrderType(actualOrderText, orderType, dateId) {
    let expectedOrderText;
    switch (orderType) {
      case 'courtOwnInitiativeOrder':
        await I.click(ownInitiativeOrder);
        await I.waitForText('Please enter date', 3);
        await I.see(ownInitiativeOrder);
        expectedOrderText = initiativeOrderText.replace(/\//g, '').toString();
        await expect(actualOrderText).to.equals(expectedOrderText);
        if (dateId !== undefined) {
          await dateFrag.enterDate(dateId, +1);
        }
        break;
      case 'withoutNoticeOrder':
        await I.click(withOutNoticeOrder);
        await I.waitForText('Please enter date', 3);
        await I.see(withOutNoticeOrder);
        expectedOrderText = withOutNoticeOrderText.replace(/\//g, '').toString();
        await expect(actualOrderText).to.equals(expectedOrderText);
        if (dateId !== undefined) {
          await dateFrag.enterDate(dateId, +1);
        }
        break;
      case 'noneOrder':
        await I.click(noneOrder);
        break;
    }
  },

  verifyJudgeRecitalText: async (actualJudgeRecitalText, notice) => {
    if (notice === 'no') {
      await expect(actualJudgeRecitalText).to.equals(`The Judge considered the without notice application of the claimant dated ${fullDate}\n\nAnd the Judge considered the information provided by the claimant`);
    } else if (notice === 'yes') {
      await expect(actualJudgeRecitalText).to.equals(`The Judge considered the application of the claimant dated ${fullDate}\n\n`);
    } else {
      await expect(actualJudgeRecitalText).to.equals(`The Judge considered the application of the defendant dated ${fullDate}\n\n`);
    }
  },

  verifyGAApplicantName: async (actualApplicantName, applicantName) => {
    switch (applicantName) {
      case 'Claimant':
        await expect(actualApplicantName).to.equals('Test Inc - Claimant');
        break;
      case 'Defendant 1':
        await expect(actualApplicantName).to.equals('Sir John Doe - Defendant');
        break;
      case 'Defendant 2':
        await expect(actualApplicantName).to.equals('Dr Foo Bar - Defendant');
        break;
    }
  },
  docFullDate,
};
