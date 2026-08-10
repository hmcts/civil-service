const {I} = inject();

module.exports = {

  fields: {
    reqUpdate: '#manageStayOption-REQUEST_UPDATE',
    liftStay: '#manageStayOption-LIFT_STAY',
  },

  async verifyReqUpdateSteps(state = 'JUDICIAL_REFERRAL') {
    await I.waitForText('Request an update on this case', 60);
    await I.click(this.fields.reqUpdate);
    await I.waitForText('A notification will be sent to all parties, asking them for an update on the case.');
    await I.waitForText('After 7 days, a new task will be created to progress the case.');
    await I.click('Continue');
    await I.waitForText('You are requesting an update on this case, the stay will not be lifted.');
    await I.waitForText('A notification will be sent to all parties, asking for an update on the case.');
    await I.waitForText('After 7 days, a new task will be created to progress the case.');
  },

  async verifyLiftCaseStaySteps(state = 'JUDICIAL_REFERRAL') {
    await I.waitForText('Lift the stay from this case');
    await I.click(this.fields.liftStay);
    await I.click('Continue');
    if (['IN_MEDIATION', 'JUDICIAL_REFERRAL'].includes(state)) {
      await I.waitForText('By lifting the stay, this case will automatically be sent to a judge.');
      await I.waitForText('This will also raise a work allocation task for a judge to make a standard directions order for this case.');
    } else if (['CASE_PROGRESSION', 'HEARING_READINESS', 'PREPARE_FOR_CONDUCT_HEARING'].includes(state)) {
      await I.waitForText('By lifting the stay, this case will return to \'Case progression\' state.');
      await I.waitForText('A caseworker may need to schedule the next hearing for this case.');
    }
  }
};