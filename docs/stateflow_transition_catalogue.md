# State Flow Transition Catalogue

Generated from builder definitions. Guards appear as business-facing conditions.

## ALL_RESPONSES_RECEIVED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| COUNTER_CLAIM | (Latest defence is a counter claim (unspecified journey).) AND (NOT (Defendant responses diverge so the case must go offline.)) | unspec |
| DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE | Responses diverge and discrete directions questionnaires must be produced before going offline. | offline/timeout, unspec |
| DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE | SPEC divergent responses requiring DQs before going offline. | offline/timeout, spec |
| DIVERGENT_RESPOND_GO_OFFLINE | Defendant responses diverge so the case must go offline. | offline/timeout, unspec |
| DIVERGENT_RESPOND_GO_OFFLINE | SPEC defendant/claimant responses diverge so the case must go offline. | offline/timeout, spec |
| DIVERGENT_RESPOND_GO_OFFLINE | Latest defence is a full admission in SPEC. | spec |
| DIVERGENT_RESPOND_GO_OFFLINE | Latest defence is a part admission in SPEC. | spec |
| DIVERGENT_RESPOND_GO_OFFLINE | Latest defence is a counter claim for SPEC. | spec |
| FULL_ADMISSION | (Latest defence is a full admission (unspecified).) AND (NOT (Defendant responses diverge so the case must go offline.)) | unspec |
| FULL_DEFENCE | Latest defence is a full defence (unspecified). | unspec |
| FULL_DEFENCE | Latest defence is a full defence in SPEC. | spec |
| PART_ADMISSION | (Latest defence is a part admission (unspecified).) AND (NOT (Defendant responses diverge so the case must go offline.)) | unspec |
| PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA | Claim details notification deadline expired with no response or staff action. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after claim details were notified (and before any responses/time extensions). | offline/timeout, unspec |

## AWAITING_RESPONSES_FULL_ADMIT_RECEIVED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| ALL_RESPONSES_RECEIVED | Every required defendant response has been received | multi-party, unspec |
| PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA | Claim details notification deadline has expired. | multi-party, offline/timeout, unspec |
| TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED | Claim was taken offline because not all defendants were notified of claim details online. | multi-party, offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after notification but before claim details were served. | multi-party, offline/timeout, unspec |

## AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| ALL_RESPONSES_RECEIVED | Every required defendant response has been received | multi-party, unspec |
| PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA | Claim details notification deadline has expired. | multi-party, offline/timeout, unspec |
| TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED | Claim was taken offline because not all defendants were notified of claim details online. | multi-party, offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after notification but before claim details were served. | multi-party, offline/timeout, unspec |

## AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| ALL_RESPONSES_RECEIVED | Every required defendant response has been received | multi-party, unspec |
| PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA | Claim details notification deadline has expired. | multi-party, offline/timeout, unspec |
| TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED | Claim was taken offline because not all defendants were notified of claim details online. | multi-party, offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after notification but before claim details were served. | multi-party, offline/timeout, unspec |

## CLAIM_DETAILS_NOTIFIED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| ALL_RESPONSES_RECEIVED | (Every required defendant response has been received) AND (((NOT (At least one defendant has acknowledged service (per scenario rules).)) AND (NOT (A defendant has obtained a time extension to respond.))) AND (NOT (Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline).))) | unspec |
| AWAITING_RESPONSES_FULL_ADMIT_RECEIVED | ((In a 1v2 two-solicitor case only one defendant has responded with a full admission and the second is outstanding.) AND (NOT (At least one defendant has acknowledged service (per scenario rules).))) AND (NOT (A defendant has obtained a time extension to respond.)) | multi-party, unspec |
| AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED | (((In a 1v2 two-solicitor case only one defendant has provided a full defence and the other is yet to respond.) AND (NOT (At least one defendant has acknowledged service (per scenario rules).))) AND (NOT (A defendant has obtained a time extension to respond.))) AND (NOT (Claim details notification deadline expired with no response or staff action.)) | multi-party, unspec |
| AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED | ((In a 1v2 two-solicitor case a single defendant has responded with something other than full defence/admission while the co-defendant is outstanding.) AND (NOT (At least one defendant has acknowledged service (per scenario rules).))) AND (NOT (A defendant has obtained a time extension to respond.)) | multi-party, unspec |
| CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION | ((A defendant has obtained a time extension to respond.) AND (NOT (At least one defendant has acknowledged service (per scenario rules).))) AND (NOT (Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline).)) | unspec |
| IN_HEARING_READINESS | Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline). | unspec |
| NOTIFICATION_ACKNOWLEDGED | (At least one defendant has acknowledged service (per scenario rules).) AND (NOT (Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline).)) | unspec |
| PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA | (Claim details notification deadline expired with no response or staff action.) AND (NOT (Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline).)) | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after claim details were notified (and before any responses/time extensions). | offline/timeout, unspec |
| TAKEN_OFFLINE_SDO_NOT_DRAWN | Case marked not suitable for SDO and taken offline after claim details notification. | offline/timeout, unspec |

## CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| ALL_RESPONSES_RECEIVED | (A defendant has obtained a time extension to respond.) AND (Every required defendant response has been received) | unspec |
| AWAITING_RESPONSES_FULL_ADMIT_RECEIVED | (In a 1v2 two-solicitor case only one defendant has responded with a full admission and the second is outstanding.) AND (A defendant has obtained a time extension to respond.) | multi-party, unspec |
| AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED | ((In a 1v2 two-solicitor case only one defendant has provided a full defence and the other is yet to respond.) AND (A defendant has obtained a time extension to respond.)) AND (NOT (Claim details notification deadline expired while an extension applied and no responses or staff action occurred.)) | multi-party, unspec |
| AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED | (In a 1v2 two-solicitor case a single defendant has responded with something other than full defence/admission while the co-defendant is outstanding.) AND (A defendant has obtained a time extension to respond.) | multi-party, unspec |
| NOTIFICATION_ACKNOWLEDGED | At least one defendant has acknowledged service (per scenario rules). | unspec |
| PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA | Claim details notification deadline expired while an extension applied and no responses or staff action occurred. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after claim details were notified while an extension was active. | offline/timeout, unspec |
| TAKEN_OFFLINE_SDO_NOT_DRAWN | Case marked not suitable for SDO and taken offline during a claim-details extension. | offline/timeout, unspec |

## CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |

## CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |

## CLAIM_ISSUED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| AWAITING_RESPONSES_FULL_ADMIT_RECEIVED | (SPEC analogue: only one defendant has submitted a full admission and the matching response from the other side is pending.) AND (Case is in the SPEC (damages) service.) | multi-party, unspec |
| AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED | (SPEC analogue: one defendant has filed a full defence and the paired response is pending.) AND (Case is in the SPEC (damages) service.) | multi-party, unspec |
| AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED | (SPEC analogue: a non full-defence/admission response was received while the paired response is still outstanding.) AND (Case is in the SPEC (damages) service.) | multi-party, unspec |
| CLAIM_NOTIFIED | Claim notification has been sent (acknowledgement deadline exists). | unspec |
| CONTACT_DETAILS_CHANGE | Contact details change event has been triggered on the case. | unspec |
| COUNTER_CLAIM | ((Latest defence is a counter claim for SPEC.) AND (NOT (Contact details change event has been triggered on the case.))) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.)) | unspec |
| DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE | (SPEC divergent responses requiring DQs before going offline.) AND (Case is in the SPEC (damages) service.) | unspec |
| DIVERGENT_RESPOND_GO_OFFLINE | (SPEC defendant/claimant responses diverge so the case must go offline.) AND (Case is in the SPEC (damages) service.) | unspec |
| FULL_ADMISSION | ((Latest defence is a full admission in SPEC.) AND (NOT (Contact details change event has been triggered on the case.))) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.)) | unspec |
| FULL_DEFENCE | (((Latest defence is a full defence in SPEC.) AND (NOT (Contact details change event has been triggered on the case.))) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.))) AND (NOT (Claim notification deadline has expired.)) | unspec |
| PART_ADMISSION | ((Latest defence is a part admission in SPEC.) AND (NOT (Contact details change event has been triggered on the case.))) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.)) | unspec |
| PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA | Claim notification deadline has expired. | offline/timeout, unspec |
| RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL | (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.) AND (NOT (Contact details change event has been triggered on the case.)) | unspec |
| TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED | Claim was taken offline after notification but before claim details were served. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline between claim submission and issue. | offline/timeout, unspec |

## CLAIM_ISSUED_PAYMENT_FAILED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL | Card payment succeeded for the claim issue fee. | unspec |

## CLAIM_ISSUED_PAYMENT_SUCCESSFUL
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| PENDING_CLAIM_ISSUED | Case ready to move from payment success to pending issue (represented defendants with registered orgs). | unspec |
| PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT | ((((First defendant is represented but their organisation is not registered.) AND (NOT (First defendant is unrepresented.))) AND ((Second defendant is represented but their organisation is not registered.) AND (NOT (Second defendant is unrepresented.)))) OR (((First defendant is represented but their organisation is not registered.) AND (NOT (First defendant is unrepresented.))) AND ((NOT (Second defendant is represented but their organisation is not registered.)) AND (NOT (Second defendant is unrepresented.))))) OR ((((NOT (First defendant is represented but their organisation is not registered.)) AND (NOT (First defendant is unrepresented.))) AND ((Second defendant is represented but their organisation is not registered.) AND (NOT (Second defendant is unrepresented.)))) AND (NOT (Both defendants share the same legal representative.))) | unspec |
| PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT | At least one defendant is unrepresented (with scenario-specific rules) so the LiP pending issue state applies. | lip, unspec |
| PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC | ((Case is a single claimant v single defendant matter.) AND (First defendant is unrepresented.)) AND (Case is in the SPEC (damages) service.) | lip, spec |
| PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT | ((First defendant is unrepresented.) AND ((Second defendant is represented but their organisation is not registered.) AND (NOT (Second defendant is unrepresented.)))) OR (((First defendant is represented but their organisation is not registered.) AND (NOT (First defendant is unrepresented.))) AND (Second defendant is unrepresented.)) | lip, unspec |

## CLAIM_NOTIFIED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_DETAILS_NOTIFIED | Claim details have been successfully notified. | unspec |
| PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA | Claim details notification deadline has expired. | offline/timeout, unspec |
| TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED | Claim was taken offline because not all defendants were notified of claim details online. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after notification but before claim details were served. | offline/timeout, unspec |

## CLAIM_SUBMITTED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_ISSUED_PAYMENT_FAILED | Card payment failed for the claim issue fee. | unspec |
| CLAIM_ISSUED_PAYMENT_SUCCESSFUL | Card payment succeeded for the claim issue fee. | unspec |
| PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC | (Case is LiP v LiP in a 1v1 scenario.) AND (NOT (Staff took the case offline before the claim was issued.)) | lip, spec |
| PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC | Notice of Change submitted for a LiP claimant. | lip, spec |
| PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC | (NOT (Defendant Notice of Change online feature flag is active for this case.)) AND (((Case is LiP v represented (one party self-represented and the other legally represented) in a 1v1.) AND (NOT (Notice of Change submitted for a LiP defendant.))) AND (NOT (LiP defendant submitted a Notice of Change before the case went offline.))) | lip, spec |
| PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC | (Defendant Notice of Change online feature flag is active for this case.) AND (Case is LiP v represented (one party self-represented and the other legally represented) in a 1v1.) | lip, spec |
| SPEC_DEFENDANT_NOC | (NOT (Defendant Notice of Change online feature flag is active for this case.)) AND (LiP defendant submitted a Notice of Change before the case went offline.) | spec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline before the claim was issued. | offline/timeout, unspec |

## CONTACT_DETAILS_CHANGE
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| COUNTER_CLAIM | (Latest defence is a counter claim for SPEC.) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.)) | unspec |
| FULL_ADMISSION | (Latest defence is a full admission in SPEC.) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.)) | unspec |
| FULL_DEFENCE | (Latest defence is a full defence in SPEC.) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.)) | unspec |
| PART_ADMISSION | (Latest defence is a part admission in SPEC.) AND (NOT (Respondent marked their response as bilingual but has not requested ongoing bilingual processing.)) | unspec |
| RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL | Respondent marked their response as bilingual but has not requested ongoing bilingual processing. | unspec |

## DRAFT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_SUBMITTED | (Single defendant representative covers all parties.) OR (1v1 claim where the respondent representative is not registered.) | unspec |
| CLAIM_SUBMITTED | ((Two defendants, both represented by registered organisations.) OR (Two defendants with separate representatives, one organisation unregistered.)) OR (Both defendant solicitors are unregistered organisations.) | unspec |
| CLAIM_SUBMITTED | Only defendant is unrepresented. | unspec |
| CLAIM_SUBMITTED | ((Defendant 1 is unrepresented.) AND (NOT (Only defendant is unrepresented.))) AND (NOT (Defendant 2 is unrepresented.)) | unspec |
| CLAIM_SUBMITTED | (Defendant 2 is unrepresented.) AND (NOT (Defendant 1 is unrepresented.)) | unspec |
| CLAIM_SUBMITTED | (Defendant 1 is unrepresented.) AND (Defendant 2 is unrepresented.) | unspec |

## FULL_ADMISSION
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| FULL_ADMIT_AGREE_REPAYMENT | Applicant accepts the defendant's repayment plan (for LiP 1v1 the case must remain online). | settlement/Judgment, unspec |
| FULL_ADMIT_JUDGMENT_ADMISSION | ((Applicant has requested a CCJ by admission.) AND (Defendant has proposed to pay the admitted amount immediately.)) AND (Case is LiP v LiP in a 1v1 scenario.) | settlement/Judgment, unspec |
| FULL_ADMIT_NOT_PROCEED | Claimant indicates they do not wish to proceed against the defendant(s). | unspec |
| FULL_ADMIT_PAY_IMMEDIATELY | SPEC 1v1 full admission where the defendant proposes to pay immediately and claimant has not decided whether to proceed. | unspec |
| FULL_ADMIT_PROCEED | Claimant elects to proceed against the defendant(s). | unspec |
| FULL_ADMIT_REJECT_REPAYMENT | Applicant rejected the defendant’s repayment plan (and case remains online for LiP 1v1). | settlement/Judgment, unspec |
| PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA | Claimant response deadline has passed with no response and staff have not taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | (Staff took the case offline after a defendant response and before claimant action.) AND (NOT (Applicant has requested a CCJ by admission.)) | offline/timeout, unspec |
| TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA | ((Defendant Notice of Change online feature flag is active for this case.) AND (Defendant has proposed to pay the admitted amount immediately.)) AND (Case is LiP with an active judgment by admission and a post-judgment Notice of Change has occurred.) | offline/timeout, spec |

## FULL_ADMIT_AGREE_REPAYMENT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| SIGN_SETTLEMENT_AGREEMENT | Defendant has signed the digital settlement agreement. | settlement/Judgment, unspec |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA | (Defendant Notice of Change online feature flag is active for this case.) AND (Case is LiP with an active judgment by admission and a post-judgment Notice of Change has occurred.) | offline/timeout, spec |

## FULL_ADMIT_JUDGMENT_ADMISSION
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |

## FULL_ADMIT_PAY_IMMEDIATELY
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| FULL_ADMIT_JUDGMENT_ADMISSION | (Applicant has requested a CCJ by admission.) AND (Defendant has proposed to pay the admitted amount immediately.) | settlement/Judgment, unspec |
| TAKEN_OFFLINE_BY_STAFF | (HMCTS staff have manually taken the case offline.) AND (NOT (Applicant has requested a CCJ by admission.)) | offline/timeout, unspec |

## FULL_ADMIT_REJECT_REPAYMENT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |

## FULL_DEFENCE
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| FULL_DEFENCE_NOT_PROCEED | Claimant indicates they do not wish to proceed against the defendant(s). | unspec |
| FULL_DEFENCE_PROCEED | (((((Claimant elects to proceed against the defendant(s).) AND (In a SPEC small claim all represented parties have agreed to legal representative mediation (and no party has opted out).)) AND (NOT (Claimant opted into free mediation.))) AND (NOT (Claimant has declined free mediation.))) AND (NOT (Case meets CARM pilot criteria for LiP participation in SPEC small claims.))) AND (NOT (Case meets CARM pilot criteria for represented parties in SPEC small claims.)) | unspec |
| FULL_DEFENCE_PROCEED | (((Claimant elects to proceed against the defendant(s).) AND (((NOT (In a SPEC small claim all represented parties have agreed to legal representative mediation (and no party has opted out).)) AND (NOT (Claimant opted into free mediation.))) OR (Claimant has declined free mediation.))) AND (NOT (Claimant response deadline has passed with no response and staff have not taken the case offline.))) AND (Allocated track is multi-track for an UNSPEC claim.) | unspec |
| FULL_DEFENCE_PROCEED | ((((((Claimant elects to proceed against the defendant(s).) AND (NOT (Case meets CARM pilot criteria for LiP participation in SPEC small claims.))) AND (NOT (Case meets CARM pilot criteria for represented parties in SPEC small claims.))) AND (((NOT (In a SPEC small claim all represented parties have agreed to legal representative mediation (and no party has opted out).)) AND (NOT (Claimant opted into free mediation.))) OR (Claimant has declined free mediation.))) AND (NOT (Claimant response deadline has passed with no response and staff have not taken the case offline.))) AND (NOT (Allocated track is multi-track for an UNSPEC claim.))) AND ((NOT (Case is LiP v LiP in a 1v1 scenario.)) AND (NOT (Case is LiP v represented (1v1).))) | unspec |
| FULL_DEFENCE_PROCEED | ((((((Claimant elects to proceed against the defendant(s).) OR (Claimant indicates they will not settle after a full defence.)) OR (Claimant states the defendant has not paid the amount ordered on a full defence outcome.)) OR (LiP claimant has opted to proceed following a full defence response.)) AND (NOT (Claimant opted into free mediation.))) AND (NOT (Case meets CARM pilot criteria for LiP participation in SPEC small claims.))) AND ((Case is LiP v LiP in a 1v1 scenario.) OR (Case is LiP v represented (1v1).)) | unspec |
| FULL_DEFENCE_PROCEED | (Claimant indicates the claim has been settled.) AND (NOT (Claimant opted into free mediation.)) | unspec |
| IN_MEDIATION | ((Claimant opted into free mediation.) AND (NOT (In a SPEC small claim all represented parties have agreed to legal representative mediation (and no party has opted out).))) AND (NOT (Claimant indicates they do not wish to proceed against the defendant(s).)) | unspec |
| IN_MEDIATION | (Case meets CARM pilot criteria for represented parties in SPEC small claims.) AND (Claimant elects to proceed against the defendant(s).) | unspec |
| PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA | Claimant response deadline has passed with no response and staff have not taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after a defendant response and before claimant action. | offline/timeout, unspec |

## FULL_DEFENCE_PROCEED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE | Hearing fee due date has passed without payment. | offline/timeout, unspec |
| IN_HEARING_READINESS | Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline). | unspec |
| TAKEN_OFFLINE_AFTER_SDO | Claim was taken offline after directions (SDO) were drawn. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | (((Staff took the case offline after claimant response but before an SDO was drawn.) OR (Staff took the case offline after an SDO was drawn.)) OR (Claim flagged as not suitable for SDO and taken offline.)) AND (NOT (Hearing fee due date has passed without payment.)) | offline/timeout, unspec |
| TAKEN_OFFLINE_SDO_NOT_DRAWN | Case was taken offline because it was marked not suitable for an SDO. | offline/timeout, unspec |

## IN_MEDIATION
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| MEDIATION_UNSUCCESSFUL_PROCEED | Mediation service recorded the session as unsuccessful. | mediation, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline before mediation recorded an outcome. | mediation, offline/timeout, unspec |

## MEDIATION_UNSUCCESSFUL_PROCEED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE | (Hearing fee due date has passed without payment.) AND (NOT (HMCTS staff have manually taken the case offline.)) | offline/timeout, unspec |
| IN_HEARING_READINESS | Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline). | unspec |
| TAKEN_OFFLINE_AFTER_SDO | Claim was taken offline after directions (SDO) were drawn. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_SDO_NOT_DRAWN | Case was taken offline because it was marked not suitable for an SDO. | offline/timeout, unspec |

## NOTIFICATION_ACKNOWLEDGED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| ALL_RESPONSES_RECEIVED | ((At least one defendant has acknowledged service (per scenario rules).) AND (NOT (A defendant has obtained a time extension to respond.))) AND (Every required defendant response has been received) | unspec |
| AWAITING_RESPONSES_FULL_ADMIT_RECEIVED | ((At least one defendant has acknowledged service (per scenario rules).) AND (NOT (A defendant has obtained a time extension to respond.))) AND (In a 1v2 two-solicitor case only one defendant has responded with a full admission and the second is outstanding.) | multi-party, unspec |
| AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED | (((At least one defendant has acknowledged service (per scenario rules).) AND (NOT (A defendant has obtained a time extension to respond.))) AND (In a 1v2 two-solicitor case only one defendant has provided a full defence and the other is yet to respond.)) AND (NOT (Claim was acknowledged but the response deadline has expired without a response and staff have not intervened.)) | multi-party, unspec |
| AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED | ((At least one defendant has acknowledged service (per scenario rules).) AND (NOT (A defendant has obtained a time extension to respond.))) AND (In a 1v2 two-solicitor case a single defendant has responded with something other than full defence/admission while the co-defendant is outstanding.) | multi-party, unspec |
| NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION | (At least one defendant has acknowledged service (per scenario rules).) AND (A defendant has obtained a time extension to respond.) | unspec |
| PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA | (Claim was acknowledged but the response deadline has expired without a response and staff have not intervened.) AND (NOT (Claimant flagged that the case is not suitable for an SDO.)) | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after service acknowledgement and before defence/extension. | offline/timeout, unspec |
| TAKEN_OFFLINE_SDO_NOT_DRAWN | Case marked not suitable for SDO and taken offline after service acknowledgement. | offline/timeout, unspec |

## NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| ALL_RESPONSES_RECEIVED | ((((At least one defendant has acknowledged service (per scenario rules).) AND (A defendant has obtained a time extension to respond.)) AND (Every required defendant response has been received)) AND (NOT (claimDismissalOutOfTime))) AND (NOT (HMCTS staff have manually taken the case offline.)) | unspec |
| AWAITING_RESPONSES_FULL_ADMIT_RECEIVED | ((At least one defendant has acknowledged service (per scenario rules).) AND (A defendant has obtained a time extension to respond.)) AND (In a 1v2 two-solicitor case only one defendant has responded with a full admission and the second is outstanding.) | multi-party, unspec |
| AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED | ((((At least one defendant has acknowledged service (per scenario rules).) AND (A defendant has obtained a time extension to respond.)) AND (In a 1v2 two-solicitor case only one defendant has provided a full defence and the other is yet to respond.)) AND (NOT (Claim was acknowledged, an extension applied, and the dismissal deadline passed without response or staff action.))) AND (NOT (Staff took the case offline after acknowledgement while a time extension was in play.)) | multi-party, unspec |
| AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED | ((At least one defendant has acknowledged service (per scenario rules).) AND (A defendant has obtained a time extension to respond.)) AND (In a 1v2 two-solicitor case a single defendant has responded with something other than full defence/admission while the co-defendant is outstanding.) | multi-party, unspec |
| PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA | Claim was acknowledged, an extension applied, and the dismissal deadline passed without response or staff action. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after acknowledgement while a time extension was in play. | offline/timeout, unspec |
| TAKEN_OFFLINE_SDO_NOT_DRAWN | Case marked not suitable for SDO and taken offline after acknowledgement while an extension applied. | offline/timeout, unspec |

## PART_ADMISSION
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| IN_MEDIATION | ((((Claimant opted into free mediation.) AND (NOT (HMCTS staff have manually taken the case offline.))) AND (NOT (Defendant offered to pay the part-admitted amount immediately and claimant accepted.))) AND (NOT (Applicant accepts the defendant's repayment plan (for LiP 1v1 the case must remain online).))) AND (NOT (Applicant rejected the defendant’s repayment plan (and case remains online for LiP 1v1).)) | unspec |
| IN_MEDIATION | carmMediation | mediation, unspec |
| PART_ADMIT_AGREE_REPAYMENT | Applicant accepts the defendant's repayment plan (for LiP 1v1 the case must remain online). | settlement/Judgment, unspec |
| PART_ADMIT_AGREE_SETTLE | Claimant confirms the part-admission offer settles the dispute. | settlement/Judgment, unspec |
| PART_ADMIT_NOT_PROCEED | Claimant indicates they do not wish to proceed against the defendant(s). | unspec |
| PART_ADMIT_NOT_SETTLED_NO_MEDIATION | ((((Claimant indicates they will not settle after a part admission.) AND (NOT (Claimant opted into free mediation.))) AND (NOT (Case meets CARM pilot criteria for represented parties in SPEC small claims.))) AND (NOT (Case meets CARM pilot criteria for LiP participation in SPEC small claims.))) AND (NOT (HMCTS staff have manually taken the case offline.)) | unspec |
| PART_ADMIT_PAY_IMMEDIATELY | Defendant offered to pay the part-admitted amount immediately and claimant accepted. | unspec |
| PART_ADMIT_PROCEED | partAdmitProceed | unspec |
| PART_ADMIT_REJECT_REPAYMENT | Applicant rejected the defendant’s repayment plan (and case remains online for LiP 1v1). | settlement/Judgment, unspec |
| PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA | Claimant response deadline has passed with no response and staff have not taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | Staff took the case offline after a defendant response and before claimant action. | offline/timeout, unspec |

## PART_ADMIT_AGREE_REPAYMENT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| SIGN_SETTLEMENT_AGREEMENT | Defendant has signed the digital settlement agreement. | settlement/Judgment, unspec |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA | (Defendant Notice of Change online feature flag is active for this case.) AND (Case is LiP with an active judgment by admission and a post-judgment Notice of Change has occurred.) | offline/timeout, spec |

## PART_ADMIT_AGREE_SETTLE
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |

## PART_ADMIT_NOT_SETTLED_NO_MEDIATION
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE | Hearing fee due date has passed without payment. | offline/timeout, unspec |
| IN_HEARING_READINESS | Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline). | unspec |
| TAKEN_OFFLINE_AFTER_SDO | Claim was taken offline after directions (SDO) were drawn. | offline/timeout, unspec |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_SDO_NOT_DRAWN | Case was taken offline because it was marked not suitable for an SDO. | offline/timeout, unspec |

## PART_ADMIT_PAY_IMMEDIATELY
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |
| TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA | (Defendant Notice of Change online feature flag is active for this case.) AND (Case is LiP with an active judgment by admission and a post-judgment Notice of Change has occurred.) | offline/timeout, spec |

## PART_ADMIT_REJECT_REPAYMENT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_BY_STAFF | HMCTS staff have manually taken the case offline. | offline/timeout, unspec |

## PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE | Automation has already processed the out-of-time applicant response. | offline/timeout, unspec |

## PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE | Camunda automation has already dismissed the claim. | offline/timeout, unspec |

## PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE | Camunda automation has already dismissed the claim. | offline/timeout, unspec |

## PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE | Camunda automation has already dismissed the claim. | offline/timeout, unspec |

## PENDING_CLAIM_ISSUED
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_ISSUED | Claim has been issued (a notification deadline exists). | unspec |

## PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_UNREGISTERED_DEFENDANT | System automatically took the case offline (for example because of representation changes). | offline/timeout, unspec |

## PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_ISSUED | ((Claim has been issued (a notification deadline exists).) AND (NOT (Case is in the SPEC (damages) service.))) AND (Certificate of Service workflow is enabled for this case.) | lip, unspec |
| TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT | (System automatically took the case offline (for example because of representation changes).) AND (Case is in the SPEC (damages) service.) | lip, offline/timeout, unspec |

## PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_ISSUED | (Claim has been issued (a notification deadline exists).) AND (PIN-in-Post feature is enabled and the defendant is a LiP.) | lip, spec |
| TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT | (System automatically took the case offline (for example because of representation changes).) AND (NOT (PIN-in-Post feature is enabled and the defendant is a LiP.)) | lip, offline/timeout, spec |

## PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT | System automatically took the case offline (for example because of representation changes). | lip, offline/timeout, unspec |

## RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| COUNTER_CLAIM | (Latest defence is a counter claim for SPEC.) AND (Translated response documents have been uploaded.) | unspec |
| FULL_ADMISSION | (Latest defence is a full admission in SPEC.) AND (Translated response documents have been uploaded.) | unspec |
| FULL_DEFENCE | (Latest defence is a full defence in SPEC.) AND (Translated response documents have been uploaded.) | unspec |
| PART_ADMISSION | (Latest defence is a part admission in SPEC.) AND (Translated response documents have been uploaded.) | unspec |

## SIGN_SETTLEMENT_AGREEMENT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| FULL_ADMIT_JUDGMENT_ADMISSION | Applicant has requested a CCJ by admission. | settlement/Judgment, unspec |

## SPEC_DEFENDANT_NOC
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| TAKEN_OFFLINE_SPEC_DEFENDANT_NOC | (NOT (Defendant Notice of Change online feature flag is active for this case.)) AND (Notice of Change submitted for a LiP defendant.) | offline/timeout, spec |

## SPEC_DRAFT
| To state | Business condition | Scenario tags |
| --- | --- | --- |
| CLAIM_SUBMITTED | (Single defendant representative covers all parties.) OR (1v1 claim where the respondent representative is not registered.) | spec |
| CLAIM_SUBMITTED | ((Two defendants, both represented by registered organisations.) OR (Two defendants with separate representatives, one organisation unregistered.)) OR (Both defendant solicitors are unregistered organisations.) | spec |
| CLAIM_SUBMITTED | Only defendant is unrepresented. | spec |
| CLAIM_SUBMITTED | ((Defendant 1 is unrepresented.) AND (NOT (Only defendant is unrepresented.))) AND (NOT (Defendant 2 is unrepresented.)) | spec |
| CLAIM_SUBMITTED | (Defendant 2 is unrepresented.) AND (NOT (Defendant 1 is unrepresented.)) | spec |
| CLAIM_SUBMITTED | (Defendant 1 is unrepresented.) AND (Defendant 2 is unrepresented.) | spec |
