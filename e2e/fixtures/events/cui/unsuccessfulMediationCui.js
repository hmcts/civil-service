module.exports = {
  unsuccessfulMediation: (carmEnabled = false) => {
    if (carmEnabled) {
      return {
        mediationUnsuccessfulReasonsMultiSelect: ['PARTY_WITHDRAWS', 'APPOINTMENT_NOT_ASSIGNED', 'NOT_CONTACTABLE_CLAIMANT_TWO']
      };
    } else {
      return {
        unsuccessfulMediationReason: 'PARTY_WITHDRAWS'
      };
    }
  },
};
