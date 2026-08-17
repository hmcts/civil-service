package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum CaseState {
    //Parent Case states needed to create Pojo at application start
    @CCD(
            label = "Claim Issue Pending",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Holding state whilst payment is taken"
    )
    PENDING_CASE_ISSUED("Pending case issued"),
    @CCD(
            label = "Awaiting Claim Notification",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Claim is created"
    )
    CASE_ISSUED("Case issued"),
    @CCD(label = "Awaiting Claim Details Notification", hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}")
    AWAITING_CASE_DETAILS_NOTIFICATION("Awaiting case details notofication"),
    @CCD(label = "Awaiting Defendant Response", hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}")
    AWAITING_RESPONDENT_ACKNOWLEDGEMENT("Awaiting respondent acknowledgement"),
    @CCD(label = "Judgment Requested", hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}")
    JUDGMENT_REQUESTED("Judgment Requested"),
    @CCD(
            label = "Claim Dismissed",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Claim has been dismissed"
    )
    CASE_DISMISSED("Case Dismissed"),
    @CCD(label = "Claimant Intent Pending", hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}")
    AWAITING_APPLICANT_INTENTION("Awaiting applicant intention"),
    @CCD(
            label = "Case Proceeds Offline",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Claim is not able to be progressed online in CCD and will be taken offline"
    )
    PROCEEDS_IN_HERITAGE_SYSTEM("Proceeds in heritage system"),
    @CCD(
            label = "Judicial Referral",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Claim is moved onto judges"
    )
    JUDICIAL_REFERRAL("Judicial Referral"),
    @CCD(
            label = "Case Progression",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Claim proceeds online"
    )
    CASE_PROGRESSION("Case Progression"),
    @CCD(
            label = "Hearing Readiness",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "placeholder"
    )
    HEARING_READINESS("Hearing Readiness"),
    @CCD(
            label = "Prepare For Hearing Conduct Hearing",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Placeholder"
    )
    PREPARE_FOR_HEARING_CONDUCT_HEARING("Prepare for hearing conduct hearing"),
    @CCD(
            label = "Decision Outcome",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Day of Trial state"
    )
    DECISION_OUTCOME("Decision Outcome"),
    @CCD(
            label = "In Mediation",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Placeholder"
    )
    IN_MEDIATION("In Mediation"),
    @CCD(
            label = "Case Stayed",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Case stayed"
    )
    CASE_STAYED("Case Stayed"),
    @CCD(label = "All final orders issued", hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}")
    All_FINAL_ORDERS_ISSUED("All final orders issued"),
    @CCD(
            label = "Case Settled",
            hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}",
            description = "Case settled"
    )
    CASE_SETTLED("Case Settled"),
    @CCD(label = "Case Discontinued", hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}")
    CASE_DISCONTINUED("Case Discontinued"),
    @CCD(label = "Closed", hint = "# #${[CASE_REFERENCE]} <br/> ${caseNameHmctsInternal}")
    CLOSED("Closed"),

    //General Application states
    @CCD(ignore = true)
    PENDING_APPLICATION_ISSUED("General Application Issue Pending"),
    @CCD(ignore = true)
    AWAITING_RESPONDENT_RESPONSE("Awaiting Respondent Response"),
    @CCD(ignore = true)
    APPLICATION_ADD_PAYMENT("Application Additional Payment"),
    @CCD(ignore = true)
    APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION("Application Submitted - Awaiting Judicial Decision"),
    @CCD(ignore = true)
    ADDITIONAL_RESPONSE_TIME_EXPIRED("Additional Response Time Expired"),
    @CCD(ignore = true)
    ADDITIONAL_RESPONSE_TIME_PROVIDED("Additional Response Time Provided"),
    @CCD(ignore = true)
    AWAITING_DIRECTIONS_ORDER_DOCS("Directions Order Made"),
    @CCD(ignore = true)
    ORDER_MADE("Order Made"),
    @CCD(ignore = true)
    LISTING_FOR_A_HEARING("Listed for a Hearing"),
    @CCD(ignore = true)
    HEARING_SCHEDULED("Hearing Scheduled"),
    @CCD(ignore = true)
    APPLICATION_PAYMENT_FAILED("Application Payment Failed"),
    @CCD(ignore = true)
    AWAITING_APPLICATION_PAYMENT("Awaiting Application Payment"),
    @CCD(ignore = true)
    AWAITING_WRITTEN_REPRESENTATIONS("Awaiting Written Representations"),
    @CCD(ignore = true)
    AWAITING_ADDITIONAL_INFORMATION("Additional Information Required"),
    @CCD(ignore = true)
    APPLICATION_DISMISSED("Application Dismissed"),
    @CCD(ignore = true)
    APPLICATION_CLOSED("Application Closed"),
    @CCD(ignore = true)
    PROCEEDS_IN_HERITAGE("Proceeds In Heritage"),
    @CCD(ignore = true)
    RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION("Respond to judge for Written Representations");

    private final String displayedValue;
}

