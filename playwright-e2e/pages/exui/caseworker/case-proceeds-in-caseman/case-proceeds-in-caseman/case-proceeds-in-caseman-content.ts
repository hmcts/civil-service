export const inputs = {
  date: {
    label: 'Date when claim was transferred to Caseman',
    hintText: "The date entered can be today's or a previous date.",
    selectorKey: 'date',
  },
  otherReason: {
    label: 'Other reason',
    selector: '#claimProceedsInCaseman_other',
  },
};

export const radioButtons = {
  reasons: {
    label: 'Reason for proceeding on paper',
    application: {
      label: 'Application',
      selector: '#claimProceedsInCaseman_reason-APPLICATION',
    },
    judgment: {
      label: 'Judgment request',
      selector: '#claimProceedsInCaseman_reason-JUDGEMENT_REQUEST',
    },
    caseSettled: {
      label: 'Case settled',
      selector: '#claimProceedsInCaseman_reason-CASE_SETTLED',
    },
    other: {
      label: 'Other',
      selector: '#claimProceedsInCaseman_reason-OTHER',
    },
  },
};
