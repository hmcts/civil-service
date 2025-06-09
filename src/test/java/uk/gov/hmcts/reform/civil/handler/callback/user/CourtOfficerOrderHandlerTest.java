package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.CourtOfficerOrderGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CourtOfficerOrderHandler.HEADER;

@ExtendWith(MockitoExtension.class)
public class CourtOfficerOrderHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CourtOfficerOrderHandler handler;
    @Mock
    private DocumentHearingLocationHelper locationHelper;
    @Mock
    private WorkingDayIndicator workingDayIndicator;
    @Mock
    private LocationReferenceDataService locationRefDataService;
    @Spy
    private AssignCategoryId assignCategoryId;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock
    private CourtOfficerOrderGenerator courtOfficerOrderGenerator;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private UserService userService;

    private static LocationRefData locationRefData =   LocationRefData.builder().siteName("A nice Site Name")
        .courtAddress("1").postcode("1")
        .courtName("Court Name example").region("Region").regionId("2").courtVenueId("666")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();
    public static final CaseDocument courtOfficerOrder = CaseDocument.builder()
        .createdBy("Test")
        .documentName("Court Officer Order test name")
        .documentSize(0L)
        .documentType(COURT_OFFICER_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name.pdf")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    @BeforeEach
    void setup() {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Nested
    class AboutToStartCallback {

        @BeforeEach
        void setup() {
            when(locationHelper.getHearingLocation(any(), any(), any())).thenReturn(locationRefData);
            List<LocationRefData> locationRefDataList = new ArrayList<>();
            locationRefDataList.add(locationRefData);
            when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locationRefDataList);
        }

        @Test
        void shouldPopulateValues_whenInvoked() {
            when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(7));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("courtOfficerFurtherHearingComplex"))
                .extracting("datesToAvoidDateDropdown")
                .extracting("datesToAvoidDates").isEqualTo(LocalDate.now().plusDays(7).toString());
            assertThat(response.getData().get("courtOfficerFurtherHearingComplex"))
                .extracting("hearingLocationList").asString().contains("A nice Site Name");
            assertThat(response.getData().get("courtOfficerFurtherHearingComplex"))
                .extracting("alternativeHearingList").asString().contains("A nice Site Name");

        }

    }

    @Nested
    class MidEventShowCertifyConditionCallback {

        private static final String PAGE_ID = "validateValues";

        @BeforeEach
        void setUp() {
            when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Court")
                                                                        .surname("OfficerName")
                                                                        .roles(Collections.emptyList()).build());
            when(courtOfficerOrderGenerator.generate(any(), any())).thenReturn(courtOfficerOrder);
        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            String fileName = LocalDate.now() + "_Court OfficerName.pdf";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                       .listFromDate(null).build())
                .build();

            when(courtOfficerOrderGenerator.generate(any(), any())).thenReturn(CaseDocument.builder().documentLink(
                Document.builder()
                    .documentFileName(fileName)
                    .categoryID("caseManagementOrders")
                    .build()).createdDatetime(LocalDateTime.now()).build());

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            System.out.println(response.getData());
            assertThat(response.getData())
                .extracting("previewCourtOfficerOrder")
                .extracting("documentLink")
                .extracting("document_filename")
                .isEqualTo(fileName);
            assertThat(response.getData())
                .extracting("previewCourtOfficerOrder")
                .extracting("documentLink")
                .extracting("category_id")
                .isEqualTo("caseManagementOrders");
        }

        @Test
        void shouldNotReturnError_whenNoDate() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                       .listFromDate(null).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNoError_whenDateInFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                       .listFromDate(LocalDate.now().plusDays(7)).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldError_whenDateInPast() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                       .listFromDate(LocalDate.now().minusDays(7)).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("List from date cannot be in the past");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldPopulateConfirmationHeader_WhenSubmitted() {
            // Given
            String confirmationHeader = format(HEADER, 1234567);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .ccdCaseReference(1234567L).build();
            // When
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);
            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(confirmationHeader)
                                                                          .build());
        }
    }

    @Nested
    class AboutToSubmitCallback {

        public static final String REFERENCE_NUMBER = "000DC001";
        private CallbackParams params;
        private CaseData caseData;
        private String userId;

        private ObjectMapper objectMapper;

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();
            objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
        }

        @Test
        void shouldSubmitted_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(COURT_OFFICER_ORDER.name(), "READY");
        }

        @Test
        void shouldHideDocumentIfClaimantWelsh_onAboutToSubmit() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            // Given
            caseData = caseData.toBuilder()
                .previewCourtOfficerOrder(courtOfficerOrder)
                .preTranslationDocuments(new ArrayList<>())
                .claimantBilingualLanguagePreference("BOTH")
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
            assertThat(updatedData.getPreviewCourtOfficerOrder()).isNull();
            assertThat(updatedData.getCurrentCamundaBusinessProcessName()).isNull();
        }

        @Test
        void shouldNotHideDocumentIfWelshDisabled_onAboutToSubmit() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            // Given
            caseData = caseData.toBuilder()
                .previewCourtOfficerOrder(courtOfficerOrder)
                .preTranslationDocuments(new ArrayList<>())
                .claimantBilingualLanguagePreference("BOTH")
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getPreTranslationDocuments()).hasSize(0);
            assertThat(updatedData.getPreviewCourtOfficerOrder()).isNotNull();
            assertThat(updatedData.getCurrentCamundaBusinessProcessName()).isNotNull();
        }

        @Test
        void shouldHideDocumentIfDefendantWelsh_onAboutToSubmit() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            // Given
            caseData = caseData.toBuilder()
                .previewCourtOfficerOrder(courtOfficerOrder)
                .preTranslationDocuments(new ArrayList<>())
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("WELSH").build()).build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
            assertThat(updatedData.getPreviewCourtOfficerOrder()).isNull();
            assertThat(updatedData.getCurrentCamundaBusinessProcessName()).isNull();
        }

        @Test
        void shouldHideDocumentIfClaimantWelshDocs_onAboutToSubmit() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            // Given
            caseData = caseData.toBuilder()
                .previewCourtOfficerOrder(courtOfficerOrder)
                .preTranslationDocuments(new ArrayList<>())
                .applicant1Represented(NO)
                .applicant1DQ(Applicant1DQ.builder().applicant1DQLanguage(WelshLanguageRequirements.builder().documents(
                    Language.WELSH).build()).build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
            assertThat(updatedData.getPreviewCourtOfficerOrder()).isNull();
            assertThat(updatedData.getCurrentCamundaBusinessProcessName()).isNull();
        }

        @Test
        void shouldHideDocumentIfDefendantWelshDocs_onAboutToSubmit() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            // Given
            caseData = caseData.toBuilder()
                .previewCourtOfficerOrder(courtOfficerOrder)
                .preTranslationDocuments(new ArrayList<>())
                .respondent1Represented(NO)
                .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder().documents(
                    Language.WELSH).build()).build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
            assertThat(updatedData.getPreviewCourtOfficerOrder()).isNull();
            assertThat(updatedData.getCurrentCamundaBusinessProcessName()).isNull();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(CaseEvent.COURT_OFFICER_ORDER);
    }

}
