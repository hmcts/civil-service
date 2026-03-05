package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.WAIT_GA_DRAFT;

@ExtendWith(MockitoExtension.class)
public class WaitCivilDocUpdatedTaskHandlerTest {

    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private GaCoreCaseDataService coreCaseDataService;
    @Spy
    private ObjectMapper mapper = ObjectMapperFactory.instance();
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private GaForLipService gaForLipService;
    @Mock
    private ExternalTask mockTask;
    @InjectMocks
    private WaitCivilDocUpdatedTaskHandler waitCivilDocUpdatedTaskHandler;
    @Mock
    private FeatureToggleService featureToggleService;

    private GeneralApplicationCaseData gaCaseData;
    private GeneralApplicationCaseData civilCaseDataEmpty;
    private GeneralApplicationCaseData civilCaseDataOld;
    private GeneralApplicationCaseData civilCaseDataNow;
    private static final String CASE_ID = "1644495739087775L";

    @BeforeEach
    void init() {
        CaseDocument caseDocumentNow = new CaseDocument().setDocumentName("current")
                .setDocumentLink(new Document().setDocumentUrl("url")
                        .setDocumentFileName("filename").setDocumentHash("hash")
                        .setDocumentBinaryUrl("binaryUrl"))
                .setDocumentType(DocumentType.GENERAL_APPLICATION_DRAFT).setDocumentSize(12L);
        CaseDocument caseDocumentOld = new CaseDocument().setDocumentName("old")
                .setDocumentLink(new Document().setDocumentUrl("url")
                        .setDocumentFileName("filename").setDocumentHash("hash")
                        .setDocumentBinaryUrl("binaryUrl"))
                .setDocumentType(DocumentType.GENERAL_APPLICATION_DRAFT).setDocumentSize(12L);
        gaCaseData = new GeneralApplicationCaseData()
                .generalAppParentCaseLink(
                        new GeneralAppParentCaseLink().setCaseReference("123"))
                .gaDraftDocument(ElementUtils.wrapElements(caseDocumentNow))
                .build();
        civilCaseDataEmpty = new GeneralApplicationCaseData().build();
        civilCaseDataOld = new GeneralApplicationCaseData()
                .gaDraftDocStaff(ElementUtils.wrapElements(caseDocumentOld))
                .build();
        civilCaseDataNow = new GeneralApplicationCaseData()
                .gaDraftDocStaff(ElementUtils.wrapElements(caseDocumentNow))
                .build();
    }

    @Test
    void should_handle_task_fail() {
        CaseDetails caseDetails = CaseDetailsBuilder.builder().id(1L).data(gaCaseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startGaUpdate("1L", WAIT_GA_DRAFT))
            .thenReturn(startEventResponse);
        when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(gaCaseData);
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil)).thenReturn(civilCaseDataOld);
        WaitCivilDocUpdatedTaskHandler.maxWait = 1;
        WaitCivilDocUpdatedTaskHandler.waitGap = 1;
        waitCivilDocUpdatedTaskHandler.execute(mockTask, externalTaskService);
        WaitCivilDocUpdatedTaskHandler.maxWait = 10;
        WaitCivilDocUpdatedTaskHandler.waitGap = 6;
        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
    }

    @Test
    void updated_should_success_ga_has_no_doc() {
        GeneralApplicationCaseData emptyCaseData = new GeneralApplicationCaseData().build();
        assertThat(waitCivilDocUpdatedTaskHandler.checkCivilDocUpdated(emptyCaseData)).isTrue();
    }

    @Test
    void updated_should_fail_civil_doc_is_empty() {
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCaseDataEmpty);
        assertThat(waitCivilDocUpdatedTaskHandler.checkCivilDocUpdated(gaCaseData)).isFalse();
    }

    @Test
    void updated_should_fail_civil_doc_is_old() {
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCaseDataOld);
        assertThat(waitCivilDocUpdatedTaskHandler.checkCivilDocUpdated(gaCaseData)).isFalse();
    }

    @Test
    void updated_should_success_civil_doc_is_updated() {
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCaseDataNow);
        assertThat(waitCivilDocUpdatedTaskHandler.checkCivilDocUpdated(gaCaseData)).isTrue();
    }

    @Test
    void shouldCallHandleFailureMethod_whenFeignExceptionFromBusinessLogic() {
        String errorMessage = "there was an error";
        int status = 422;
        Request.HttpMethod requestType = Request.HttpMethod.POST;
        String exampleUrl = "example url";
        CaseDetails ga = CaseDetails.builder().id(1L).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(ga).build();

        when(coreCaseDataService.startGaUpdate("1L", WAIT_GA_DRAFT))
            .thenReturn(startEventResponse);
        when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(gaCaseData);
        when(mockTask.getRetries()).thenReturn(null);
        when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails()))
                .thenAnswer(invocation -> {
                    throw FeignException.errorStatus(errorMessage, Response.builder()
                            .request(
                                    Request.create(
                                            requestType,
                                            exampleUrl,
                                            new HashMap<>(), //this field is required for construtor//
                                            null,
                                            null,
                                            null
                                    ))
                            .status(status)
                            .build());
                });

        waitCivilDocUpdatedTaskHandler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockTask);
    }

    @Test
    void shouldUpdateGaDraftList_whenHandlerIsExecuted() {
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID,
            "caseEvent", WAIT_GA_DRAFT.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeDraftAppCaseData().copy()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        GeneralApplicationCaseData updatedCaseData =  GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeDraftAppCaseData().copy()
            .gaDraftDocument(singletonList(
            Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                .value(pdfDocument).build())).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startGaUpdate(CASE_ID, WAIT_GA_DRAFT)).thenReturn(startEventResponse);
        when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);
        when(coreCaseDataService.submitGaUpdate(anyString(), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        waitCivilDocUpdatedTaskHandler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startGaUpdate(CASE_ID, WAIT_GA_DRAFT);
        verify(coreCaseDataService).submitGaUpdate(eq(CASE_ID), any(CaseDataContent.class));
    }

    @Test
    void shouldUpdateGaDraftList_whenHandlerIsExecuted_pass() {
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID,
            "caseEvent", WAIT_GA_DRAFT.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeDraftAppCaseData().copy()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        GeneralApplicationCaseData updatedCaseData =  GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeDraftAppCaseData().copy()
            .gaDraftDocument(singletonList(
                Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                    .value(pdfDocument).build())).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(updatedCaseData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();

        when(coreCaseDataService.startGaUpdate(CASE_ID, WAIT_GA_DRAFT))
            .thenReturn(startEventResponse);
        when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        waitCivilDocUpdatedTaskHandler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startGaUpdate(CASE_ID, WAIT_GA_DRAFT);
        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldDeleteOnlyDraftAndTranslatedDocApplicationForGaLip() {
        String uid1 = "f000aa01-0451-4000-b000-000000000001";
        String uid2 = "f000aa01-0451-4000-b000-000000000002";
        String uid3 = "f000aa01-0451-4000-b000-000000000003";
        String uid4 = "f000aa01-0451-4000-b000-000000000004";

        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID,
            "caseEvent", WAIT_GA_DRAFT.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);

        when(gaForLipService.isGaForLip(any())).thenReturn(true); // GA for LIP condition
        var draftDocumentsList = List.of(
            Element.<CaseDocument>builder()
                .id(UUID.fromString(uid1))
                .value(new CaseDocument()
                           .setDocumentName("Draft_application_2024-12-02 14:48:26.pdf")
                           .setCreatedDatetime(LocalDateTime.parse("2024-12-02T14:48:26"))
                           .setDocumentLink(new Document()
                                             .setDocumentUrl("fake-url-draft-1")
                                             .setDocumentFileName("Draft_application_2024-12-02 14:48:26.pdf")
                                             .setDocumentBinaryUrl("binary-url-draft-1"))).build(),

            Element.<CaseDocument>builder()
                .id(UUID.fromString(uid2))
                .value(new CaseDocument()
                           .setDocumentName("Translated_draft_application_2024-12-02 14:54:15.pdf")
                           .setCreatedDatetime(LocalDateTime.parse("2024-12-02T14:54:15"))
                           .setDocumentLink(new Document()
                                             .setDocumentUrl("fake-url-translated-1")
                                             .setDocumentFileName(
                                                 "Translated_draft_application_2024-12-02 14:54:15.pdf")
                                             .setDocumentBinaryUrl("binary-url-translated-1"))).build(),

            Element.<CaseDocument>builder()
                .id(UUID.fromString(uid3))
                .value(new CaseDocument()
                           .setDocumentName("Draft_application_2024-12-02 15:27:01.pdf")
                           .setCreatedDatetime(LocalDateTime.parse("2024-12-02T15:27:01"))
                           .setDocumentLink(new Document()
                                             .setDocumentUrl("fake-url-draft-2")
                                             .setDocumentFileName("Draft_application_2024-12-02 15:27:01.pdf")
                                             .setDocumentBinaryUrl("binary-url-draft-2"))).build(),

            Element.<CaseDocument>builder()
                .id(UUID.fromString(uid4))
                .value(new CaseDocument()
                           .setDocumentName("Translated_draft_application_2024-12-02 15:45:15.pdf")
                           .setCreatedDatetime(LocalDateTime.parse("2024-12-02T15:45:15"))
                           .setDocumentLink(new Document()
                                             .setDocumentUrl("fake-url-translated-2")
                                             .setDocumentFileName(
                                                 "Translated_draft_application_2024-12-02 15:45:15.pdf")
                                             .setDocumentBinaryUrl("binary-url-translated-2"))).build()
        );
        GeneralApplicationCaseData gaLipCaseData = new GeneralApplicationCaseData()
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .gaDraftDocument(draftDocumentsList)
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(gaLipCaseData).build();

        StartEventResponse startEventResponse = StartEventResponse.builder().caseDetails(caseDetails).build();
        when(coreCaseDataService.startGaUpdate(anyString(), eq(WAIT_GA_DRAFT))).thenReturn(startEventResponse);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(gaLipCaseData);

        waitCivilDocUpdatedTaskHandler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService).startGaUpdate(CASE_ID, WAIT_GA_DRAFT);
        verify(caseDetailsConverter).toGeneralApplicationCaseData(any());
    }

    public final CaseDocument pdfDocument = new CaseDocument()
        .setCreatedBy("John")
        .setDocumentName("documentName")
        .setDocumentSize(0L)
        .setDocumentType(DocumentType.GENERAL_APPLICATION_DRAFT)
        .setCreatedDatetime(LocalDateTime.now())
        .setDocumentLink(new Document()
                          .setDocumentUrl("fake-url")
                          .setDocumentFileName("file-name")
                          .setDocumentBinaryUrl("binary-url"));
}
