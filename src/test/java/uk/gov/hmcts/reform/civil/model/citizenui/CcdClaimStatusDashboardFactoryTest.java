package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.MediationAgreementDocument;
import uk.gov.hmcts.reform.civil.model.MediationSuccessful;
import uk.gov.hmcts.reform.civil.model.PaymentUponCourtOrder;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

@ExtendWith(MockitoExtension.class)
class CcdClaimStatusDashboardFactoryTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DashboardClaimStatusFactory ccdClaimStatusDashboardFactory;

    @BeforeEach
    void setUp() {
        ccdClaimStatusDashboardFactory = new DashboardClaimStatusFactory();
    }

    @Test
    void given_hasResponsePending_whenGetStatus_thenReturnNoResponse() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_hwf_claim_submit_whenGetStatus_thenReturnNoResponse() {
        CaseData claim = CaseData.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_SUBMIT_HWF);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.of(2022, 2, 2, 16, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnDefaultJudgementStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.of(2022, 2, 2, 16, 0))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFAULT_JUDGEMENT);
    }

    @Test
    void given_hasResponseDueToday_whenGetStatus_thenReturnResponseDueNow() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().atTime(10, 0, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_DUE_NOW);
    }

    @Test
    void given_moreTimeRequested_whenGetStatus_thenReturnMoreTimeRequested() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1TimeExtensionDate(LocalDateTime.now().plusDays(30))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_TIME_REQUESTED);
    }

    @Test
    void given_responseAdmitPayImmediately_whenGetStatus_thenReturnAdmitPayImmediately() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY);
    }

    @Test
    void given_responseAdmitPayBySetDate_whenGetStatus_thenReturnAdmitPayBySetDate() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE);
    }

    @Test
    void given_responseAdmitPayByInstallments_whenGetStatus_thenReturnAdmitPayByInstallments() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_INSTALLMENTS);
    }

    @Test
    void given_claimantConfirmedDefendantPaid_whenGetStatus_thenReturnClaimantAcceptedStatesPaid() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1CourtOrderPayment(new PaymentUponCourtOrder(YesOrNo.NO, Collections.emptyList()))
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    void given_caseStateSettled_whenGetStatus_thenReturnSettledClaimant() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.CASE_SETTLED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_caseStateSettled_whenGetStatus_thenReturnSettledDefendant() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.CASE_SETTLED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimantRequestedCountyCourtJudgement_whenGetStatus_thenReturnRequestedCountryCourtJudgement() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .applicant1DQ(Applicant1DQ.builder().applicant1DQRequestedCourt(RequestedCourt.builder().build()).build())
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT);
    }

    @Test
    void given_claimantAcceptedAdmission_whenGetStatus_thenReturnRelevantStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT);
    }

    @Test
    void given_defendantRespondedWithPartAdmit_whenGetStatus_thenReturnRelevantStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_PART_ADMIT);
    }

    @Test
    void given_hearingNoticeDocumentIssued_whenGetStatus_thenReturnHearingFormGenerated() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .hearingDocuments(List.of(Element.<CaseDocument>builder().value(CaseDocument.builder()
                                                                                .documentName("testDoc")
                                                                                .build()).build()))
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.HEARING_FORM_GENERATED);
    }

    @Test
    void given_hearingDateForSmallClaimIsAfterToday_and_SDOBeenDrawn_whenGetStatus_moreDetailsRequired() {
        Element<CaseDocument> document = new Element<>(
            UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
            CaseDocument.builder()
                .documentType(DocumentType.SDO_ORDER)
                .build()
        );
        CaseData claim = CaseData.builder()
            .smallClaimsHearing(SmallClaimsHearing.builder()
                                    .dateFrom(LocalDate.now().plusDays(10))
                                    .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .systemGeneratedCaseDocuments(List.of(document))
            .build();
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(true);
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_DETAILS_REQUIRED);
    }

    @Test
    void given_hearingDateForFastTrackClaimIsAfterToday_and_SDOBeenDrawn_whenGetStatus_moreDetailsRequired() {
        Element<CaseDocument> document = new Element<>(
            UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
            CaseDocument.builder()
                .documentType(DocumentType.SDO_ORDER)
                .build()
        );
        CaseData claim = CaseData.builder()
            .fastTrackHearingTime(FastTrackHearingTime.builder()
                                      .dateFrom(LocalDate.now().plusDays(10))
                                      .build())
            .respondent1ResponseDate(LocalDateTime.now())
            .systemGeneratedCaseDocuments(List.of(document))
            .build();
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(true);
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_DETAILS_REQUIRED);
    }

    @Test
    void given_mediation_whenGetSatus_mediationSuccessful() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .mediation(Mediation.builder()
                           .mediationSuccessful(MediationSuccessful.builder()
                                                    .mediationAgreement(MediationAgreementDocument.builder().build())
                                                    .build())
                           .build())
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.MEDIATION_SUCCESSFUL);
    }

    @Test
    void given_mediation_whenGetStatus_mediationUnsuccessful() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ResponseDate(LocalDateTime.now())
            .mediation(Mediation.builder()
                           .unsuccessfulMediationReason("this is a reason")
                           .build())
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.MEDIATION_UNSUCCESSFUL);
    }

    @Test
    void given_mediation_whenGetStatus_mediationPending() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .ccdState(CaseState.IN_MEDIATION)
            .mediation(Mediation.builder()
                           .mediationSuccessful(MediationSuccessful.builder()
                                                    .build())
                           .build())
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.IN_MEDIATION);
    }

    @Test
    void given_court_whenGetStatus_courtReview() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ResponseDate(LocalDateTime.now())
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_COURT_REVIEW);
    }

    @Test
    void given_respondentFullDefenceAndApplicantNotProceedsWithClaim_whenGetStatus_claimEnded() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ResponseDate(LocalDateTime.now())
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_ENDED);
    }

    @Test
    void given_applicantRejectPartialAdmit_whenGetStatus_rejectOffer() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECT_PARTIAL_ADMISSION);
    }

    private static CaseData getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec paymentMethod) {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(paymentMethod)
            .build();
        return claim;
    }

    @Test
    void given_SDOBeenDrawn_whenGetStatus_sdoOrderCreatedRequired() {
        Element<CaseDocument> document = new Element<>(
            UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
            CaseDocument.builder()
                .documentType(DocumentType.SDO_ORDER)
                .build()
        );
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .systemGeneratedCaseDocuments(List.of(document))
            .ccdState(CaseState.CASE_PROGRESSION)
            .build();
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.SDO_ORDER_CREATED);
    }

    @Test
    void given_claimantNotRespondedWithInDeadLine_whenGetStatus_claimEnded() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now().minusDays(2))
            .applicant1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .build();
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_ENDED);
    }

    @Test
    void given_claimantRejectsDefendantsPaymentPlan() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_COURT_REVIEW);
    }

    @Test
    void givenClaimStatusInProcessHeritageSystem_WhenGetStatus_thenReturnDefendantPartAdmit() {

        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .takenOfflineDate(LocalDateTime.now())
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_PART_ADMIT);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFNoRemission_WhenGetStatus_thenReturnHwfNoRemission() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(NO_REMISSION_HWF).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFNoRemission_WhenGetStatus_thenReturnHwfNoRemission() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(NO_REMISSION_HWF).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFPartialRemission_WhenGetStatus_thenReturnHwfPartialRemission() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFNoRemission_WhenGetStatus_thenReturnHwfPartialRemission() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFUpdateRefNumber_WhenGetStatus_thenReturnHwfUpdateRefNumber() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFUpdateRefNumber_WhenGetStatus_thenReturnHwfUpdateRefNumber() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFInvalidRefNumber_WhenGetStatus_thenReturnHwfInvalidRefNumber() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(INVALID_HWF_REFERENCE).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFInvalidRefNumber_WhenGetStatus_thenReturnHwfInvalidRefNumber() {
        HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(INVALID_HWF_REFERENCE).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFFeePaymentOutcome_WhenGetStatus_thenReturnHearingFeePaidStatus() {
        HelpWithFeesDetails hwfDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(FEE_PAYMENT_OUTCOME).build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME);
    }

    @Test
    void givenBilingualLanguageIsWelsh_ClaimantIntentDocUploadPending_thenReturnDocUploadStatus() {
        CaseData caseData = CaseData.builder()
                .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant1ResponseDate(LocalDateTime.now())
                .claimantBilingualLanguagePreference(Language.BOTH.toString()).build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_FOR_CLAIMANT_INTENT_DOC_UPLOAD);
    }
}
