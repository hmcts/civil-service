package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.PaymentDateService.DATE_FORMATTER;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private LocationHelper locationHelper;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private AirlineEpimsService airlineEpimsService;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private ClaimantResponseCuiCallbackHandler handler;

    private ObjectMapper mapper;

    @Mock
    private ResponseOneVOneShowTagService responseOneVOneShowTagService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private OrganisationService organisationService;
    @Mock
    private JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;
    @Mock
    private RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;
    @Mock
    private InterestCalculator interestCalculator;

    @Mock
    private Time time;

    @Mock
    private PaymentDateService paymentDateService;

    private static final String courtLocation = "Site 1 - Adr 1 - AAA 111";
    private static final String LIVERPOOL_SITE_NAME = "Liverpool Civil and Family Court";

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        CaseFlagsInitialiser caseFlagsInitialiser = new CaseFlagsInitialiser(organisationService);
        JudgementService judgementService = new JudgementService(featureToggleService, interestCalculator);
        CourtLocationUtils courtLocationUtils = new CourtLocationUtils();
        UpdateCaseManagementDetailsService updateCaseManagementLocationDetailsService = new UpdateCaseManagementDetailsService(locationHelper,
                                                                                                                               locationRefDataService,
                                                                                                                               courtLocationUtils,
                                                                                                                               airlineEpimsService
        );
        handler = new ClaimantResponseCuiCallbackHandler(responseOneVOneShowTagService, featureToggleService,
                                                         judgementService, mapper,
                                                         time,
                                                         updateCaseManagementLocationDetailsService,
                                                         deadlinesCalculator,
                                                         caseFlagsInitialiser,
                                                         judgmentByAdmissionOnlineMapper,
                                                         requestedCourtForClaimDetailsTab,
                                                         paymentDateService
        );
    }

    private List<LocationRefData> getSampleCourLocationsRefObject() {
        return List.of(LocationRefData.builder()
                .epimmsId("2")
                .courtLocationCode("121")
                .siteName(courtLocation)
                .courtAddress("Adr")
                .postcode("AAA 111")
                .courtName("Court Name")
                .region("Region")
                .regionId("2")
                .courtTypeId("10")
                .courtLocationCode("court location code")
                .build(),
            LocationRefData.builder()
                .epimmsId("4")
                .courtLocationCode("231")
                .siteName(LIVERPOOL_SITE_NAME)
                .courtAddress("Adr")
                .postcode("L2 1EJ")
                .courtName("Court Name")
                .region("Region")
                .regionId("4")
                .courtTypeId("10")
                .courtLocationCode("court location code")
                .build());
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return mapper.convertValue(response.getData(), CaseData.class);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetSettlementAgreementDeadLine_whenClaimantAcceptedRepaymentPlanForImmediatePayment() {
            // Given
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCaseDataLiP(
                CaseDataLiP.builder()
                    .applicant1LiPResponse(
                        ClaimantLiPResponse.builder()
                            .applicant1SignedSettlementAgreement(YesOrNo.YES)
                            .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                            .build())
                    .build());
            caseData.setApplicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY);
            caseData.setApplicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build());
            caseData.setRespondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .partyName("CLAIMANT_NAME")
                .build());
            caseData.setNextDeadline(LocalDate.now());

            given(deadlinesCalculator.getRespondentToImmediateSettlementAgreement(any())).willReturn(LocalDateTime.MAX);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            // Then
            assertThat(updatedCaseData.getRespondent1RespondToSettlementAgreementDeadline()).isNotNull();
            assertThat(updatedCaseData.getRespondent1RespondToSettlementAgreementDeadline()).isNotNull();
        }

        @Test
        void shouldSetSettlementAgreementDeadLine_whenClaimantSignedSettlementAgreement() {
            // Given
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            given(deadlinesCalculator.getRespondToSettlementAgreementDeadline(any())).willReturn(LocalDateTime.MAX);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCaseDataLiP(
                CaseDataLiP.builder()
                    .applicant1LiPResponse(
                        ClaimantLiPResponse.builder()
                            .applicant1SignedSettlementAgreement(YesOrNo.YES)
                            .build())
                    .build());
            caseData.setApplicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build());
            caseData.setRespondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .partyName("CLAIMANT_NAME")
                .build());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            // Then
            assertThat(updatedCaseData.getRespondent1RespondToSettlementAgreementDeadline()).isNotNull();
        }

        @Test
        void shouldAddActiveJudgmentWhenClaimantAcceptedRepaymentPlanAndJudgmentOnlineLiveEnabled() {
            // Given
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            JudgmentDetails activeJudgment = JudgmentDetails.builder()
                .totalAmount("10100.00")
                .claimFeeAmount("100.00")
                .orderedAmount("10000").build();
            given(judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(any()))
                .willReturn(activeJudgment);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1ResponseDate(LocalDateTime.now());
            caseData.setApplicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build());
            caseData.setRespondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("DEFENDANT_NAME").build());
            caseData.setRespondent1Represented(NO);
            caseData.setSpecRespondent1Represented(NO);
            caseData.setApplicant1Represented(NO);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE);
            caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YES);
            caseData.setCcjPaymentDetails(CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
                .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(150))
                .build());
            caseData.setCaseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
            try {
                when(time.now()).thenReturn(now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // When
            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }
            CaseData updatedCaseData = getCaseData(response);

            // Then
            assertThat(updatedCaseData.getActiveJudgment()).isNotNull();
            assertThat(updatedCaseData.getJoIsLiveJudgmentExists()).isEqualTo(YES);
            assertThat(updatedCaseData.getJoJudgementByAdmissionIssueDate()).isEqualTo(now);
            assertThat(updatedCaseData.getJoRepaymentSummaryObject()).isNotNull();
        }

        @Test
        void shouldUpdateLanguagePreferenceWhenWelshDocsSelected() {
            // Given
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1ResponseDate(LocalDateTime.now());
            caseData.setApplicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build());
            caseData.setRespondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("DEFENDANT_NAME").build());
            caseData.setRespondent1Represented(NO);
            caseData.setSpecRespondent1Represented(NO);
            caseData.setApplicant1Represented(NO);
            caseData.setApplicant1DQ(Applicant1DQ.builder().applicant1DQLanguage(WelshLanguageRequirements.builder().documents(
                Language.WELSH).build()).build());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            // Then
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isEqualTo("WELSH");
        }

        @Test
        void shouldUpdateDefendantPaymentDeadlineForPartAdmitImmediateWhenClaimantAcceptedRepaymentPlan() {
            // Given
            LocalDate paymentDeadline = LocalDate.now().plusDays(1);
            when(paymentDateService.calculatePaymentDeadline()).thenReturn(paymentDeadline);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1ResponseDate(LocalDateTime.now());
            caseData.setApplicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build());
            caseData.setRespondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("DEFENDANT_NAME").build());
            caseData.setRespondent1Represented(NO);
            caseData.setSpecRespondent1Represented(NO);
            caseData.setApplicant1Represented(NO);
            caseData.setApplicant1DQ(Applicant1DQ.builder().applicant1DQLanguage(WelshLanguageRequirements.builder().documents(
                Language.WELSH).build()).build());
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YES);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            // Then
            assertThat(updatedCaseData.getWhenToBePaidText()).isEqualTo(paymentDeadline.format(DATE_FORMATTER));
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(CLAIMANT_RESPONSE_CUI);
    }

}
