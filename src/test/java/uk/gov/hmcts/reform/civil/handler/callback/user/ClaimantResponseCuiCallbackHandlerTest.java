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
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
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
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
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
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

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
import static uk.gov.hmcts.reform.civil.service.PaymentDateService.DATE_FORMATTER;
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
            caseData.setRespondToAdmittedClaim(new RespondToClaim().setHowMuchWasPaid(new BigDecimal(suppliedValuePennies)));
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setCaseDataLiP(
                new CaseDataLiP()
                    .setApplicant1ClaimMediationSpecRequiredLip(
                        new ClaimantMediationLip()
                            .setHasAgreedFreeMediation(MediationDecision.Yes)));

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
            Applicant1DQ applicant1DQ = new Applicant1DQ()
                .setApplicant1DQRequestedCourt(
                    new RequestedCourt()
                        .setResponseCourtCode("court1")
                        .setCaseLocation(
                            new CaseLocationCivil()
                                .setRegion(courtLocation)
                                .setBaseLocation(courtLocation)
                        )
                )
                .setApplicant1DQHearing(new Hearing()
                                          .setHearingLength(ONE_DAY)
                                          .setUnavailableDatesRequired(YES)
                                          .setUnavailableDates(wrapElements(List.of(
                                              UnavailableDate.builder()
                                                  .date(LocalDate.of(2024, 2, 1))
                                                  .dateAdded(LocalDate.of(2024, 1, 1))
                                                  .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                  .build()))));
            Respondent1DQ respondent1DQ = new Respondent1DQ()
                .setRespondent1DQRequestedCourt(new RequestedCourt()
                                                 .setResponseCourtCode("court2")
                                                 .setCaseLocation(new CaseLocationCivil()
                                                                      .setRegion(courtLocation)
                                                                      .setBaseLocation(courtLocation)));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(NO);
            caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(NO);
            caseData.setApplicant1DQ(applicant1DQ);
            caseData.setRespondent1DQ(respondent1DQ);
            caseData.setApplicant1AcceptAdmitAmountPaidSpec(NO);
            caseData.setCaseDataLiP(new CaseDataLiP().setApplicant1ClaimMediationSpecRequiredLip(new ClaimantMediationLip().setHasAgreedFreeMediation(
                    MediationDecision.No)));

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
                new Applicant1DQ().setApplicant1DQRequestedCourt(new RequestedCourt()
                                                                      .setCaseLocation(new CaseLocationCivil()));
            Respondent1DQ respondent1DQ =
                new Respondent1DQ().setRespondent1DQRequestedCourt(new RequestedCourt()
                                                                        .setCaseLocation(new CaseLocationCivil()));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(NO);
            caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(NO);
            caseData.setApplicant1DQ(applicant1DQ);
            caseData.setRespondent1DQ(respondent1DQ);
            caseData.setApplicant1AcceptAdmitAmountPaidSpec(NO);
            caseData.setCaseDataLiP(new CaseDataLiP().setApplicant1ClaimMediationSpecRequiredLip(new ClaimantMediationLip().setHasAgreedFreeMediation(
                    MediationDecision.No)));
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
                new Applicant1DQ().setApplicant1DQRequestedCourt(new RequestedCourt());
            Respondent1DQ respondent1DQ =
                new Respondent1DQ().setRespondent1DQRequestedCourt(new RequestedCourt());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setApplicant1PartAdmitConfirmAmountPaidSpec(NO);
            caseData.setApplicant1PartAdmitIntentionToSettleClaimSpec(NO);
            caseData.setApplicant1DQ(applicant1DQ);
            caseData.setRespondent1DQ(respondent1DQ);
            caseData.setApplicant1AcceptAdmitAmountPaidSpec(NO);
            caseData.setCaseDataLiP(new CaseDataLiP().setApplicant1ClaimMediationSpecRequiredLip(new ClaimantMediationLip().setHasAgreedFreeMediation(
                    MediationDecision.No)));
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
                new LocationRefData()
                    .setEpimmsId("111").setSiteName("Site 1").setCourtAddress("Adr 1").setPostcode("AAA 111")
                    .setRegionId("region 1").setCourtLocationCode("court1"),
                new LocationRefData()
                    .setEpimmsId("222").setSiteName("Site 2").setCourtAddress("Adr 2").setPostcode("BBB 222")
                    .setRegionId("region 2").setCourtLocationCode("court2"),
                new LocationRefData()
                    .setEpimmsId("333").setSiteName("Site 3").setCourtAddress("Adr 3").setPostcode("CCC 333")
                    .setRegionId("region 3").setCourtLocationCode("court3"),
                new LocationRefData()
                    .setEpimmsId("444").setSiteName(LIVERPOOL_SITE_NAME).setCourtAddress("Adr 3").setPostcode("CCC 333")
                    .setRegionId("region 4").setCourtLocationCode("court4")
            ));
        }

        @Test
        void shouldUpdateCCJRequestPaymentDetails() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails()
                .setCcjPaymentPaidSomeOption(YES)
                .setCcjPaymentPaidSomeAmount(BigDecimal.valueOf(600.0))
                .setCcjJudgmentLipInterest(BigDecimal.valueOf(300))
                .setCcjJudgmentAmountClaimFee(BigDecimal.valueOf(0));
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_INDIVIDUAL").build());
            caseData.setRespondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("RESPONDENT_INDIVIDUAL").build());
            caseData.setCaseDataLiP(
                new CaseDataLiP()
                    .setApplicant1LiPResponse(new ClaimantLiPResponse().setApplicant1ChoosesHowToProceed(
                        ChooseHowToProceed.REQUEST_A_CCJ)));
            caseData.setRespondent1Represented(NO);
            caseData.setSpecRespondent1Represented(NO);
            caseData.setApplicant1Represented(NO);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
            caseData.setCcjPaymentDetails(ccjPaymentDetails);
            caseData.setClaimFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(10000)));
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
            assertThat(caseData.getTotalClaimAmount().setScale(2)).isEqualTo(ccjResponseForJudgement.getCcjJudgmentAmountClaimAmount());
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return mapper.convertValue(response.getData(), CaseData.class);
        }

        @Test
        void shouldAddTheCaseFlagIntialiazerForClaimant() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build());
            caseData.setRespondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("CLAIMANT_NAME")
                             .build());
            caseData.setApplicant1DQ(new Applicant1DQ()
                              .setApplicant1DQExperts(new Experts()
                                                       .setExpertRequired(YES)
                                                       .setDetails(wrapElements(new Expert()
                                                                                 .setName("John Smith")
                                                                                 .setFirstName("Jane")
                                                                                 .setLastName("Smith")
                                                       )))
                              .setApplicant1DQWitnesses(new Witnesses().setWitnessesToAppear(YES)
                                                         .setDetails(wrapElements(new Witness()
                                                                                   .setName("John Smith")
                                                                                   .setFirstName("Jane")
                                                                                   .setLastName("Smith")
                                                         ))));

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
            caseData.setIsFlightDelayClaim(YES);
            caseData.setResponseClaimTrack(AllocatedTrack.SMALL_CLAIM.name());
            caseData.setFlightDelayDetails(new FlightDelayDetails().setNameOfAirline("Sri Lankan"));
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
            caseData.setIsFlightDelayClaim(YES);
            caseData.setResponseClaimTrack(AllocatedTrack.SMALL_CLAIM.name());
            caseData.setFlightDelayDetails(new FlightDelayDetails().setNameOfAirline("INVALID_AIRLINE"));
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
                .applicant1DQ(new Applicant1DQ()
                                  .setApplicant1DQExperts(new Experts()
                                                           .setExpertRequired(YES)
                                                           .setDetails(wrapElements(new Expert()
                                                                                     .setName("John Smith")
                                                                                     .setFirstName("Jane")
                                                                                     .setLastName("Smith")
                                                           )))
                                  .setApplicant1DQWitnesses(new Witnesses().setWitnessesToAppear(YES)
                                                             .setDetails(wrapElements(new Witness()
                                                                                       .setName("John Smith")
                                                                                       .setFirstName("Jane")
                                                                                       .setLastName("Smith")
                                                             ))))
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
                            new CaseDataLiP()
                                    .setApplicant1LiPResponse(
                                            new ClaimantLiPResponse()
                                                    .setApplicant1SignedSettlementAgreement(YesOrNo.YES)
                                                    .setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)))
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
                            new CaseDataLiP()
                                    .setApplicant1LiPResponse(
                                            new ClaimantLiPResponse()
                                                    .setApplicant1SignedSettlementAgreement(YesOrNo.YES)))
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
            JudgmentDetails activeJudgment = new JudgmentDetails()
                .setTotalAmount("10100.00")
                .setClaimFeeAmount("100.00")
                .setOrderedAmount("10000");
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
                .ccjPaymentDetails(new CCJPaymentDetails()
                                       .setCcjPaymentPaidSomeOption(YesOrNo.YES)
                                       .setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
                                       .setCcjJudgmentTotalStillOwed(BigDecimal.valueOf(150)))
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
            try {
                when(time.now()).thenReturn(now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getActiveJudgment()).isNotNull();
            assertThat(updatedCaseData.getJoIsLiveJudgmentExists()).isEqualTo(YES);
            assertThat(updatedCaseData.getJoJudgementByAdmissionIssueDate()).isEqualTo(now);
            assertThat(updatedCaseData.getJoRepaymentSummaryObject()).isNotNull();
        }

        @Test
        void shouldUpdateLanguagePreferenceWhenWelshDocsSelected() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1ResponseDate(LocalDateTime.now())
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("DEFENDANT_NAME").build())
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(NO)
                .applicant1DQ(new Applicant1DQ().setApplicant1DQLanguage(
                    new WelshLanguageRequirements().setDocuments(Language.WELSH)))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isEqualTo("WELSH");
        }

        @Test
        void shouldUpdateDefendantPaymentDeadlineForPartAdmitImmediateWhenClaimantAcceptedRepaymentPlan() {
            LocalDate paymentDeadline = LocalDate.now().plusDays(1);
            when(paymentDateService.calculatePaymentDeadline()).thenReturn(paymentDeadline);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1ResponseDate(LocalDateTime.now())
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("DEFENDANT_NAME").build())
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(NO)
                .applicant1DQ(new Applicant1DQ().setApplicant1DQLanguage(
                    new WelshLanguageRequirements().setDocuments(Language.WELSH)))
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1AcceptPartAdmitPaymentPlanSpec(YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getWhenToBePaidText()).isEqualTo(paymentDeadline.format(DATE_FORMATTER));
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(CLAIMANT_RESPONSE_CUI);
    }

}
