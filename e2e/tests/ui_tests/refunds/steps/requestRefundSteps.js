const issueRefund = require('../../../../pages/refunds/requestRefund/issueRefund.page');
const refundOption = require('../../../../pages/refunds/requestRefund/refundOption.page');
const refundReasons = require('../../../../pages/refunds/requestRefund/refundReasons.page');
const refundContactInformation = require('../../../../pages/refunds/requestRefund/refundContactInformation.page');
const refundCheckYourAnswers = require('../../../../pages/refunds/requestRefund/refundCheckYourAnswers.page');
const refundConfirmation = require('../../../../pages/refunds/requestRefund/refundConfirmation.page');

const refundList = require('../../../../pages/refunds/returnRefund/refundList.page');
const reviewRefundDetails = require('../../../../pages/refunds/returnRefund/reviewRefundDetails.page');
const refundDetails = require('../../../../pages/refunds/reviewRefund/refundDetails.page');
const reviewRefundCheckYourAnswers = require('../../../../pages/refunds/reviewRefund/refundChangeYourAnswers.page');


const I = actor();  
/*const latestUpdateTab = new LatestUpdate();
const documentsTab = new Documents();
const bundlesTab = new Bundles();
const uploadYourDocumentsIntroduction = new UploadYourDocumentsIntroduction();
const whatTypeOfDocumentsDoYouWantToUpload = new WhatTypeOfDocumentsDoYouWantToUpload();
const uploadYourDocument = new UploadYourDocument();
const checkYourAnswers = new CheckYourAnswers();
const uploadYourDocumentsConfirmation = new UploadYourDocumentsConfirmation();*/

class RequestRefundSteps {

  performRefunds() {
    issueRefund.issueRefunds();
    refundOption.chooseRefunds();
    refundReasons.chooseRefundsReason('Amended claim');
    refundContactInformation.inputContactInformation();
    refundCheckYourAnswers.checkYourAnswers(false);
    refundConfirmation.verifyConfirmationPage();
  }

  returnRefunds(caseNumber) {
    refundList.verifyAndChooseRefundFromRefundList(false, caseNumber);
    reviewRefundDetails.verifyAndProcessRefundsDetails('Return to caseworker', 'Amended claim');
  }

  reviewRefunds(caseNumber) {
    refundList.verifyAndChooseRefundFromRefundList(true, caseNumber);
    refundDetails.verifyRefundsDetailsAndInitiateChange();
    reviewRefundCheckYourAnswers.verifyChangeYourAnswersPageAndChangeReason();
    refundReasons.chooseRefundsReason('System/technical error');
    refundCheckYourAnswers.checkYourAnswers(true);
    refundConfirmation.verifyConfirmationPage();
  }

  approveRefund(caseId) {
    refundList.verifyAndChooseRefundFromRefundList(false, caseId);
    reviewRefundDetails.verifyAndProcessRefundsDetails('Approve', 'System/technical error');
  }

  rejectRefund(caseId) {
    refundList.verifyAndChooseRefundFromRefundList(false, caseId);
    reviewRefundDetails.verifyAndProcessRefundsDetails('Reject', 'System/technical error');
  }

}

module.exports = new RequestRefundSteps();
