const {verifyJudgeRecitalText, verifyGAApplicantName} = require('../../generalAppCommons');
const {I} = inject();

module.exports = {

  fields: {
    summaryLabels: 'table.Application ccd-read-text-area-field span',
    applicantNameText: 'td[id*="gaApplicantDisplayName"] span'
  },

  async verifyJudgesSummaryPage(decisionType, notice, applicantName) {
    I.waitInUrl('#Application');
    I.see('Application');
    I.see('Parent Case ID');
    I.see('Hearing details');
    I.see('Preferred location');
    I.see('Vulnerability questions');
    await verifyGAApplicantName(await I.grabTextFrom(locate(this.fields.applicantNameText).last()), applicantName);
    switch (decisionType) {
      case 'Judges Directions':
        await I.see('The court records that:');
        await verifyJudgeRecitalText(await I.grabTextFrom(locate(this.fields.summaryLabels).first()), notice);
        await I.see('Reasons');
        await I.see('When should this application be referred to a Judge again?');
        break;
      case 'Concurrent representations':
        await I.see('Judge’s recital');
        await verifyJudgeRecitalText(await I.grabTextFrom(locate(this.fields.summaryLabels).first()), notice);
        await I.see('Make an order for written representations');
        await I.see('Concurrent representations');
        await I.see('Order in relation to written representations');
        break;
      case 'Sequential representations':
        await I.see('Judge’s recital');
        await verifyJudgeRecitalText(await I.grabTextFrom(locate(this.fields.summaryLabels).first()), notice);
        await I.see('Make an order for written representations');
        await I.see('Sequential representations');
        await I.see('Order in relation to written representations');
        await I.see('The defendant should upload any written responses or evidence by 4pm on');
        await I.see('The claimant should upload any written responses or evidence in reply by 4pm on');
        break;
      case 'Request more information':
        await I.see('Judge’s recital');
        await I.see('Request more Info - Judge recital text');
        await I.see('Enter the information required');
        await I.see('Request for information');
        await I.see('When should this application be referred to a Judge again?');
        break;
      case 'Dismissal order':
        await I.see('The court records that:');
        await I.see('The court orders that:');
        await verifyJudgeRecitalText(await I.grabTextFrom(locate(this.fields.summaryLabels).first()), notice);
        await I.see('Judges dismissed the order');
        await I.see('Reasons');
        break;
      case 'Approve order':
        await I.see('The court records that:');
        await verifyJudgeRecitalText(await I.grabTextFrom(locate(this.fields.summaryLabels).first()), notice);
        await I.see('Reasons');
        await I.see('Test Order details');
        break;
      case 'Hearing order':
        await I.see('Judge’s recital');
        await verifyJudgeRecitalText(await I.grabTextFrom(locate(this.fields.summaryLabels).first()), notice);
        await I.see('Directions in relation to hearing');
        break;
      case 'Send application to other party':
        await I.see('Request for information');
        await I.see('Send application to other party and request hearing details');
        break;
    }
  }
};

