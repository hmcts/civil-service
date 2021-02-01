const events =  require('./events.js');

module.exports = {
  CREATED: [
    events.REQUEST_EXTENSION,
    events.RESPOND_EXTENSION,
    events.ACKNOWLEDGE_SERVICE,
    events.ADD_DEFENDANT_LITIGATION_FRIEND,
    events.DEFENDANT_RESPONSE
  ],
  PROCEEDS_WITH_OFFLINE_JOURNEY: [],
  AWAITING_CLAIMANT_INTENTION: [
    events.ADD_DEFENDANT_LITIGATION_FRIEND,
    events.CLAIMANT_RESPONSE
  ]
};
