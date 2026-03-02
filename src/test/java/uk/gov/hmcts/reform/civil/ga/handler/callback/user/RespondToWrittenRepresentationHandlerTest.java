package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.RespondToWrittenRepresentationGenerator;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class RespondToWrittenRepresentationHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @InjectMocks
    private RespondToWrittenRepresentationHandler handler;

    @Mock
    private IdamClient idamClient;
    @Mock
    private RespondToWrittenRepresentationGenerator respondToWrittenRepresentationGenerator;
    @Mock
    private DocUploadDashboardNotificationService docUploadDashboardNotificationService;
    @Mock
    private GaForLipService gaForLipService;
    @Mock
    private FeatureToggleService featureToggleService;

    private static final String CAMUNDA_EVENT = "INITIATE_GENERAL_APPLICATION";
    private static final String BUSINESS_PROCESS_INSTANCE_ID = "11111";
    private static final String ACTIVITY_ID = "anyActivity";
    private static final String TEST_STRING = "anyValue";
    private static final String DUMMY_EMAIL = "test@gmail.com";
    private static final String APP_UID = "9";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION);
    }

    @Nested
    class AboutToSubmit {

        @BeforeEach
        void setup() {
            when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                                                                     .sub(DUMMY_EMAIL)
                                                                     .uid(APP_UID)
                                                                     .build());
        }

        @Test
        void shouldPopulateDocListAndReturnNullWrittenRepUpload() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);

            List<Element<Document>> generalAppWrittenRepUpload = new ArrayList<>();

            Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            generalAppWrittenRepUpload.add(element(document1));
            generalAppWrittenRepUpload.add(element(document2));

            GeneralApplicationCaseData caseData = getCase(generalAppWrittenRepUpload, null, null);

            Map<String, Object> dataMap = objectMapper.convertValue(
                caseData, new TypeReference<>() {
                }
            );
            CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            var responseCaseData = getCaseData(response);
            assertThat(response).isNotNull();
            assertThat(responseCaseData.getGeneralAppWrittenRepUpload()).isEqualTo(null);
            assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(2);
            assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
            assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);

        }

        @Test
        void shouldPopulateDocListWithExitingDocElement() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);

            List<Element<Document>> generalAppWrittenRepUpload = new ArrayList<>();

            Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            generalAppWrittenRepUpload.add(element(document1));
            generalAppWrittenRepUpload.add(element(document2));

            List<Element<Document>> gaWrittenRepDocList = new ArrayList<>();

            gaWrittenRepDocList.add(element(document1));
            gaWrittenRepDocList.add(element(document2));

            GeneralApplicationCaseData caseData = getCase(
                generalAppWrittenRepUpload,
                DocUploadUtils.prepareDocuments(
                    gaWrittenRepDocList, DocUploadUtils.APPLICANT,
                    RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION
                ), null
            );

            Map<String, Object> dataMap = objectMapper.convertValue(
                caseData, new TypeReference<>() {
                }
            );
            CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            var responseCaseData = getCaseData(response);
            assertThat(response).isNotNull();
            assertThat(responseCaseData.getGeneralAppWrittenRepUpload()).isEqualTo(null);
            assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(4);
            assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
            assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);
        }

        @Test
        void shouldPopulateDocListWithExitingDocElementWhenGaForWelshEnabled() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);

            List<Element<Document>> generalAppWrittenRepUpload = new ArrayList<>();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            generalAppWrittenRepUpload.add(element(document1));
            generalAppWrittenRepUpload.add(element(document2));

            List<Element<Document>> gaWrittenRepDocList = new ArrayList<>();

            gaWrittenRepDocList.add(element(document1));
            gaWrittenRepDocList.add(element(document2));

            GeneralApplicationCaseData caseData = getCase(
                generalAppWrittenRepUpload,
                DocUploadUtils.prepareDocuments(
                    gaWrittenRepDocList, DocUploadUtils.APPLICANT,
                    RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION
                ), null
            );

            Map<String, Object> dataMap = objectMapper.convertValue(
                caseData, new TypeReference<>() {
                }
            );
            CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            var responseCaseData = getCaseData(response);

            assertThat(response).isNotNull();
            assertThat(responseCaseData.getIsApplicantResponded()).isEqualTo(YES);
            assertThat(responseCaseData.getIsRespondentResponded()).isEqualTo(null);
            assertThat(responseCaseData.getGeneralAppWrittenRepUpload()).isEqualTo(null);
            assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(4);
            assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
            assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);
        }

        @Test
        void shouldConvertToDocAndReturnNullWrittenRepText() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            when(respondToWrittenRepresentationGenerator.generate(any(), anyString(), anyString()))
                .thenReturn(new CaseDocument().setDocumentLink(new Document()));

            GeneralApplicationCaseData caseData = getCase(null, null, "writtenRep text");

            Map<String, Object> dataMap = objectMapper.convertValue(
                caseData, new TypeReference<>() {
                }
            );
            CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            var responseCaseData = getCaseData(response);
            assertThat(response).isNotNull();
            assertThat(responseCaseData.getGeneralAppWrittenRepUpload()).isEqualTo(null);
            assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(1);
            assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(1);
            assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(1);
            assertThat(responseCaseData.getGeneralAppWrittenRepText()).isEqualTo(null);
        }

        @Test
        void shouldCreateDashboardNotificationIfGaForLipIsTrue() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);

            List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

            Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            generalAppAddlnInfoUpload.add(element(document1));
            generalAppAddlnInfoUpload.add(element(document2));

            GeneralApplicationCaseData caseData = getCase(generalAppAddlnInfoUpload, null, null);

            Map<String, Object> dataMap = objectMapper.convertValue(
                caseData, new TypeReference<>() {
                }
            );
            CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);

            handler.handle(params);
            verify(docUploadDashboardNotificationService).createDashboardNotification(
                any(GeneralApplicationCaseData.class),
                anyString(),
                anyString(),
                anyBoolean()
            );
            verify(docUploadDashboardNotificationService).createResponseDashboardNotification(
                any(),
                eq("RESPONDENT"),
                anyString()
            );
            verify(docUploadDashboardNotificationService).createResponseDashboardNotification(
                any(),
                eq("APPLICANT"),
                anyString()
            );

        }

        @Test
        void shouldNotCreateDashboardNotificationIfTranslationRequired() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(respondToWrittenRepresentationGenerator.generate(any(), anyString(), anyString()))
                .thenReturn(new CaseDocument().setDocumentLink(new Document()));
            List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

            Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            generalAppAddlnInfoUpload.add(element(document1));
            generalAppAddlnInfoUpload.add(element(document2));

            GeneralApplicationCaseData caseData = getCase(generalAppAddlnInfoUpload, null, null);
            caseData = caseData.copy().isGaApplicantLip(YES).applicantBilingualLanguagePreference(YES)
                .generalAppWrittenRepText("test").build();

            Map<String, Object> dataMap = objectMapper.convertValue(
                caseData, new TypeReference<>() {
                }
            );
            CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);

            handler.handle(params);
            verify(docUploadDashboardNotificationService, never()).createDashboardNotification(
                any(GeneralApplicationCaseData.class), anyString(), anyString(), anyBoolean());
            verify(docUploadDashboardNotificationService, never()).createResponseDashboardNotification(
                any(),
                eq("RESPONDENT"),
                anyString()
            );
            verify(docUploadDashboardNotificationService).createResponseDashboardNotification(
                any(),
                eq("APPLICANT"),
                anyString()
            );
        }

        @Test
        void shouldNotCreateDashboardNotificationIfTranslationAwaiting() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);

            List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

            Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
                .setDocumentBinaryUrl(TEST_STRING)
                .setDocumentHash(TEST_STRING);

            generalAppAddlnInfoUpload.add(element(document1));
            generalAppAddlnInfoUpload.add(element(document2));

            GeneralApplicationCaseData caseData = getCase(generalAppAddlnInfoUpload, null, null);
            caseData = caseData.copy().isGaApplicantLip(YES).applicantBilingualLanguagePreference(YES)
                .preTranslationGaDocuments(List.of(element(new CaseDocument().setDocumentName("Written representation").setCreatedBy("Applicant")))).build();

            Map<String, Object> dataMap = objectMapper.convertValue(
                caseData, new TypeReference<>() {
                }
            );
            CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);

            handler.handle(params);
            verify(docUploadDashboardNotificationService, never()).createDashboardNotification(
                any(GeneralApplicationCaseData.class), anyString(), anyString(), anyBoolean());
            verify(docUploadDashboardNotificationService, never()).createResponseDashboardNotification(
                any(),
                eq("RESPONDENT"),
                anyString()
            );
            verify(docUploadDashboardNotificationService).createResponseDashboardNotification(
                any(),
                eq("APPLICANT"),
                anyString()
            );
        }
    }

    private GeneralApplicationCaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
    }

    private GeneralApplicationCaseData getCase(List<Element<Document>> generalAppWrittenRepUpload,
                             List<Element<CaseDocument>> gaAddlDoc,
                             String generalAppWrittenRepText) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        return new GeneralApplicationCaseData().parentClaimantIsApplicant(YES)
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email("abc@gmail.com").id(APP_UID).build())
            .generalAppWrittenRepUpload(generalAppWrittenRepUpload)
            .generalAppWrittenRepText(generalAppWrittenRepText)
            .gaAddlDoc(gaAddlDoc)
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.READY)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

}

