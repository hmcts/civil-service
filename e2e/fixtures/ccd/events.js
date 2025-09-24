module.exports = {
  NOTIFY_DEFENDANT_OF_CLAIM: {
    id: 'NOTIFY_DEFENDANT_OF_CLAIM',
    name: 'Notify claim',
    description: 'Notify defendant solicitor of claim',
    order: 2
  },
  NOTIFY_DEFENDANT_OF_CLAIM_DETAILS: {
    id: 'NOTIFY_DEFENDANT_OF_CLAIM_DETAILS',
    name: 'Notify claim details',
    description: 'Inform the defendant of particulars of claim',
    order: 3
  },
  ACKNOWLEDGE_CLAIM: {
    id: 'ACKNOWLEDGE_CLAIM',
    name: 'Acknowledge claim',
    description: 'Defendant solicitor is acknowledging claim',
    order: 4
  },
  ACKNOWLEDGEMENT_OF_SERVICE: {
    description: 'Acknowledgement of Service (AoS)',
    id: 'ACKNOWLEDGEMENT_OF_SERVICE',
    name: 'Acknowledgement of Service',
    order: 4
  },
  ADD_DEFENDANT_LITIGATION_FRIEND: {
    id: 'ADD_DEFENDANT_LITIGATION_FRIEND',
    name: 'Add litigation friend',
    description: 'Add litigation friend',
    order: 5
  },
  DEFENDANT_RESPONSE: {
    id: 'DEFENDANT_RESPONSE',
    name: 'Respond to claim',
    description: 'Defendant response to claim',
    order: 6
  },
  DEFENDANT_RESPONSE_SPEC: {
    id: 'DEFENDANT_RESPONSE_SPEC',
    name: 'Respond to claim',
    description: 'Defendant response to Specified claim',
    order: 6
  },
  CLAIMANT_RESPONSE: {
    id: 'CLAIMANT_RESPONSE',
    name: 'View and respond to defence',
    description: 'View and respond to defendant',
    order: 7
  },
  WITHDRAW_CLAIM: {
    id: 'WITHDRAW_CLAIM',
    name: 'Withdraw claim',
    description: 'Withdraw a claim',
    order: 10
  },
  ADD_OR_AMEND_CLAIM_DOCUMENTS: {
    id: 'ADD_OR_AMEND_CLAIM_DOCUMENTS',
    name: 'Add or amend claim documents',
    description: 'Add or amend documents attached to the claim',
    order: 10
  },
  DISCONTINUE_CLAIM: {
    id: 'DISCONTINUE_CLAIM',
    name: 'Discontinue claim',
    description: 'Discontinue a claim',
    order: 11
  },
  CASE_PROCEEDS_IN_CASEMAN: {
    description: 'Case will proceed offline in Caseman system',
    id: 'CASE_PROCEEDS_IN_CASEMAN',
    name: 'Case proceeds in Caseman',
    order: 9
  },
  TAKE_CASE_OFFLINE: {
    id: 'TAKE_CASE_OFFLINE',
    name: 'Take case offline',
    description: 'Take case offline',
    order: 15,
  },
  RESUBMIT_CLAIM: {
    id: 'RESUBMIT_CLAIM',
    name: 'Resubmit claim',
    description: 'Resubmits claim for unsuccessful PBA payment',
    order: 11
  },
  INFORM_AGREED_EXTENSION_DATE: {
    description: 'Enter an extension date that has already been agreed with other parties',
    id: 'INFORM_AGREED_EXTENSION_DATE',
    name: 'Inform agreed 28 day extension',
    order: 12
  },
  INFORM_AGREED_EXTENSION_DATE_SPEC: {
    description: 'Specified Enter an extension date that has already been agreed with other parties',
    id: 'INFORM_AGREED_EXTENSION_DATE_SPEC',
    name: 'Inform agreed extension date',
    order: 12
  },
  AMEND_PARTY_DETAILS: {
    description: 'Update defendant and claimant solicitor email addresses',
    id: 'AMEND_PARTY_DETAILS',
    name: 'Amend party details',
    order: 13
  },
  ADD_CASE_NOTE: {
    description: 'Add a case note',
    id: 'ADD_CASE_NOTE',
    name: 'Add a case note',
    order: 16
  },
  EVIDENCE_UPLOAD_JUDGE: {
    id: 'EVIDENCE_UPLOAD_JUDGE',
    name: 'Add a case note',
    description: 'Add a case note',
    order: 22,
  },
  CHANGE_SOLICITOR_EMAIL: {
    description: 'Change solicitor\'s information',
    id: 'CHANGE_SOLICITOR_EMAIL',
    name: 'Change solicitor\'s information',
    order: 17
  },
  ENTER_BREATHING_SPACE_SPEC: {
    description: 'Enter Breathing Space',
    id: 'ENTER_BREATHING_SPACE_SPEC',
    name: 'Enter Breathing Space',
    order: 7
  },
  LIFT_BREATHING_SPACE_SPEC: {
    description: 'Lift Breathing Space',
    id: 'LIFT_BREATHING_SPACE_SPEC',
    name: 'Lift Breathing Space',
    order: 8
  },
  DEFAULT_JUDGEMENT: {
    id: 'DEFAULT_JUDGEMENT',
    name: 'Request Default Judgment',
    description: 'Request Default Judgment',
    order: 17
  },
  DEFAULT_JUDGEMENT_SPEC: {
    id: 'DEFAULT_JUDGEMENT_SPEC',
    name: 'Request Default Judgment',
    description: 'Request Default Judgment',
    order: 18
  },
  CREATE_SDO: {
    id: 'CREATE_SDO',
    name: 'Standard Direction Order',
    description: 'Standard Direction Order',
    order: 17
  },
  STANDARD_DIRECTION_ORDER_DJ: {
    id: 'STANDARD_DIRECTION_ORDER_DJ',
    name: 'Directions Order (Judgment)',
    description: 'Standard Directions Order (Judgment)',
    order: 18
  },
  GENERATE_DIRECTIONS_ORDER: {
    id: 'GENERATE_DIRECTIONS_ORDER',
    name: 'Make an order',
    description: 'Make an order',
    order: null,
  },
  CLAIMANT_RESPONSE_SPEC: {
    description: 'View and respond to defendant (Specified)',
    id: 'CLAIMANT_RESPONSE_SPEC',
    name: 'View and respond to defence',
    order: 7
  },
  REFER_TO_JUDGE: {
    id: 'REFER_TO_JUDGE',
    name: 'Refer to Judge',
    description: 'Refer to Judge',
    order: 19
  },
  INITIATE_GENERAL_APPLICATION: {
    id: 'INITIATE_GENERAL_APPLICATION',
    name: 'Make an application',
    description: 'Application created, post actions triggered',
    order: 17
  },
  REQUEST_JUDGEMENT_ADMISSION_SPEC: {
    id: 'REQUEST_JUDGEMENT_ADMISSION_SPEC',
    name: 'Request Judgement by Admission',
    description: 'Request Judgement by Admission',
    order: 10
  },
  MANAGE_CONTACT_INFORMATION: {
    id: 'MANAGE_CONTACT_INFORMATION',
    name: 'Manage Contact Information',
    description: 'Updated party contact details',
    order: 99
  },
  MANAGE_DOCUMENTS: {
    id: 'MANAGE_DOCUMENTS',
    name: 'Manage Documents',
    description: 'Manage Documents',
    order: 4
  },
  RESET_PIN: {
    id: 'RESET_PIN',
    name: 'Reset Pin',
    description: 'Reset Pin for case',
    order: 7
  },
  REQUEST_FOR_RECONSIDERATION: {
    id: 'REQUEST_FOR_RECONSIDERATION',
    name: 'Request for reconsideration',
    description: 'Request for reconsideration check',
    order: null
  },
  SETTLE_CLAIM_MARK_PAID_FULL: {
    id: 'SETTLE_CLAIM_MARK_PAID_FULL',
    name: 'Settle this claim',
    description: 'Settle this claim',
    order: 13,
  },
  SETTLE_CLAIM: {
    id: 'SETTLE_CLAIM',
    name: 'Settle this claim',
    description: 'Settle this claim',
    order: 12,
  },
  DISCONTINUE_CLAIM_CLAIMANT: {
    id: 'DISCONTINUE_CLAIM_CLAIMANT',
    name: 'Discontinue this claim',
    description: 'Discontinue this claim',
    order: 14,
  },
  VALIDATE_DISCONTINUE_CLAIM_CLAIMANT: {
    id: 'VALIDATE_DISCONTINUE_CLAIM_CLAIMANT',
    name: 'Validate discontinuance',
    description: 'Validate discontinuance',
    order: 15
  },
  DECISION_ON_RECONSIDERATION_REQUEST: {
    id: 'DECISION_ON_RECONSIDERATION_REQUEST',
    name: 'Decision on reconsideration',
    description: 'Decision on reconsideration',
    order: 1
  },
  MEDIATION_UNSUCCESSFUL: {
    id: 'MEDIATION_UNSUCCESSFUL',
    name: 'Mediation unsuccessful',
    description: 'Mediation was unsuccessful',
    order: 9
  },
  UPLOAD_MEDIATION_DOCUMENTS: {
    id: 'UPLOAD_MEDIATION_DOCUMENTS',
    name: 'Upload mediation documents',
    description: 'Upload mediation documents',
    order: 24
  },
  QUERY_MANAGEMENT: {
    id: 'queryManagementRaiseQuery',
    name: 'Raise a new query',
    description: 'Raise a new query',
    order: 1
  },
  CREATE_CASE_FLAGS: {
    id: 'CREATE_CASE_FLAGS',
    name: 'Create case flags',
    description: 'Create case flags',
    order: 23,
  },
  MANAGE_CASE_FLAGS: {
    id: 'MANAGE_CASE_FLAGS',
    name: 'Manage case flags',
    description: 'Manage case flags',
    order: 24
  },
  STAY_CASE: {
    id: 'STAY_CASE',
    name: 'Stay case',
    description: 'Stay the case',
    order: null,
  },
  MANAGE_STAY: {
    id: 'MANAGE_STAY',
    name: 'Manage stay',
    description: 'Manage the stay',
    order: null
  },
  TRIAL_READINESS: {
    id: 'TRIAL_READINESS',
    name: 'Confirm trial arrangements',
    description: 'Confirm trial arrangements',
    order: 20
  },
  TRANSFER_ONLINE_CASE: {
    id: 'TRANSFER_ONLINE_CASE',
    name: 'Transfer online case',
    description: 'Transfer online case',
    order: 1,
  },
  ADD_UNAVAILABLE_DATES: {
    id: 'ADD_UNAVAILABLE_DATES',
    name: 'Add Unavailable Dates',
    description: 'Add unavailable dates after claimant response or default judgment',
    order: 50
  },
  HEARING_SCHEDULED: {
    id: 'HEARING_SCHEDULED',
    name: 'Create a hearing notice',
    description: 'Create a hearing notice',
    order: 18
  }
};
