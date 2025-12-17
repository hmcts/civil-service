# Civil Service Business Rules

> This file is auto-generated from the source code. Do not edit manually.

## Composed predicates (Business Rules)

| Group | Rule Name | Logic Description |
|---|---|---|
| **Claim** | `afterIssued` | No claim‑details notification yet; respondent has not acknowledged service or responded; claim notification deadline is in the future. For SPEC, a notification date exists; for UNSPEC, it does not |
| **Claim** | `changeOfRepresentation` | A change of legal representation has been recorded on the case |
| **Claim** | `isFullDefenceNotPaid` | Claim is defendant not paid full defence |
| **Claim** | `isMulti` | Case is Multi Claim track |
| **Claim** | `isOneVOne` | Case is One V One based on case party scenario |
| **Claim** | `isPartAdmitSettled` | Case is Part Admit Settled |
| **Claim** | `isSpec` | Case is in the SPEC (damages) service based on case access category |
| **Claim** | `isUnspec` | Case is in the UNSPEC service based on case access category |
| **Claim** | `issued` | Acknowledgement deadline exists - claim notification has been sent (State Flow: claim notified) |
| **Claim** | `issuedRespondent1OrgNotRegistered` | Issue date is set where defendant is represented but their organisation is not registered |
| **Claim** | `issuedRespondent1Unrepresented` | Issue date is set and respondent 1 is unrepresented |
| **Claim** | `issuedRespondent2OrgNotRegistered` | Issue date is set where defendant is represented but their organisation is not registered |
| **Claim** | `issuedRespondent2Unrepresented` | Issue date is set and respondent 2 is unrepresented |
| **Claim** | `pendingIssued` | Issue date is set and all represented defendants have registered organisations (second defendant absent or registered/same solicitor). Used for moving to pending issue |
| **Claim** | `pendingIssuedUnrepresented` | Issue date is set and at least one defendant is unrepresented. Applies to all UNSPEC, and to SPEC only for multi‑party scenarios |
| **Claim** | `sameRepresentationBoth` | Same legal representation for both defendants |
| **Claim** | `submitted1v1RespondentOneUnregistered` | Submitted 1v1 claim where defendant is represented but their organisation is not registered |
| **Claim** | `submittedBothUnregisteredSolicitors` | Submitted claim with two defendants, each represented by different solicitors and both defendant organisations are not registered |
| **Claim** | `submittedOneRespondentRepresentative` | Submitted claim where respondent 1 is represented and either there is a single defendant, or there are two defendants sharing the same legal representative |
| **Claim** | `submittedOneUnrepresentedDefendantOnly` | Submitted claim with a single unrepresented defendant (no second defendant) |
| **Claim** | `submittedRespondent1Unrepresented` | Submitted claim where respondent 1 is unrepresented |
| **Claim** | `submittedRespondent2Unrepresented` | Submitted claim where respondent 2 is unrepresented |
| **Claim** | `submittedTwoRegisteredRespondentRepresentatives` | Submitted claim with two defendants, each represented by different solicitors and both defendant organisations are registered |
| **Claim** | `submittedTwoRespondentRepresentativesOneUnregistered` | Submitted claim with two defendants, each represented by different solicitors where exactly one defendant organisation is not registered |
| **Claimant** | `beforeResponse` | Applicant initial response has not been recorded yet (for UNSPEC with applicant 2, neither applicant has responded) |
| **Claimant** | `correspondenceAddressNotRequired` | Applicant correspondence address not required (Spec) |
| **Claimant** | `fullDefenceNotProceed` | Applicant has decided not to proceed with the claim. In UNSPEC 1v2, 'not proceed' is recorded against both defendants; in 2v1, both applicants record 'not proceed'; in 1v1, a single 'not proceed' decision applies |
| **Claimant** | `fullDefenceProceed` | Applicant has decided to proceed with the claim (SPEC/UNSPEC, 1v1/1v2/2v1). In UNSPEC 1v2, proceeding against at least one defendant qualifies; in 2v1, at least one applicant chooses to proceed |
| **Claimant** | `isIntentionNotSettlePartAdmit` | Claimant intention not settle |
| **Claimant** | `isIntentionSettlePartAdmit` | Claimant intention settle |
| **Claimant** | `isNotSettlePartAdmit` | Claimant not settle |
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
| **Lip** | `caseContainsLiP` | At least one party (applicant or respondent) is a litigant-in-person |
| **Lip** | `ccjRequestJudgmentByAdmission` | Applicant has requested a County Court Judgment (CCJ) by admission |
| **Lip** | `certificateOfServiceEnabled` | True when at least one litigant-in-person (LiP) defendant is flagged as 'at claim issued' (either `defendant1LIPAtClaimIssued` or `defendant2LIPAtClaimIssued` = Yes). |
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
| **OutOfTime** | `notBeingTakenOffline` | Applicant response deadline passed, applicant has not responded and staff offline date does not exist |
| **Payment** | `failed` | Card payment for the issue fee failed (or claim issue recorded as failed) and the applicant is represented |
| **Payment** | `payImmediatelyAcceptedPartAdmit` | Part admission payment time (IMMEDIATELY) Accepted |
| **Payment** | `payImmediatelyFullAdmission` | SPEC 1v1 full admission where 'when to be paid' is set and the applicant chose not to proceed |
| **Payment** | `payImmediatelyPartAdmit` | Part admission where the payment time selected is 'IMMEDIATELY' |
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
| **Applicant** | `isNotApplicantCorrespondenceAddressRequiredSpec` | True when applicant correspondence address is not required (Spec) — field `specAoSApplicantCorrespondenceAddressRequired` = No. |
| **Applicant** | `isRepresented` | Applicant is legally represented (applicant not marked as self-represented) |
| **Applicant** | `isUnrepresentedApplicant1` | Returns true when applicant 1 is marked as not legally represented / self-represented (the applicant1Represented field is set to No). |
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
| **Claim** | `hasNotifyOptions` | True when defendant claim-notification options are present (dynamic list non-null). |
| **Claim** | `hasPassedDismissalDeadline` | Claim dismissal deadline exists and is before now (deadline expired) |
| **Claim** | `hasPassedNotificationDeadline` | Claim notification deadline exists and is before now (deadline expired) |
| **Claim** | `hasSubmittedDate` | A claim has been submitted |
| **Claim** | `isFullDefenceNotPaid` | True when the case is in a 'full defence not paid' situation (case data indicates the defendant has not paid as required following a full defence outcome). |
| **Claim** | `isMultiClaim` | True when the case is allocated to the multi-track (field `allocatedTrack` = MULTI_CLAIM). |
| **Claim** | `isMultiParty` | True when the case has multiple parties (either `applicant2` or `respondent2` is present). |
| **Claim** | `isNotifyOptionsBoth` | Defendant claim-notification option value equals 'Both' (compared against the dynamic list label). |
| **Claim** | `isPartAdmitSettled` | True when a part admission case is treated as settled based on the recorded settlement indicators (e.g. claimant intention to settle and, where applicable, confirmation of payment). |
| **Claim** | `isSmallClaim` | True when the case is on the small claims track (field `responseClaimTrack` indicates small claims). |
| **Claim** | `isSpecClaim` | Case access category indicates SPEC (damages) |
| **Claim** | `isUnspecClaim` | Case access category indicates UNSPEC |
| **ClaimDetails** | `futureNotificationDeadline` | Claim details notification deadline exists and is after now |
| **ClaimDetails** | `hasNotificationDate` | Claim details notification date exists on the case |
| **ClaimDetails** | `hasNotifyOptions` | True when claim-details notification options are present for the defendant solicitor (dynamic list non-null). |
| **ClaimDetails** | `isNotifyOptionsBoth` | Claim details notify option equals 'Both' (compared against the dynamic list label). |
| **ClaimDetails** | `passedNotificationDeadline` | Claim details notification deadline exists and is before now (deadline expired) |
| **Claimant** | `agreedToMediation` | True when the claimant has opted into free mediation. |
| **Claimant** | `declinedMediation` | True when the claimant has opted out of free mediation (field indicates the claimant has not agreed to free mediation). |
| **Claimant** | `defendantSingleResponseToBothClaimants` | Defendant indicated a single response applies to both claimants |
| **Claimant** | `isIntentionNotSettlePartAdmit` | True when, in a part admission scenario, the claimant's recorded intention is not to settle the claim (intention-to-settle flag is No). |
| **Claimant** | `isIntentionSettlePartAdmit` | True when, in a part admission scenario, the claimant's recorded intention is to settle the claim (intention-to-settle flag is Yes). |
| **Claimant** | `isNotSettlePartAdmit` | True when, in a part admission scenario, the claimant's recorded decision indicates they will not settle the claim on the defendant's part-admission terms. |
| **Claimant** | `responseTypeSpecClaimant1()` | Checks claimant 1's SPEC response enum equals the provided type |
| **Claimant** | `responseTypeSpecClaimant2()` | Checks claimant 2's SPEC response enum equals the provided type |
| **Claimant** | `responsesDifferSpec` | Both claimant SPEC response enums are present and not equal |
| **Hearing** | `hasDismissedFeeDueDate` | True when a hearing fee due date (dismissed) is recorded on the case. |
| **Hearing** | `hasReference` | True when a hearing reference number is recorded for the case. |
| **Hearing** | `isListed` | True when the case has a hearing listing (listing status is LISTING). |
| **Judgment** | `isByAdmission` | True when an active judgment exists and its type is 'judgment by admission'. |
| **Language** | `hasChangePreference` | True when the case records a change in language preference. |
| **Language** | `isClaimantBilingual` | True when the claimant has a bilingual language preference. |
| **Language** | `isRespondentBilingual` | True when the respondent indicated their response is bilingual (translated documents may be present). |
| **Lip** | `caseContainsLiP` | True when at least one party on the case is a LiP (applicant or respondent). |
| **Lip** | `ccjRequestByAdmissionFlag` | True when the applicant has requested a County Court Judgment (CCJ) by admission. |
| **Lip** | `hasPinInPost` | True when respondent 1 has PIN-in-post enabled (LR, SPEC). |
| **Lip** | `isClaimIssued` | True when at least one defendant is a litigant in person and is flagged as being 'at claim issued' (either `defendant1LIPAtClaimIssued` or `defendant2LIPAtClaimIssued` = Yes). |
| **Lip** | `isHelpWithFees` | True when the case is a LiP with Help With Fees. |
| **Lip** | `isLiPvLRCase` | True when the case is a LiP v LR one-v-one variant. |
| **Lip** | `isLiPvLipCase` | True when the case is a LiP v LiP one-v-one variant. |
| **Lip** | `isNotSettleClaimApplicant1` | True when applicant 1 has not indicated they will settle the claim. |
| **Lip** | `isPartyUnrepresented` | Case has at least one Litigant-in-Person (LiP) participant |
| **Lip** | `nocApplyForLiPClaimant` | True when a Notice of Change for a LiP claimant was submitted. |
| **Lip** | `nocSubmittedForLiPDefendant` | True when a Notice of Change for a LiP defendant was submitted. |
| **Lip** | `nocSubmittedForLiPDefendantBeforeOffline` | True when a Notice of Change for a LiP defendant was submitted prior to the case being taken offline. |
| **Lip** | `respondentSignedSettlementAgreement` | True when the respondent has signed the digital settlement agreement. |
| **Lip** | `translatedDocumentUploaded` | True when a translated response document has been uploaded to system documents. |
| **Mediation** | `hasContactInfoApplicant1` | True when applicant 1's mediation contact information has been provided on the case (contact info object present). |
| **Mediation** | `hasContactInfoRespondent1` | True when respondent 1's mediation contact information has been provided on the case (contact info object present). |
| **Mediation** | `hasContactInfoRespondent2` | True when respondent 2's mediation contact information has been provided on the case (contact info object present). |
| **Mediation** | `hasReasonUnsuccessful` | True when an unsuccessful mediation reason has been recorded for the case (single 'unsuccessful reason' value present). |
| **Mediation** | `hasReasonUnsuccessfulMultiSelect` | True when the multi-select list of unsuccessful mediation reasons is present on the case (list field exists, even if nothing is selected yet). |
| **Mediation** | `hasReasonUnsuccessfulMultiSelectValue` | True when at least one unsuccessful mediation reason has been selected in the multi-select list (list exists and contains one or more values). |
| **Mediation** | `hasResponseCarmLiPApplicant1` | True when the LiP applicant 1 CARM response value is present in the LiP case data (field answered). |
| **Mediation** | `hasResponseCarmLiPRespondent1` | True when the LiP respondent 1 CARM response value is present in the LiP case data (field answered). |
| **Mediation** | `isNotAgreedFreeMediationApplicant1Spec` | True when applicant 1 is recorded as not having agreed to free mediation in the SPEC small-claims mediation fields (`hasAgreedFreeMediation` = No). |
| **Mediation** | `isNotRequiredApplicantMPSpec` | True when the multi-party applicant is recorded as not having agreed to free mediation in the SPEC small-claims mediation fields (`hasAgreedFreeMediation` = No). |
| **Mediation** | `isNotRequiredRespondent2Spec` | True when respondent 2 is recorded as not having agreed to free mediation for SPEC (`responseClaimMediationSpec2Required` = No). |
| **Mediation** | `isRequiredRespondent1Spec` | True when respondent 1 is recorded as having agreed to free mediation for SPEC (`responseClaimMediationSpecRequired` = Yes). |
| **MultiParty** | `isOneVOne` | True when the multi-party scenario equals ONE_V_ONE. |
| **MultiParty** | `isOneVTwoOneLegalRep` | True when the multi-party scenario equals ONE_V_TWO_ONE_LEGAL_REP. |
| **MultiParty** | `isOneVTwoTwoLegalRep` | True when the multi-party scenario equals ONE_V_TWO_TWO_LEGAL_REP. |
| **Payment** | `claimIssuedPaymentFailed` | True when claim-issue payment details exist and payment status equals FAILED. |
| **Payment** | `claimIssuedPaymentSucceeded` | True when claim-issue payment details exist and payment status equals SUCCESS. |
| **Payment** | `hasPaymentSuccessfulDate` | True when a successful payment timestamp exists on the case. |
| **Payment** | `hasWhenToBePaid` | True when the 'When To Be Paid' text exists on the case. |
| **Payment** | `isPartAdmitPayImmediately` | True when the defendant has proposed to pay the part-admitted amount immediately and the claimant has accepted that immediate payment option (determined from the SPEC pay-immediately acceptance flag/tag). |
| **Payment** | `isPayImmediately` | Part admission payment time (IMMEDIATELY) |
| **Payment** | `paymentDetailsFailed` | True when general payment details exist and payment status equals FAILED. |
| **Repayment** | `accepted` | True when the applicant has accepted the proposed repayment plan. |
| **Repayment** | `rejected` | True when the applicant has rejected the proposed repayment plan. |
| **Respondent** | `hasAcknowledgedNotificationRespondent1` | Respondent 1 has acknowledged service of claim details |
| **Respondent** | `hasAcknowledgedNotificationRespondent2` | Respondent 2 has acknowledged service of claim details |
| **Respondent** | `hasAddRespondent2` | True when field `addRespondent2` is present (non-null). |
| **Respondent** | `hasIntentionToProceedRespondent1` | Respondent 1 has indicated an intention to proceed (intention field present) |
| **Respondent** | `hasIntentionToProceedRespondent2` | Respondent 2 has indicated an intention to proceed (intention field present) |
| **Respondent** | `hasRespondent2` | Checks if respondent 2 is present in the case data. |
| **Respondent** | `hasResponseDateRespondent1` | Respondent 1 has submitted a response (response date recorded) |
| **Respondent** | `hasResponseDateRespondent2` | Respondent 2 has submitted a response (response date recorded) |
| **Respondent** | `hasResponseTypeRespondent1` | True when respondent 1 has a non-null response type enum. |
| **Respondent** | `hasResponseTypeRespondent2` | True when respondent 2 has a non-null response type enum. |
| **Respondent** | `hasResponseTypeSpecRespondent1` | True when respondent 1 has a non-null SPEC response type enum. |
| **Respondent** | `hasResponseTypeSpecRespondent2` | True when respondent 2 has a non-null SPEC response type enum. |
| **Respondent** | `hasSameLegalRepresentative` | Field `respondent2SameLegalRepresentative` present (non-null). |
| **Respondent** | `hasTimeExtensionRespondent1` | Respondent 1 has been granted a time extension to respond |
| **Respondent** | `hasTimeExtensionRespondent2` | Respondent 2 has been granted a time extension to respond |
| **Respondent** | `isAddRespondent2` | True when `addRespondent2` = Yes. |
| **Respondent** | `isNotAddRespondent2` | True when `addRespondent2` = No. |
| **Respondent** | `isNotOrgRegisteredRespondent1` | True when respondent 1 is not a registered organisation (`respondent1OrgRegistered` = No). |
| **Respondent** | `isNotOrgRegisteredRespondent2` | True when respondent 2 is not a registered organisation (`respondent2OrgRegistered` = No). |
| **Respondent** | `isNotSameLegalRepresentative` | True when respondents are recorded as not having the same legal representative (`respondent2SameLegalRepresentative` = No). |
| **Respondent** | `isOrgRegisteredRespondent1` | True when respondent 1 is a registered organisation (`respondent1OrgRegistered` = Yes). |
| **Respondent** | `isOrgRegisteredRespondent2` | True when respondent 2 is a registered organisation (`respondent2OrgRegistered` = Yes). |
| **Respondent** | `isRepresentedNotOrgRegisteredRespondent1` | True when respondent 1 is legally represented and is not a registered organisation (`respondent1Represented` = Yes AND `respondent1OrgRegistered` = No). |
| **Respondent** | `isRepresentedNotOrgRegisteredRespondent2` | True when respondent 2 is legally represented and is not a registered organisation (`respondent2Represented` = Yes AND `respondent2OrgRegistered` = No or null). |
| **Respondent** | `isRepresentedRespondent1` | True when respondent 1 is legally represented (`respondent1Represented` = Yes). |
| **Respondent** | `isRepresentedRespondent2` | True when respondent 2 is legally represented (`respondent2Represented` = Yes). |
| **Respondent** | `isSameLegalRepresentative` | True when respondents are recorded as having the same legal representative (`respondent2SameLegalRepresentative` = Yes). |
| **Respondent** | `isSameResponseFlag` | True when `respondentResponseIsSame` = Yes (respondents marked their responses as the same). |
| **Respondent** | `isTypeRespondent1()` | Factory: checks respondent 1's non-SPEC response enum equals the provided RespondentResponseType |
| **Respondent** | `isTypeRespondent1ToApplicant2()` | Factory: checks respondent 1's response-to-applicant2 enum equals the provided RespondentResponseType |
| **Respondent** | `isTypeRespondent2()` | Factory: checks respondent 2's non-SPEC response enum equals the provided RespondentResponseType |
| **Respondent** | `isTypeSpecRespondent1()` | Factory: checks respondent 1's SPEC response enum equals the provided RespondentResponseTypeSpec |
| **Respondent** | `isTypeSpecRespondent2()` | Factory: checks respondent 2's SPEC response enum equals the provided `RespondentResponseTypeSpec`. |
| **Respondent** | `isUnrepresentedRespondent1` | True when respondent 1 is not legally represented (`respondent1Represented` = No). |
| **Respondent** | `isUnrepresentedRespondent2` | True when respondent 2 is not legally represented (`respondent2Represented` = No). |
| **Respondent** | `respondent1ResponseAfterRespondent2` | True when respondent 1 response timestamp is after respondent 2 response timestamp. |
| **Respondent** | `respondent2ResponseAfterRespondent1` | True when respondent 2 response timestamp is after respondent 1 response timestamp. |
| **Respondent** | `responsesDiffer` | Both respondents have non-SPEC response enums and they are different |
| **Respondent** | `responsesDifferSpec` | Both respondents have SPEC response enums and they are different |
| **TakenOffline** | `byStaffDateExists` | True when HMCTS staff recorded that the case was taken offline. |
| **TakenOffline** | `dateExists` | True when the case has been marked as taken offline (offline date recorded). |
| **TakenOffline** | `hasDrawDirectionsOrderRequired` | True when the case has been marked as requiring a 'draw directions order'. |
| **TakenOffline** | `hasSdoReasonNotSuitable` | True when a reason explaining why the case is not suitable for a standard directions order (SDO) has been provided. |
