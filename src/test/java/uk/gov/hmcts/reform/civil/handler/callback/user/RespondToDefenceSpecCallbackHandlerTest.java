package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.service.citizenui.RespondentMediationService;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceSpecCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class,
    CourtLocationUtils.class,
    LocationHelper.class
})
class RespondToDefenceSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToDefenceSpecCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UnavailableDateValidator unavailableDateValidator;

    @MockBean
    private Time time;

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private LocationRefDataService locationRefDataService;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;
    @MockBean
    private RespondentMediationService respondentMediationService;

    @Nested
    class AboutToStart {

        @Test
        void shouldPopulateInitialData() {
            var params = callbackParamsOf(
                CaseData.builder()
                    .respondent1(Party.builder()
                                     .type(Party.Type.COMPANY)
                                     .companyName("company name")
                                     .build())
                    .build(),
                ABOUT_TO_START
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("respondent1Copy")
                .isNotNull();
            assertThat(response.getData()).extracting("claimantResponseScenarioFlag")
                .isNotNull();
        }

        @Test
        void shouldPopulateInitialDataV1() {
            var params = callbackParamsOf(
                CallbackVersion.V_1,
                CaseData.builder()
                    .respondent1(Party.builder()
                                     .type(Party.Type.COMPANY)
                                     .companyName("company name")
                                     .build())
                    .build(),
                ABOUT_TO_START
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("respondent1Copy")
                .isNotNull();
            assertThat(response.getData()).extracting("claimantResponseScenarioFlag")
                .isNotNull();
        }

        // TODO my test
        @Test
        void shouldPopulateCourtLocations() {
            when(featureToggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);
            when(courtLocationUtils.getLocationsFromList(any()))
                .thenReturn(fromList(List.of("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124")));

            CaseData caseData = CaseData.builder().build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            System.out.println(getCaseData(response));

            DynamicList dynamicList = getCaseData(response).getApplicant1DQ()
                .getApplicant1DQRequestedCourt().getResponseCourtLocations();

            List<String> courtlist = dynamicList.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .collect(Collectors.toList());

            assertThat(courtlist).containsOnly("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124");
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }
    }

    @Nested
    class ValidateUnavailableDates {

        @Test
        void shouldCheckDates_whenFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearingLRspec(Hearing.builder()
                                                                 .build())
                                  .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(MID, caseData)
                .pageId("validate-unavailable-dates")
                .build();

            Mockito.when(unavailableDateValidator.validateFastClaimHearing(
                    caseData.getApplicant1DQ().getApplicant1DQHearingLRspec()))
                .thenReturn(Collections.emptyList());

            handler.handle(params);

            Mockito.verify(unavailableDateValidator).validateFastClaimHearing(
                caseData.getApplicant1DQ().getApplicant1DQHearingLRspec());
        }

        @Test
        void shouldCheckDates_whenSmallClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQSmallClaimHearing(SmallClaimHearing.builder()
                                                                     .build())
                                  .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(MID, caseData)
                .pageId("validate-unavailable-dates")
                .build();

            Mockito.when(unavailableDateValidator.validateSmallClaimsHearing(
                    caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing()))
                .thenReturn(Collections.emptyList());

            handler.handle(params);

            Mockito.verify(unavailableDateValidator).validateSmallClaimsHearing(
                caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing());
        }
    }

    @Nested
    class MidEventCallbackValidateWitnesses {

        private static final String PAGE_ID = "witnesses";

        @Test
        void shouldReturnError_whenWitnessRequiredAndNullDetails() {
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvided() {
            List<Element<Witness>> testWitness = wrapElements(Witness.builder().name("test witness").build());
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).details(testWitness).build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessNotRequired() {
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(NO).build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateExperts {

        private static final String PAGE_ID = "experts";

        @Test
        void shouldReturnError_whenExpertRequiredAndNullDetails() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Expert details required");
        }

        @Test
        void shouldReturnNoError_whenExpertRequiredAndDetailsProvided() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .details(wrapElements(Expert.builder()
                                                                                     .name("test expert").build()))
                                                           .build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenExpertNotRequired() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(NO)
                                                           .build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackSetApplicantProceedFlag {

        private static final String PAGE_ID = "set-applicant1-proceed-flag";

        @Test
        void shouldSetApplicant1Proceed_whenCaseIs2v1AndApplicantIntendsToProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec2v1ClaimSubmitted()
                .atStateRespondent2v1FullDefence()
                .applicant1ProceedWithClaimSpec2v1(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicant1ProceedWithClaim"))
                .isEqualTo("Yes");
        }

        @Test
        void shouldNotSetApplicant1Proceed_whenCaseIs2v1AndApplicantNotIntendsToProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec2v1ClaimSubmitted()
                .atStateRespondent2v1FullDefence()
                .applicant1ProceedWithClaimSpec2v1(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicant1ProceedWithClaim"))
                .isEqualTo(null);
        }

        @Test
        void shouldSetVulnerability_whenRejectAllAndProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullDefenceSpec()
                .applicant1ProceedWithClaim(YES)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_2, caseData, MID,
                                                     "set-applicant-route-flags");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("showConditionFlags").asList()
                    .contains(DefendantResponseShowTag.VULNERABILITY.name());
        }

        @Test
        void shouldSetVulnerability_whenNotAgreeOwedAmount() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec1v1ClaimSubmitted()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_2, caseData, MID,
                                                     "set-applicant-route-flags");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("showConditionFlags").asList()
                .contains(DefendantResponseShowTag.VULNERABILITY.name());
        }
    }

    @Nested
    class MidEventCallbackSetApplicantRoutesFlag {

        private static final String PAGE_ID = "set-applicant-route-flags";

        @Test
        void shouldSetApplicantRouteFlag_whenClaimantRejectPartPaymentPlan() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1AcceptAdmitAmountPaidSpec(NO)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicantDefenceResponseDocumentAndDQFlag"))
                .isEqualTo("Yes");
        }

        @Test
        void shouldSetApplicantRouteFlag_whenItsFullDefence() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant1ProceedWithClaim(YES)
                .applicant1ProceedWithClaimSpec2v1(YES)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicantDefenceResponseDocumentAndDQFlag"))
                .isEqualTo("Yes");
        }

        @Test
        void shouldNotSetApplicantRouteFlag_whenClaimantAcceptPartPaymentPlan() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1AcceptAdmitAmountPaidSpec(YES)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicantDefenceResponseDocumentAndDQFlag"))
                .isEqualTo("No");
        }
    }

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetStatementOfTruthFieldsToNull_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("uiStatementOfTruth")
                .doesNotHaveToString("name")
                .doesNotHaveToString("role");
        }
    }

    @Nested
    class AboutToSubmitCallback {
        private final LocalDateTime localDateTime = now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(localDateTime);
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateBusinessProcess_whenAtFullDefenceState(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CaseDataBuilder.builder().atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE_SPEC.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateBusinessProcess_whenAtFullDefenceStateV1(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CallbackVersion.V_1,
                CaseDataBuilder.builder().atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE_SPEC.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }

        @Nested
        class ResetStatementOfTruth {

            @Test
            void shouldAddUiStatementOfTruthToApplicantStatementOfTruth_whenInvoked() {
                String name = "John Smith";
                String role = "Solicitor";

                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build()
                    .toBuilder()
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(
                        caseData,
                        ABOUT_TO_SUBMIT
                    ));

                assertThat(response.getData())
                    .extracting("applicant1DQStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly("John Smith", "Solicitor");

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .doesNotHaveToString("name")
                    .doesNotHaveToString("role");
            }
        }

        @Nested
        class HandleCourtLocation {
            @BeforeEach
            void setup() {
                when(featureToggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);
            }

            @Test
            void shouldHandleCourtLocationData() {
                LocationRefData locationA = LocationRefData.builder()
                    .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                    .courtAddress("Lane 1").postcode("123").build();
                when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                    .thenReturn(locationA);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .applicant1DQ(
                        Applicant1DQ.builder().applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(DynamicList.builder().build())
                                .build()).build())
                    .build();

                CallbackParams callbackParams = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtLocations").isNull();

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("caseLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("regionId1", "epimmsId1");

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtCode").isEqualTo("312");
            }

        }
    }

    @Nested
    class ConfirmationText {

        @Test
        void summary_WhenProceeds() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .applicant1ProceedWithClaim(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);

            assertThat(response.getConfirmationBody())
                .contains("contact you about what to do next");
            assertThat(response.getConfirmationHeader())
                .contains(
                    "decided to proceed",
                    caseData.getLegacyCaseReference()
                );
        }

        @Test
        void summary_WhenDoesNotProceed() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .applicant1ProceedWithClaim(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);

            assertThat(response.getConfirmationBody())
                .contains("not to proceed");
            assertThat(response.getConfirmationHeader())
                .contains(
                    "not to proceed",
                    caseData.getLegacyCaseReference()
                );
        }
    }

    @Nested
    class SetUpOveVOneFlag {

        @Test
        void shouldGetOneVOneFullDefenceFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOnePartAdmitFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YES)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_HAS_PAID);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOnePartAdmitBySetDateFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .specDefenceAdmittedRequired(NO)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_BY_SET_DATE);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOneFullAdmitFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YES)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();
            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_HAS_PAID);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOneFullAdmitBySetDateFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .specDefenceFullAdmittedRequired(NO)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();
            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_BY_SET_DATE);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOneCounterClaimFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_COUNTER_CLAIM);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetNullFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent2(PartyBuilder.builder().company().build())
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isNull();
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }
    }

    @Nested
    class SetUpPaymentDateToStringField {
        @Test
        void shouldSetUpPaymentDateToString() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            LocalDate whenWillPay = LocalDate.now().plusDays(5);

            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec =
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build();

            CaseData caseData = CaseData.builder()
                .respondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            String result = getCaseData(response).getRespondent1PaymentDateToStringSpec();

            assertThat(result).isEqualTo(whenWillPay
                                             .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldSetUpPaymentDateToStringForPartAdmitPaid() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            LocalDate whenWillPay = LocalDate.now().plusDays(5);

            RespondToClaim respondToAdmittedClaim =
                RespondToClaim.builder()
                    .howMuchWasPaid(null)
                    .whenWasThisAmountPaid(whenWillPay)
                    .build();

            CaseData caseData = CaseData.builder()
                .respondToAdmittedClaim(respondToAdmittedClaim)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            String result = getCaseData(response).getRespondent1PaymentDateToStringSpec();

            assertThat(result).isEqualTo(whenWillPay
                                             .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldSetUpPaymentDateForResponseDateToString() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            LocalDate whenWillPay = LocalDate.now().plusDays(5);

            CaseData caseData = CaseData.builder()
                .respondent1ResponseDate(LocalDateTime.now())
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            String result = getCaseData(response).getRespondent1PaymentDateToStringSpec();

            assertThat(result).isEqualTo(whenWillPay
                                             .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }

    }

    @Nested
    class PaymentDateValidationCallback {

        private static final String PAGE_ID = "validate-respondent-payment-date";

        @Test
        void shouldReturnError_whenPastPaymentDate() {
            PaymentBySetDate paymentBySetDate = PaymentBySetDate.builder()
                .paymentSetDate(LocalDate.now().minusDays(15)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1RequestedPaymentDateForDefendantSpec(paymentBySetDate)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Enter a date that is today or in the future");
        }

        @Test
        void shouldNotReturnError_whenFuturePaymentDate() {
            PaymentBySetDate paymentBySetDate = PaymentBySetDate.builder()
                .paymentSetDate(LocalDate.now().plusDays(15)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1RequestedPaymentDateForDefendantSpec(paymentBySetDate)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class SetUpPaymentAmountField {

        @Test
        void shouldConvertPartAdmitPaidValueFromPenniesToPounds() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            RespondToClaim respondToAdmittedClaim =
                RespondToClaim.builder()
                    .howMuchWasPaid(BigDecimal.valueOf(1050))
                    .build();

            CaseData caseData = CaseData.builder()
                .respondToAdmittedClaim(respondToAdmittedClaim)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            BigDecimal result = getCaseData(response).getPartAdmitPaidValuePounds();

            assertThat(result).isEqualTo(new BigDecimal("10.50"));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }
    }

    @Nested
    class MidEventCallbackValidateAmountPaidFlag {

        private static final String PAGE_ID = "validate-amount-paid";

        @Test
        void shouldSetApplicant1Proceed_whenCaseIs2v1AndApplicantIntendsToProceed() {

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(150000))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("The amount paid must be less than the full claim amount.");
        }
    }

    @Nested
    class MidEventCallbackSetUpCcjSummaryPage {

        private static final String PAGE_ID = "set-up-ccj-amount-summary";

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceed() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWhenPartPaymentAccepted() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1AcceptPartAdmitPaymentPlanSpec(YES)
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(500))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimAmount = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount();
            assertThat(claimAmount).isEqualTo(BigDecimal.valueOf(500));

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWithFixedCost() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount())
                .add(BigDecimal.valueOf(40));
            BigDecimal fixedCost = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentFixedCostAmount();
            BigDecimal expectedFixedCost = BigDecimal.valueOf(40);
            assertThat(subTotal).isEqualTo(expectedSubTotal);
            assertThat(fixedCost).isEqualTo(expectedFixedCost);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }
    }

    @Nested
    class MidEventCallbackSetMediationShowFlag {

        private static final String PAGE_ID = "set-mediation-show-tag";

        @Test
        void shouldSetMediationShowFlag_whenGivenConditionMeets() {
            CaseData caseData = CaseDataBuilder.builder().build();
            given(respondentMediationService.setMediationRequired(any())).willReturn(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            Set<DefendantResponseShowTag> showConditionFlags = getCaseData(response).getShowConditionFlags();
            assertThat(showConditionFlags).contains(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);
        }

        @Test
        void shouldNotSetMediationShowFlag_whenGivenConditionNotMeet() {
            CaseData caseData = CaseDataBuilder.builder().build();
            given(respondentMediationService.setMediationRequired(any())).willReturn(null);
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("showConditionFlags").isNull();
        }
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }
}
