# Civil Service Business Rules

> This file is auto-generated from the source code. Do not edit manually.

## Composed predicates (Business Rules)

| Group | Rule Name | Logic Description |
|---|---|---|
| **Claim** | `afterIssued` | No claim‑details notification yet; respondent has not acknowledged service or responded; claim notification deadline is in the future. For SPEC, a notification date exists; for UNSPEC, it does not |
| **Claim** | `changeOfRepresentation` | A change of legal representation has been recorded on the case |
| **Claim** | `isSpec` | Case is in the SPEC (damages) service based on case access category |
| **Claim** | `issued` | Acknowledgement deadline exists - claim notification has been sent (State Flow: claim notified) |
| **Claim** | `pendingIssued` | Issue date is set and all represented defendants have registered organisations (second defendant absent or registered/same solicitor). Used for moving to pending issue |
| **Claim** | `pendingIssuedUnrepresented` | Issue date is set and at least one defendant is unrepresented. Applies to all UNSPEC, and to SPEC only for multi‑party scenarios |
| **Claim** | `submitted` | Claim has a submitted date (claim has been submitted) |
| **Claim** | `submitted1v1RespondentOneUnregistered` | Submitted 1v1 claim where defendant is represented but their organisation is not registered |
| **Claim** | `submittedBothUnregisteredSolicitors` | Submitted claim with two defendants, each represented by different solicitors and both defendant organisations are not registered |
| **Claim** | `submittedOneRespondentRepresentative` | Submitted claim where respondent 1 is represented and either there is a single defendant, or there are two defendants sharing the same legal representative |
| **Claim** | `submittedOneUnrepresentedDefendantOnly` | Submitted claim with a single unrepresented defendant (no second defendant) |
| **Claim** | `submittedRespondent1Unrepresented` | Submitted claim where respondent 1 is unrepresented |
| **Claim** | `submittedRespondent2Unrepresented` | Submitted claim where respondent 2 is unrepresented |
| **Claim** | `submittedTwoRegisteredRespondentRepresentatives` | Submitted claim with two defendants, each represented by different solicitors and both defendant organisations are registered |
| **Claim** | `submittedTwoRespondentRepresentativesOneUnregistered` | Submitted claim with two defendants, each represented by different solicitors where exactly one defendant organisation is not registered |
| **Claimant** | `beforeResponse` | Applicant initial response has not been recorded yet (for UNSPEC with applicant 2, neither applicant has responded) |
| **Claimant** | `fullDefenceNotProceed` | Applicant has decided not to proceed with the claim. In UNSPEC 1v2, 'not proceed' is recorded against both defendants; in 2v1, both applicants record 'not proceed'; in 1v1, a single 'not proceed' decision applies |
| **Claimant** | `fullDefenceProceed` | Applicant has decided to proceed with the claim (SPEC/UNSPEC, 1v1/1v2/2v1). In UNSPEC 1v2, proceeding against at least one defendant qualifies; in 2v1, at least one applicant chooses to proceed |
| **Dismissed** | `afterClaimAcknowledged` | Dismissal deadline has passed; at least respondent 1 acknowledged service and has no time extension; not taken offline by staff; in 1v2 two‑solicitor cases both acknowledged with at least one still without a response |
| **Dismissed** | `afterClaimAcknowledgedExtension` | Dismissal deadline has passed; respondent(s) acknowledged service and at least one time extension applies; not marked 'not suitable for SDO' and not taken offline by staff |
| **Dismissed** | `afterClaimDetailNotified` | Dismissal deadline has passed; no acknowledgement, no response, no time extension or intention to proceed recorded; not taken offline by staff (supports 1v1 and 1v2 scenarios) |
| **Dismissed** | `afterClaimNotifiedExtension` | Dismissal deadline has passed; no respondent intends to proceed; at least one respondent with a time extension has not acknowledged service |
| **Dismissed** | `byCamunda` | Claim has a recorded dismissed date indicating automated processing (Camunda) |
| **Dismissed** | `pastClaimDeadline` | Case dismissed because the claim dismissal deadline has passed |
| **Dismissed** | `pastClaimDetailsNotificationDeadline` | Claim details notification deadline has passed with no claim details notification date set (claim notification exists) |
| **Dismissed** | `pastClaimNotificationDeadline` | Claim notification deadline has passed and no claim notification date is set |
| **Dismissed** | `pastHearingFeeDue` | Case dismissed because the hearing fee due date has passed |
| **Divergence** | `divergentRespondGoOffline` | Defendant responses diverge so the case must go offline. |
| **Divergence** | `divergentRespondGoOfflineSpec` | SPEC defendant/claimant responses diverge so the case must go offline. |
| **Divergence** | `divergentRespondWithDQAndGoOffline` | Responses diverge and discrete directions questionnaires must be produced before going offline. |
| **Divergence** | `divergentRespondWithDQAndGoOfflineSpec` | SPEC divergent responses requiring DQs before going offline. |
| **Hearing** | `isInReadiness` | Hearing has been listed and the case is in hearing readiness (no dismissals/taken offline) |
| **Language** | `claimantIsBilingual` | Claimant has bilingual language preference |
| **Language** | `onlyInitialResponseIsBilingual` | Respondent marked their response as bilingual but has not requested ongoing bilingual processing |
| **Language** | `respondentIsBilingual` | Respondent indicated their response is bilingual |
| **Lip** | `agreedToMediation` | Claimant has opted into free mediation |
| **Lip** | `caseContainsLiP` | At least one party (applicant or respondent) is a litigant-in-person |
| **Lip** | `ccjRequestJudgmentByAdmission` | Applicant has requested a County Court Judgment (CCJ) by admission |
| **Lip** | `fullDefenceProceed` | SPEC claim and applicant 1 has not settled the claim |
| **Lip** | `isHelpWithFees` | A litigant-in-person has an active Help With Fees application |
| **Lip** | `isLiPvLRCase` | One party is a litigant-in-person (LiP) and the other is legally represented |
| **Lip** | `isLiPvLiPCase` | Both applicant and respondent are unrepresented (litigants in person) |
| **Lip** | `isRespondentSignSettlementAgreement` | Respondent has signed the digital settlement agreement |
| **Lip** | `isTranslatedDocumentUploaded` | A translated response document has been uploaded |
| **Lip** | `nocApplyForLiPClaimant` | A Notice of Change was submitted for an unrepresented claimant (LiP) |
| **Lip** | `nocSubmittedForLiPDefendant` | A Notice of Change was submitted for an unrepresented defendant (LiP) |
| **Lip** | `nocSubmittedForLiPDefendantBeforeOffline` | A Notice of Change was submitted for an unrepresented defendant (LiP) before the case was taken offline |
| **Lip** | `pinInPostEnabled` | PIN-in-post service is enabled for this case |
| **Notification** | `hasClaimDetailsNotifiedToBoth` | Claim details notification date exists and defendant notify options are either not set or set to 'Both' |
| **Notification** | `hasClaimNotifiedToBoth` | Claim notification date exists and defendant notify options are either not set or set to 'Both' |
| **Notification** | `hasNotifyOptionsBoth` | Claim details notification option was set to 'Both' in the defendant notification options |
| **Notification** | `notifiedTimeExtension` | A defendant has obtained a time extension to respond and has not acknowledged service (claim details) |
| **OutOfTime** | `notBeingTakenOffline` | Applicant response deadline passed, applicant has not responded and staff offline date does not exist |
| **OutOfTime** | `processedByCamunda` | Case has been taken offline (takenOffline date present) indicating automated processing of out-of-time cases |
| **Payment** | `failed` | Card payment for the issue fee failed (or claim issue recorded as failed) and the applicant is represented |
| **Payment** | `payImmediatelyFullAdmission` | SPEC 1v1 full admission where 'when to be paid' is set and the applicant chose not to proceed |
| **Payment** | `payImmediatelyPartAdmission` | Part admission where the payment time selected is 'IMMEDIATELY' |
| **Payment** | `successful` | Card payment for the issue fee succeeded (or claim issue recorded as successful) and the applicant is represented |
| **Repayment** | `acceptRepaymentPlan` | Applicant accepted the proposed repayment plan; if LiP v LiP, the case has not been taken offline by staff |
| **Repayment** | `rejectRepaymentPlan` | Applicant rejected the proposed repayment plan; if LiP v LiP, the case has not been taken offline by staff |
| **Response** | `allResponsesReceived` | All required defendant responses for the current multi-party scenario have been received (State Flow 'ALL_RESPONSES_RECEIVED') |
| **Response** | `awaitingResponsesFullAdmitReceived` | In a 1v2 two‑solicitor case one defendant has provided a full admission and the co‑defendant is yet to respond |
| **Response** | `awaitingResponsesFullAdmitReceivedSpec` | In a 1v2 two‑solicitor SPEC case one defendant has provided a full admission and the co‑defendant is yet to respond |
| **Response** | `awaitingResponsesFullDefenceReceived` | In a 1v2 two‑solicitor case one defendant has provided a full defence and the co‑defendant is yet to respond |
| **Response** | `awaitingResponsesFullDefenceReceivedSpec` | In a 1v2 two‑solicitor SPEC case one defendant has provided a full defence and the co‑defendant is yet to respond |
| **Response** | `awaitingResponsesNonFullDefenceOrFullAdmitReceived` | In a 1v2 two‑solicitor case one defendant has provided a response other than full defence / admission and the co‑defendant is yet to respond |
| **Response** | `awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec` | In a 1v2 two‑solicitor SPEC case one defendant has provided a response other than full defence / admission and the co‑defendant is yet to respond |
| **Response** | `isOneVOneResponseFlagSpec` | Flag indicating a one‑v‑one response was provided (used in SPEC response routing) |
| **Response** | `isType()` | Checks if the respondent response type(s) for SPEC match the given RespondentResponseTypeSpec for multi‑party scenario |
| **Response** | `isType()` | Checks if respondent response type(s) for UNSPEC match the given RespondentResponseType for multi‑party scenario |
| **Response** | `notificationAcknowledged` | At least one required defendant has acknowledged service (matches State Flow 'NOTIFICATION_ACKNOWLEDGED') |
| **Response** | `respondentTimeExtension` | A defendant has obtained a time extension to respond |
| **TakenOffline** | `afterClaimDetailsNotified` | Claim details notification date exists and notify options were set (not 'Both') |
| **TakenOffline** | `afterClaimNotified` | Claim notification date exists and notify options were set (not 'Both') |
| **TakenOffline** | `afterClaimNotifiedAckExtension` | Respondent(s) acknowledged service; time extension(s) granted |
| **TakenOffline** | `afterClaimNotifiedAckNoResponseExtension` | Respondent(s) acknowledged service; no response yet; time extension(s) granted |
| **TakenOffline** | `afterClaimNotifiedAckNoResponseNoExtension` | Respondent(s) acknowledged service; no response yet; no time extension |
| **TakenOffline** | `afterClaimNotifiedExtension` | Respondent 1 has a time extension but has not acknowledged service or responded |
| **TakenOffline** | `afterClaimNotifiedFutureDeadline` | After claim notified but before claim details notification and any respondent acknowledgement |
| **TakenOffline** | `afterClaimNotifiedNoAckNoResponseNoExtension` | Taken offline by staff with no respondent acknowledgment and no time extension |
| **TakenOffline** | `afterSdo` | Draw-directions-order required; NOT marked 'not suitable for SDO' |
| **TakenOffline** | `afterSdoNotSuitable` | Draw-directions-order NOT required; marked 'not suitable for SDO' |
| **TakenOffline** | `beforeClaimIssue` | Case submitted; claim notification NOT set (no notification deadline/date) |
| **TakenOffline** | `beforeSdo` | Applicant response date present; draw-directions-order NOT required; NOT marked 'not suitable for SDO' |
| **TakenOffline** | `byStaff` | Manual taken-offline marker present (takenOfflineByStaff date exists) |
| **TakenOffline** | `bySystem` | System-driven taken-offline marker present (takenOffline date exists) |
| **TakenOffline** | `isDefendantNoCOnlineForCaseAfterJBA` | Case taken offline via Camunda/automation (takenOffline date present) where LiP NoC/JO by admission and representation change apply |
| **TakenOffline** | `sdoNotDrawn` | Case flagged 'not suitable for SDO' and a taken-offline date exists |
| **TakenOffline** | `sdoNotSuitable` | Case flagged 'not suitable for SDO' |

---

## Atomic predicates (CaseData Predicates)

| Group | Rule Name | Logic Description |
|---|---|---|
| **Applicant** | `hasPassedResponseDeadline` | Applicant response deadline exists and is before now (deadline expired) |
| **Applicant** | `hasProceedAgainstRespondent1_1v2` | Proceed-decision field exists for applicant against respondent 1 in 1v2 scenario |
| **Applicant** | `hasProceedAgainstRespondent2_1v2` | Proceed-decision field exists for applicant against respondent 2 in 1v2 scenario |
| **Applicant** | `hasProceedApplicant2Multi_2v1` | Proceed-decision field present for applicant 2 in 2v1 multi-party scenario |
| **Applicant** | `hasProceedDecision` | There is a recorded proceed / not-proceed decision for the applicant |
| **Applicant** | `hasProceedDecisionSpec2v1` | Proceed-decision field present for SPEC 2v1 scenario |
| **Applicant** | `hasProceedMulti_2v1` | Proceed-decision field present for applicant in 2v1 multi-party scenario |
| **Applicant** | `hasResponseDateApplicant1` | Applicant 1 initial response date exists on the case |
| **Applicant** | `hasResponseDateApplicant2` | Applicant 2 initial response date exists on the case |
| **Applicant** | `isAddApplicant2` | Checks if add applicant 2 is equal to YES |
| **Applicant** | `isRepresented` | Applicant is legally represented (applicant not marked as self-represented) |
| **Applicant** | `willProceed` | Applicant has indicated they will proceed with the claim (Yes) |
| **Applicant** | `willProceedAgainstRespondent1_1v2` | Applicant indicated they will proceed against respondent 1 in 1v2 scenario (Yes) |
| **Applicant** | `willProceedAgainstRespondent2_1v2` | Applicant indicated they will proceed against respondent 2 in 1v2 scenario (Yes) |
| **Applicant** | `willProceedApplicant2Multi_2v1` | Applicant 2 indicated they will proceed in 2v1 multi-party scenario (Yes) |
| **Applicant** | `willProceedMulti_2v1` | Applicant indicated they will proceed in 2v1 multi-party scenario (Yes) |
| **Applicant** | `willProceedSpec2v1` | Applicant indicated they will proceed in SPEC 2v1 scenario (Yes) |
| **Claim** | `hasChangeOfRepresentation` | A change of representation record exists on the case |
| **Claim** | `hasDismissedDate` | A claim dismissed date has been recorded (claim dismissed) |
| **Claim** | `hasFutureNotificationDeadline` | Claim notification deadline exists and is after now |
| **Claim** | `hasIssueDate` | A claim issue date has been recorded |
| **Claim** | `hasNotificationDate` | A claim notification date has been recorded |
| **Claim** | `hasNotificationDeadline` | A claim notification / issue deadline exists (claim has been notified) |
| **Claim** | `hasNotifyOptions` | Defendant claim-notification options exist |
| **Claim** | `hasOneVOneResponseFlag` | Flag used to indicate a one-v-one response was provided (LR ITP update) |
| **Claim** | `hasPassedDismissalDeadline` | Claim dismissal deadline exists and is before now (deadline expired) |
| **Claim** | `hasPassedNotificationDeadline` | Claim notification deadline exists and is before now (deadline expired) |
| **Claim** | `hasSubmittedDate` | A claim has been submitted |
| **Claim** | `isMultiParty` | Case has two applicants or respondents |
| **Claim** | `isNotifyOptionsBoth` | Defendant claim-notification option value is 'Both' |
| **Claim** | `isSpecClaim` | Case access category indicates SPEC (damages) |
| **Claim** | `isType()` | Checks the one‑v‑one response flag equals the provided ResponseOneVOneShowTag |
| **Claim** | `isUnspecClaim` | Case access category indicates UNSPEC |
| **ClaimDetails** | `futureNotificationDeadline` | Claim details notification deadline exists and is after now |
| **ClaimDetails** | `hasNotificationDate` | Claim details notification date exists on the case |
| **ClaimDetails** | `hasNotifyOptions` | Notification options for claim details were sent to defendant solicitor |
| **ClaimDetails** | `isNotifyOptionsBoth` | The dynamic list for claim details notify options was set to 'Both' |
| **ClaimDetails** | `passedNotificationDeadline` | Claim details notification deadline exists and is before now (deadline expired) |
| **Claimant** | `agreedToMediation` | Claimant has opted into free mediation |
| **Claimant** | `defendantSingleResponseToBothClaimants` | Defendant indicated a single response applies to both claimants |
| **Claimant** | `responseTypeSpecClaimant1()` | Checks claimant 1's SPEC response enum equals the provided type |
| **Claimant** | `responseTypeSpecClaimant2()` | Checks claimant 2's SPEC response enum equals the provided type |
| **Claimant** | `responsesDifferSpec` | Both claimant SPEC response enums are present and not equal |
| **Hearing** | `hasDismissedFeeDueDate` | A hearing fee due date (dismissed) is recorded on the case |
| **Hearing** | `hasReference` | A hearing reference number is recorded for the case |
| **Hearing** | `isListed` | The case has a hearing listing (listing status is LISTING) |
| **Judgment** | `isByAdmission` | An active judgment exists and its type is 'judgment by admission' |
| **Language** | `hasChangePreference` | The case records a change in language preference |
| **Language** | `isClaimantBilingual` | Claimant has bilingual language preference |
| **Language** | `isRespondentBilingual` | Respondent indicated their response is bilingual (translated documents may be present) |
| **Lip** | `caseContainsLiP` | At least one party on the case is a LiP (applicant or respondent) |
| **Lip** | `ccjRequestByAdmissionFlag` | Applicant has requested a County Court Judgment (CCJ) by admission |
| **Lip** | `hasPinInPost` | Respondent 1 has Pin-In-Post enabled and LiP (LR,SPEC) |
| **Lip** | `isHelpWithFees` | Case is a LiP with Help With Fee |
| **Lip** | `isLiPvLRCase` | Case is a Lip v LR one-v-one variant |
| **Lip** | `isLiPvLipCase` | Case is a Lip v Lip one-v-one variant |
| **Lip** | `isNotSettleClaimApplicant1` | Applicant 1 has not indicated they will settle the claim |
| **Lip** | `isPartyUnrepresented` | Case has at least one Litigant-in-Person (LiP) participant |
| **Lip** | `nocApplyForLiPClaimant` | A Notice of Change for a LiP claimant was submitted |
| **Lip** | `nocSubmittedForLiPDefendant` | A Notice of Change for a LiP defendant was submitted |
| **Lip** | `nocSubmittedForLiPDefendantBeforeOffline` | A Notice of Change for a LiP defendant was submitted prior to the case being taken offline |
| **Lip** | `respondentSignedSettlementAgreement` | Respondent has signed the digital settlement agreement |
| **Lip** | `translatedDocumentUploaded` | A translated response document has been uploaded to system documents |
| **MultiParty** | `isOneVOne` | Case multi-party scenario equals ONE_V_ONE |
| **MultiParty** | `isOneVTwoOneLegalRep` | Case multi-party scenario equals ONE_V_TWO_ONE_LEGAL_REP |
| **MultiParty** | `isOneVTwoTwoLegalRep` | Case multi-party scenario equals ONE_V_TWO_TWO_LEGAL_REP |
| **MultiParty** | `isTwoVOne` | Case multi-party scenario equals TWO_V_ONE |
| **Payment** | `claimIssuedPaymentFailed` | Claim-issue payment details exist and payment status equals FAILED |
| **Payment** | `claimIssuedPaymentSucceeded` | Claim-issue payment details exist and payment status equals SUCCESS |
| **Payment** | `hasPaymentSuccessfulDate` | A successful payment timestamp exists on the case |
| **Payment** | `hasWhenToBePaid` | When To Be Paid exists on the case |
| **Payment** | `isPayImmediately` | Part admission payment time (IMMEDIATELY) |
| **Payment** | `paymentDetailsFailed` | Claim-issue payment details exist and payment status equals FAILED |
| **Repayment** | `accepted` | Applicant has accepted the proposed repayment plan |
| **Repayment** | `rejected` | Applicant has rejected the proposed repayment plan |
| **Respondent** | `hasAcknowledgedNotificationRespondent1` | Respondent 1 has acknowledged service of claim details |
| **Respondent** | `hasAcknowledgedNotificationRespondent2` | Respondent 2 has acknowledged service of claim details |
| **Respondent** | `hasAddRespondent2` | Checks if add respondent 2 is present in the case data. |
| **Respondent** | `hasIntentionToProceedRespondent1` | Respondent 1 has indicated an intention to proceed (intention field present) |
| **Respondent** | `hasIntentionToProceedRespondent2` | Respondent 2 has indicated an intention to proceed (intention field present) |
| **Respondent** | `hasRespondent2` | Checks if respondent 2 is present in the case data. |
| **Respondent** | `hasResponseDateRespondent1` | Respondent 1 has submitted a response (response date recorded) |
| **Respondent** | `hasResponseDateRespondent2` | Respondent 2 has submitted a response (response date recorded) |
| **Respondent** | `hasResponseTypeRespondent1` | Respondent 1 has a non-null response type enum |
| **Respondent** | `hasResponseTypeRespondent2` | Respondent 2 has a non-null response type enum |
| **Respondent** | `hasResponseTypeSpecRespondent1` | Respondent 1 has a non-null response type enum |
| **Respondent** | `hasResponseTypeSpecRespondent2` | Respondent 2 has a non-null response type enum |
| **Respondent** | `hasSameLegalRepresentative` | Checks if respondent 2 is represented by the same legal representative as respondent 1. |
| **Respondent** | `hasTimeExtensionRespondent1` | Respondent 1 has been granted a time extension to respond |
| **Respondent** | `hasTimeExtensionRespondent2` | Respondent 2 has been granted a time extension to respond |
| **Respondent** | `isAddRespondent2` | Checks if add respondent 2 is equal to YES |
| **Respondent** | `isNotAddRespondent2` | Checks if add respondent 2 is equal to NO |
| **Respondent** | `isNotOrgRegisteredRespondent1` | Checks if respondent 1 is not a registered organisation. |
| **Respondent** | `isNotOrgRegisteredRespondent2` | Checks if respondent 2 is not a registered organisation. |
| **Respondent** | `isNotSameLegalRepresentative` | Checks if respondent 2 is represented by the same legal representative as respondent 1. |
| **Respondent** | `isOrgRegisteredRespondent1` | Checks if respondent 1 is a registered organisation. |
| **Respondent** | `isOrgRegisteredRespondent2` | Checks if respondent 2 is a registered organisation. |
| **Respondent** | `isRepresentedNotOrgRegisteredRespondent1` | Checks if respondent 1 is not a registered organisation. |
| **Respondent** | `isRepresentedNotOrgRegisteredRespondent2` | Checks if respondent 2 is not a registered organisation. |
| **Respondent** | `isRepresentedRespondent1` | Respondent 1 is represented |
| **Respondent** | `isRepresentedRespondent2` | Respondent 2 is represented |
| **Respondent** | `isSameLegalRepresentative` | Checks if respondent 2 is represented by the same legal representative as respondent 1. |
| **Respondent** | `isSameResponseFlag` | Yes/No flag indicating respondents marked their responses as the same |
| **Respondent** | `isTypeRespondent1()` | Factory: checks respondent 1's non-SPEC response enum equals the provided RespondentResponseType |
| **Respondent** | `isTypeRespondent1ToApplicant2()` | Factory: checks respondent 1's response-to-applicant2 enum equals the provided RespondentResponseType |
| **Respondent** | `isTypeRespondent2()` | Factory: checks respondent 2's non-SPEC response enum equals the provided RespondentResponseType |
| **Respondent** | `isTypeSpecRespondent1()` | Factory: checks respondent 1's SPEC response enum equals the provided RespondentResponseTypeSpec |
| **Respondent** | `isTypeSpecRespondent2()` | Factory: checks respondent 2's non-SPEC response enum equals the provided RespondentResponseType |
| **Respondent** | `isUnrepresentedRespondent1` | Respondent 1 is unrepresented |
| **Respondent** | `isUnrepresentedRespondent2` | Respondent 2 is unrepresented |
| **Respondent** | `respondent1ResponseAfterRespondent2` | Respondent 1 response timestamp is after respondent 2 response timestamp |
| **Respondent** | `respondent2ResponseAfterRespondent1` | Respondent 2 response timestamp is after respondent 1 response timestamp |
| **Respondent** | `responsesDiffer` | Both respondents have non-SPEC response enums and they are different |
| **Respondent** | `responsesDifferSpec` | Both respondents have SPEC response enums and they are different |
| **TakenOffline** | `byStaffDateExists` | HMCTS staff recorded that the case was taken offline |
| **TakenOffline** | `dateExists` | The case has been marked as taken offline (offline date recorded) |
| **TakenOffline** | `hasDrawDirectionsOrderRequired` | The case has been marked as requiring a 'draw directions order' |
| **TakenOffline** | `hasSdoReasonNotSuitable` | A reason explaining why the case is not suitable for a standard directions order (SDO) was provided |
