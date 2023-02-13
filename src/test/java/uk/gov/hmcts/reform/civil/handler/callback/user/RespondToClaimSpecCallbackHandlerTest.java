package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.CounterClaimConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitAlreadyPaidConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidFullConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidLessConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPayImmediatelyConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.RepayPlanConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.SpecResponse1v2DivergentText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.SpecResponse1v2DivergentHeaderText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.SpecResponse2v1DifferentHeaderText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWOSPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class RespondToClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private RespondToClaimSpecCallbackHandler handler;

    @Mock
    private Time time;
    @Mock
    private PaymentDateValidator validator;
    @Mock
    private UnavailableDateValidator dateValidator;
    @Mock
    private ExitSurveyContentService exitSurveyContentService;
    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private PostcodeValidator postcodeValidator;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;
    @Mock
    private UserService userService;
    @Mock
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private StateFlow mockedStateFlow;
    @Mock
    private StateFlowEngine stateFlowEngine;
    @Mock
    private DateOfBirthValidator dateOfBirthValidator;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Spy
    private List<RespondToClaimConfirmationTextSpecGenerator> confirmationTextGenerators = List.of(
        new FullAdmitAlreadyPaidConfirmationText(),
        new FullAdmitSetDateConfirmationText(),
        new PartialAdmitPaidFullConfirmationText(),
        new PartialAdmitPaidLessConfirmationText(),
        new PartialAdmitPayImmediatelyConfirmationText(),
        new PartialAdmitSetDateConfirmationText(),
        new RepayPlanConfirmationText(),
        new SpecResponse1v2DivergentText(),
        new RepayPlanConfirmationText(),
        new CounterClaimConfirmationText()
    );

    private List<RespondToClaimConfirmationHeaderSpecGenerator> confirmationHeaderSpecGenerators = List.of(
        new SpecResponse1v2DivergentHeaderText(),
        new SpecResponse2v1DifferentHeaderText()
    );

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        ReflectionTestUtils.setField(handler, "confirmationTextSpecGenerators",
                                     confirmationTextGenerators
        );
        ReflectionTestUtils.setField(handler, "confirmationHeaderGenerators",
                                     confirmationHeaderSpecGenerators
        );
    }

    @Test
    void midSpecCorrespondenceAddress_checkAddressIfWasIncorrect() {
        // Given
        String postCode = "postCode";
        CaseData caseData = CaseData.builder()
            .specAoSApplicantCorrespondenceAddressRequired(YesOrNo.NO)
            .specAoSApplicantCorrespondenceAddressdetails(Address.builder()
                                                              .postCode(postCode)
                                                              .build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "specCorrespondenceAddress");
        CallbackRequest request = CallbackRequest.builder()
            .eventId(SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC)
            .build();
        params = params.toBuilder().request(request).build();

        List<String> errors = Collections.singletonList("error 1");
        Mockito.when(postcodeValidator.validatePostCodeForDefendant(postCode)).thenReturn(errors);

        // When
        CallbackResponse response = handler.handle(params);

        // Then
        assertEquals(errors, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
    }

    @Nested
    class DefendAllOfClaimTests {

        @Test
        public void testNotSpecDefendantResponse() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track");
            when(validator.validate(any())).thenReturn(List.of());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
        }

        @Test
        public void testSpecDefendantResponseValidationError() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getErrors()).isNotNull();
            assertEquals(1, response.getErrors().size());
            assertEquals("Validation error", response.getErrors().get(0));
        }

        @Test
        public void testSpecDefendantResponseFastTrack() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
            // need to be non-null to ensure previous data is cleaned
            assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec"))
                .isNotNull();
        }

        @Test
        public void testSpecDefendantResponseFastTrackDefendantPaid() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();

            RespondToClaim respondToClaim = RespondToClaim.builder()
                // how much was paid is pence, total claim amount is pounds
                .howMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(100)))
                .build();

            caseData = caseData.toBuilder()
                .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondToClaim(respondToClaim)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec"))
                .isEqualTo(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT.name());
        }

        @Test
        public void testSpecDefendantResponseFastTrackDefendantPaidLessThanClaimed() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();

            RespondToClaim respondToClaim = RespondToClaim.builder()
                // how much was paid is pence, total claim amount is pounds
                // multiply by less than 100 so defendant paid less than claimed
                .howMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50)))
                .build();

            caseData = caseData.toBuilder()
                .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondToClaim(respondToClaim)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec"))
                .isEqualTo(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT.name());
        }
    }

    @Nested
    class AdmitsPartOfTheClaimTest {

        @Test
        public void testSpecDefendantResponseAdmitPartOfClaimValidationError() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(
                caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getErrors()).isNotNull();
            assertEquals(1, response.getErrors().size());
            assertEquals("Validation error", response.getErrors().get(0));
        }

        @Test
        public void testSpecDefendantResponseAdmitPartOfClaimFastTrack() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(
                caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        }

        @Test
        public void testSpecDefendantResponseAdmitPartOfClaimFastTrackStillOwes() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
            // admitted amount is pence, total claimed is pounds
            BigDecimal admittedAmount = caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50));
            caseData = caseData.toBuilder()
                .respondToAdmittedClaimOwingAmount(admittedAmount)
                .build();
            CallbackParams params = callbackParamsOf(
                caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
            assertEquals(0, new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmount").toString())
                .compareTo(
                    new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmountPounds").toString())
                        .multiply(BigDecimal.valueOf(100))));
        }

        @Test
        public void testValidateLengthOfUnemploymentWithError() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().generateYearsAndMonthsIncorrectInput().build();
            CallbackParams params = callbackParamsOf(caseData,
                                                     MID, "validate-length-of-unemployment",
                                                     "DEFENDANT_RESPONSE_SPEC"
            );

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expectedErrorArray = new ArrayList<>();
            expectedErrorArray.add("Length of time unemployed must be a whole number, for example, 10.");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEqualTo(expectedErrorArray);
        }

        @Test
        public void testValidateRespondentPaymentDate() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().generatePaymentDateForAdmitPartResponse().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-payment-date",
                                                     "DEFENDANT_RESPONSE_SPEC"
            );
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expectedErrorArray = new ArrayList<>();
            expectedErrorArray.add("Date for when will the amount be paid must be today or in the future.");

            // Then
            assertThat(response).isNotNull();
            /*
             * It was not possible to capture the error message generated by @FutureOrPresent in the class
             * */
            //assertThat(response.getErrors()).isEqualTo(expectedErrorArray);
            assertEquals("Validation error", response.getErrors().get(0));
        }

        @Test
        public void testValidateRepaymentDate() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().generateRepaymentDateForAdmitPartResponse().build();
            CallbackParams params = callbackParamsOf(caseData, MID,
                                                     "validate-repayment-plan", "DEFENDANT_RESPONSE_SPEC"
            );
            when(dateValidator.validateFuturePaymentDate(any())).thenReturn(List.of("Validation error"));

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertEquals("Validation error", response.getErrors().get(0));

        }

        @Test
        public void testValidateDefendant2RepaymentDate() {
            CaseData caseData = CaseDataBuilder.builder().generateDefendant2RepaymentDateForAdmitPartResponse().build();
            CallbackParams params = callbackParamsOf(caseData, MID,
                                                     "validate-repayment-plan-2", "DEFENDANT_RESPONSE_SPEC"
            );
            when(dateValidator.validateFuturePaymentDate(any())).thenReturn(List.of("Validation error"));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertEquals("Validation error", response.getErrors().get(0));

        }
    }

    @Nested
    class AboutToSubmitTests {

        @Test
        void updateRespondent1AddressWhenUpdated() {
            // Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWOSPEC))).thenReturn(true);

            Address changedAddress = AddressBuilder.maximal().build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any(), any()))
                .thenReturn(LocalDateTime.now());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine1").isEqualTo(changedAddress.getAddressLine1());
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine2").isEqualTo(changedAddress.getAddressLine2());
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine3").isEqualTo(changedAddress.getAddressLine3());
        }

        @Test
        void updateRespondent2AddressWhenUpdated() {
            // Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWOSPEC))).thenReturn(true);

            Address changedAddress = AddressBuilder.maximal().build();

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(NO)
                .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any(), any()))
                .thenReturn(LocalDateTime.now());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine1").isEqualTo("address line 1");
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine2").isEqualTo("address line 2");
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine3").isEqualTo("address line 3");
        }

        @Test
        void defendantResponsePopulatesWitnessesData() {
            // Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWOSPEC))).thenReturn(true);

            var res1witnesses = Witnesses.builder().details(
                wrapElements(
                    Witness.builder()
                        .firstName("Witness")
                        .lastName("One")
                        .emailAddress("test-witness-one@example.com")
                        .phoneNumber("07865456789")
                        .reasonForWitness("great reasons")
                        .build())
            ).build();

            var res2witnesses = Witnesses.builder().details(
                wrapElements(
                    Witness.builder()
                        .firstName("Witness")
                        .lastName("Two")
                        .emailAddress("test-witness-two@example.com")
                        .phoneNumber("07532628263")
                        .reasonForWitness("good reasons")
                        .build())
            ).build();

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(NO)
                .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                .build().toBuilder()
                .respondent1DQWitnessesSmallClaim(res1witnesses)
                .respondent2DQWitnessesSmallClaim(res2witnesses)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any(), any()))
                .thenReturn(LocalDateTime.now());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getData())
                .extracting("respondent1DQWitnesses")
                .isEqualTo(new ObjectMapper().convertValue(res1witnesses, new TypeReference<>() {
                }));
            assertThat(response.getData())
                .extracting("respondent2DQWitnesses")
                .isEqualTo(new ObjectMapper().convertValue(res2witnesses, new TypeReference<>() {
                }));
        }
    }

    @Nested
    class AboutToSubmitTestsV1 {

        @BeforeEach
        void setup() {
            when(toggleService.isAccessProfilesEnabled()).thenReturn(true);
        }

        @Test
        void updateRespondent1AddressWhenUpdated() {
            // Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            Address changedAddress = AddressBuilder.maximal().build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any(), any()))
                .thenReturn(LocalDateTime.now());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine1").isEqualTo(changedAddress.getAddressLine1());
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine2").isEqualTo(changedAddress.getAddressLine2());
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine3").isEqualTo(changedAddress.getAddressLine3());
        }

        @Test
        void updateRespondent2AddressWhenUpdated() {
            // Given
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            Address changedAddress = AddressBuilder.maximal().build();

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(NO)
                .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any(), any()))
                .thenReturn(LocalDateTime.now());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine1").isEqualTo("address line 1");
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine2").isEqualTo("address line 2");
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine3").isEqualTo("address line 3");
        }

        @Nested
        class HandleLocations {

            @Test
            void oneVOne() {
                // Given
                DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
                DynamicList preferredCourt = DynamicList.builder()
                    .listItems(locationValues.getListItems())
                    .value(locationValues.getListItems().get(0))
                    .build();
                when(toggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);
                Party defendant1 = Party.builder()
                    .type(Party.Type.COMPANY)
                    .companyName("company")
                    .build();
                CaseData caseData = CaseData.builder()
                    .superClaimType(SuperClaimType.SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .respondent1(defendant1)
                    .respondent1Copy(defendant1)
                    .respondent1DQ(
                        Respondent1DQ.builder()
                            .respondToCourtLocation(
                                RequestedCourt.builder()
                                    .responseCourtLocations(preferredCourt)
                                    .reasonForHearingAtSpecificCourt("Reason")
                                    .build()
                            )
                            .build()
                    )
                    .showConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1
                    ))
                    .build();
                CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

                List<LocationRefData> locations = List.of(LocationRefData.builder().build());
                when(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                    .thenReturn(locations);
                LocationRefData completePreferredLocation = LocationRefData.builder()
                    .regionId("regionId")
                    .epimmsId("epimms")
                    .courtLocationCode("code")
                    .build();
                when(courtLocationUtils.findPreferredLocationData(
                    locations, preferredCourt
                )).thenReturn(completePreferredLocation);
                StateFlow flow = mock(StateFlow.class);
                when(flow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
                when(stateFlowEngine.evaluate(caseData))
                    .thenReturn(flow);
                when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                    .thenReturn(true);
                UserInfo userInfo = UserInfo.builder().uid("798").build();
                when(userService.getUserInfo(anyString())).thenReturn(userInfo);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                AbstractObjectAssert<?, ?> sent1 = assertThat(response.getData())
                    .extracting("respondent1DQRequestedCourt");
                sent1.extracting("caseLocation")
                    .extracting("region")
                    .isEqualTo(completePreferredLocation.getRegionId());
                sent1.extracting("caseLocation")
                    .extracting("baseLocation")
                    .isEqualTo(completePreferredLocation.getEpimmsId());
                sent1.extracting("responseCourtCode")
                    .isEqualTo(completePreferredLocation.getCourtLocationCode());
                sent1.extracting("reasonForHearingAtSpecificCourt")
                    .isEqualTo("Reason");
            }

            @Test
            void oneVTwo_SecondDefendantReplies() {
                // Given
                DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
                DynamicList preferredCourt = DynamicList.builder()
                    .listItems(locationValues.getListItems())
                    .value(locationValues.getListItems().get(0))
                    .build();
                when(toggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);
                Party defendant1 = Party.builder()
                    .type(Party.Type.COMPANY)
                    .companyName("company")
                    .build();
                CaseData caseData = CaseData.builder()
                    .superClaimType(SuperClaimType.SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .respondent1(defendant1)
                    .respondent1Copy(defendant1)
                    .respondent1DQ(
                        Respondent1DQ.builder()
                            .respondToCourtLocation(
                                RequestedCourt.builder()
                                    .responseCourtLocations(preferredCourt)
                                    .reasonForHearingAtSpecificCourt("Reason")
                                    .build()
                            )
                            .build()
                    )
                    .respondent2DQ(
                        Respondent2DQ.builder()
                            .respondToCourtLocation2(
                                RequestedCourt.builder()
                                    .responseCourtLocations(preferredCourt)
                                    .reasonForHearingAtSpecificCourt("Reason123")
                                    .build()
                            )
                            .build()
                    )
                    .showConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                    ))
                    .build();
                CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

                List<LocationRefData> locations = List.of(LocationRefData.builder().build());
                when(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                    .thenReturn(locations);
                LocationRefData completePreferredLocation = LocationRefData.builder()
                    .regionId("regionId")
                    .epimmsId("epimms")
                    .courtLocationCode("code")
                    .build();
                when(courtLocationUtils.findPreferredLocationData(
                    locations, preferredCourt
                )).thenReturn(completePreferredLocation);
                StateFlow flow = mock(StateFlow.class);
                when(flow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
                when(stateFlowEngine.evaluate(caseData)).thenReturn(flow);
                when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
                    .thenReturn(true);
                UserInfo userInfo = UserInfo.builder().uid("798").build();
                when(userService.getUserInfo(anyString())).thenReturn(userInfo);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                AbstractObjectAssert<?, ?> sent2 = assertThat(response.getData())
                    .extracting("respondent2DQRequestedCourt");
                sent2.extracting("caseLocation")
                    .extracting("region")
                    .isEqualTo(completePreferredLocation.getRegionId());
                sent2.extracting("caseLocation")
                    .extracting("baseLocation")
                    .isEqualTo(completePreferredLocation.getEpimmsId());
                sent2.extracting("responseCourtCode")
                    .isEqualTo(completePreferredLocation.getCourtLocationCode());
                sent2.extracting("reasonForHearingAtSpecificCourt")
                    .isEqualTo("Reason123");
            }
        }
    }

    @Nested
    class MidEventSetGenericResponseTypeFlagCallback {

        private static final String PAGE_ID = "set-generic-response-type-flag";

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Counter_Admit_OR_Admit_Part_combination1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2v1BothNotFullDefence_PartAdmissionX2()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Counter_Admit_OR_Admit_Part_combination2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2v1BothNotFullDefence_CounterClaimX2()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
        }

        /**
         * if solicitor says that each defendant gets their response but then chooses the same
         * option from full defence/part admit/full admit/counterclaim, then it is not different response.
         */
        @Test
        void shouldSetMultiPartyResponseTypeFlags_1v2_sameSolicitor_DifferentResponse() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("FULL_DEFENCE");
            assertThat(response.getData()).extracting("sameSolicitorSameResponse")
                .isEqualTo("Yes");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_AdmitAll_OR_Admit_Part_1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent1v2AdmitAll_AdmitPart()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_FullDefence_OR_AdmitAll_1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent1v2FullDefence_AdmitPart()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
        }
    }

    @Nested
    class MidEventCallbackValidateWitnesses {

        private static final String PAGE_ID = "witnesses";

        @Test
        void shouldReturnError_whenWitnessRequiredAndNullDetails() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1DQWitnessesRequiredSpec(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvided() {
            // Given
            List<Element<Witness>> testWitness = wrapElements(Witness.builder().name("test witness").build());
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).details(testWitness).build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1DQWitnessesRequiredSpec(YES)
                .respondent1DQWitnessesDetailsSpec(testWitness)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessNotRequired() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1DQWitnessesRequiredSpec(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
            String claimNumber = caseData.getLegacyCaseReference();

            String body = format(
                "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                    + "%n%nThe claimant has until 4pm on %s to respond to your claim. "
                    + "We will let you know when they respond."
                    + "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
                formatLocalDateTime(responseDeadline, DATE),
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You have submitted your response%n## Claim number: %s", claimNumber))
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void specificSummary_whenPartialAdmitNotPay() {
            // Given
            BigDecimal admitted = BigDecimal.valueOf(1000);
            LocalDate whenWillPay = LocalDate.now().plusMonths(1);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .respondToClaimAdmitPartLRspec(
                    RespondToClaimAdmitPartLRspec.builder()
                        .whenWillThisAmountBePaid(whenWillPay)
                        .build()
                )
                .totalClaimAmount(admitted.multiply(BigDecimal.valueOf(2)))
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
            String claimNumber = caseData.getLegacyCaseReference();

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(admitted.toString())
                .contains(caseData.getTotalClaimAmount().toString())
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE));
        }

        @Test
        void specificSummary_whenPartialAdmitPayImmediately() {
            // Given
            BigDecimal admitted = BigDecimal.valueOf(1000);
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE));
        }

        @Test
        void specificSummary_whenRepayPlanFullAdmit() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(
                    RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("repayment plan");
        }

        @Test
        void specificSummary_whenRepayPlanPartialAdmit() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(
                    RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("repayment plan");
        }

        @Test
        void specificSummary_whenFullAdmitAlreadyPaid() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.YES)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(caseData.getTotalClaimAmount().toString())
                .contains("you've paid");
        }

        @Test
        void specificSummary_whenFullAdmitBySetDate() {
            // Given
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .defenceAdmitPartPaymentTimeRouteRequired(
                    RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToClaimAdmitPartLRspec(
                    RespondToClaimAdmitPartLRspec.builder()
                        .whenWillThisAmountBePaid(whenWillPay)
                        .build()
                )
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(" and your explanation of why you cannot pay before then.")
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE))
                .doesNotContain(caseData.getTotalClaimAmount().toString());

        }

        @Test
        void specificSummary_whenPartialAdmitPaidFull() {
            // Given
            BigDecimal totalClaimAmount = BigDecimal.valueOf(1000);
            BigDecimal howMuchWasPaid = new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YesOrNo.YES)
                .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
                .totalClaimAmount(totalClaimAmount)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(caseData.getTotalClaimAmount().toString());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseFullDefenceFullAdmission() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(
                    RespondentResponseTypeSpec.FULL_DEFENCE,
                    RespondentResponseTypeSpec.FULL_ADMISSION
                )
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format(
                    "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append(String.format(
                    "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# The defendants have chosen their responses%n## Claim number <br>%s",
                        claimNumber
                    ))
                    .confirmationBody(body.toString())
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponsePartAdmissionFullAdmission() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(
                    RespondentResponseTypeSpec.PART_ADMISSION,
                    RespondentResponseTypeSpec.FULL_ADMISSION
                )
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format(
                    "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append(String.format(
                    "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# The defendants have chosen their responses%n## Claim number <br>%s",
                        claimNumber
                    ))
                    .confirmationBody(body.toString())
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseCounterClaimFullAdmission() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(
                    RespondentResponseTypeSpec.COUNTER_CLAIM,
                    RespondentResponseTypeSpec.FULL_ADMISSION
                )
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format(
                    "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append(String.format(
                    "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# The defendants have chosen their responses%n## Claim number <br>%s",
                        claimNumber
                    ))
                    .confirmationBody(body.toString())
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseCounterClaimPartAdmission() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(
                    RespondentResponseTypeSpec.COUNTER_CLAIM,
                    RespondentResponseTypeSpec.PART_ADMISSION
                )
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format(
                    "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append(String.format(
                    "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                    format("https://www.gov.uk/respond-money-claim")
                ))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# The defendants have chosen their responses%n## Claim number <br>%s",
                        claimNumber
                    ))
                    .confirmationBody(body.toString())
                    .build());
        }

        @Test
        void specificSummary_whenPartialAdmitPaidLess() {
            // Given
            BigDecimal howMuchWasPaid = BigDecimal.valueOf(1000);
            BigDecimal totalClaimAmount = BigDecimal.valueOf(10000);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YesOrNo.YES)
                .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
                .totalClaimAmount(totalClaimAmount)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("The claim will be settled. We'll contact you when they respond.")
                .contains(MonetaryConversions.penniesToPounds(caseData.getRespondToAdmittedClaim().getHowMuchWasPaid())
                              .toString());

        }

        @Test
        void specificSummary_whenCounterClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                .doesNotContain(caseData.getApplicant1().getPartyName())
                .contains("You've chosen to counterclaim - this means your defence cannot continue online.");
        }
    }

    @Nested
    class ValidateDateOfBirth {

        @Test
        void when1v1_thenSameSolSameResponseNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "confirm-details");
            when(dateOfBirthValidator.validate(any())).thenReturn(Collections.emptyList());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getData())
                .doesNotHaveToString("sameSolicitorSameResponse");
        }
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldPopulateCourtLocations() {
            // Given
            CaseData caseData = CaseData.builder().build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            List<LocationRefData> locations = List.of(LocationRefData.builder()
                                                          .build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .thenReturn(locations);
            when(toggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            when(courtLocationUtils.getLocationsFromList(locations))
                .thenReturn(locationValues);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            System.out.println(response.getData());

            // Then
            assertThat(response.getData())
                .extracting("respondToCourtLocation")
                .extracting("responseCourtLocations")
                .extracting("list_items").asList()
                .extracting("label")
                .containsExactly(locationValues.getListItems().get(0).getLabel());
        }
    }
}
