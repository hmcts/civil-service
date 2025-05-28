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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.EventAddedEvents.CLAIMANT_INTENTION_EVENT;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.ONE_DAY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

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
    private Time time;

    private static final String courtLocation = "Site 1 - Adr 1 - AAA 111";
    private static final String LIVERPOOL_SITE_NAME = "Liverpool Civil and Family Court";

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        CaseFlagsInitialiser caseFlagsInitialiser = new CaseFlagsInitialiser(organisationService);
        JudgementService judgementService = new JudgementService(featureToggleService);
        CourtLocationUtils courtLocationUtils = new CourtLocationUtils();
        UpdateCaseManagementDetailsService updateCaseManagementLocationDetailsService = new UpdateCaseManagementDetailsService(locationHelper,
                                                                                                                               locationRefDataService,
                                                                                                                               courtLocationUtils,
                                                                                                                               airlineEpimsService
        );
        handler = new ClaimantResponseCuiCallbackHandler(responseOneVOneShowTagService, featureToggleService,
                                                         judgementService, mapper,
                                                         time,
                                                         updateCaseManagementLocationDetailsService, deadlinesCalculator,
                                                         caseFlagsInitialiser, judgmentByAdmissionOnlineMapper, requestedCourtForClaimDetailsTab
        );
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldUpdatePartAdmitPaidValuePounds_WhenAboutToStartIsInvoked() {
            String suppliedValuePennies = "12345";
            String expectedValuePounds = "123.45";
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData = caseData.toBuilder()
                .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(new BigDecimal(suppliedValuePennies)).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData())
                .extracting("partAdmitPaidValuePounds")
                .isEqualTo(expectedValuePounds);
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void before() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
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
                    .regionId("region 1").courtLocationCode("court1").build(),
                LocationRefData.builder()
                    .epimmsId("222").siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                    .regionId("region 2").courtLocationCode("court2").build(),
                LocationRefData.builder()
                    .epimmsId("333").siteName("Site 3").courtAddress("Adr 3").postcode("CCC 333")
                    .regionId("region 3").courtLocationCode("court3").build(),
                LocationRefData.builder()
                    .epimmsId("444").siteName(LIVERPOOL_SITE_NAME).courtAddress("Adr 3").postcode("CCC 333")
                    .regionId("region 4").courtLocationCode("court4").build()
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

        @Test
        void shouldUpdateCaseManagementLocationForFlightDelayClaimSpecificAirline() {
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(airlineEpimsService.getEpimsIdForAirlineIgnoreCase("Sri Lankan")).thenReturn("111");
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .build();
            caseData = caseData.toBuilder()
                .isFlightDelayClaim(YES)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .flightDelayDetails(FlightDelayDetails.builder()
                    .nameOfAirline("Sri Lankan")
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("111");
            assertThat(updatedCaseData.getCaseManagementLocation().getRegion()).isEqualTo("region 1");
            assertThat(updatedCaseData.getLocationName()).isEqualTo("Site 1");
        }

        @Test
        void shouldUpdateCaseManagementLocationForFlightDelayClaimInvalidAirline() {
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(airlineEpimsService.getEpimsIdForAirlineIgnoreCase("INVALID_AIRLINE")).thenReturn(null);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .build();
            caseData = caseData.toBuilder()
                .isFlightDelayClaim(YES)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .flightDelayDetails(FlightDelayDetails.builder()
                    .nameOfAirline("INVALID_AIRLINE")
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("444");
            assertThat(updatedCaseData.getCaseManagementLocation().getRegion()).isEqualTo("region 4");
            assertThat(updatedCaseData.getLocationName()).isEqualTo(LIVERPOOL_SITE_NAME);
        }

        @Test
        void shouldAddEventAndDateAddedToClaimantExpertsAndWitness() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1ResponseDate(LocalDateTime.now())
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

            Expert expert = updatedCaseData.getApplicant1DQ().getApplicant1DQExperts().getDetails().get(0).getValue();
            Witness witness = updatedCaseData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails().get(0).getValue();

            assertThat(expert.getDateAdded()).isEqualTo(LocalDateTime.now().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(CLAIMANT_INTENTION_EVENT.getValue());
            assertThat(witness.getDateAdded()).isEqualTo(LocalDateTime.now().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(CLAIMANT_INTENTION_EVENT.getValue());
        }

        @Test
        void shouldSetImmediateSettlementAgreementDeadLine_whenClaimantSignedSettlementAgreement() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseDataLip(
                            CaseDataLiP.builder()
                                    .applicant1LiPResponse(
                                            ClaimantLiPResponse.builder()
                                                    .applicant1SignedSettlementAgreement(YesOrNo.YES)
                                                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                                                    .build())
                                    .build())
                    .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
                    .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                    .respondent1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("CLAIMANT_NAME")
                            .build())
                    .nextDeadline(LocalDate.now())
                    .build();
            given(deadlinesCalculator.getRespondentToImmediateSettlementAgreement(any())).willReturn(LocalDateTime.MAX);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getRespondent1RespondToSettlementAgreementDeadline()).isNotNull();
            assertThat(updatedCaseData.getRespondent1RespondToSettlementAgreementDeadline()).isNotNull();
        }

        @Test
        void shouldSetSettlementAgreementDeadLine_whenClaimantSignedSettlementAgreement() {
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            given(deadlinesCalculator.getRespondToSettlementAgreementDeadline(any())).willReturn(LocalDateTime.MAX);
            CaseData caseData = CaseDataBuilder.builder()
                    .caseDataLip(
                            CaseDataLiP.builder()
                                    .applicant1LiPResponse(
                                            ClaimantLiPResponse.builder()
                                                    .applicant1SignedSettlementAgreement(YesOrNo.YES)
                                                    .build())
                                    .build())
                    .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                    .respondent1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("CLAIMANT_NAME")
                            .build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getRespondent1RespondToSettlementAgreementDeadline()).isNotNull();
        }

        @Test
        void shouldAddActiveJudgmentWhenClaimantAcceptedRepaymentPlanAndJudgmentOnlineLiveEnabled() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            JudgmentDetails activeJudgment = JudgmentDetails.builder().build();
            given(judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(any()))
                .willReturn(activeJudgment);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1ResponseDate(LocalDateTime.now())
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("DEFENDANT_NAME").build())
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(NO)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
                .ccjPaymentDetails(CCJPaymentDetails.builder()
                                       .ccjPaymentPaidSomeOption(YesOrNo.YES)
                                       .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
                                       .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(150))
                                       .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getActiveJudgment()).isNotNull();
            assertThat(updatedCaseData.getJoIsLiveJudgmentExists()).isEqualTo(YES);
            assertThat(updatedCaseData.getJoJudgementByAdmissionIssueDate()).isEqualTo(now);
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(CLAIMANT_RESPONSE_CUI);
    }

}
