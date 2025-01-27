package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
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
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF1;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF2;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
        RespondToClaimSpecCallbackHandlerTestConfig.class
})
class RespondToClaimSpecCallbackHandlerIntegrationTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private Time time;
    @MockBean
    private PaymentDateValidator validator;
    @MockBean
    private UnavailableDateValidator dateValidator;
    @Autowired
    private ExitSurveyContentService exitSurveyContentService;
    @MockBean
    private FeatureToggleService toggleService;
    @MockBean
    private PostcodeValidator postcodeValidator;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @Autowired
    private UserService userService;
    @MockBean
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private StateFlow mockedStateFlow;
    @MockBean
    private SimpleStateFlowEngine stateFlowEngine;
    @MockBean
    private SimpleStateFlowBuilder simpleStateFlowBuilder;
    @Mock
    private DateOfBirthValidator dateOfBirthValidator;
    @MockBean
    private LocationReferenceDataService locationRefDataService;
    @Autowired
    private AssignCategoryId assignCategoryId;
    @MockBean
    private CourtLocationUtils courtLocationUtils;
    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;
    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;
    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;
    @Autowired
    private FrcDocumentsUtils frcDocumentsUtils;

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
        LocalDate date = dateTime.toLocalDate();
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(time.now()).thenReturn(dateTime);
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

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
                .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(NO)
                .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                .build().toBuilder()
                .respondent1DQWitnessesSmallClaim(res1witnesses)
                .respondent2DQWitnessesSmallClaim(res2witnesses)
                .build().toBuilder()
                .respondent2ResponseDate(dateTime)
                .respondent1ResponseDate(dateTime).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                .thenReturn(LocalDateTime.now());

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Then

        Witnesses actualRespondent1DQWitnesses = objectMapper.convertValue(response.getData().get(
                "respondent1DQWitnesses"), new TypeReference<>() {
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

        Witnesses actualRespondent2DQWitnesses = objectMapper.convertValue(response.getData().get(
                "respondent2DQWitnesses"), new TypeReference<>() {
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
        when(deadlinesCalculator.calculateApplicantResponseDeadline(
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
        when(deadlinesCalculator.calculateApplicantResponseDeadline(
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
        when(deadlinesCalculator.calculateApplicantResponseDeadline(
                any(LocalDateTime.class)
        )).thenReturn(deadline);

        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        Document document = DocumentBuilder.builder().build();
        List<Element<CaseDocument>> existingResponseDocuments = new ArrayList<>();
        existingResponseDocuments.add(
                Element.<CaseDocument>builder()
                        .id(UUID.randomUUID())
                        .value(CaseDocument.builder()
                                .documentLink(document)
                                .documentName("doc-1")
                                .createdBy("Defendant 1")
                                .createdDatetime(LocalDateTime.now())
                                .build())
                        .build());

        var newResponseDocuments = List.of(
                Element.<CaseDocument>builder()
                        .id(UUID.randomUUID())
                        .value(CaseDocument.builder()
                                .documentLink(document)
                                .documentName("doc-2")
                                .createdBy("Defendant 2")
                                .createdDatetime(LocalDateTime.now())
                                .build())
                        .build());

        when(dqResponseDocumentUtils.buildDefendantResponseDocuments(any(CaseData.class))).thenReturn(newResponseDocuments);

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
                .build().toBuilder()
                .defendantResponseDocuments(existingResponseDocuments)
                .build();
        caseData = caseData.toBuilder()
                .respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                        .respondent2DQDraftDirections(document).build()
                ).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        var actualCaseData = getCaseData(response);

        assertThat(actualCaseData.getDefendantResponseDocuments().size()).isEqualTo(2);
        assertThat(actualCaseData.getDefendantResponseDocuments().get(0).getValue().getDocumentName()).isEqualTo("doc-1");
        assertThat(actualCaseData.getDefendantResponseDocuments().get(1).getValue().getDocumentName()).isEqualTo("doc-2");
        assertThat(actualCaseData.getRespondent2DQ().getRespondent2DQDraftDirections()).isEqualTo(null);

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
        when(deadlinesCalculator.calculateApplicantResponseDeadline(
                any(LocalDateTime.class)
        )).thenReturn(deadline);

        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        Document document = DocumentBuilder.builder().build();
        var expectedResponseDocuments = List.of(
                Element.<CaseDocument>builder()
                        .id(UUID.randomUUID())
                        .value(CaseDocument.builder()
                                .documentLink(document)
                                .documentName("doc-name")
                                .createdBy("Defendant")
                                .createdDatetime(LocalDateTime.now())
                                .build())
                        .build());
        when(dqResponseDocumentUtils.buildDefendantResponseDocuments(any(CaseData.class))).thenReturn(expectedResponseDocuments);

        CaseData caseData = CaseDataBuilder.builder()
                .setIntermediateTrackClaim()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .atStateRespondentFullDefence()
                .respondent1DQWithFixedRecoverableCostsIntermediate()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .build();
        caseData = caseData.toBuilder()
                .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                        .respondent1DQDraftDirections(document).build()
                ).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        var actualCaseData = getCaseData(response);

        assertThat(actualCaseData.getDefendantResponseDocuments().size()).isEqualTo(1);
        assertThat(actualCaseData.getDefendantResponseDocuments().get(0).getValue().getDocumentName()).isEqualTo("doc-name");
        assertThat(actualCaseData.getRespondent1DQ().getRespondent1DQDraftDirections()).isEqualTo(null);

        verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
    }

    @Test
    void shouldNullDocuments_whenInvokedAndCaseFileEnabled() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
        var testDocument = ResponseDocument.builder()
                .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl(
                        "binary-url").build()).build();

        CaseData caseData = CaseData.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent2DQ(Respondent2DQ.builder().build())
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
    void shouldUpdateCorrespondence1_whenProvided() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        var testDocument = ResponseDocument.builder()
                .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl(
                        "binary-url").build()).build();

        CaseData caseData = CaseData.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent2DQ(Respondent2DQ.builder().build())
                .ccdCaseReference(354L)
                .respondent1SpecDefenceResponseDocument(testDocument)
                .respondent2SpecDefenceResponseDocument(testDocument)
                .isRespondent1(YesOrNo.YES)
                .specAoSRespondentCorrespondenceAddressRequired(YesOrNo.NO)
                .specAoSRespondentCorrespondenceAddressdetails(
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
    }

    @Test
    void shouldUpdateCorrespondence1_whenProvided1v2ss() {
        // Given
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        var testDocument = ResponseDocument.builder()
                .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl(
                        "binary-url").build()).build();

        CaseData caseData = CaseData.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent2DQ(Respondent2DQ.builder().build())
                .ccdCaseReference(354L)
                .respondent1SpecDefenceResponseDocument(testDocument)
                .respondent2SpecDefenceResponseDocument(testDocument)
                .isRespondent1(YesOrNo.YES)
                .specAoSRespondentCorrespondenceAddressRequired(YesOrNo.NO)
                .specAoSRespondentCorrespondenceAddressdetails(
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
        var testDocument = ResponseDocument.builder()
                .file(Document.builder().documentUrl("fake-url").documentFileName("file-name").documentBinaryUrl(
                        "binary-url").build()).build();

        CaseData caseData = CaseData.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent2DQ(Respondent2DQ.builder().build())
                .ccdCaseReference(354L)
                .respondent1SpecDefenceResponseDocument(testDocument)
                .respondent2SpecDefenceResponseDocument(testDocument)
                .isRespondent2(YesOrNo.YES)
                .specAoSRespondent2CorrespondenceAddressRequired(YesOrNo.NO)
                .specAoSRespondent2CorrespondenceAddressdetails(
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
    }

    @Test
    void shouldPopulateRespondent2Flag_WhenInvoked() {
        // Given
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
    void shouldNotPopulateRespondent2Flag_WhenInvoked() {
        // Given
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

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(DEFENDANT_RESPONSE_SPEC);
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }
}
