const {I} = inject();

module.exports = {
  fields: {
    refundlist: {
      last_updated_tab: '//div[@role=\'button\'][.//text()[contains(., \'Last updated\')]]',
      process_refund_link: '//mat-row[1]//a[.=\'Process refund\']',
      review_refund_link : '//mat-row[1]//a[.=\'Review refund\']'
    }
  },


  async verifyAndChooseRefundFromRefundList(reviewRefundFlag = false, caseId) {

    I.waitForText('Refunds returned to caseworker');
    I.see('Refund list', 'h1');
    if (!reviewRefundFlag) {
      I.see('Refunds to be approved', 'h2');
    }
    I.see('Refunds returned to caseworker', 'h2');
    I.wait(3);//Waiting till the List Records are Loaded...
    I.click(this.fields.refundlist.last_updated_tab);
    I.wait(2);
    I.click(this.fields.refundlist.last_updated_tab);
    I.wait(2);
    let formattedCaseNumber = caseId.toString().substring(0,4)+'-'+caseId.toString().substring(4,8)+'-'+caseId.toString().substring(8,12)+'-'+caseId.toString().substring(12,16);
    console.log('The value of the formatted Case Number : '+formattedCaseNumber);
    if (reviewRefundFlag === false) {
      //I.click(this.fields.refundlist.process_refund_link);
      I.click(`//mat-cell[contains(.,'${formattedCaseNumber}')]/following-sibling::mat-cell//a[.='Process refund']`);
    } else if (reviewRefundFlag === true){
      //I.click(this.fields.refundlist.review_refund_link);
      I.click(`//mat-cell[contains(.,'${formattedCaseNumber}')]/following-sibling::mat-cell//a[.='Review refund']`);
    }
  }
};
