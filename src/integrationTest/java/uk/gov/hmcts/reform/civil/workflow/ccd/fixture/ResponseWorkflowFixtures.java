package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.DocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_ONE_MONTH;

public final class ResponseWorkflowFixtures {

    private static final CaseLocationCivil TRANSFERRED_LOCATION = new CaseLocationCivil()
        .setBaseLocation("99999")
        .setRegion("4");

    private ResponseWorkflowFixtures() {
    }

    public static CaseData unspecifiedFullDefenceResponse() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .build()
            .toBuilder()
            .addRespondent2(YesOrNo.NO)
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondent1ClaimResponseDocument(defendantResponseDocument())
            .build();
    }

    public static CaseData unspecifiedFullAdmitResponse() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .build()
            .toBuilder()
            .addRespondent2(YesOrNo.NO)
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
            .respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION)
            .build();
    }

    public static CaseData unspecified1v2DifferentSolicitorFirstFullDefenceResponse() {
        return CaseDataBuilder.builder()
            .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .build()
            .toBuilder()
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondent1ClaimResponseDocument(defendantResponseDocument())
            .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
            .build();
    }

    public static CaseData unspecified1v2DifferentSolicitorSecondFullDefenceResponse() {
        return CaseDataBuilder.builder()
            .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent2Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .respondent2DQ()
            .uiStatementOfTruth(new StatementOfTruth().setName("Jane Smith").setRole("Solicitor"))
            .build()
            .toBuilder()
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondent1ClaimResponseDocument(defendantResponseDocument())
            .respondent1ResponseDate(LocalDateTime.now().minusDays(1))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondent2ClaimResponseDocument(defendantResponseDocument())
            .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
            .build();
    }

    public static CaseData unspecified1v2SameSolicitorFullDefenceResponse() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged1v2SameSolicitor()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent2Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .build()
            .toBuilder()
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondentResponseIsSame(YesOrNo.YES)
            .respondent1ClaimResponseDocument(defendantResponseDocument())
            .build();
    }

    public static CaseData specifiedPartAdmitResponse() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .build()
            .toBuilder()
            .addRespondent2(YesOrNo.NO)
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();
    }

    public static CaseData specifiedFullAdmitResponse() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .build()
            .toBuilder()
            .addRespondent2(YesOrNo.NO)
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();
    }

    public static CaseData specified1v2SameSolicitorFullAdmitResponse() {
        return specified1v2SameSolicitorDefendantResponse(RespondentResponseTypeSpec.FULL_ADMISSION);
    }

    public static CaseData specified1v2SameSolicitorPartAdmitResponse() {
        return specified1v2SameSolicitorDefendantResponse(RespondentResponseTypeSpec.PART_ADMISSION);
    }

    public static CaseData unspecifiedClaimantProceedsResponse() {
        return withTransferredLocation(CaseDataBuilder.builder()
                                           .atStateApplicantRespondToDefenceAndProceed()
                                           .build());
    }

    public static CaseData unspecifiedClaimantDoesNotProceedResponse() {
        return withTransferredLocation(CaseDataBuilder.builder()
                                           .atStateApplicantRespondToDefenceAndNotProceed()
                                           .build());
    }

    public static CaseData unspecified2v1Applicant2ProceedsResponse() {
        return withTransferredLocation(CaseDataBuilder.builder()
                                           .atStateApplicant2RespondToDefenceAndProceed_2v1()
                                           .build());
    }

    public static CaseData specifiedFullAdmitClaimantAcceptsResponse() {
        return withTransferredLocation(CaseDataBuilder.builder()
                                           .atStateRespondentFullAdmissionSpec()
                                           .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
                                           .defenceAdmitPartPaymentTimeRouteRequired(
                                               RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                                           .respondent1RepaymentPlan(repaymentPlan())
                                           .ccjPaymentDetails(ccjPaymentDetails())
                                           .respondent1DQ()
                                           .build());
    }

    public static CaseData specified1v2SameSolicitorFullAdmitClaimantResponse() {
        return withTransferredLocation(CaseDataBuilder.builder()
                                           .atStateApplicantProceedAllMediation(ONE_V_TWO_ONE_LEGAL_REP)
                                           .build()
                                           .toBuilder()
                                           .ccdState(AWAITING_APPLICANT_INTENTION)
                                           .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                                           .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                                           .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
                                           .defenceAdmitPartPaymentTimeRouteRequired(
                                               RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                                           .build());
    }

    public static CaseData specified1v2SameSolicitorPartAdmitClaimantResponse() {
        return withTransferredLocation(CaseDataBuilder.builder()
                                           .atStateApplicantProceedAllMediation(ONE_V_TWO_ONE_LEGAL_REP)
                                           .build()
                                           .toBuilder()
                                           .ccdState(AWAITING_APPLICANT_INTENTION)
                                           .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                                           .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                                           .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
                                           .defenceAdmitPartPaymentTimeRouteRequired(
                                               RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                                           .build());
    }

    private static CaseData withTransferredLocation(CaseData caseData) {
        return caseData.toBuilder()
            .caseManagementLocation(TRANSFERRED_LOCATION)
            .build();
    }

    private static CaseData specified1v2SameSolicitorDefendantResponse(RespondentResponseTypeSpec responseType) {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged1v2SameSolicitor()
            .respondent1Copy(new PartyBuilder().individual().build())
            .respondent2Copy(new PartyBuilder().individual().build())
            .respondent1DQ()
            .build()
            .toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
            .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
            .respondent1ClaimResponseTypeForSpec(responseType)
            .respondent2ClaimResponseTypeForSpec(responseType)
            .build();
    }

    private static ResponseDocument defendantResponseDocument() {
        return new ResponseDocument(DocumentBuilder.builder()
                                        .setDocumentName("defendant-response.pdf")
                                        .build());
    }

    private static RepaymentPlanLRspec repaymentPlan() {
        return new RepaymentPlanLRspec()
            .setPaymentAmount(BigDecimal.valueOf(9000))
            .setRepaymentFrequency(ONCE_ONE_MONTH)
            .setFirstRepaymentDate(LocalDate.now().plusDays(14));
    }

    private static CCJPaymentDetails ccjPaymentDetails() {
        return new CCJPaymentDetails()
            .setCcjPaymentPaidSomeOption(YesOrNo.YES)
            .setCcjPaymentPaidSomeAmount(BigDecimal.valueOf(500))
            .setCcjJudgmentLipInterest(BigDecimal.valueOf(300))
            .setCcjJudgmentAmountClaimFee(BigDecimal.ZERO)
            .setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(500))
            .setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(50))
            .setCcjJudgmentTotalStillOwed(BigDecimal.valueOf(500));
    }
}
