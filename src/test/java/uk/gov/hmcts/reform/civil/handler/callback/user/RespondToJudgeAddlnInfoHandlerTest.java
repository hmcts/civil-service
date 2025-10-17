package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.docmosis.requestmoreinformation.RespondForInformationGenerator;
import uk.gov.hmcts.reform.civil.utils.DocUploadUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes = {
    RespondToJudgeAddlnInfoHandler.class,
    CaseDetailsConverter.class,
    JacksonAutoConfiguration.class},
    properties = {"reference.database.enabled=false"})
public class RespondToJudgeAddlnInfoHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    RespondToJudgeAddlnInfoHandler handler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseDetailsConverter caseDetailsConverter;
    @MockBean
    IdamClient idamClient;
    @MockBean
    RespondForInformationGenerator respondForInformationGenerator;
    @MockBean
    DocUploadDashboardNotificationService docUploadDashboardNotificationService;

    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    GaForLipService gaForLipService;

    private static final String CAMUNDA_EVENT = "INITIATE_GENERAL_APPLICATION";
    private static final String BUSINESS_PROCESS_INSTANCE_ID = "11111";
    private static final String ACTIVITY_ID = "anyActivity";
    private static final String TEST_STRING = "anyValue";
    private static final String DUMMY_EMAIL = "test@gmail.com";
    private static final String APP_UID = "9";

    @BeforeEach
    public void setUp() throws IOException {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                .sub(DUMMY_EMAIL)
                .uid(APP_UID)
                .build());
        when(respondForInformationGenerator.generate(any(), anyString(), anyString()))
                .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(RESPOND_TO_JUDGE_ADDITIONAL_INFO);
    }

    @Test
    void shouldPopulateDocListAndReturnNullWrittenRepUpload() {

        List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

        Document document1 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        Document document2 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        generalAppAddlnInfoUpload.add(element(document1));
        generalAppAddlnInfoUpload.add(element(document2));

        CaseData caseData = getCase(generalAppAddlnInfoUpload, null, null);

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        var responseCaseData = getCaseData(response);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getGeneralAppAddlnInfoUpload()).isEqualTo(null);
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(2);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
        assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);
    }

    @Test
    void shouldPopulateDocListWithExitingDocElement() {

        List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

        Document document1 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        Document document2 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        generalAppAddlnInfoUpload.add(element(document1));
        generalAppAddlnInfoUpload.add(element(document2));

        List<Element<Document>> gaAddlnInfoList = new ArrayList<>();

        gaAddlnInfoList.add(element(document1));
        gaAddlnInfoList.add(element(document2));

        CaseData caseData = getCase(generalAppAddlnInfoUpload,
                DocUploadUtils.prepareDocuments(gaAddlnInfoList, DocUploadUtils.APPLICANT, RESPOND_TO_JUDGE_ADDITIONAL_INFO), null);

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        var responseCaseData = getCaseData(response);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getGeneralAppAddlnInfoUpload()).isEqualTo(null);
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(4);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
        assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);
    }

    @Test
    void shouldPopulateDocListWithExitingDocElementWhenGaForWelshEnabled() {

        List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);

        Document document1 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        Document document2 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        generalAppAddlnInfoUpload.add(element(document1));
        generalAppAddlnInfoUpload.add(element(document2));

        List<Element<Document>> gaAddlnInfoList = new ArrayList<>();

        gaAddlnInfoList.add(element(document1));
        gaAddlnInfoList.add(element(document2));

        CaseData caseData = getCase(generalAppAddlnInfoUpload,
                DocUploadUtils.prepareDocuments(gaAddlnInfoList, DocUploadUtils.APPLICANT, RESPOND_TO_JUDGE_ADDITIONAL_INFO), null);

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        var responseCaseData = getCaseData(response);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getIsApplicantResponded()).isEqualTo(YES);
        assertThat(responseCaseData.getIsRespondentResponded()).isEqualTo(null);
        assertThat(responseCaseData.getGeneralAppAddlnInfoUpload()).isEqualTo(null);
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(4);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
        assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);
    }

    @Test
    void shouldConvertToDocAndReturnNullAddlnInfoText() {
        CaseData caseData = getCase(null, null, "more info");

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        var responseCaseData = getCaseData(response);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getGeneralAppAddlnInfoUpload()).isEqualTo(null);
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(1);
        assertThat(responseCaseData.getGeneralAppAddlnInfoText()).isEqualTo(null);
    }

    @Test
    void shouldCreateDashboardNotificationIfGaForLipIsTrue() {

        List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

        Document document1 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        Document document2 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        generalAppAddlnInfoUpload.add(element(document1));
        generalAppAddlnInfoUpload.add(element(document2));

        CaseData caseData = getCase(generalAppAddlnInfoUpload, null, null);

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);

        handler.handle(params);
        verify(docUploadDashboardNotificationService).createDashboardNotification(any(CaseData.class), anyString(), anyString(), anyBoolean());
    }

    @Test
    void shouldNotCreateDashboardNotificationIfTranslationRequired() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

        Document document1 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        Document document2 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        generalAppAddlnInfoUpload.add(element(document1));
        generalAppAddlnInfoUpload.add(element(document2));

        CaseData caseData = getCase(generalAppAddlnInfoUpload, null, null);
        caseData = caseData.toBuilder().isGaApplicantLip(YES).applicantBilingualLanguagePreference(YES)
                .generalAppAddlnInfoText("test").build();

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);

        handler.handle(params);
        verify(docUploadDashboardNotificationService, never()).createDashboardNotification(any(CaseData.class), anyString(), anyString(), anyBoolean());
    }

    @Test
    void shouldNotCreateDashboardNotificationIfTranslationAwaiting() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

        Document document1 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        Document document2 = Document.builder().documentFileName(TEST_STRING).documentUrl(TEST_STRING)
                .documentBinaryUrl(TEST_STRING)
                .documentHash(TEST_STRING).build();

        generalAppAddlnInfoUpload.add(element(document1));
        generalAppAddlnInfoUpload.add(element(document2));

        CaseData caseData = getCase(generalAppAddlnInfoUpload, null, null);
        caseData = caseData.toBuilder().isGaApplicantLip(YES).applicantBilingualLanguagePreference(YES)
                .preTranslationGaDocuments(List.of(element(CaseDocument.builder().documentName("Additional information").createdBy("Applicant").build()))).build();

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);

        handler.handle(params);
        verify(docUploadDashboardNotificationService, never()).createDashboardNotification(any(CaseData.class), anyString(), anyString(), anyBoolean());
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        return responseCaseData;
    }

    private CaseData getCase(List<Element<Document>> generalAppAddlnInfoUpload,
                             List<Element<CaseDocument>> gaAddlDoc,
                             String generalAppAddlnInfoText) {
        List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        return CaseData.builder().parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                        .email("abc@gmail.com").id(APP_UID).build())
                .generalAppAddlnInfoUpload(generalAppAddlnInfoUpload)
                .generalAppAddlnInfoText(generalAppAddlnInfoText)
                .gaAddlDoc(gaAddlDoc)
                .generalAppRespondent1Representative(
                        GARespondentRepresentative.builder()
                                .generalAppRespondent1Representative(YES)
                                .build())
                .generalAppType(
                        GAApplicationType
                                .builder()
                                .types(types).build())
                .businessProcess(BusinessProcess
                        .builder()
                        .camundaEvent(CAMUNDA_EVENT)
                        .processInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                        .status(BusinessProcessStatus.READY)
                        .activityId(ACTIVITY_ID)
                        .build())
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
    }
}
