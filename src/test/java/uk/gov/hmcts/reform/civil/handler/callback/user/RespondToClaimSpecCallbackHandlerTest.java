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
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
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
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
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
    private AssignCategoryId assignCategoryId;
    @Mock
    private CourtLocationUtils courtLocationUtils;
    @Mock
    private CaseFlagsInitialiser caseFlagsInitialiser;

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
    void whenCallBackEventNotImplementedOrEventInvalid() {
        // Given
        String postCode = "postCode";
        CaseData caseData = CaseData.builder()
            .build().toBuilder()
            .respondentSolicitor1ServiceAddressRequired(NO)
            .respondentSolicitor1ServiceAddress(Address.builder().postCode(postCode).build())
            .isRespondent1(YES)
            .build();
        CallbackParams callbackParams = callbackParamsOf(caseData, CallbackType.MID, " ").toBuilder()
            .request(CallbackRequest.builder().eventId(SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC)
                         .build()).build();

        //When
        CallbackException ex = assertThrows(CallbackException.class, () -> handler.handle(callbackParams),

                                                                            "A CallbackException was expected to be thrown but wasn't.");
        // Then
        assertThat(ex.getMessage()).contains("Callback for event");
    }

    @Test
    void resetStatementOfTruth() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "statement-of-truth");
        CallbackRequest request = CallbackRequest.builder()
            .eventId(SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC)
            .build();
        params = params.toBuilder().request(request).build();

        // When
        CallbackResponse response = handler.handle(params);

        // Then
        assertNotNull(response);
    }

    @Test
    void midSpecCorrespondenceAddress_checkAddressIfWasIncorrect() {
        // Given
        String postCode = "postCode";
        CaseData caseData = CaseData.builder()
            .build().toBuilder()
            .respondentSolicitor1ServiceAddressRequired(NO)
            .respondentSolicitor1ServiceAddress(Address.builder().postCode(postCode).build())
            .isRespondent1(YES)
            .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "specCorrespondenceAddress");
        CallbackRequest request = CallbackRequest.builder()
            .eventId(SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC)
            .build();
        params = params.toBuilder().request(request).build();

        List<String> errors = Collections.singletonList("error 1");
        Mockito.when(postcodeValidator.validate(postCode)).thenReturn(errors);

        // When
        CallbackResponse response = handler.handle(params);

        // Then
        assertEquals(errors, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
    }

    @Test
    void midSpecCorrespondenceAddress_checkAddressIfWasIncorrectForSol2() {
        // Given
        String postCode = "postCode";
        CaseData caseData = CaseData.builder()
            .build().toBuilder()
            .respondentSolicitor1ServiceAddressRequired(YES)
            .respondentSolicitor1ServiceAddress(Address.builder().postCode(postCode).build())
            .isRespondent1(YES)
            .isRespondent2(YES)
            .respondentSolicitor2ServiceAddressRequired(NO)
            .respondentSolicitor2ServiceAddress(Address.builder().postCode(postCode).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "specCorrespondenceAddress");
        CallbackRequest request = CallbackRequest.builder()
            .eventId(SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC)
            .build();
        params = params.toBuilder().request(request).build();

        List<String> errors = Collections.singletonList("error 1");
        Mockito.when(postcodeValidator.validate(postCode)).thenReturn(errors);

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
        void testSpecDefendantResponseFastTrackOneVTwoLegalRep() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(NO)
                .isRespondent2(YES)
                .build();
            caseData = caseData.toBuilder().showConditionFlags(EnumSet.of(
                DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1
            )).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData()).containsEntry("specDisputesOrPartAdmission", "No");
            assertThat(response.getData()).containsEntry("specPaidLessAmountOrDisputesOrPartAdmission",
                "No");
        }

        @Test
        void testSpecDefendantResponseFastTrackOneVTwoSameLegalRep() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .isRespondent2(YES)
                .build();
            caseData = caseData.toBuilder().showConditionFlags(EnumSet.of(
                DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
            )).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData()).containsEntry("specDisputesOrPartAdmission", "No");
            assertThat(response.getData()).containsEntry("specPaidLessAmountOrDisputesOrPartAdmission",
                                                         "No");
        }

        @Test
        void testSpecDefendantResponseFastTrackTwoVOne() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .addApplicant2()
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            caseData = caseData.toBuilder().defendantSingleResponseToBothClaimants(YES)
                .respondent1ClaimResponseTestForSpec(FULL_ADMISSION).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData()).containsEntry("specDisputesOrPartAdmission", "No");
            assertThat(response.getData()).containsEntry("specPaidLessAmountOrDisputesOrPartAdmission",
                                                         "No");
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
    class MidSpecHandleResponseType {

        @Test
        public void testHandleRespondentResponseTypeForSpec() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "specHandleResponseType");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("specDefenceFullAdmittedRequired", "No");
        }
    }

    @Nested
    class MidSetUploadTimelineTypeFlag {

        @Test
        void testSetUploadTimelineTypeFlag() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .isRespondent1(YES)
                .isRespondent2(YES)
                .setSpecClaimResponseTimelineList(TimelineUploadTypeSpec.UPLOAD)
                .setSpecClaimResponseTimelineList2(TimelineUploadTypeSpec.UPLOAD)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "set-upload-timeline-type-flag");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).extracting("showConditionFlags").asList()
                .contains(DefendantResponseShowTag.TIMELINE_UPLOAD.name());
        }

        @Test
        void testSetManualTimelineTypeFlag() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .isRespondent1(YES)
                .isRespondent2(YES)
                .setSpecClaimResponseTimelineList(TimelineUploadTypeSpec.MANUAL)
                .setSpecClaimResponseTimelineList2(TimelineUploadTypeSpec.MANUAL)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "set-upload-timeline-type-flag");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).extracting("showConditionFlags").asList()
                .contains(DefendantResponseShowTag.TIMELINE_MANUALLY.name());
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
        public void testSpecDefendantResponseAdmitPartOfClaimFastTrackRespondent2() {
            // Given
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .ccdCaseReference(354L)
                .totalClaimAmount(new BigDecimal(100000))
                .respondent1(PartyBuilder.builder().individual().build())
                .isRespondent1(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .isRespondent2(YES)
                .defenceAdmitPartEmploymentType2Required(YES)
                .defenceAdmitPartEmploymentType2Required(YES)
                .specDefenceAdmitted2Required(YES)
                .specDefenceAdmittedRequired(YES)
                .showConditionFlags(EnumSet.of(
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                ))
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
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.MULTI_CLAIM.name());
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

            Address changedAddress = AddressBuilder.maximal().build();

            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .tempAddress1Required(NO)
                .tempAddress1(AddressBuilder.maximal().build())
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
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            Address newAddress2 = AddressBuilder.defaults().build();
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build().toBuilder()
                .tempAddress1Required(YES)
                .tempAddress1(Address.builder().build())
                .build().toBuilder()
                .tempAddress2Required(NO)
                .tempAddress2(newAddress2)
                .respondentSolicitor2ServiceAddressRequired(NO)
                .respondentSolicitor2ServiceAddress(newAddress2)
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
                .extracting("AddressLine1").isEqualTo(newAddress2.getAddressLine1());
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine2").isEqualTo(newAddress2.getAddressLine2());
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine3").isEqualTo(newAddress2.getAddressLine3());
        }

        @Test
        void defendantResponsePopulatesWitnessesData() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            LocalDate date = dateTime.toLocalDate();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(toggleService.isHmcEnabled()).thenReturn(true);

            Witnesses res1witnesses = Witnesses.builder().details(
                wrapElements(
                    Witness.builder()
                        .firstName("Witness")
                        .lastName("One")
                        .emailAddress("test-witness-one@example.com")
                        .phoneNumber("07865456789")
                        .reasonForWitness("great reasons")
                        .eventAdded("Defendant Response Event")
                        .dateAdded(date)
                        .build())
            ).build();

            Witnesses res2witnesses = Witnesses.builder().details(
                wrapElements(
                    Witness.builder()
                        .firstName("Witness")
                        .lastName("Two")
                        .emailAddress("test-witness-two@example.com")
                        .phoneNumber("07532628263")
                        .reasonForWitness("good reasons")
                        .eventAdded("Defendant Response Event")
                        .dateAdded(date)
                        .build())
            ).build();

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build().toBuilder()
                .respondent1DQWitnessesSmallClaim(res1witnesses)
                .respondent2DQWitnessesSmallClaim(res2witnesses)
                .build().toBuilder()
                .tempAddress1Required(YES)
                .tempAddress1(Address.builder().build())
                .respondent2ResponseDate(dateTime)
                .respondent1ResponseDate(dateTime).build().toBuilder()
                .tempAddress2Required(NO)
                .tempAddress2(AddressBuilder.maximal().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any(), any()))
                .thenReturn(LocalDateTime.now());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            var objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Then

            Witnesses actualRespondent1DQWitnesses = objectMapper.convertValue(response.getData().get("respondent1DQWitnesses"), new TypeReference<>() {});
            Witness actualRespondent1Witness = unwrapElements(actualRespondent1DQWitnesses.getDetails()).get(0);
            assertThat(actualRespondent1Witness.getPartyID()).isNotNull();
            assertThat(actualRespondent1Witness.getFirstName()).isEqualTo("Witness");
            assertThat(actualRespondent1Witness.getLastName()).isEqualTo("One");
            assertThat(actualRespondent1Witness.getEmailAddress()).isEqualTo("test-witness-one@example.com");
            assertThat(actualRespondent1Witness.getPhoneNumber()).isEqualTo("07865456789");
            assertThat(actualRespondent1Witness.getReasonForWitness()).isEqualTo("great reasons");
            assertThat(actualRespondent1Witness.getEventAdded()).isEqualTo("Defendant Response Event");
            assertThat(actualRespondent1Witness.getDateAdded()).isEqualTo(date);

            Witnesses actualRespondent2DQWitnesses = objectMapper.convertValue(response.getData().get("respondent2DQWitnesses"), new TypeReference<>() {});
            Witness respondent2Witness = unwrapElements(actualRespondent2DQWitnesses.getDetails()).get(0);
            assertThat(respondent2Witness.getPartyID()).isNotNull();
            assertThat(respondent2Witness.getFirstName()).isEqualTo("Witness");
            assertThat(respondent2Witness.getLastName()).isEqualTo("Two");
            assertThat(respondent2Witness.getEmailAddress()).isEqualTo("test-witness-two@example.com");
            assertThat(respondent2Witness.getPhoneNumber()).isEqualTo("07532628263");
            assertThat(respondent2Witness.getReasonForWitness()).isEqualTo("good reasons");
            assertThat(respondent2Witness.getEventAdded()).isEqualTo("Defendant Response Event");
            assertThat(respondent2Witness.getDateAdded()).isEqualTo(date);

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
                Party defendant1 = Party.builder()
                    .type(Party.Type.COMPANY)
                    .companyName("company")
                    .build();
                CaseData caseData = CaseData.builder()
                    .caseAccessCategory(SPEC_CLAIM)
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
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

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
            void oneVTwo_SecondDefendantRepliesSameLegalRep() {
                // Given
                DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
                DynamicList preferredCourt = DynamicList.builder()
                    .listItems(locationValues.getListItems())
                    .value(locationValues.getListItems().get(0))
                    .build();
                Party defendant1 = Party.builder()
                    .type(Party.Type.COMPANY)
                    .companyName("company")
                    .build();
                Party defendant2 = Party.builder()
                    .type(Party.Type.COMPANY)
                    .companyName("company2")
                    .build();
                CaseData caseData = CaseData.builder()
                    .respondent2SameLegalRepresentative(YES)
                    .caseAccessCategory(SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
                    .respondent2ClaimResponseTypeForSpec(FULL_ADMISSION)
                    .respondent1(defendant1)
                    .respondent1Copy(defendant1)
                    .respondent2(defendant2)
                    .respondent2Copy(defendant2)
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
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

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

            @Test
            void oneVTwo_SecondDefendantReplies() {
                // Given
                DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
                DynamicList preferredCourt = DynamicList.builder()
                    .listItems(locationValues.getListItems())
                    .value(locationValues.getListItems().get(0))
                    .build();
                Party defendant1 = Party.builder()
                    .type(Party.Type.COMPANY)
                    .companyName("company")
                    .build();
                Party defendant2 = Party.builder()
                    .type(Party.Type.COMPANY)
                    .companyName("company 2")
                    .build();
                CaseData caseData = CaseData.builder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .respondent1(defendant1)
                    .respondent1Copy(defendant1)
                    .respondent2(defendant2)
                    .respondent2Copy(defendant2)
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
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

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

    @Test
    void shouldNullDocuments_whenInvokedAndCaseFileEnabled() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        var testDocument = ResponseDocument.builder()
            .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl("binary-url").build()).build();

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .ccdCaseReference(354L)
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("respondent1SpecDefenceResponseDocument")).isNull();
        assertThat(response.getData().get("respondent2SpecDefenceResponseDocument")).isNull();
    }

    @Test
    void shouldUpdateExpertEvents_whenInvokedAndUpdateContactDetailsEnabled_For2DivergeResponse() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isUpdateContactDetailsEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts.builder()
                                                         .details(wrapElements(Expert.builder().build())).build())
                               .build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .claimant1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .claimant2ClaimResponseTypeForSpec(FULL_ADMISSION)
            .ccdCaseReference(354L)
            .respondent1ResponseDate(dateTime).build().toBuilder()
            .respondent2SameLegalRepresentative(YES)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("respondent1DQExperts")).isNotNull();
        assertThat(response.getData().get("respondent2DQExperts")).isNull();

    }

    @Test
    void shouldUpdateExpertEvents_whenInvokedAndUpdateContactDetailsEnabled_CanAddApplicant2() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isUpdateContactDetailsEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts.builder()
                                                         .details(wrapElements(Expert.builder().build())).build())
                               .build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .claimant1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .claimant2ClaimResponseTypeForSpec(FULL_ADMISSION)
            .ccdCaseReference(354L)
            .respondent1ResponseDate(dateTime).build().toBuilder()
            .addApplicant2(YES)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("respondent1DQExperts")).isNotNull();
        assertThat(response.getData().get("respondent2DQExperts")).isNull();

    }

    @Test
    void shouldUpdateExpertEvents_whenInvokedAndUpdateContactDetailsEnabled_FullAdmission() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isUpdateContactDetailsEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts.builder()
                                                         .details(wrapElements(Expert.builder().build())).build())
                               .build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .ccdCaseReference(354L)
            .respondent1ResponseDate(dateTime).build().toBuilder()
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("respondent1DQExperts")).isNotNull();
        assertThat(response.getData().get("respondent2DQExperts")).isNull();

    }

    @Test
    void shouldUpdateExpertEvents_whenInvokedAndUpdateContactDetailsEnabled() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isUpdateContactDetailsEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts.builder()
                                                         .details(wrapElements(Expert.builder().build())).build())
                               .build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .ccdCaseReference(354L)
            .respondent1ResponseDate(dateTime).build().toBuilder()
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("respondent1DQExperts")).isNotNull();
        assertThat(response.getData().get("respondent2DQExperts")).isNull();

    }

    @Test
    void shouldUpdateExpertEvents_whenInvokedAndUpdateContactDetailsEnabled_V2DraftDirections() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isUpdateContactDetailsEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQExperts(Experts.builder()
                                                         .details(wrapElements(Expert.builder().build())).build())
                               .build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQDraftDirections(Document.builder().build()).build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .ccdCaseReference(354L)
            .respondent1ResponseDate(dateTime).build().toBuilder()
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("respondent1DQExperts")).isNotNull();
        assertThat(response.getData().get("respondent2DQExperts")).isNull();

    }

    @Test
    void shouldUpdateWitnessEvents_whenInvokedAndUpdateContactDetailsEnabled() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isUpdateContactDetailsEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQWitnesses(Witnesses.builder()
                                                         .details(wrapElements(Witness.builder().build())).build())
                               .build())

            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .ccdCaseReference(354L)
            .respondent1ResponseDate(dateTime).build().toBuilder()
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("respondent1DQWitnesses")).isNotNull();
        assertThat(response.getData().get("respondent2DQWitnesses")).isNull();

    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        var testDocument = ResponseDocument.builder()
            .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl("binary-url").build()).build();

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .ccdCaseReference(354L)
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .isRespondent1(YesOrNo.YES)
            .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
            .respondentSolicitor1ServiceAddress(
                Address.builder()
                    .postCode("new postcode")
                    .build()
            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
            .extracting("PostCode")
            .isEqualTo("new postcode");
        assertThat(response.getData().get("respondentSolicitor1ServiceAddress"))
            .extracting("PostCode")
                .isNull();
    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided1v2ss() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        var testDocument = ResponseDocument.builder()
            .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl("binary-url").build()).build();

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .ccdCaseReference(354L)
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .isRespondent1(YesOrNo.YES)
            .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
            .respondentSolicitor1ServiceAddress(
                Address.builder()
                    .postCode("new postcode")
                    .build()
            )
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("Company 3")
                             .build())
            .respondent2SameLegalRepresentative(YES)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
            .extracting("PostCode")
            .isEqualTo("new postcode");
        assertThat(response.getData().get("respondentSolicitor1ServiceAddress"))
            .extracting("PostCode")
            .isNull();
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressdetails"),
            response.getData().get("specRespondent2CorrespondenceAddressdetails")
        );
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressRequired"),
            response.getData().get("specRespondent2CorrespondenceAddressRequired")
        );
    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided1v2ss_withSameResponse() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        var testDocument = ResponseDocument.builder()
            .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl("binary-url").build()).build();

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .ccdCaseReference(354L)
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .isRespondent1(YesOrNo.YES)
            .respondentResponseIsSame(YES)
            .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
            .respondentSolicitor1ServiceAddress(
                Address.builder()
                    .postCode("new postcode")
                    .build()
            )
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("Company 3")
                             .build())
            .respondent2SameLegalRepresentative(YES)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
            .extracting("PostCode")
            .isEqualTo("new postcode");
        assertThat(response.getData().get("respondentSolicitor1ServiceAddress"))
            .extracting("PostCode")
            .isNull();
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressdetails"),
            response.getData().get("specRespondent2CorrespondenceAddressdetails")
        );
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressRequired"),
            response.getData().get("specRespondent2CorrespondenceAddressRequired")
        );
    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided1v2ss_withSameResponse_withv2copy() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        var testDocument = ResponseDocument.builder()
            .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl("binary-url").build()).build();

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .ccdCaseReference(354L)
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .isRespondent1(YesOrNo.YES)
            .respondentResponseIsSame(YES)
            .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
            .respondentSolicitor1ServiceAddress(
                Address.builder()
                    .postCode("new postcode")
                    .build()
            )
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("Company 3")
                             .build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(YES)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
            .extracting("PostCode")
            .isEqualTo("new postcode");
        assertThat(response.getData().get("respondentSolicitor1ServiceAddress"))
            .extracting("PostCode")
            .isNull();
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressdetails"),
            response.getData().get("specRespondent2CorrespondenceAddressdetails")
        );
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressRequired"),
            response.getData().get("specRespondent2CorrespondenceAddressRequired")
        );
    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided1v2ss_withSameResponse_withv2copyAndNoTempAdr() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        var testDocument = ResponseDocument.builder()
            .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl("binary-url").build()).build();

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .ccdCaseReference(354L)
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .isRespondent1(YesOrNo.YES)
            .respondentResponseIsSame(YES)
            .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
            .respondentSolicitor1ServiceAddress(
                Address.builder()
                    .postCode("new postcode")
                    .build()
            )
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("Company 3")
                             .build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(YES)
            .tempAddress2Required(NO)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
            .extracting("PostCode")
            .isEqualTo("new postcode");
        assertThat(response.getData().get("respondentSolicitor1ServiceAddress"))
            .extracting("PostCode")
            .isNull();
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressdetails"),
            response.getData().get("specRespondent2CorrespondenceAddressdetails")
        );
        assertEquals(
            response.getData().get("specRespondentCorrespondenceAddressRequired"),
            response.getData().get("specRespondent2CorrespondenceAddressRequired")
        );
    }

    @Test
    void shouldUpdateCorrespondence2_whenProvided() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        var testDocument = ResponseDocument.builder()
            .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl("binary-url").build()).build();

        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .ccdCaseReference(354L)
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .isRespondent2(YesOrNo.YES)
            .respondentSolicitor2ServiceAddressRequired(YesOrNo.NO)
            .respondentSolicitor2ServiceAddress(
                Address.builder()
                    .postCode("new postcode")
                    .build()
            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        // Then
        assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
            .extracting("PostCode")
            .isEqualTo("new postcode");
        assertThat(response.getData().get("respondentSolicitor2ServiceAddress"))
            .extracting("PostCode")
            .isNull();
    }

    @Test
    void shouldPopulateRespondent2Flag_WhenInvoked() {
        // Given
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Given
        assertThat(response.getData().get("respondent2DocumentGeneration")).isEqualTo("userRespondent2");
    }

    @Test
    void shouldPopulateRespondent2Flag_WhenInvokedWithSmallClaimExperts_1DQ() {
        // Given
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondToClaimExperts(ExpertDetails.builder().build()).build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Given
        assertThat(response.getData().get("respondent2DocumentGeneration")).isEqualTo("userRespondent2");
    }

    @Test
    void shouldPopulateRespondent2Flag_WhenInvokedWithSmallClaimExperts_2DQ() {
        // Given
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondToClaimExperts2(ExpertDetails.builder().build()).build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Given
        assertThat(response.getData().get("respondent2DocumentGeneration")).isEqualTo("userRespondent2");
    }

    @Test
    void shouldNotPopulateRespondent2Flag_WhenInvoked() {
        // Given
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Given
        assertThat(response.getData().get("respondent2DocumentGeneration")).isNull();
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

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Counter_Admit_OR_Admit_Part_combination2_CanAddApplicant2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2v1BothNotFullDefence_CounterClaimX2()
                .addApplicant2(YES)
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
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
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                   .whenWillThisAmountBePaid(whenWillPay).build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
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
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
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
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
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
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
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
                    FULL_ADMISSION
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
                    FULL_ADMISSION
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
                    FULL_ADMISSION
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

        @Test
        void whenProvided_thenValidateCorrespondence1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .isRespondent1(YES)
                .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
                .respondentSolicitor1ServiceAddress(Address.builder()
                                                                   .postCode("postal code")
                                                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "specCorrespondenceAddress");
            when(postcodeValidator.validate("postal code")).thenReturn(Collections.emptyList());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            verify(postcodeValidator).validate("postal code");
        }

        @Test
        void whenProvided_thenValidateCorrespondence2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .isRespondent2(YES)
                .respondentSolicitor2ServiceAddressRequired(YesOrNo.NO)
                .respondentSolicitor2ServiceAddress(Address.builder()
                                                                   .postCode("postal code")
                                                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "specCorrespondenceAddress");
            when(postcodeValidator.validate("postal code")).thenReturn(Collections.emptyList());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            verify(postcodeValidator).validate("postal code");
        }
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldPopulateCourtLocations() {
            // Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder()
                                 .partyName("name")
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            List<LocationRefData> locations = List.of(LocationRefData.builder()
                                                          .build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .thenReturn(locations);
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

    @Nested
    class MidDetermineLoggedInSolicitor {

        @BeforeEach
        void setup() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        public void testDetermineLoggedInSolicitorForRespondentSolicitor1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build();
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, "determineLoggedInSolicitor");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("isRespondent1", "Yes");
            assertThat(response.getData()).containsEntry("isRespondent2", "No");
            assertThat(response.getData()).containsEntry("isApplicant1", "No");
            assertThat(response.getData()).containsEntry("neitherCompanyNorOrganisation", "Yes");
        }

        @Test
        public void testDetermineLoggedInSolicitorForRespondentSolicitor2() {
            // Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .isRespondent2(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "determineLoggedInSolicitor");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("isRespondent1", "No");
            assertThat(response.getData()).containsEntry("isRespondent2", "Yes");
            assertThat(response.getData()).containsEntry("isApplicant1", "No");
            assertThat(response.getData()).containsEntry("neitherCompanyNorOrganisation", "Yes");
        }

        @Test
        public void testDetermineLoggedInSolicitorForApplicantSolicitor() {
            // Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(APPLICANTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "determineLoggedInSolicitor");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("isRespondent1", "No");
            assertThat(response.getData()).containsEntry("isRespondent2", "No");
            assertThat(response.getData()).containsEntry("isApplicant1", "Yes");
        }
    }

    @Nested
    class MidValidateRespondentExperts {

        @BeforeEach
        void setup() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        public void testValidateRespondentExpertsMultipartyResSol1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(NO)
                .respondent1DQ()
                .build();
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            CallbackParams params = callbackParamsOf(caseData, MID, "experts");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        public void testValidateRespondentExpertsMultipartyResSol2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(NO)
                .respondent2DQ()
                .build();
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            CallbackParams params = callbackParamsOf(caseData, MID, "experts");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidValidateUnavailableDates {
        @Test
        public void testValidateRespondentExpertsMultiparty() {
            // Given
            List<Element<UnavailableDate>> dates = Stream.of(
                UnavailableDate.builder()
                    .date(LocalDate.of(2024, 5, 2))
                    .who("who 1")
                    .build()).map(ElementUtils::element).collect(Collectors.toList());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQHearingSmallClaim(SmallClaimHearing.builder().unavailableDatesRequired(
                                       YES).smallClaimUnavailableDate(dates).build()).build())
                .build();
            Mockito.when(dateValidator.validateSmallClaimsHearing(any())).thenReturn(null);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
        }

        @Test
        public void shouldThrowError_whenValidateRespondentExpertsMultipartyWithNoUnavailableDates() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQHearingSmallClaim(SmallClaimHearing.builder().unavailableDatesRequired(
                                       YES).build()).build())
                .build();
            List<String> errors = Collections.singletonList("error 1");
            Mockito.when(dateValidator.validateSmallClaimsHearing(any())).thenReturn(errors);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        public void shouldThrowError_whenValidateRespondentExpertsMultipartyWithNoUnavailableDates_DisputesTheClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .responseClaimTrack(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQHearingSmallClaim(SmallClaimHearing.builder().unavailableDatesRequired(
                                       YES).build()).build())
                .build();
            List<String> errors = Collections.singletonList("error 1");
            Mockito.when(dateValidator.validateFastClaimHearing(any())).thenReturn(errors);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        public void shouldThrowError_whenValidateRespondentExpertsMultipartyWithNoUnavailableDates_V2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQHearingSmallClaim(SmallClaimHearing.builder().unavailableDatesRequired(
                                       YES).build()).build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQHearingSmallClaim(SmallClaimHearing.builder().unavailableDatesRequired(
                                       YES).build()).build())
                .isRespondent2(YES)
                .build();
            List<String> errors = Collections.singletonList("error 1");
            Mockito.when(dateValidator.validateSmallClaimsHearing(any())).thenReturn(errors);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(DEFENDANT_RESPONSE_SPEC);
    }

}
