package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.ONE_DAY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ClaimantResponseCuiCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ResponseOneVOneShowTagService.class,
    JacksonAutoConfiguration.class,
    CourtLocationUtils.class,
    LocationRefDataService.class,
    LocationHelper.class,
    UpdateCaseManagementDetailsService.class,
    JudgementService.class,
    CaseFlagsInitialiser.class
})
class ClaimantResponseCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CourtLocationUtils courtLocationUtility;
    @MockBean
    private LocationHelper locationHelper;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @Autowired
    private ClaimantResponseCuiCallbackHandler handler;
    @Autowired
    CaseFlagsInitialiser caseFlagsInitialiser;
    private static final String courtLocation = "Site 1 - Adr 1 - AAA 111";

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private ResponseOneVOneShowTagService responseOneVOneShowTagService;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    OrganisationService organisationService;

    @Autowired
    private JudgementService judgementService;

    @MockBean
    private Time time;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void before() {
            LocationRefData locationRefData = LocationRefData.builder().siteName("Site 1").courtAddress("Adr 1").postcode(
                    "AAA 111")
                .courtName("Court Name").region("Region").regionId("1").courtVenueId("1")
                .courtTypeId("10").courtLocationCode("court1")
                .epimmsId("111").build();
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            given(time.now()).willReturn(submittedDate);
            given(locationHelper.updateCaseManagementLocation(any(), any(), any())).willReturn(Optional.ofNullable(
                locationRefData));
            given(deadlinesCalculator.getRespondToSettlementAgreementDeadline(any())).willReturn(LocalDateTime.MAX);
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
            when(featureToggleService.isUpdateContactDetailsEnabled()).thenReturn(true);
        }

        @Test
        void shouldUpdateBusinessProcess() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(
                    CaseDataLiP.builder()
                        .applicant1ClaimMediationSpecRequiredLip(
                            ClaimantMediationLip.builder()
                                .hasAgreedFreeMediation(MediationDecision.Yes)
                                .build())
                        .build())
                .atStateClaimIssued()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenPartAdmitNotSettled_NoMediation() {
            given(time.now()).willReturn(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
            Applicant1DQ applicant1DQ = Applicant1DQ.builder()
                .applicant1DQRequestedCourt(
                    RequestedCourt.builder()
                        .responseCourtCode("court1")
                        .caseLocation(
                            CaseLocationCivil.builder()
                                .region(courtLocation)
                                .baseLocation(courtLocation)
                                .build()
                        )
                        .build()

                )
                .applicant1DQHearing(Hearing.builder()
                                          .hearingLength(ONE_DAY)
                                          .unavailableDatesRequired(YES)
                                          .unavailableDates(wrapElements(List.of(
                                              UnavailableDate.builder()
                                                  .date(LocalDate.of(2024, 2, 1))
                                                  .dateAdded(LocalDate.of(2024, 1, 1))
                                                  .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                  .build())))
                                          .build())
                .build();
            Respondent1DQ respondent1DQ = Respondent1DQ.builder()
                .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                 .responseCourtCode("court2")
                                                 .caseLocation(CaseLocationCivil.builder()
                                                                   .region(courtLocation)
                                                                   .baseLocation(courtLocation)
                                                                   .build())
                                                 .build())
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .applicant1AcceptAdmitAmountPaidSpec(NO)
                .caseDataLip(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().hasAgreedFreeMediation(
                        MediationDecision.No).build())
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response;

            LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(response.getData())
                .extracting("applicant1")
                .hasFieldOrProperty("unavailableDates");

            CaseData data = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(data.getApplicant1DQ().getApplicant1DQRequestedCourt().getResponseCourtCode()).isEqualTo("court1");
            assertThat(data.getCaseNameHmctsInternal()).isEqualTo(data.getApplicant1().getPartyName() + " v " + data.getRespondent1().getPartyName());
            assertThat(data.getApplicant1().getUnavailableDates()).isEqualTo(
                wrapElements(List.of(UnavailableDate.builder()
                                         .eventAdded("Claimant Intention Event")
                                         .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                         .dateAdded(now.toLocalDate())
                                         .date(LocalDate.of(2024, 2, 1))
                                         .build())));
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenPartAdmitNotSettled_NoMediation_NoBaseCourt() {
            Applicant1DQ applicant1DQ =
                Applicant1DQ.builder().applicant1DQRequestedCourt(RequestedCourt.builder()
                                                                      .caseLocation(CaseLocationCivil.builder()
                                                                                        .build())
                                                                      .build()).build();
            Respondent1DQ respondent1DQ =
                Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                        .caseLocation(CaseLocationCivil.builder()
                                                                                          .build())
                                                                        .build()).build();
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .applicant1AcceptAdmitAmountPaidSpec(NO)
                .caseDataLip(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().hasAgreedFreeMediation(
                        MediationDecision.No).build())
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            CaseData data = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(data.getApplicant1DQ().getApplicant1DQRequestedCourt().getResponseCourtCode()).isNull();
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenPartAdmitNotSettled_NoMediation_NoCourtSelected() {
            Applicant1DQ applicant1DQ =
                Applicant1DQ.builder().applicant1DQRequestedCourt(RequestedCourt.builder()
                                                                      .build()).build();
            Respondent1DQ respondent1DQ =
                Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                        .build()).build();
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .applicant1AcceptAdmitAmountPaidSpec(NO)
                .caseDataLip(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().hasAgreedFreeMediation(
                        MediationDecision.No).build())
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            CaseData data = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(data.getApplicant1DQ().getApplicant1DQRequestedCourt().getResponseCourtCode()).isNull();
        }

        protected List<LocationRefData> getSampleCourLocationsRefObject() {
            return new ArrayList<>(List.of(
                LocationRefData.builder()
                    .epimmsId("111").siteName("Site 1").courtAddress("Adr 1").postcode("AAA 111")
                    .courtLocationCode("court1").build(),
                LocationRefData.builder()
                    .epimmsId("222").siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                    .courtLocationCode("court2").build(),
                LocationRefData.builder()
                    .epimmsId("333").siteName("Site 3").courtAddress("Adr 3").postcode("CCC 333")
                    .courtLocationCode("court3").build()
            ));
        }

        @Test
        void shouldUpdateCCJRequestPaymentDetails() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(600.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_INDIVIDUAL").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("RESPONDENT_INDIVIDUAL").build())
                .caseDataLip(
                    CaseDataLiP.builder()
                        .applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1ChoosesHowToProceed(
                            ChooseHowToProceed.REQUEST_A_CCJ).build())
                        .build())
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .ccjPaymentDetails(ccjPaymentDetails)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CCJPaymentDetails ccjResponseForJudgement =
                getCaseData(response).getCcjPaymentDetails();
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CLAIMANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(ccjPaymentDetails.getCcjPaymentPaidSomeOption()).isEqualTo(ccjResponseForJudgement.getCcjPaymentPaidSomeOption());
            assertThat(caseData.getTotalClaimAmount()).isEqualTo(ccjResponseForJudgement.getCcjJudgmentAmountClaimAmount());
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return mapper.convertValue(response.getData(), CaseData.class);
        }

        @Test
        void shouldAddTheCaseFlagIntialiazerForClaimant() {
            when(featureToggleService.isHmcEnabled()).thenReturn(true);
            when(featureToggleService.isCaseFlagsEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("CLAIMANT_NAME")
                                 .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .details(wrapElements(Expert.builder()
                                                                                     .name(
                                                                                         "John Smith")
                                                                                     .firstName("Jane")
                                                                                     .lastName("Smith")

                                                                                     .build()))
                                                           .build())
                                  .applicant1DQWitnesses(Witnesses.builder().witnessesToAppear(YES)
                                                             .details(wrapElements(Witness.builder()
                                                                                       .name(
                                                                                           "John Smith")
                                                                                       .firstName("Jane")
                                                                                       .lastName("Smith")

                                                                                       .build())).build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getApplicantExperts()).isNotNull();
            assertThat(updatedCaseData.getApplicantWitnesses()).isNotNull();

        }

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(CLAIMANT_RESPONSE_CUI);
    }

}
