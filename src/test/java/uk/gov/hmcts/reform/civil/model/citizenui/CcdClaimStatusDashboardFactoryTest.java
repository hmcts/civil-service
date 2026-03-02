package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.MediationAgreementDocument;
import uk.gov.hmcts.reform.civil.model.MediationSuccessful;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentUponCourtOrder;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
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
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_hwf_claim_submit_whenGetStatus_thenReturnNoResponse() {
        CaseData claim = CaseData.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIM_SUBMIT_HWF);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.of(2022, 2, 2, 16, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    void given_respondentDeadlineHasPassed_whenGetStatus_thenReturnEligibleForCCJStatus() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .respondent1ResponseDeadline(LocalDate.now().minusDays(1).atTime(16, 0, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnDefaultJudgementStatus() {
        CaseData claim = CaseData.builder()
            .ccdState(All_FINAL_ORDERS_ISSUED)
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.of(2022, 2, 2, 16, 0))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFAULT_JUDGEMENT);
    }

    @Test
    void given_hasResponseDueToday_whenGetStatus_thenReturnResponseDueNow() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().atTime(10, 0, 0))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_DUE_NOW);
    }

    @Test
    void given_moreTimeRequested_whenGetStatus_thenReturnMoreTimeRequested() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1TimeExtensionDate(LocalDateTime.now().plusDays(30))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.MORE_TIME_REQUESTED);
    }

    @Test
    void given_responseAdmitPayImmediately_whenGetStatus_thenReturnAdmitPayImmediately() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY);
    }

    @Test
    void given_responseAdmitPayBySetDate_whenGetStatus_thenReturnAdmitPayBySetDate() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE);
    }

    @Test
    void given_responseAdmitPayByInstallments_whenGetStatus_thenReturnAdmitPayByInstallments() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    void given_caseStateSettled_whenGetStatus_thenReturnSettledClaimant() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.CASE_SETTLED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_caseStateSettled_whenGetStatus_thenReturnSettledDefendant() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.CASE_SETTLED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.SETTLED);
    }

    @Test
    void given_claimantRequestedCountyCourtJudgement_whenGetStatus_thenReturnRequestedCountryCourtJudgement() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .applicant1DQ(new Applicant1DQ().setApplicant1DQRequestedCourt(new RequestedCourt()))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT);
    }

    @Test
    void given_claimantRequestedCountyCourtJudgementCui_whenGetStatus_thenReturnRequestedCountryCourtJudgement() {
        CaseData claim = CaseData.builder()
            .ccjPaymentDetails(new CCJPaymentDetails()
                                   .setCcjJudgmentStatement("test"))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_PART_ADMIT);
    }

    @Test
    void given_hearingNoticeDocumentIssued_whenGetStatus_thenReturnHearingFormGenerated() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .hearingDate(LocalDate.now().plusDays(6 * 7 + 1))
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDocuments(List.of(Element.<CaseDocument>builder().value(new CaseDocument()
                                                                                .setDocumentName("testDoc")).build()))
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.singletonList(CaseEventDetail.builder()
                                                                       .id(CaseEvent.HEARING_SCHEDULED.name())
                                                                       .createdDate(LocalDateTime.now())
                                                                       .build())));
        assertThat(status).isEqualTo(DashboardClaimStatus.HEARING_FORM_GENERATED);
    }

    @Test
    void given_hearingNoticeDocumentIssuedForAutoHearingNotice_whenGetStatus_thenReturnHearingFormGenerated() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .hearingDate(LocalDate.now().plusDays(6 * 7 + 1))
            .ccdState(CaseState.HEARING_READINESS)
            .hearingDocuments(List.of(Element.<CaseDocument>builder().value(new CaseDocument()
                                                                                .setDocumentName("testDoc")).build()))
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.singletonList(CaseEventDetail.builder()
                                                                       .id(CaseEvent.GENERATE_HEARING_NOTICE_HMC.name())
                                                                       .createdDate(LocalDateTime.now())
                                                                       .build())));
        assertThat(status).isEqualTo(DashboardClaimStatus.HEARING_FORM_GENERATED);
    }

    @Test
    void given_hearingNoticeDocumentIssuedAndRelisted_whenGetStatus_thenReturnHearingFormGeneratedRelisting() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .hearingDate(LocalDate.now().plusDays(6 * 7 + 1))
            .ccdState(CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .hearingDocuments(List.of(Element.<CaseDocument>builder().value(new CaseDocument()
                                                                                .setDocumentName("testDoc")).build()))
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.singletonList(CaseEventDetail.builder()
                                                                       .id(CaseEvent.HEARING_SCHEDULED.name())
                                                                       .createdDate(LocalDateTime.now())
                                                                       .build())));
        assertThat(status).isEqualTo(DashboardClaimStatus.HEARING_FORM_GENERATED_RELISTING);
    }

    @Test
    void given_hearingNoticeDocumentIssuedAndRelisted_AutoHearingNotice_whenGetStatus_thenReturnHearingFormGeneratedRelisting() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .hearingDate(LocalDate.now().plusDays(6 * 7 + 1))
            .ccdState(CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .hearingDocuments(List.of(Element.<CaseDocument>builder().value(new CaseDocument()
                                                                                .setDocumentName("testDoc")).build()))
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.singletonList(CaseEventDetail.builder()
                                                                       .id(CaseEvent.GENERATE_HEARING_NOTICE_HMC.name())
                                                                       .createdDate(LocalDateTime.now())
                                                                       .build())));
        assertThat(status).isEqualTo(DashboardClaimStatus.HEARING_FORM_GENERATED_RELISTING);
    }

    @Test
    void given_mediation_whenGetSatus_mediationSuccessful() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .mediation(new Mediation().setMediationSuccessful(new MediationSuccessful().setMediationAgreement(new MediationAgreementDocument())
                                                    )
                           )
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.MEDIATION_SUCCESSFUL);
    }

    @Test
    void given_mediation_whenGetStatus_mediationUnsuccessful() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ResponseDate(LocalDateTime.now())
            .mediation(new Mediation().setUnsuccessfulMediationReason("this is a reason")
                           )
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.MEDIATION_UNSUCCESSFUL);
    }

    @Test
    void given_mediation_whenGetStatus_mediationPending() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .ccdState(CaseState.IN_MEDIATION)
            .mediation(new Mediation().setMediationSuccessful(new MediationSuccessful())
                           )
            .build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_COURT_REVIEW);
    }

    @Test
    void given_court_whenGetStatus_courtReview_when_claimantIntendsToProceedMinti() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ResponseDate(LocalDateTime.now())
            .responseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name())
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECT_PARTIAL_ADMISSION);
    }

    private static CaseData getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec paymentMethod) {
        return CaseData.builder()
            .respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(paymentMethod)
            .build();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void given_SDOBeenDrawn_whenGetStatus_sdoOrderCreatedRequired(boolean caseProgressionEnabled) {
        Mockito.when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(caseProgressionEnabled);
        Element<CaseDocument> document = new Element<>(
            UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
            new CaseDocument()
                .setCreatedDatetime(LocalDateTime.now())
                .setDocumentType(SDO_ORDER)
        );
        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .systemGeneratedCaseDocuments(List.of(document))
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(selectedCourt.getCode()))
            .build();
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(caseProgressionEnabled
                                         ? DashboardClaimStatus.SDO_ORDER_CREATED_CP
                                         : DashboardClaimStatus.SDO_ORDER_CREATED_PRE_CP);
    }

    @Test
    void given_FullDefenceAndClaimantDontWantToProceed_whenGetStatus_claimEnded() {
        CaseData claim = CaseData.builder()
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .build();
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));
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
            claim, featureToggleService, Collections.emptyList()));

        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_PART_ADMIT);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFNoRemission_WhenGetStatus_thenReturnHwfNoRemission() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(NO_REMISSION_HWF);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(
            new CcdDashboardClaimantClaimMatcher(
                caseData, featureToggleService, Collections.singletonList(
                CaseEventDetail.builder()
                    .createdDate(LocalDateTime.now())
                    .id(NO_REMISSION_HWF.name())
                    .build()
            )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFNoRemission_WhenGetStatus_thenReturnHwfNoRemission() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(NO_REMISSION_HWF);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(NO_REMISSION_HWF.name())
                .build()
        )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFPartialRemission_WhenGetStatus_thenReturnHwfPartialRemission() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(PARTIAL_REMISSION_HWF_GRANTED.name())
                .build()
        )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFNoRemission_WhenGetStatus_thenReturnHwfPartialRemission() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(PARTIAL_REMISSION_HWF_GRANTED.name())
                .build()
        )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFUpdateRefNumber_WhenGetStatus_thenReturnHwfUpdateRefNumber() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(UPDATE_HELP_WITH_FEE_NUMBER.name())
                .build()
        )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFUpdateRefNumber_WhenGetStatus_thenReturnHwfUpdateRefNumber() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(UPDATE_HELP_WITH_FEE_NUMBER.name())
                .build()
        )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInPendingCaseIssuedAndHWFInvalidRefNumber_WhenGetStatus_thenReturnHwfInvalidRefNumber() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(INVALID_HWF_REFERENCE);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .claimIssuedHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.CLAIMISSUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(INVALID_HWF_REFERENCE.name())
                .build()
        )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFInvalidRefNumber_WhenGetStatus_thenReturnHwfInvalidRefNumber() {
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(INVALID_HWF_REFERENCE);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfeeDetails)
            .hwfFeeType(
                FeeType.HEARING)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(INVALID_HWF_REFERENCE.name())
                .build()
        )));

        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER);
    }

    @Test
    void givenClaimStatusInHearingReadinessAndHWFFeePaymentOutcome_WhenGetStatus_thenReturnHearingFeePaidStatus() {
        HelpWithFeesDetails hwfDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(CaseEvent.FULL_REMISSION_HWF);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(hwfDetails)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .hwfFeeType(
                FeeType.HEARING)
            .hearingHelpFeesReferenceNumber("123")
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES))
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData, featureToggleService, Collections.singletonList(
            CaseEventDetail.builder()
                .createdDate(LocalDateTime.now())
                .id(CaseEvent.FEE_PAYMENT_OUTCOME.name())
                .build()
        )));

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
            caseData, featureToggleService, Collections.emptyList()));

        assertThat(status).isEqualTo(DashboardClaimStatus.WAITING_FOR_CLAIMANT_INTENT_DOC_UPLOAD_PRE_DEF_NOC_ONLINE);
    }

    @Test
    void given_claimantRejectsDefendantsPaymentPlan_RequestedJudgeDecision_WhenGetStatus_thenReturnAwaitingJudgeReview() {
        CaseData claim = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                        .setClaimantResponseOnCourtDecision(
                                                            ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE)))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .build()).build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.CLAIMANT_REJECTED_PAYMENT_PLAN_REQ_JUDGE_DECISION);
    }

    @Test
    void given_responseAdmitPayImmediately_whenGetStatus_thenReturnResponeByPost() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        CaseData updatedClaim = claim.toBuilder().takenOfflineDate(LocalDateTime.now())
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM).previousCCDState(CaseState.AWAITING_APPLICANT_INTENTION).build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            updatedClaim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_BY_POST);
    }

    @Test
    void given_responseAdmitPayBySetDate_whenGetStatus_thenReturnResponeByPost() {
        CaseData claim = getClaimWithFullAdmitResponse(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        CaseData updatedClaim = claim.toBuilder().takenOfflineDate(LocalDateTime.now()).ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .previousCCDState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT).build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                updatedClaim,
                featureToggleService,
                Collections.emptyList()
            ));
        assertThat(status).isEqualTo(DashboardClaimStatus.RESPONSE_BY_POST);
    }

    @Test
    void given_defendantHasNoticeOfChange_whenGetStatus_thenReturnDefendantNoticeOfChangeApply() {
        CaseData claim = CaseData.builder().takenOfflineDate(LocalDateTime.now()).ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .businessProcess(new BusinessProcess()
                                 .setStatus(BusinessProcessStatus.FINISHED)
                                 .setCamundaEvent(CaseEvent.APPLY_NOC_DECISION_DEFENDANT_LIP.name()))
            .build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                claim,
                featureToggleService,
                Collections.emptyList()
            ));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFENDANT_APPLY_NOC);
    }

    @Test
    void given_defaultJudgementStatusIssuedByClaimant_thenReturnDefaultJudgementStatus() {
        CaseData claim =
            CaseData.builder().respondent1ResponseDeadline(LocalDateTime.now().minusDays(1)).activeJudgment(
                new JudgmentDetails().setType(JudgmentType.DEFAULT_JUDGMENT).setIssueDate(LocalDate.now())
                    .setState(JudgmentState.ISSUED)).defaultJudgmentDocuments(List.of(
                Element.<CaseDocument>builder()
                    .value(new CaseDocument().setDocumentType(DocumentType.DEFAULT_JUDGMENT)
                               .setCreatedDatetime(LocalDateTime.now())).build())).build();
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(
                new CcdDashboardDefendantClaimMatcher(
                    claim,
                    featureToggleService, Collections.emptyList()
                ));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFAULT_JUDGEMENT_ISSUED);
    }

    @Test
    void given_defaultJudgementStatusIssuedByClaimant_thenReturnDefaultJudgementStatus_WhenJOFlagIsOff() {
        CaseData claim =
            CaseData.builder().respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
                .defaultJudgmentDocuments(List.of(
                    Element.<CaseDocument>builder()
                        .value(new CaseDocument().setDocumentType(DocumentType.DEFAULT_JUDGMENT)
                                   .setCreatedDatetime(LocalDateTime.now())).build()))
                .build();
        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                claim,
                featureToggleService, Collections.emptyList()
            ));
        assertThat(status).isEqualTo(DashboardClaimStatus.DEFAULT_JUDGEMENT_ISSUED);
    }

    @Test
    void given_caseDismissed_whenGetStatus_thenReturnCaseDismissed() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.CASE_DISMISSED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.CASE_DISMISSED);
    }

    @Test
    void given_sdoIsDrawn_anyPartyBilingual_showStatusDocumentsTranslated() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData claim = CaseData.builder().preTranslationDocuments(List.of(Element.<CaseDocument>builder()
                                                                                .value(new CaseDocument()
                                                                                           .setDocumentType(SDO_ORDER)).build()))
            .ccdState(CaseState.CASE_PROGRESSION)
            .build();

        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.SDO_DOCUMENTS_BEING_TRANSLATED);
    }

    @Test
    void given_decisionMadeIsDrawn_anyPartyBilingual_showStatusDocumentsTranslated() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData claim = CaseData.builder().preTranslationDocuments(List.of(Element.<CaseDocument>builder()
                                                                                .value(new CaseDocument()
                                                                                           .setDocumentType(
                                                                                               DECISION_MADE_ON_APPLICATIONS)).build()))
            .ccdState(CaseState.CASE_PROGRESSION)
            .build();

        DashboardClaimStatus status =
            ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.DECISION_MADE_DOCUMENTS_BEING_TRANSLATED);
    }

    @Test
    void given_caseDiscontinued_whenGetStatus_thenReturnCaseDiscontinued() {
        CaseData claim = CaseData.builder()
            .ccdState(CaseState.CASE_DISCONTINUED)
            .build();

        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            claim, featureToggleService, Collections.emptyList()));
        assertThat(status).isEqualTo(DashboardClaimStatus.CASE_DISCONTINUED);
    }
}
