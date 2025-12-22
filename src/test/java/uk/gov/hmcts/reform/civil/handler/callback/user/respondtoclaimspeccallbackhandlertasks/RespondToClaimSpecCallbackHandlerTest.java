package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
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
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.DocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
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
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF1;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF2;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimSpecCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class,
    LocationReferenceDataService.class,
    CourtLocationUtils.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    AssignCategoryId.class,
    FrcDocumentsUtils.class,
    RequestedCourtForClaimDetailsTab.class,
    FrcDocumentsUtils.class,
    RespondToClaimSpecCallbackHandlerTestConfig.class
})
class RespondToClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private Time time;
    @MockitoBean
    private PaymentDateValidator validator;
    @MockitoBean
    private UnavailableDateValidator dateValidator;
    @MockitoBean
    private FeatureToggleService toggleService;
    @MockitoBean
    private PostcodeValidator postcodeValidator;
    @MockitoBean
    private DeadlinesCalculator deadlinesCalculator;
    @Autowired
    private UserService userService;
    @MockitoBean
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private StateFlow mockedStateFlow;
    @MockitoBean
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private DateOfBirthValidator dateOfBirthValidator;
    @MockitoBean
    private LocationReferenceDataService locationRefDataService;
    @MockitoBean
    private CourtLocationUtils courtLocationUtils;
    @MockitoBean
    private CaseFlagsInitialiser caseFlagsInitialiser;
    @MockitoBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;
    @MockitoBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;
    @MockitoBean
    private InterestCalculator interestCalculator;

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

    private final List<RespondToClaimConfirmationHeaderSpecGenerator> confirmationHeaderSpecGenerators = List.of(
            new SpecResponse1v2DivergentHeaderText(),
            new SpecResponse2v1DifferentHeaderText()
    );

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        ReflectionTestUtils.setField(handler, "confirmationTextSpecGenerators", confirmationTextGenerators);
        ReflectionTestUtils.setField(handler, "confirmationHeaderGenerators", confirmationHeaderSpecGenerators);

        when(dqResponseDocumentUtils.buildClaimantResponseDocuments(any(CaseData.class))).thenReturn(new ArrayList<>());
    }

    @Test
    void midSpecCorrespondenceAddress_checkAddressIfWasIncorrect() {
        // Given
        String postCode = "postCode";
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSpecAoSApplicantCorrespondenceAddressRequired(YesOrNo.NO);
        Address address = new Address();
        address.setPostCode(postCode);
        caseData.setSpecAoSApplicantCorrespondenceAddressdetails(address);
        CallbackRequest request = CallbackRequest.builder()
                .eventId(SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC)
                .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "specCorrespondenceAddress")
                .toBuilder().request(request).build();

        List<String> errors = Collections.singletonList("error 1");
        Mockito.when(postcodeValidator.validate(postCode)).thenReturn(errors);

        // When
        CallbackResponse response = handler.handle(params);

        // Then
        assertEquals(errors, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
    }

    @Test
    void defendantResponsePopulatesWitnessesData() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(time.now()).thenReturn(dateTime);
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        Witness witness1 = new Witness();
        witness1.setFirstName("Witness");
        witness1.setLastName("One");
        witness1.setEmailAddress("test-witness-one@example.com");
        witness1.setPhoneNumber("07865456789");
        witness1.setReasonForWitness("great reasons");
        witness1.setEventAdded("Defendant Response Event");
        LocalDate date = dateTime.toLocalDate();
        witness1.setDateAdded(date);
        Witnesses res1witnesses = new Witnesses();
        res1witnesses.setDetails(wrapElements(witness1));

        Witness witness2 = new Witness();
        witness2.setFirstName("Witness");
        witness2.setLastName("Two");
        witness2.setEmailAddress("test-witness-two@example.com");
        witness2.setPhoneNumber("07532628263");
        witness2.setReasonForWitness("good reasons");
        witness2.setEventAdded("Defendant Response Event");
        witness2.setDateAdded(date);
        Witnesses res2witnesses = new Witnesses();
        res2witnesses.setDetails(wrapElements(witness2));

        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                .respondent2SameLegalRepresentative(NO)
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(NO)
                .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                .build();
        caseData.setRespondent1DQWitnessesSmallClaim(res1witnesses);
        caseData.setRespondent2DQWitnessesSmallClaim(res2witnesses);
        caseData.setRespondent2ResponseDate(dateTime);
        caseData.setRespondent1ResponseDate(dateTime);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                .thenReturn(dateTime);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Then

        Witnesses actualRespondent1DQWitnesses =
                objectMapper.convertValue(response.getData().get("respondent1DQWitnesses"), new TypeReference<>() {
                });
        Witness actualRespondent1Witness = unwrapElements(actualRespondent1DQWitnesses.getDetails()).get(0);
        assertThat(actualRespondent1Witness.getPartyID()).isNotNull();
        assertThat(actualRespondent1Witness.getFirstName()).isEqualTo("Witness");
        assertThat(actualRespondent1Witness.getLastName()).isEqualTo("One");
        assertThat(actualRespondent1Witness.getEmailAddress()).isEqualTo("test-witness-one@example.com");
        assertThat(actualRespondent1Witness.getPhoneNumber()).isEqualTo("07865456789");
        assertThat(actualRespondent1Witness.getReasonForWitness()).isEqualTo("great reasons");
        assertThat(actualRespondent1Witness.getEventAdded()).isEqualTo("Defendant Response Event");
        assertThat(actualRespondent1Witness.getDateAdded()).isEqualTo(date);

        Witnesses actualRespondent2DQWitnesses =
                objectMapper.convertValue(response.getData().get("respondent2DQWitnesses"), new TypeReference<>() {
                });
        Witness respondent2Witness = unwrapElements(actualRespondent2DQWitnesses.getDetails()).get(0);
        assertThat(respondent2Witness.getPartyID()).isNotNull();
        assertThat(respondent2Witness.getFirstName()).isEqualTo("Witness");
        assertThat(respondent2Witness.getLastName()).isEqualTo("Two");
        assertThat(respondent2Witness.getEmailAddress()).isEqualTo("test-witness-two@example.com");
        assertThat(respondent2Witness.getPhoneNumber()).isEqualTo("07532628263");
        assertThat(respondent2Witness.getReasonForWitness()).isEqualTo("good reasons");
        assertThat(respondent2Witness.getEventAdded()).isEqualTo("Defendant Response Event");
        assertThat(respondent2Witness.getDateAdded()).isEqualTo(date);
        assertThat(response.getData()).extracting("nextDeadline").isEqualTo(dateTime.toLocalDate().toString());

    }

    @Test
    void shouldAssignCategoryId_frc_whenInvoked() {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(4);
        //Given
        when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        when(time.now()).thenReturn(responseDate);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(
                any(LocalDateTime.class)
        )).thenReturn(deadline);

        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        CaseData caseData = CaseDataBuilder.builder()
                .setIntermediateTrackClaim()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .respondent2ClaimResponseTypeForSpec(FULL_DEFENCE)
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1DQWithFixedRecoverableCostsIntermediate()
                .respondent2DQWithFixedRecoverableCostsIntermediate()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //Then
        assertThat(response.getData())
                .extracting("respondent1DQFixedRecoverableCostsIntermediate")
                .extracting("frcSupportingDocument")
                .extracting("category_id")
                .isEqualTo(DQ_DEF1.getValue());

        assertThat(response.getData())
                .extracting("respondent2DQFixedRecoverableCostsIntermediate")
                .extracting("frcSupportingDocument")
                .extracting("category_id")
                .isEqualTo(DQ_DEF2.getValue());
    }

    @Test
    void shouldAssignCategoryId_frc_whenInvokedFor1v2DiffFirstResponse() {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(4);
        //Given
        when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        when(time.now()).thenReturn(responseDate);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(
                any(LocalDateTime.class)
        )).thenReturn(deadline);

        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        CaseData caseData = CaseDataBuilder.builder()
                .setIntermediateTrackClaim()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .atStateRespondentFullDefence()
                .respondent1DQWithFixedRecoverableCostsIntermediate()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //Then
        assertThat(response.getData())
                .extracting("respondent1DQFixedRecoverableCostsIntermediate")
                .extracting("frcSupportingDocument")
                .extracting("category_id")
                .isEqualTo(DQ_DEF1.getValue());
    }

    @Test
    void shouldPopulateDefendantResponseDocuments_whenInvokedFor1v2DiffBothResponded() {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(4);
        //Given
        when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        when(time.now()).thenReturn(responseDate);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(
                any(LocalDateTime.class)
        )).thenReturn(deadline);

        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        Document document = DocumentBuilder.builder().build();
        CaseDocument caseDocument1 = new CaseDocument();
        caseDocument1.setDocumentLink(document);
        caseDocument1.setDocumentName("doc-1");
        caseDocument1.setCreatedBy("Defendant 1");
        caseDocument1.setCreatedDatetime(LocalDateTime.now());
        Element<CaseDocument> element1 = new Element<>();
        element1.setId(UUID.randomUUID());
        element1.setValue(caseDocument1);
        List<Element<CaseDocument>> existingResponseDocuments = new ArrayList<>();
        existingResponseDocuments.add(element1);

        CaseDocument caseDocument2 = new CaseDocument();
        caseDocument2.setDocumentLink(document);
        caseDocument2.setDocumentName("doc-2");
        caseDocument2.setCreatedBy("Defendant 2");
        caseDocument2.setCreatedDatetime(LocalDateTime.now());
        Element<CaseDocument> element2 = new Element<>();
        element2.setId(UUID.randomUUID());
        element2.setValue(caseDocument2);
        var newResponseDocuments = List.of(element2);

        when(dqResponseDocumentUtils.buildDefendantResponseDocuments(any(CaseData.class))).thenReturn(
                newResponseDocuments);

        CaseData caseData = CaseDataBuilder.builder()
                .setIntermediateTrackClaim()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .respondent2ClaimResponseTypeForSpec(FULL_DEFENCE)
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .respondent1DQWithFixedRecoverableCostsIntermediate()
                .respondent2DQWithFixedRecoverableCostsIntermediate()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();
        caseData.setDefendantResponseDocuments(existingResponseDocuments);
        caseData.getRespondent2DQ().setRespondent2DQDraftDirections(document);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        var actualCaseData = getCaseData(response);

        assertThat(actualCaseData.getDefendantResponseDocuments()).hasSize(2);
        assertThat(actualCaseData.getDefendantResponseDocuments().get(0).getValue().getDocumentName()).isEqualTo("doc-1");
        assertThat(actualCaseData.getDefendantResponseDocuments().get(1).getValue().getDocumentName()).isEqualTo("doc-2");
        assertThat(actualCaseData.getRespondent2DQ().getRespondent2DQDraftDirections()).isNull();

        verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
    }

    @Test
    void shouldAppendDefendantResponseDocuments_whenInvokedFor1v2DiffFirstResponse() {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(4);
        //Given
        when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        when(time.now()).thenReturn(responseDate);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(
                any(LocalDateTime.class)
        )).thenReturn(deadline);

        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        Document document = DocumentBuilder.builder().build();
        CaseDocument expectedCaseDocument = new CaseDocument();
        expectedCaseDocument.setDocumentLink(document);
        expectedCaseDocument.setDocumentName("doc-name");
        expectedCaseDocument.setCreatedBy("Defendant");
        expectedCaseDocument.setCreatedDatetime(LocalDateTime.now());
        Element<CaseDocument> expectedElement = new Element<>();
        expectedElement.setId(UUID.randomUUID());
        expectedElement.setValue(expectedCaseDocument);
        var expectedResponseDocuments = List.of(expectedElement);
        when(dqResponseDocumentUtils.buildDefendantResponseDocuments(any(CaseData.class))).thenReturn(
                expectedResponseDocuments);

        CaseData caseData = CaseDataBuilder.builder()
                .setIntermediateTrackClaim()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .atStateRespondentFullDefence()
                .respondent1DQWithFixedRecoverableCostsIntermediate()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();
        caseData.getRespondent1DQ().setRespondent1DQDraftDirections(document);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        var actualCaseData = getCaseData(response);

        assertThat(actualCaseData.getDefendantResponseDocuments()).hasSize(1);
        assertThat(actualCaseData.getDefendantResponseDocuments().get(0).getValue().getDocumentName()).isEqualTo(
                "doc-name");
        assertThat(actualCaseData.getRespondent1DQ().getRespondent1DQDraftDirections()).isNull();

        verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
    }

    @Test
    void shouldNullDocuments_whenInvokedAndCaseFileEnabled() {
        // Given
        LocalDateTime localDateTime = LocalDateTime.now();
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        Document document = DocumentBuilder.builder().build();
        document.setDocumentUrl("fake-url");
        document.setDocumentFileName("file-name");
        document.setDocumentBinaryUrl("binary-url");
        var testDocument = new ResponseDocument();
        testDocument.setFile(document);

        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .ccdCaseReference(354L).build();
        caseData.setRespondent1SpecDefenceResponseDocument(testDocument);
        caseData.setRespondent2SpecDefenceResponseDocument(testDocument);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        // Then
        assertThat(response.getData().get("respondent1SpecDefenceResponseDocument")).isNull();
        assertThat(response.getData().get("respondent2SpecDefenceResponseDocument")).isNull();
    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided() {
        // Given
        LocalDateTime localDateTime = LocalDateTime.now();
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        Document document = DocumentBuilder.builder().build();
        document.setDocumentUrl("fake-url");
        document.setDocumentFileName("file-name");
        document.setDocumentBinaryUrl("binary-url");
        var testDocument = new ResponseDocument();
        testDocument.setFile(document);

        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .ccdCaseReference(354L)
                .respondent1ResponseDeadline(LocalDateTime.now()).build();
        caseData.setRespondent1SpecDefenceResponseDocument(testDocument);
        caseData.setRespondent2SpecDefenceResponseDocument(testDocument);
        caseData.setIsRespondent1(YesOrNo.YES);
        caseData.setSpecAoSRespondentCorrespondenceAddressRequired(YesOrNo.NO);
        Address address = new Address();
        address.setPostCode("new postcode");
        caseData.setSpecAoSRespondentCorrespondenceAddressdetails(address);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        // Then
        assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("new postcode");
    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided1v2ss() {
        // Given
        LocalDateTime localDateTime = LocalDateTime.now();
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        Document document = DocumentBuilder.builder().build();
        document.setDocumentUrl("fake-url");
        document.setDocumentFileName("file-name");
        document.setDocumentBinaryUrl("binary-url");
        var testDocument = new ResponseDocument();
        testDocument.setFile(document);

        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .ccdCaseReference(354L)
                .respondent1ResponseDeadline(LocalDateTime.now()).build();
        caseData.setRespondent1SpecDefenceResponseDocument(testDocument);
        caseData.setRespondent2SpecDefenceResponseDocument(testDocument);
        caseData.setIsRespondent1(YesOrNo.YES);
        caseData.setSpecAoSRespondentCorrespondenceAddressRequired(YesOrNo.NO);
        Address address = new Address();
        address.setPostCode("new postcode");
        caseData.setSpecAoSRespondentCorrespondenceAddressdetails(address);
        Party party = PartyBuilder.builder().company().build();
        party.setCompanyName("Company 3");
        caseData.setRespondent2(party);
        caseData.setRespondent2SameLegalRepresentative(YES);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        // Then
        assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("new postcode");
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
        LocalDateTime localDateTime = LocalDateTime.now();
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        Document document = DocumentBuilder.builder().build();
        document.setDocumentUrl("fake-url");
        document.setDocumentFileName("file-name");
        document.setDocumentBinaryUrl("binary-url");
        var testDocument = new ResponseDocument();
        testDocument.setFile(document);

        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .ccdCaseReference(354L)
                .isRespondent2(YesOrNo.YES).build();
        caseData.setSpecAoSRespondent2CorrespondenceAddressRequired(YesOrNo.NO);
        caseData.setRespondent1SpecDefenceResponseDocument(testDocument);
        caseData.setRespondent2SpecDefenceResponseDocument(testDocument);
        Address address = new Address();
        address.setPostCode("new postcode");
        caseData.setSpecAoSRespondent2CorrespondenceAddressdetails(address);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        // Then
        assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("new postcode");
    }

    @Test
    void shouldPopulateRespondent2Flag_WhenInvoked() {
        // Given
        LocalDateTime localDateTime = LocalDateTime.now();
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
        // Given
        assertThat(response.getData()).containsEntry("respondent2DocumentGeneration", "userRespondent2");
    }

    @Test
    void shouldNotPopulateRespondent2Flag_WhenInvoked() {
        // Given
        LocalDateTime localDateTime = LocalDateTime.now();
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
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
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
        // Given
        assertThat(response.getData().get("respondent2DocumentGeneration")).isNull();
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(DEFENDANT_RESPONSE_SPEC);
    }

    @Test
    void shouldSetClaimDismissedDeadlineTo36MonthsInFuture() {
        // Given
        LocalDateTime localDateTime = LocalDateTime.now();
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(36, LocalDate.now()))
                .thenReturn(LocalDateTime.now().plusMonths(36));

        CaseData caseData = CaseDataBuilder.builder()
                .claimDismissedDeadline(LocalDateTime.now().plusMonths(6))
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(new Respondent1DQ())
                .respondent2DQ(new Respondent2DQ())
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        // Then
        Object deadlineValue = response.getData().get("claimDismissedDeadline");
        assertThat(deadlineValue).isNotNull();

        LocalDate expectedDate = LocalDate.now().plusMonths(36);
        LocalDate actualDate = LocalDateTime.parse(deadlineValue.toString()).toLocalDate();

        assertThat(actualDate).isEqualTo(expectedDate);
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    @Nested
    class DefendAllOfClaimTests {

        @Test
        void testNotSpecDefendantResponse() {
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
        void testSpecDefendantResponseValidationError() {
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
        void testSpecDefendantResponseFastTrack() {
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
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
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
            caseData.setShowConditionFlags(EnumSet.of(
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1
            ));
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
            assertThat(response.getData()).containsEntry(
                    "specPaidLessAmountOrDisputesOrPartAdmission",
                    "No"
            );
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
            caseData.setShowConditionFlags(EnumSet.of(
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
            ));
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
            assertThat(response.getData()).containsEntry(
                    "specPaidLessAmountOrDisputesOrPartAdmission",
                    "No"
            );
        }

        @Test
        void testSpecDefendantResponseFastTrackTwoVOne() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceFastTrack()
                    .addApplicant2()
                    .applicant2(PartyBuilder.builder().individual().build())
                    .build();
            caseData.setDefendantSingleResponseToBothClaimants(YES);
            caseData.setRespondent1ClaimResponseTestForSpec(FULL_ADMISSION);
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
            assertThat(response.getData()).containsEntry(
                    "specPaidLessAmountOrDisputesOrPartAdmission",
                    "No"
            );
        }

        @Test
        void testSpecDefendantResponseFastTrackDefendantPaid() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceFastTrack()
                    .build();

            RespondToClaim respondToClaim = new RespondToClaim();
            // how much was paid is pence, total claim amount is pounds
            respondToClaim.setHowMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(100)));

            caseData.setDefenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED);
            caseData.setRespondToClaim(respondToClaim);
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData())
                    .containsEntry("respondent1ClaimResponsePaymentAdmissionForSpec", RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT.name());
        }

        @Test
        void testSpecDefendantResponseFastTrackDefendantPaidLessThanClaimed() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceFastTrack()
                    .build();

            RespondToClaim respondToClaim = new RespondToClaim();
            // how much was paid is pence, total claim amount is pounds
            // multiply by less than 100 so defendant paid less than claimed
            respondToClaim.setHowMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50)));

            caseData.setDefenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED);
            caseData.setRespondToClaim(respondToClaim);
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData())
                    .containsEntry("respondent1ClaimResponsePaymentAdmissionForSpec", RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT.name());
        }
    }

    @Nested
    class MidSpecHandleResponseType {

        @Test
        void testHandleRespondentResponseTypeForSpec() {
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
            assertThat(response.getData())
                    .extracting("showConditionFlags", as(list(String.class)))
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
            assertThat(response.getData())
                    .extracting("showConditionFlags", as(list(String.class)))
                    .contains(DefendantResponseShowTag.TIMELINE_MANUALLY.name());
        }
    }

    @Nested
    class AdmitsPartOfTheClaimTest {

        @Test
        void testSpecDefendantResponseAdmitPartOfClaimValidationError() {
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
        void testSpecDefendantResponseAdmitPartOfClaimFastTrack() {
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
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
        }

        @Test
        void testSpecDefendantResponseAdmitPartOfClaimFastTrackStillOwes() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentAdmitPartOfClaimFastTrack()
                    .build();
            // admitted amount is pence, total claimed is pounds
            BigDecimal admittedAmount = caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50));
            caseData.setRespondToAdmittedClaimOwingAmount(admittedAmount);
            CallbackParams params = callbackParamsOf(
                    caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.FAST_CLAIM.name());
            assertEquals(0, new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmount").toString())
                    .compareTo(
                            new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmountPounds").toString())
                                    .multiply(BigDecimal.valueOf(100))));
        }

        @Test
        void testSpecDefendantResponseAdmitPartOfClaimFastTrackRespondent2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .totalClaimAmount(new BigDecimal(100000))
                    .respondent1(PartyBuilder.builder().individual().build())
                    .isRespondent1(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .isRespondent2(YES).build();
            caseData.setDefenceAdmitPartEmploymentType2Required(YES);
            caseData.setDefenceAdmitPartEmploymentType2Required(YES);
            caseData.setSpecDefenceAdmitted2Required(YES);
            caseData.setSpecDefenceAdmittedRequired(YES);
            caseData.setShowConditionFlags(EnumSet.of(
                            DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                            DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                    ));
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
        void shouldSetIntermediateAllocatedTrack_whenInvoked() {
            // New multi and intermediate track change track logic
            // total claim amount is 100000, so track is intermediate, as this is the upper limit
            when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceFastTrack()
                    .totalClaimAmount(BigDecimal.valueOf(100000))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
            // Then
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.INTERMEDIATE_CLAIM.name());
        }

        @Test
        void shouldSetMultiAllocatedTrack_whenInvoked() {
            // New multi and intermediate track change track logic
            // total claim amount is 100001, so track is multi
            when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceFastTrack()
                    .totalClaimAmount(BigDecimal.valueOf(100001))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
            // Then
            assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.MULTI_CLAIM.name());
        }

        @Test
        void testValidateLengthOfUnemploymentWithError() {
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
        void testValidateRespondentPaymentDate() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().generatePaymentDateForAdmitPartResponse().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-payment-date", "DEFENDANT_RESPONSE_SPEC");
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            List<String> expectedErrorArray = List.of("Validation error");
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEqualTo(expectedErrorArray);
        }

        @Test
        void testValidateRepaymentDate() {
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
        void testValidateDefendant2RepaymentDate() {
            CaseData caseData = CaseDataBuilder.builder().generateDefendant2RepaymentDateForAdmitPartResponse().build();
            CallbackParams params = callbackParamsOf(caseData, MID,
                    "validate-repayment-plan-2", "DEFENDANT_RESPONSE_SPEC"
            );
            when(dateValidator.validateFuturePaymentDate(any())).thenReturn(List.of("Validation error"));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertEquals("Validation error", response.getErrors().get(0));

        }

        @Test
        void testValidateSpecDefendantResponseAdmitClaimOwingAmount() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setRespondent1(PartyBuilder.builder().individual().build());
            caseData.setIsRespondent1(YES);
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setIsRespondent2(YES);
            caseData.setSpecDefenceAdmitted2Required(NO);
            caseData.setSpecDefenceAdmittedRequired(NO);
            caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setTotalClaimAmount(new BigDecimal(7000));
            caseData.setTotalClaimAmountPlusInterestAdmitPart(new BigDecimal("7000.05"));
            caseData.setRespondToAdmittedClaimOwingAmount(new BigDecimal("705000"));
            caseData.setRespondToAdmittedClaimOwingAmount2(new BigDecimal(50000));
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal("0.05"));
            CallbackParams params = callbackParamsOf(
                    caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNotNull();
        }

        @Test
        void testValidateSpecDefendantResponseAdmitClaimOwingAmountIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .isRespondent1(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .isRespondent2(YES).build();
            caseData.setSpecDefenceAdmitted2Required(YES);
            caseData.setSpecDefenceAdmittedRequired(YES);
            caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setTotalClaimAmount(new BigDecimal(7000));
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal("0.05"));
            CallbackParams params = callbackParamsOf(
                    caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void testValidateSpecDefendantResponseAdmitClaimOwingAmountNotPartAdmit() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setRespondent1(PartyBuilder.builder().individual().build());
            caseData.setIsRespondent1(YES);
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setIsRespondent2(NO);
            caseData.setSpecDefenceAdmitted2Required(NO);
            caseData.setSpecDefenceAdmittedRequired(YES);
            caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setTotalClaimAmount(new BigDecimal(7000));
            caseData.setRespondToAdmittedClaimOwingAmount(new BigDecimal(50000));
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal("0.05"));
            CallbackParams params = callbackParamsOf(
                    caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitTests {

        @Test
        void updateRespondent1AddressWhenUpdated() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

            Address changedAddress = AddressBuilder.maximal().build();

            CaseData caseData = CaseDataBuilder.builder()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .atStateApplicantRespondToDefenceAndProceed()
                    .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                    .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(localDateTime);

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
            assertThat(response.getData()).extracting("nextDeadline").isEqualTo(localDateTime.toLocalDate().toString());
        }

        @Test
        void defendantResponseDoesNotPopulateNextDeadline1vs2DSSolicitor2Only() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE)
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .addRespondent2(YES)
                    .respondent2DQ()
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSRespondent2HomeAddressRequired(NO)
                    .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                    .build();
            caseData.setRespondent2ResponseDate(dateTime);
            caseData.setRespondent1ResponseDate(null);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(dateTime);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Then
            assertThat(response.getData()).doesNotContainKey("nextDeadline");

        }

        @Test
        void defendantResponseDoesPopulateNextDeadline1vs2DSSolicitor2AfterSolicitor1() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE)
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .addRespondent2(YES)
                    .respondent2DQ()
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSRespondent2HomeAddressRequired(NO)
                    .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                    .build();
            caseData.setRespondent2ResponseDate(dateTime);
            caseData.setRespondent1ResponseDate(dateTime);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(dateTime);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Then
            assertThat(response.getData()).extracting("nextDeadline").isEqualTo(dateTime.toLocalDate().toString());

        }

        @Test
        void defendantResponseDoesPopulateNextDeadline1vs2DSSolicitor1AfterSolicitor2() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE)
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .respondent1DQ()
                    .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .addRespondent2(YES)
                    .respondent2DQ()
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSRespondent2HomeAddressRequired(NO)
                    .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                    .build();
            caseData.setRespondent2ResponseDate(dateTime);
            caseData.setRespondent1ResponseDate(dateTime);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(dateTime);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Then
            assertThat(response.getData()).extracting("nextDeadline").isEqualTo(dateTime.toLocalDate().toString());

        }

        @Test
        void defendantResponseDoesNotPopulateNextDeadline1vs2DSSolicitor1BeforeSolicitor2() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE)
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .respondent1DQ()
                    .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSRespondent2HomeAddressRequired(NO)
                    .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                    .build();
            caseData.setRespondent2ResponseDate(null);
            caseData.setRespondent1ResponseDeadline(dateTime.plusDays(1));
            caseData.setRespondent1ResponseDate(dateTime);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(dateTime);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Then
            assertThat(response.getData()).extracting("nextDeadline")
                    .isEqualTo(dateTime.plusDays(1).toLocalDate().toString());

        }

        @Test
        void defendantResponseDoesNotPopulateNextDeadline1vs2DSSolicitor1BeforeSolicitor2ExtendedDeadline() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE)
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .respondent1DQ()
                    .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .addRespondent2(YES)
                    .respondent2ResponseDeadline(dateTime.plusDays(2))
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSRespondent2HomeAddressRequired(NO)
                    .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                    .build();
            caseData.setRespondent2ResponseDate(null);
            caseData.setRespondent1ResponseDeadline(dateTime);
            caseData.setRespondent1ResponseDate(dateTime);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(dateTime);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Then
            assertThat(response.getData()).extracting("nextDeadline")
                    .isEqualTo(dateTime.plusDays(2).toLocalDate().toString());

        }

        @Test
        void defendantResponseDoesPopulateNextDeadline1vs1Solicitor1() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondent1v1FullDefenceSpec()
                    .ccdCaseReference(123456789)
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .respondent1DQ()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                    .addRespondent2(NO)
                    .respondent1ResponseDeadline(dateTime)
                    .atSpecAoSRespondent2HomeAddressRequired(NO)
                    .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                    .build();
            caseData.setRespondent1ResponseDate(dateTime);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(dateTime);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Then
            assertThat(response.getData()).extracting("nextDeadline").isEqualTo(dateTime.toLocalDate().toString());

        }

        @Test
        void shouldPauseLipVsLrCaseIfClaimantLipIsWelsh() {
            // Given
            when(toggleService.isWelshEnabledForMainCase()).thenReturn(true);
            LocalDateTime dateTime = LocalDateTime.of(2023, 6, 6, 6, 6, 6);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(time.now()).thenReturn(dateTime);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent1v1FullDefenceSpec()
                    .ccdCaseReference(123456789)
                    .claimantBilingualLanguagePreference("BOTH")
                    .applicant1Represented(NO)
                    .respondent1Represented(YES)
                    .addRespondent2(NO)
                    .respondent1DQ()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .respondent1ResponseDate(LocalDateTime.now())
                    .respondent1ResponseDeadline(dateTime)
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(dateTime);
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
            // Then
            assertThat(response.getState()).isNull();
        }

        @Test
        void updateRespondent2AddressWhenUpdated() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

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
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(localDateTime);

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
            assertThat(response.getData()).extracting("nextDeadline").isEqualTo(localDateTime.toLocalDate().toString());
        }

        @Test
        void updateRespondent2AddressWhenSpecAoSRespondent2HomeAddressRequiredIsNO() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now();
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                .thenReturn(localDateTime);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

            Party partyWithPrimaryAddress = PartyBuilder.builder().individual().build();
            partyWithPrimaryAddress.setPrimaryAddress(AddressBuilder.maximal()
                                                          .addressLine1("address line 1")
                                                          .addressLine2("address line 2")
                                                          .addressLine3("address line 3")
                                                          .build());

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

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
            assertThat(response.getData()).extracting("nextDeadline").isEqualTo(localDateTime.toLocalDate().toString());
        }

        @Test
        void updateRespondent2AddressWhenSpecAoSRespondent2HomeAddressRequiredIsNotNO() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now();
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(localDateTime);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

            Party partyWithPrimaryAddress = PartyBuilder.builder().individual().build();
            partyWithPrimaryAddress.setPrimaryAddress(AddressBuilder.maximal()
                    .addressLine1("address line 1")
                    .addressLine2("address line 2")
                    .addressLine3("address line 3")
                    .build());

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .respondent2DQ()
                    .respondent1Copy(PartyBuilder.builder().individual().build())
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .atSpecAoSRespondent2HomeAddressRequired(YES)
                    .build();

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

            // Then
            assertThat(response.getData())
                    .extracting("respondent2")
                    .extracting("primaryAddress")
                    .extracting("AddressLine1")
                    .isEqualTo("address line 1");
            assertThat(response.getData())
                    .extracting("respondent2")
                    .extracting("primaryAddress")
                    .extracting("AddressLine2")
                    .isEqualTo("address line 2");
            assertThat(response.getData())
                    .extracting("respondent2")
                    .extracting("primaryAddress")
                    .extracting("AddressLine3")
                    .isEqualTo("address line 3");
        }

        @Nested
        class UpdateExperts {
            @Test
            void updateRespondent1Experts() {
                // Given
                LocalDateTime localDateTime = LocalDateTime.of(2022, 2, 18, 12, 10, 55);
                when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
                when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
                when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
                when(time.now()).thenReturn(localDateTime);

                ExpertDetails experts = new ExpertDetails();
                experts.setExpertName("Mr Expert Defendant");
                experts.setFirstName("Expert");
                experts.setLastName("Defendant");
                experts.setPhoneNumber("07123456789");
                experts.setEmailAddress("test@email.com");
                experts.setFieldofExpertise("Roofing");
                experts.setEstimatedCost(new BigDecimal(434));

                CaseData caseData = CaseDataBuilder.builder()
                        .respondent1(PartyBuilder.builder().individual().build())
                        .atStateApplicantRespondToDefenceAndProceed()
                        .respondent1DQSmallClaimExperts(experts, YES)
                        .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                        .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                        .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                        .thenReturn(localDateTime);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                // Then
                assertThat(response.getData())
                        .extracting("responseClaimExpertSpecRequired").isEqualTo("Yes");
                assertThat(response.getData()).extracting("respondent1DQExperts").extracting("expertRequired").isEqualTo(
                        "Yes");
                assertThat(response.getData()).extracting("nextDeadline").isEqualTo(localDateTime.toLocalDate().toString());
            }

            @Test
            void updateRespondent1Experts_WhenNoExperts() {
                // Given
                LocalDateTime localDateTime = LocalDateTime.now();
                when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
                when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
                when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

                CaseData caseData = CaseDataBuilder.builder()
                        .respondent1(PartyBuilder.builder().individual().build())
                        .atStateApplicantRespondToDefenceAndProceed()
                        .respondent1DQSmallClaimExperts(null, NO)
                        .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                        .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                        .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                        .thenReturn(localDateTime);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                // Then
                assertThat(response.getData())
                        .extracting("responseClaimExpertSpecRequired").isEqualTo("No");
                assertThat(response.getData()).extracting("respondent1DQExperts").extracting("expertRequired").isEqualTo(
                        "No");
                assertThat(response.getData()).extracting("nextDeadline").isEqualTo(localDateTime.toLocalDate().toString());
            }

            @Test
            void updateRespondent2Experts() {
                // Given
                when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
                when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
                when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
                when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
                when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

                ExpertDetails experts = new ExpertDetails();
                experts.setExpertName("Mr Expert Defendant");
                experts.setFirstName("Expert");
                experts.setLastName("Defendant");
                experts.setPhoneNumber("07123456789");
                experts.setEmailAddress("test@email.com");
                experts.setFieldofExpertise("Roofing");
                experts.setEstimatedCost(new BigDecimal(434));

                CaseData caseData = CaseDataBuilder.builder()
                        .respondent2(PartyBuilder.builder().individual().build())
                        .multiPartyClaimTwoDefendantSolicitors()
                        .atStateApplicantRespondToDefenceAndProceed()
                        .respondent2DQSmallClaimExperts(experts, YES)
                        .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                        .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                        .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                LocalDateTime localDateTime = LocalDateTime.now();
                when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                        .thenReturn(localDateTime);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                // Then
                assertThat(response.getData())
                        .extracting("responseClaimExpertSpecRequired2").isEqualTo("Yes");
                assertThat(response.getData()).extracting("nextDeadline").isEqualTo(localDateTime.toLocalDate().toString());
            }

            @Test
            void updateRespondent2Experts_WhenNoExperts() {
                // Given
                LocalDateTime localDateTime = LocalDateTime.now();
                when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
                when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
                when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

                CaseData caseData = CaseDataBuilder.builder()
                        .respondent1(PartyBuilder.builder().individual().build())
                        .atStateApplicantRespondToDefenceAndProceed()
                        .respondent2DQSmallClaimExperts(null, NO)
                        .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                        .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                        .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                        .thenReturn(localDateTime);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                // Then
                assertThat(response.getData())
                        .extracting("responseClaimExpertSpecRequired2").isEqualTo("No");
                assertThat(response.getData()).extracting("nextDeadline").isEqualTo(localDateTime.toLocalDate().toString());
            }
        }
    }

    @Nested
    class HandleLocations {

        @Test
        void oneVOne() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now();
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            DynamicList preferredCourt = new DynamicList();
            preferredCourt.setListItems(locationValues.getListItems());
            preferredCourt.setValue(locationValues.getListItems().get(0));
            Party defendant1 = PartyBuilder.builder()
                    .company().build();
            defendant1.setCompanyName("company");
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtLocations(preferredCourt);
            requestedCourt.setReasonForHearingAtSpecificCourt("Reason");
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondToCourtLocation(requestedCourt);
            CaseData caseData = CaseDataBuilder.builder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .respondent1(defendant1)
                    .respondent1Copy(defendant1)
                    .respondent1ResponseDeadline(LocalDateTime.now())
                    .respondent1DQ(respondent1DQ).build();
            caseData.setShowConditionFlags(EnumSet.of(
                            DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1
                    ));
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

            verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
        }

        @Test
        void oneVTwo_SecondDefendantRepliesSameLegalRep() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now();
            when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(localDateTime);
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            DynamicList preferredCourt = new DynamicList();
            preferredCourt.setListItems(locationValues.getListItems());
            preferredCourt.setValue(locationValues.getListItems().get(0));
            Party defendant1 = new Party();
            defendant1.setType(Party.Type.COMPANY);
            defendant1.setCompanyName("company");
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtLocations(preferredCourt);
            requestedCourt.setReasonForHearingAtSpecificCourt("Reason");
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondToCourtLocation(requestedCourt);
            RequestedCourt requestedCourt1 = new RequestedCourt();
            requestedCourt1.setResponseCourtLocations(preferredCourt);
            requestedCourt1.setReasonForHearingAtSpecificCourt("Reason123");
            Respondent2DQ respondent2DQ = new Respondent2DQ();
            respondent2DQ.setRespondToCourtLocation2(requestedCourt1);
            CaseData caseData = CaseDataBuilder.builder()
                    .respondent2SameLegalRepresentative(YES)
                    .caseAccessCategory(SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
                    .respondent2ClaimResponseTypeForSpec(FULL_ADMISSION)
                    .respondent1ResponseDeadline(LocalDateTime.now())
                    .respondent1(defendant1)
                    .respondent1Copy(defendant1)
                    .respondent1DQ(respondent1DQ)
                    .respondent2DQ(respondent2DQ).build();
            caseData.setShowConditionFlags(EnumSet.of(
                            DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                            DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                    ));

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
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            given(deadlineExtensionCalculatorService.calculateExtendedDeadline(any(LocalDateTime.class), anyInt())).willReturn(whenWillPay);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

            verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
        }

        @Test
        void oneVTwo_SecondDefendantReplies() {
            // Given
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            DynamicList preferredCourt = new DynamicList();
            preferredCourt.setListItems(locationValues.getListItems());
            preferredCourt.setValue(locationValues.getListItems().get(0));
            Party defendant1 = new Party();
            defendant1.setType(Party.Type.COMPANY);
            defendant1.setCompanyName("company");
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtLocations(preferredCourt);
            requestedCourt.setReasonForHearingAtSpecificCourt("Reason");
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondToCourtLocation(requestedCourt);
            RequestedCourt requestedCourt1 = new RequestedCourt();
            requestedCourt1.setResponseCourtLocations(preferredCourt);
            requestedCourt1.setReasonForHearingAtSpecificCourt("Reason123");
            Respondent2DQ respondent2DQ = new Respondent2DQ();
            respondent2DQ.setRespondToCourtLocation2(requestedCourt1);
            CaseData caseData = CaseDataBuilder.builder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .ccdCaseReference(354L)
                    .respondent1(defendant1)
                    .respondent1Copy(defendant1)
                    .respondent1ResponseDeadline(LocalDateTime.now())
                    .respondent1DQ(respondent1DQ)
                    .respondent2DQ(respondent2DQ).build();
            caseData.setShowConditionFlags(EnumSet.of(
                            DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                            DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                    ));
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

            verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
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
                    .respondent1DQ(new Respondent1DQ())
                    .respondent1DQWitnessesRequiredSpec(YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvided() {
            // Given
            Witness witness = new Witness();
            witness.setFirstName("test witness");
            List<Element<Witness>> testWitness = wrapElements(witness);
            CaseData caseData = CaseDataBuilder.builder()
                    .respondent1DQ(new Respondent1DQ())
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
                    .respondent1DQ(new Respondent1DQ())
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
        void specificSummary_whenPartialAdmitNotPay_LrAdmissionBulkEnabled() {
            // Given
            BigDecimal admitted = BigDecimal.valueOf(1000);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .setDefendantMediationFlag(YES)
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(admitted);
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new  RespondToClaimAdmitPartLRspec();
            LocalDate whenWillPay = LocalDate.now().plusMonths(1);
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            caseData.setTotalClaimAmount(admitted.multiply(BigDecimal.valueOf(2)));
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                    .contains(caseData.getApplicant1().getPartyName())
                    .contains(admitted.toString())
                    .contains(caseData.getTotalClaimAmount().toString() + " plus the claim fee and any costs and further interest claimed")
                    .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE));
        }

        @Test
        void specificSummary_whenPartialAdmitPayImmediately() {
            // Given
            BigDecimal admitted = BigDecimal.valueOf(1000);

            CaseData caseData = CaseDataBuilder.builder()
                    .totalClaimAmount(BigDecimal.valueOf(1000))
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec());
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(admitted);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                    .contains(caseData.getApplicant1().getPartyName());
        }

        @Test
        void specificSummary_whenPartialAdmitPayImmediately_LrAdmissionBulkEnabled() {
            // Given
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CaseData caseData = CaseDataBuilder.builder()
                    .totalClaimAmount(BigDecimal.valueOf(1000))
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new  RespondToClaimAdmitPartLRspec();
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
            BigDecimal admitted = BigDecimal.valueOf(1000);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(admitted);
            caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                    .contains(caseData.getApplicant1().getPartyName())
                    .contains("the claimant can request a County Court Judgment against you.");
        }

        @Test
        void specificSummary_whenPartialAdmitPayImmediately1v2_LrAdmissionBulkEnabled() {
            // Given
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CaseData caseData = CaseDataBuilder.builder()
                    .totalClaimAmount(BigDecimal.valueOf(1000))
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new  RespondToClaimAdmitPartLRspec();
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
            BigDecimal admitted = BigDecimal.valueOf(1000);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(admitted);
            caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setRespondent2SameLegalRepresentative(NO);
            caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setIsRespondent2(YES);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                    .contains(caseData.getApplicant1().getPartyName())
                    .contains("the claimant can request a County Court Judgment against you.");
        }

        @Test
        void specificSummary_whenRepayPlanFullAdmit() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(FULL_ADMISSION);
            caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
                            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
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
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
                            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
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
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(FULL_ADMISSION);
            caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.YES);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
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
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(FULL_ADMISSION);
            caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
                            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new  RespondToClaimAdmitPartLRspec();
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
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
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);
            RespondToClaim respondToClaim = new  RespondToClaim();
            respondToClaim.setHowMuchWasPaid(howMuchWasPaid);
            caseData.setRespondToAdmittedClaim(respondToClaim);
            caseData.setTotalClaimAmount(totalClaimAmount);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                    .contains(caseData.getApplicant1().getPartyName())
                    .contains(caseData.getTotalClaimAmount().toString());
        }

        @Test
        void specificSummary_whenFullAdmitNotPaid() {
            // Given
            BigDecimal totalClaimAmount = BigDecimal.valueOf(1000);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            caseData.setRespondentClaimResponseTypeForSpecGeneric(FULL_ADMISSION);
            caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);
            caseData.setTotalClaimAmount(totalClaimAmount);
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new  RespondToClaimAdmitPartLRspec();
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
            caseData.setRespondent1Represented(YES);
            caseData.setApplicant1Represented(NO);
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
                            FULL_DEFENCE,
                            FULL_ADMISSION
                    )
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            String body = "<br>The defendants have chosen different responses and the claim cannot continue online." +
                    "<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                    + "counterclaim." +
                    String.format(
                            "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    String.format(
                            "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    "<br><br>Post the completed form to:" +
                    "<br><br>County Court Business Centre<br>St. Katherine's House" +
                    "<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH";

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                            .confirmationHeader(format(
                                    "# The defendants have chosen their responses%n## Claim number <br>%s",
                                    claimNumber
                            ))
                            .confirmationBody(body)
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

            String body = "<br>The defendants have chosen different responses and the claim cannot continue online." +
                    "<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                    + "counterclaim." +
                    String.format(
                            "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    String.format(
                            "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    "<br><br>Post the completed form to:" +
                    "<br><br>County Court Business Centre<br>St. Katherine's House" +
                    "<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH";

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                            .confirmationHeader(format(
                                    "# The defendants have chosen their responses%n## Claim number <br>%s",
                                    claimNumber
                            ))
                            .confirmationBody(body)
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

            String body = "<br>The defendants have chosen different responses and the claim cannot continue online." +
                    "<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                    + "counterclaim." +
                    String.format(
                            "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    String.format(
                            "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    "<br><br>Post the completed form to:" +
                    "<br><br>County Court Business Centre<br>St. Katherine's House" +
                    "<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH";

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                            .confirmationHeader(format(
                                    "# The defendants have chosen their responses%n## Claim number <br>%s",
                                    claimNumber
                            ))
                            .confirmationBody(body)
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

            String body = "<br>The defendants have chosen different responses and the claim cannot continue online." +
                    "<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                    + "counterclaim." +
                    String.format(
                            "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    String.format(
                            "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                            "https://www.gov.uk/respond-money-claim"
                    ) +
                    "<br><br>Post the completed form to:" +
                    "<br><br>County Court Business Centre<br>St. Katherine's House" +
                    "<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH";

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                            .confirmationHeader(format(
                                    "# The defendants have chosen their responses%n## Claim number <br>%s",
                                    claimNumber
                            ))
                            .confirmationBody(body)
                            .build());
        }

        @Test
        void specificSummary_whenPartialAdmitPaidLess() {
            // Given
            BigDecimal howMuchWasPaid = BigDecimal.valueOf(1000);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);
            RespondToClaim respondToClaim  = new RespondToClaim();
            respondToClaim.setHowMuchWasPaid(howMuchWasPaid);
            caseData.setRespondToAdmittedClaim(respondToClaim);
            BigDecimal totalClaimAmount = BigDecimal.valueOf(10000);
            caseData.setTotalClaimAmount(totalClaimAmount);
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

        @Test
        void shouldReturnSubmittedResponse_whenFullAdmitWithPayBySetDate() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(FULL_ADMISSION);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
                            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                    .doesNotContain("Download questionnaire (opens in a new tab)");
        }

        @Test
        void shouldReturnSubmittedResponse_whenFullAdmitWithRepaymentPlan() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
            caseData.setRespondent1ClaimResponseTypeForSpec(FULL_ADMISSION);
            caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
                            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getConfirmationBody())
                    .doesNotContain("Download questionnaire (opens in a new tab)");
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setIsRespondent1(YES);
            caseData.setSpecAoSRespondentCorrespondenceAddressRequired(YesOrNo.NO);
            Address address = new Address();
            address.setPostCode("postal code");
            caseData.setSpecAoSRespondentCorrespondenceAddressdetails(address);
            CallbackParams params = callbackParamsOf(caseData, MID, "confirm-details");
            when(postcodeValidator.validate("postal code")).thenReturn(Collections.emptyList());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            verify(postcodeValidator).validate("postal code");
            assertNotNull(response.getData());
        }

        @Test
        void whenProvided_thenValidateCorrespondence2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setIsRespondent2(YES);
            caseData.setSpecAoSRespondent2CorrespondenceAddressRequired(YesOrNo.NO);
            Address address = new Address();
            address.setPostCode("postal code");
            caseData.setSpecAoSRespondent2CorrespondenceAddressdetails(address);
            CallbackParams params = callbackParamsOf(caseData, MID, "confirm-details");
            when(postcodeValidator.validate("postal code")).thenReturn(Collections.emptyList());

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            verify(postcodeValidator).validate("postal code");
            assertNotNull(response.getData());
        }
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldPopulateCourtLocations() {
            // Given
            Party party = new Party();
            party.setPartyName("name");
            party.setType(Party.Type.INDIVIDUAL);
            CaseData caseData = CaseDataBuilder.builder()
                    .respondent1(party)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            List<LocationRefData> locations = List.of(LocationRefData.builder()
                    .build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                    .thenReturn(locations);
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            when(courtLocationUtils.getLocationsFromList(locations))
                    .thenReturn(locationValues);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            System.out.println(response.getData());

            // Then
            assertThat(response.getData())
                    .extracting("respondToCourtLocation")
                    .extracting("responseCourtLocations")
                    .extracting("list_items")
                    .asInstanceOf(list(Object.class))
                    .extracting("label")
                    .containsExactly(locationValues.getListItems().get(0).getLabel());
            assertThat(response.getData()).containsEntry("showCarmFields", "Yes");
        }

        @Test
        void shouldCheckToggleNotToShowCarmFieldsBeforePopulateCourtLocations() {
            // Given
            Party party = new Party();
            party.setPartyName("name");
            party.setType(Party.Type.INDIVIDUAL);
            CaseData caseData = CaseDataBuilder.builder()
                    .respondent1(party)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            List<LocationRefData> locations = List.of(LocationRefData.builder()
                    .build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                    .thenReturn(locations);
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            when(courtLocationUtils.getLocationsFromList(locations))
                    .thenReturn(locationValues);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(false);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            System.out.println(response.getData());

            // Then
            assertThat(response.getData())
                    .extracting("respondToCourtLocation")
                    .extracting("responseCourtLocations")
                    .extracting("list_items")
                    .asInstanceOf(list(Object.class))
                    .extracting("label")
                    .containsExactly(locationValues.getListItems().get(0).getLabel());
            assertThat(response.getData()).containsEntry("showCarmFields", "No");
        }

        @Test
        void shouldTriggerError_WhenRespondent1AlreadyRespondedAndTryToSubmitAgain() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .respondent1ResponseDate(LocalDateTime.now())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
            //Then
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).contains(
                    "There is a problem \n You have already submitted the defendant's response");
        }

        @Test
        void shouldTriggerError_WhenRespondent2AlreadyRespondedAndTryToSubmitAgain() {
            //Given
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
            //Then
            assertThat(response.getErrors()).isNotNull();
        }

    }

    @Nested
    class MidDetermineLoggedInSolicitor {

        @BeforeEach
        void setup() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void testDetermineLoggedInSolicitorForRespondentSolicitor1() {
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
        void testDetermineLoggedInSolicitorForRespondentSolicitor2() {
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
        void testDetermineLoggedInSolicitorForApplicantSolicitor() {
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
    class MidValidateMediationUnavailabiltyDates {

        @Test
        void testValidateResp2UnavailableDateWhenAvailabilityIsNo() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(NO);
            caseData.setResp2MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-mediation-unavailable-dates");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void testValidateResp2UnavailableDateWhenAvailabilityIsYesAndSingleDate() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().plusDays(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().plusDays(4));
            unavailableDate2.setToDate(LocalDate.now().plusDays(6));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp2MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void testValidateResp1UnavailableDateWhenAvailabilityIsNo() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(NO);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void testValidateResp1UnavailableDateWhenAvailabilityIsYesAndSingleDate() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().plusDays(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().plusDays(4));
            unavailableDate2.setToDate(LocalDate.now().plusDays(6));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void testValidateResp1UnavailableDateWhenAvailabilityIsYesAndSingleDateErrored() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().minusDays(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().plusDays(4));
            unavailableDate2.setToDate(LocalDate.now().plusDays(6));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date must not be before today.");
        }

        @Test
        void testResp1UnavailableDateWhenAvailabilityIsYesAndSingleDateIsBeyondYear() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().plusMonths(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().plusDays(4));
            unavailableDate2.setToDate(LocalDate.now().plusDays(6));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains(
                    "Unavailability Date must not be more than three months in the future.");
        }

        @Test
        void testResp1UnavailableDateWhenDateToIsBeforeDateFrom() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().plusDays(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().plusDays(6));
            unavailableDate2.setToDate(LocalDate.now().plusDays(4));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains(
                    "Unavailability Date From cannot be after Unavailability Date To. Please enter valid range.");
        }

        @Test
        void testResp1UnavailableDateWhenDateFromIsBeforeToday() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().plusDays(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().minusDays(6));
            unavailableDate2.setToDate(LocalDate.now().plusDays(4));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date From must not be before today.");
        }

        @Test
        void testResp1UnavailableDateWhenDateToIsBeforeToday() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().plusDays(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().plusDays(6));
            unavailableDate2.setToDate(LocalDate.now().minusDays(4));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains(
                    "Unavailability Date From cannot be after Unavailability Date To. Please enter valid range.");
        }

        @Test
        void testResp1UnavailableDateWhenDateToIsBeyondOneYear() {

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDate(LocalDate.now().plusDays(4));
            UnavailableDate unavailableDate2 = new UnavailableDate();
            unavailableDate2.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            unavailableDate2.setFromDate(LocalDate.now().plusDays(6));
            unavailableDate2.setToDate(LocalDate.now().plusMonths(4));
            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                    unavailableDate1,
                    unavailableDate2
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2Copy(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(NO)
                    .respondent1DQ()
                    .build();
            CaseData updatedCaseData = caseData;
            MediationAvailability mediationAvailability = new MediationAvailability();
            mediationAvailability.setIsMediationUnavailablityExists(YES);
            mediationAvailability.setUnavailableDatesForMediation(unAvailableDates);
            updatedCaseData.setResp1MediationAvailability(mediationAvailability);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains(
                    "Unavailability Date To must not be more than three months in the future.");
        }

    }

    @Nested
    class MidValidateRespondentExperts {

        @BeforeEach
        void setup() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void testValidateRespondentExpertsMultipartyResSol1() {
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
        void testValidateRespondentExpertsMultipartyResSol2() {
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
        void testValidateRespondentExpertsMultiparty() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate();
            unavailableDate.setDate(LocalDate.of(2024, 5, 2));
            unavailableDate.setWho("who 1");
            List<Element<UnavailableDate>> dates = Stream.of(unavailableDate).map(ElementUtils::element).toList();

            SmallClaimHearing smallClaimHearing = new SmallClaimHearing();
            smallClaimHearing.setUnavailableDatesRequired(YES);
            smallClaimHearing.setSmallClaimUnavailableDate(dates);
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQHearingSmallClaim(smallClaimHearing);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                    .respondent1DQ(respondent1DQ)
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
        void shouldThrowError_whenValidateRespondentExpertsMultipartyWithNoUnavailableDates() {
            // Given
            SmallClaimHearing smallClaimHearing = new SmallClaimHearing();
            smallClaimHearing.setUnavailableDatesRequired(YES);
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQHearingSmallClaim(smallClaimHearing);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                    .respondent1DQ(respondent1DQ)
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

    @Nested
    class HideHadPaidSomeAmount {
        @Test
        void hideHadPaidSomeAmountFOr1V1() {

            // Given
            CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotified().isRespondent1(YES)
                            .respondent1ClaimResponseTypeForSpec(
                                    FULL_ADMISSION).build();
            CaseData updatedCaseData = caseData;
            updatedCaseData.setShowConditionFlags(EnumSet.of(
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1
            ));
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "set-generic-response-type-flag");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response.getData())
                    .extracting("showConditionFlags", as(list(String.class)))
                    .contains(DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID.name());
        }

        @Test
        void hideHadPaidSomeAmountFOr1V2() {

            // Given
            CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotified().isRespondent2(YES)
                            .respondent2ClaimResponseTypeForSpec(
                                    FULL_ADMISSION).build();
            CaseData updatedCaseData = caseData;
            updatedCaseData.setShowConditionFlags(EnumSet.of(
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
            ));
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "set-generic-response-type-flag");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response.getData())
                    .extracting("showConditionFlags", as(list(String.class)))
                    .contains(DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID.name());
        }

        @Test
        void hideHadPaidSomeAmountFOr1V2IfNOCFlagIsOff() {

            // Given
            CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotified().isRespondent2(YES)
                            .respondent2ClaimResponseTypeForSpec(
                                    FULL_ADMISSION).build();
            CaseData updatedCaseData = caseData;
            updatedCaseData.setShowConditionFlags(EnumSet.of(
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
            ));
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "set-generic-response-type-flag");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response.getData())
                    .extracting("showConditionFlags", as(list(String.class)))
                    .contains(DefendantResponseShowTag.SHOW_ADMITTED_AMOUNT_SCREEN.name());
        }

        @Test
        void shouldsetspecDefenceFullAdmittedRequiredFor1V1() {
            // Given
            CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotified().isRespondent1(YES)
                            .respondent1ClaimResponseTypeForSpec(
                                    FULL_ADMISSION).build();
            CaseData updatedCaseData = caseData;
            updatedCaseData.setShowConditionFlags(EnumSet.of(
                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1
            ));
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);

            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "specHandleAdmitPartClaim");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
            // Then
            assertThat(response.getData()).extracting("specDefenceFullAdmittedRequired").isEqualTo("No");
        }

        @Test
        void shouldsetspecDefenceFullAdmitted2RequiredFOr1V2() {

            // Given
            CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotified().isRespondent2(YES)
                            .respondent2ClaimResponseTypeForSpec(
                                    FULL_ADMISSION).build();
            CaseData updatedCaseData = caseData;
            updatedCaseData.setRespondent2(PartyBuilder.builder().individual().build());
            updatedCaseData.setShowConditionFlags(EnumSet.of(
                                    DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                            ));
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "specHandleAdmitPartClaim");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            // Then
            assertThat(response.getData()).extracting("specDefenceFullAdmitted2Required").isEqualTo("No");

        }
    }
}
