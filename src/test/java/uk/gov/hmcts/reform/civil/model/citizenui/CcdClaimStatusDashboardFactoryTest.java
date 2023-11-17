package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
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

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.of(2022, 2, 2, 16, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFAULT_JUDGEMENT);
    }

    @Test
    void given_hasResponseDueToday_whenGetStatus_thenReturnResponseDueNow() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().atTime(10, 0, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_DUE_NOW);
    }

    @Test
    void given_moreTimeRequested_whenGetStatus_thenReturnMoreTimeRequested() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1TimeExtensionDate(LocalDateTime.now().plusDays(30))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_TIME_REQUESTED);
    }

    @Test
    void given_responseAdmitPayImmediately_whenGetStatus_thenReturnAdmitPayImmediately() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY);
    }

    @Test
    void given_responseAdmitPayBySetDate_whenGetStatus_thenReturnAdmitPayBySetDate() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE);
    }

    @Test
    void given_responseAdmitPayByInstallments_whenGetStatus_thenReturnAdmitPayByInstallments() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    void given_defendantPayedInFull_whenGetStatus_thenReturnSettled() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimantAcceptedDefendantResponse_whenGetStatus_thenReturnSettled() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_COURT_REVIEW);
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
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
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
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));
        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_COURT_REVIEW);
    }

    @Test
    void givenClaimStatusInProcessHeritageSystem_WhenGetStatus_thenReturnResponseByPost() {
        given(featureToggleService.isLipVLipEnabled()).willReturn(true);

        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .takenOfflineDate(LocalDateTime.now())
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(
            claim, featureToggleService));

        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_BY_POST);
    }
}
