package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Long.parseLong;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_PDF_TO_MAIN_CASE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;

@SpringBootTest(classes = {
    UpdateFromGACaseEventTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    CaseDataContentConverter.class,
    CoreCaseDataService.class
})
@ExtendWith(SpringExtension.class)
public class UpdateFromGACaseEventTaskHandlerTest {

    private static final String CIVIL_CASE_ID = "1594901956117591";
    private static final String GENERAL_APP_CASE_ID = "1234";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private UpdateFromGACaseEventTaskHandler handler;

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getWorkerId()).thenReturn("worker");
        when(mockExternalTask.getActivityId()).thenReturn("activityId");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));
    }

    @Test
    void testShouldAddGeneralOrderDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithGeneralOrderPDFDocument(CaseData.builder().build());
        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithGeneralOrderStaffPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void testShouldAddDismissalOrderDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDismissalOrderPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDismissalOrderStaffPDFDocument(CaseData.builder().build());

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void testShouldAddDirectionOrderDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDirectionOrderPDFDocument(CaseData.builder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDirectionOrderStaffPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void testShouldAddHearingNoticeDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithHearingNoticeDocumentPDFDocument(CaseData.builder().build());
        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithHearingNoticeStaffDocumentPDFDocument((CaseData.builder().build()));

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
                .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByResp2() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        false,
                        false,
                        true, true,
                        new HashMap<>() {{
                            put("1234", "Order Made");
                        }}
                );
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(1234L).build();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "2")).isTrue();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "1")).isFalse();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData)).isFalse();
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByResp1() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        false,
                        true,
                        false, true,
                        new HashMap<>() {{
                            put("1234", "Order Made");
                        }}
                );
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(1234L).build();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "2")).isFalse();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "1")).isTrue();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData)).isFalse();
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByClaimant() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        true,
                        false,
                        false, true,
                        new HashMap<>() {{
                            put("1234", "Order Made");
                        }}
                );
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(1234L).build();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "2")).isFalse();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "1")).isFalse();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData)).isTrue();
    }

    @Test
    void testCanViewWithNoticeGaCreatedByResp2() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        true,
                        true,
                        true, true,
                        new HashMap<>() {{
                            put("1234", "Order Made");
                        }}
                );
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(1234L).build();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "2")).isTrue();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "1")).isTrue();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateDocCollection() {
        CaseData gaCaseData = new CaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();
        String uid = "f000aa01-0451-4000-b000-000000000000";
        gaCaseData = gaCaseData.toBuilder()
                .directionOrderDocument(singletonList(Element.<CaseDocument>builder()
                        .id(UUID.fromString(uid))
                        .value(pdfDocument).build())).build();
        Map<String, Object> output = new HashMap<>();
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();
        try {
            handler.updateDocCollection(output, gaCaseData, "directionOrderDocument",
                    caseData, "directionOrderDocStaff");
            List<Element<CaseDocument>> toUpdatedDocs =
                    (List<Element<CaseDocument>>)output.get("directionOrderDocStaff");
            assertThat(toUpdatedDocs).isNotNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotUpdateNullDocCollection() {
        CaseData gaCaseData = new CaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();
        String uid = "f000aa01-0451-4000-b000-000000000000";
        gaCaseData = gaCaseData.toBuilder().build();
        Map<String, Object> output = new HashMap<>();
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();
        try {
            handler.updateDocCollection(output, gaCaseData, "directionOrderDocument",
                    caseData, "directionOrderDocStaff");
            List<Element<CaseDocument>> toUpdatedDocs =
                    (List<Element<CaseDocument>>)output.get("directionOrderDocStaff");
            assertThat(toUpdatedDocs).isNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotUpdateNoExistFieldDocCollection() {
        CaseData gaCaseData = new CaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();
        String uid = "f000aa01-0451-4000-b000-000000000000";
        gaCaseData = gaCaseData.toBuilder().build();
        Map<String, Object> output = new HashMap<>();
        String noExistingField = "notExist";
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();
        try {
            handler.updateDocCollection(output, gaCaseData, noExistingField + "Document",
                    caseData, noExistingField + "DocStaff");
            List<Element<CaseDocument>> toUpdatedDocs =
                    (List<Element<CaseDocument>>)output.get(noExistingField + "DocStaff");
            assertThat(toUpdatedDocs).isNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testUpdateDocCollectionWithoutNoticeGaCreatedByResp2() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        false,
                        false,
                        true, true,
                        new HashMap<>() {{
                            put("1234", "Order Made");
                        }}
                );
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData generalAppCaseData = CaseData.builder()
                .directionOrderDocument(singletonList(Element.<CaseDocument>builder()
                .id(UUID.fromString(uid))
                .value(pdfDocument).build())).ccdCaseReference(1234L).build();
        Map<String, Object> output = new HashMap<>();
        try {
            handler.updateDocCollectionField(output, caseData, generalAppCaseData, "directionOrder");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(output.get("directionOrderDocStaff")).isNotNull();
        assertThat(output.get("directionOrderDocRespondentSolTwo")).isNotNull();
        assertThat(output.get("directionOrderDocClaimant")).isNull();
        assertThat(output.get("directionOrderDocRespondentSol")).isNull();
    }

    @Test
    void testUpdateDocCollectionWithNoticeGaCreatedByResp2() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        true,
                        true,
                        true, true,
                        new HashMap<>() {{
                            put("1234", "Order Made");
                        }}
                );
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData generalAppCaseData = CaseData.builder()
                .directionOrderDocument(singletonList(Element.<CaseDocument>builder()
                        .id(UUID.fromString(uid))
                        .value(pdfDocument).build())).ccdCaseReference(1234L).build();
        Map<String, Object> output = new HashMap<>();
        try {
            handler.updateDocCollectionField(output, caseData, generalAppCaseData, "directionOrder");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(output.get("directionOrderDocStaff")).isNotNull();
        assertThat(output.get("directionOrderDocRespondentSolTwo")).isNotNull();
        assertThat(output.get("directionOrderDocClaimant")).isNotNull();
        assertThat(output.get("directionOrderDocRespondentSol")).isNotNull();
    }

    @Test
    void testShouldAddConsentOrderDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithConsentOrderPDFDocument(CaseData.builder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithConsentOrderStaffPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void testShouldAddGaDraftApplicationDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocument(CaseData.builder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftStaffPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .token("1594901956117591")
            .eventId(ADD_PDF_TO_MAIN_CASE.name())
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    void shouldNotAddDirectionOrderDocument() {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        String uid1 = "f000aa01-0451-4000-b000-000000000111";
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();
        CaseData updatedCaseData = caseData.toBuilder()
            .directionOrderDocument(singletonList(Element.<CaseDocument>builder()
                                                      .id(UUID.fromString(uid))
                                                      .value(pdfDocument).build())).build();
        CaseData updatedCaseData1 = caseData.toBuilder()
            .generalOrderDocument(singletonList(Element.<CaseDocument>builder()
                                                    .id(UUID.fromString(uid1))
                                                    .value(pdfDocument).build()))
            .directionOrderDocument(singletonList(Element.<CaseDocument>builder()
                                                      .id(UUID.fromString(uid))
                                                      .value(pdfDocument).build())).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(updatedCaseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDirectionOrderPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(updatedCaseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(updatedCaseData1);

        handler.execute(mockExternalTask, externalTaskService);

        assertThat(updatedCaseData1.getDirectionOrderDocument().size())
            .isEqualTo(generalCaseData.getDirectionOrderDocument().size());
        assertThat(updatedCaseData1.getGeneralOrderDocument().size())
            .isEqualTo(generalCaseData.getGeneralOrderDocument().size());

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void shouldNotAddgaResponseDocument() {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        String uid1 = "f000aa01-0451-4000-b000-000000000111";
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();
        CaseData updatedCaseData = caseData.toBuilder()
            .directionOrderDocument(singletonList(Element.<CaseDocument>builder()
                                                      .id(UUID.fromString(uid))
                                                      .value(pdfDocument).build())).build();
        CaseData updatedCaseData1 = caseData.toBuilder()
            .gaRespDocStaff(singletonList(Element.<Document>builder().id(UUID.fromString(uid1))
                                              .value(pdfDocument1).build()))
            .gaRespDocClaimant(singletonList(Element.<Document>builder().id(UUID.fromString(uid1))
                                           .value(pdfDocument1).build())).build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(updatedCaseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDirectionResponseDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(updatedCaseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(updatedCaseData1);

        handler.execute(mockExternalTask, externalTaskService);

        assertThat(updatedCaseData1.getGaRespDocClaimant().size())
            .isEqualTo(generalCaseData.getGaRespDocument().size());
        assertThat(updatedCaseData1.getGaRespDocStaff().size())
            .isEqualTo(generalCaseData.getGaRespDocument().size());

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Nested
    class NotRetryableFailureTest {
        @Test
        void shouldNotCallHandleFailureMethod_whenMapperConversionFailed() {
            //given: ExternalTask.getAllVariables throws ValueMapperException
            when(mockExternalTask.getAllVariables())
                .thenThrow(new ValueMapperException("Mapper conversion failed due to incompatible types"));

            //when: Task handler is called and ValueMapperException is thrown
            handler.execute(mockExternalTask, externalTaskService);

            //then: Retry should not happen in this case
            verify(externalTaskService).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }

        @Test
        void shouldNotCallHandleFailureMethod_whenIllegalArgumentExceptionThrown() {
            //given: ExternalTask variables with incompatible event type
            String incompatibleEventType = "test";
            Map<String, Object> allVariables = Map.of("caseId", CIVIL_CASE_ID, "caseEvent", incompatibleEventType);
            when(mockExternalTask.getAllVariables())
                .thenReturn(allVariables);

            //when: Task handler is called and IllegalArgumentException is thrown
            handler.execute(mockExternalTask, externalTaskService);

            //then: Retry should not happen in this case
            verify(externalTaskService).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }

        @Test
        void shouldNotCallHandleFailureMethod_whenCaseIdNotFound() {
            //given: ExternalTask variables without caseId
            Map<String, Object> allVariables = Map.of("caseEvent", ADD_PDF_TO_MAIN_CASE);
            when(mockExternalTask.getAllVariables())
                .thenReturn(allVariables);

            //when: Task handler is called and CaseIdNotProvidedException is thrown
            handler.execute(mockExternalTask, externalTaskService);

            //then: Retry should not happen in this case
            verify(externalTaskService).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }
    }

    @Test
    void checkIfDocumentExists() {
        Element<?> same = Element.<CaseDocument>builder().id(UUID.randomUUID())
            .value(CaseDocument.builder().documentLink(Document.builder().documentUrl("string").build())
                       .build()).build();
        List<Element<?>> civilCaseDocumentList = new ArrayList<>();
        List<Element<?>> gaDocumentList = new ArrayList<>();
        gaDocumentList.add(same);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isNotPositive();
        civilCaseDocumentList.add(same);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(1);
    }

    @Test
    void checkIfDocumentExists_whenDocumentTypeIsDocumentClass() {
        Element<Document> documentElement = Element.<Document>builder()
            .id(UUID.randomUUID())
            .value(Document.builder().documentUrl("string").build()).build();
        List<Element<?>> gaDocumentList = new ArrayList<>();
        List<Element<?>> civilCaseDocumentList = new ArrayList<>();
        gaDocumentList.add(documentElement);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(0);
        civilCaseDocumentList.add(documentElement);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(1);
    }

    public final CaseDocument pdfDocument = CaseDocument.builder()
        .createdBy("John")
        .documentName("documentName")
        .documentSize(0L)
        .documentType(GENERAL_ORDER)
        .createdDatetime(now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public final Document pdfDocument1 = Document.builder()
        .documentUrl("fake-url")
        .documentFileName("file-name")
        .documentBinaryUrl("binary-url")
        .build();
}
