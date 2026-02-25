package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class ResponseToJudgeDirectionsOrderTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @InjectMocks
    private ResponseToJudgeDirectionsOrder handler;

    @Mock
    private IdamClient idamClient;

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
        assertThat(handler.handledEvents()).contains(RESPOND_TO_JUDGE_DIRECTIONS);
    }

    @Test
    void shouldPopulateDocListAndReturnNullWrittenRepUpload() {
        mockIdamClient();

        List<Element<Document>> generalAppDirOrderUpload = new ArrayList<>();

        Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
            .setDocumentBinaryUrl(TEST_STRING)
            .setDocumentHash(TEST_STRING);

        Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
            .setDocumentBinaryUrl(TEST_STRING)
            .setDocumentHash(TEST_STRING);

        generalAppDirOrderUpload.add(element(document1));
        generalAppDirOrderUpload.add(element(document2));
        GeneralApplicationCaseData caseData = getCase(generalAppDirOrderUpload,
                                    null);

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        var responseCaseData = getCaseData(response);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getGeneralAppDirOrderUpload()).isEqualTo(null);
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(2);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
        assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);
    }

    @Test
    void shouldPopulateDocListWithExitingDocElement() {
        mockIdamClient();

        List<Element<Document>> generalAppDirOrderUpload = new ArrayList<>();

        Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
            .setDocumentBinaryUrl(TEST_STRING)
            .setDocumentHash(TEST_STRING);

        Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
            .setDocumentBinaryUrl(TEST_STRING)
            .setDocumentHash(TEST_STRING);

        generalAppDirOrderUpload.add(element(document1));
        generalAppDirOrderUpload.add(element(document2));

        List<Element<Document>> gaDirectionDocList = new ArrayList<>();

        gaDirectionDocList.add(element(document1));
        gaDirectionDocList.add(element(document2));

        GeneralApplicationCaseData caseData = getCase(generalAppDirOrderUpload,
                                    DocUploadUtils.prepareDocuments(gaDirectionDocList, DocUploadUtils.APPLICANT,
                                                                    RESPOND_TO_JUDGE_DIRECTIONS));

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        var responseCaseData = getCaseData(response);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getGeneralAppDirOrderUpload()).isEqualTo(null);
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(4);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(2);
        assertThat(responseCaseData.getGaAddlDocClaimant().size()).isEqualTo(2);
    }

    @Test
    void shouldCreateDashboardNotificationIfGaForLipIsTrue() {
        mockIdamClient();

        List<Element<Document>> generalAppAddlnInfoUpload = new ArrayList<>();

        Document document1 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
            .setDocumentBinaryUrl(TEST_STRING)
            .setDocumentHash(TEST_STRING);

        Document document2 = new Document().setDocumentFileName(TEST_STRING).setDocumentUrl(TEST_STRING)
            .setDocumentBinaryUrl(TEST_STRING)
            .setDocumentHash(TEST_STRING);

        generalAppAddlnInfoUpload.add(element(document1));
        generalAppAddlnInfoUpload.add(element(document2));

        GeneralApplicationCaseData caseData = getCase(generalAppAddlnInfoUpload, null);

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);

        handler.handle(params);
        verify(docUploadDashboardNotificationService).createDashboardNotification(any(GeneralApplicationCaseData.class), anyString(), anyString(), anyBoolean());
        verify(docUploadDashboardNotificationService).createResponseDashboardNotification(any(), eq("RESPONDENT"), anyString());
        verify(docUploadDashboardNotificationService).createResponseDashboardNotification(any(), eq("APPLICANT"), anyString());
    }

    private GeneralApplicationCaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
    }

    private GeneralApplicationCaseData getCase(List<Element<Document>> generalAppDirOrderUpload,
                             List<Element<CaseDocument>> gaAddlDoc) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        return new GeneralApplicationCaseData().parentClaimantIsApplicant(YES)
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email("abc@gmail.com").id(APP_UID).build())
            .generalAppDirOrderUpload(generalAppDirOrderUpload)
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
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    private void mockIdamClient() {
        when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                                                                 .sub(DUMMY_EMAIL)
                                                                 .uid(APP_UID)
                                                                 .build());
    }
}
