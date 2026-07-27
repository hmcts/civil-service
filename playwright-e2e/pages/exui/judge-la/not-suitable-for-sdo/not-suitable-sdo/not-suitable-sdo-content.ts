export const radioButtons = {
  reason: {
    label: 'What is the reason for not drawing a Standard Directions Order?',
    transferCase: {
      label: 'The case should be sent to another hearing centre for directions',
      selector: '#notSuitableSdoOptions-CHANGE_LOCATION',
    },
    otherReason: {
      label: 'Other reason',
      selector: '#notSuitableSdoOptions-OTHER_REASONS',
    },
  },
};

export const inputs = {
  transferCase: {
    label: 'The case should be sent to another hearing centre for directions',
    paragraph1: 'Give details',
    paragraph2:
      'State the court location name where the case will be transferred and the reason as to why the case needs to be transferred to another hearing court centre (i.e. Transfer the case to Leicester County Court. This case is not on our court jurisdiction to draw an order)',
    selector: '#tocTransferCaseReason_reasonForCaseTransferJudgeTxt',
  },
  otherReason: {
    label: 'Other reason',
    paragraph1: 'The case will be taken offline automatically.',
    paragraph2:
      'If you are judge submitting this information, the case will be sent to a listing officer. If a legal advisor has submitted this information, the case will be sent to a judge for review.',
    paragraph3: 'Give reasons',
    selector: '#reasonNotSuitableSdo_input',
  },
};
