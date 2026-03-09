package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Long.parseLong;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_PDF_TO_MAIN_CASE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEND_APP_TO_OTHER_PARTY;

@ExtendWith(MockitoExtension.class)
public class UpdateFromGACaseEventTaskHandlerTest {

    private static final String CIVIL_CASE_ID = "1594901956117591";
    private static final String GENERAL_APP_CASE_ID = "1234";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private FeatureToggleService featureToggleService;

    private UpdateFromGACaseEventTaskHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new UpdateFromGACaseEventTaskHandler(coreCaseDataService, caseDetailsConverter, objectMapper, featureToggleService);
    }

    @Test
    void testShouldAddGeneralOrderDocument() {
        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithGeneralOrderPDFDocument(new CaseDataBuilder().build());
        CaseData updatedCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithGeneralOrderStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddDismissalOrderDocument() {
        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithDismissalOrderPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        CaseData updatedCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithDismissalOrderStaffPDFDocument(new CaseDataBuilder().build());

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddDirectionOrderDocument() {
        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithDirectionOrderPDFDocument(new CaseDataBuilder().build());

        CaseData updatedCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithDirectionOrderStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddHearingNoticeDocument() {
        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithHearingNoticeDocumentPDFDocument(new CaseDataBuilder().build());
        CaseData updatedCaseData = new GeneralApplicationDetailsBuilder()
            .getTestCaseDataWithHearingNoticeStaffDocumentPDFDocument((new CaseDataBuilder().build()));

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
                .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByResp2() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(
                new CaseDataBuilder().build(),
                false,
                false,
                true,
                true,
                Map.of("1234", "Order Made")
            );
        CaseData generalAppCaseData = new CaseDataBuilder().ccdCaseReference(1234L).build();

        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "2")).isTrue();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "1")).isFalse();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData, "")).isFalse();
    }

    @Test
    void testCanViewWithoutNoticeGaGivenDocTypeForGARespondent() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(
                new CaseDataBuilder().build(),
                false,
                false,
                true,
                true,
                Map.of("1234", "Order Made")
            );
        CaseData generalAppCaseData = new CaseDataBuilder().ccdCaseReference(1234L).build();

        assertThat(handler.canViewResp(caseData, generalAppCaseData, "generalOrder", "1")).isTrue();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData, "generalOrder")).isTrue();
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByResp1() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(
                new CaseDataBuilder().build(),
                false,
                true,
                false,
                true,
                Map.of("1234", "Order Made")
            );
        CaseData generalAppCaseData = new CaseDataBuilder().ccdCaseReference(1234L).build();

        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "2")).isFalse();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "1")).isTrue();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData, "")).isFalse();
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByClaimant() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(
                new CaseDataBuilder().build(),
                true,
                false,
                false,
                true,
                Map.of("1234", "Order Made")
            );
        CaseData generalAppCaseData = new CaseDataBuilder().ccdCaseReference(1234L).build();

        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "2")).isFalse();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "1")).isFalse();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData, "")).isTrue();
    }

    @Test
    void testCanViewWithNoticeGaCreatedByResp2() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(
                new CaseDataBuilder().build(),
                true,
                true,
                true,
                true,
                Map.of("1234", "Order Made")
            );
        CaseData generalAppCaseData = new CaseDataBuilder().ccdCaseReference(1234L).build();

        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "2")).isTrue();
        assertThat(handler.canViewResp(caseData, generalAppCaseData, "", "1")).isTrue();
        assertThat(handler.canViewClaimant(caseData, generalAppCaseData, "")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateDocCollection() throws Exception {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData gaCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        gaCaseData.setDirectionOrderDocument(singletonList(new Element<CaseDocument>()
                                                               .setId(UUID.fromString(uid))
                                                               .setValue(pdfDocument)));
        Map<String, Object> output = new HashMap<>();
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();

        handler.updateDocCollection(
            output, gaCaseData, "directionOrderDocument",
            caseData, "directionOrderDocStaff"
        );

        List<Element<CaseDocument>> toUpdatedDocs =
            (List<Element<CaseDocument>>) output.get("directionOrderDocStaff");

        assertThat(toUpdatedDocs).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotUpdateDocCollectionForRespondentIfMakeWithNoticeDoc() throws Exception {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData gaCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        gaCaseData.setParentClaimantIsApplicant(YesOrNo.YES)
            .setRequestForInformationDocument(singletonList(new Element<CaseDocument>()
                                                                .setId(UUID.fromString(uid))
                                                                .setValue(makeWithNoticeDocument)));
        Map<String, Object> output = new HashMap<>();
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();

        handler.updateDocCollection(
            output, gaCaseData, "requestForInformationDocument",
            caseData, "requestForInfoDocRespondentSol"
        );

        List<Element<CaseDocument>> toUpdatedDocs =
            (List<Element<CaseDocument>>) output.get("requestForInfoDocRespondentSol");

        assertThat(toUpdatedDocs).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotUpdateDocCollectionForClaimantIfMakeWithNoticeDoc() throws Exception {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData gaCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        gaCaseData
            .setParentClaimantIsApplicant(YesOrNo.NO)
            .setRequestForInformationDocument(singletonList(new Element<CaseDocument>()
                                                                .setId(UUID.fromString(uid))
                                                                .setValue(makeWithNoticeDocument)));
        Map<String, Object> output = new HashMap<>();
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();

        handler.updateDocCollection(
            output, gaCaseData, "requestForInformationDocument",
            caseData, "requestForInfoDocClaimant"
        );

        List<Element<CaseDocument>> toUpdatedDocs =
            (List<Element<CaseDocument>>) output.get("requestForInfoDocClaimant");

        assertThat(toUpdatedDocs).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotUpdateNullDocCollection() throws Exception {
        CaseData gaCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        Map<String, Object> output = new HashMap<>();
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();

        handler.updateDocCollection(
            output, gaCaseData, "directionOrderDocument",
            caseData, "directionOrderDocStaff"
        );

        List<Element<CaseDocument>> toUpdatedDocs =
            (List<Element<CaseDocument>>) output.get("directionOrderDocStaff");

        assertThat(toUpdatedDocs).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddToCivilDocsCopy() throws Exception {
        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocumentLip(new CaseDataBuilder().build());

        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build()
            .setRespondent1Represented(YesOrNo.NO);

        Method gaGetter = ReflectionUtils.findMethod(
            CaseData.class,
            "get" + StringUtils.capitalize("gaDraftDocument")
        );
        Method civilGetter = ReflectionUtils.findMethod(
            CaseData.class,
            "get" + StringUtils.capitalize("directionOrderDocStaff")
        );

        List<Element<?>> gaDocs =
            (List<Element<?>>) (gaGetter != null ? gaGetter.invoke(generalCaseData) : null);
        List<Element<?>> civilDocs =
            (List<Element<?>>) ofNullable(civilGetter != null ? civilGetter.invoke(caseData) : null)
                .orElse(newArrayList());
        List<Element<?>> civilDocsPre = List.copyOf(civilDocs);

        civilDocs = handler.checkDraftDocumentsInMainCase(civilDocs, gaDocs);

        assertTrue(civilDocs.size() > civilDocsPre.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddToCivilDocsCopyIfGADocsNotInCivilDocs() throws Exception {
        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocumentLip(new CaseDataBuilder().build());

        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDirectionOrderStaffPDFDocument(new CaseDataBuilder().atStateClaimDraft().build())
            .setRespondent1Represented(YesOrNo.NO);

        Method gaGetter = ReflectionUtils.findMethod(
            CaseData.class,
            "get" + StringUtils.capitalize("gaDraftDocument")
        );
        Method civilGetter = ReflectionUtils.findMethod(
            CaseData.class,
            "get" + StringUtils.capitalize("directionOrderDocStaff")
        );

        List<Element<?>> gaDocs =
            (List<Element<?>>) (gaGetter != null ? gaGetter.invoke(generalCaseData) : null);
        List<Element<?>> civilDocs =
            (List<Element<?>>) ofNullable(civilGetter != null ? civilGetter.invoke(caseData) : null)
                .orElse(new ArrayList<>());
        List<Element<?>> civilDocsPre = List.copyOf(civilDocs);

        civilDocs = handler.checkDraftDocumentsInMainCase(civilDocs, gaDocs);

        assertNotSame(civilDocs.get(0).getId(), civilDocsPre.get(0).getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotUpdateNoExistFieldDocCollection() throws Exception {
        CaseData gaCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        Map<String, Object> output = new HashMap<>();
        String noExistingField = "notExist";
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();

        handler.updateDocCollection(
            output, gaCaseData, noExistingField + "Document",
            caseData, noExistingField + "DocStaff"
        );

        List<Element<CaseDocument>> toUpdatedDocs =
            (List<Element<CaseDocument>>) output.get(noExistingField + "DocStaff");

        assertThat(toUpdatedDocs).isNull();
    }

    @Test
    void testUpdateDocCollectionWithoutNoticeGaCreatedByResp2() throws Exception {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(
                CaseData.builder().build(),
                false,
                false,
                true, true,
                Map.of("1234", "Order Made")
            );
        CaseData generalAppCaseData = new CaseDataBuilder().ccdCaseReference(1234L).build();
        generalAppCaseData.setDirectionOrderDocument(singletonList(new Element<CaseDocument>()
                                                                       .setId(UUID.fromString(uid))
                                                                       .setValue(pdfDocument)));
        Map<String, Object> output = new HashMap<>();

        handler.updateDocCollectionField(output, caseData, generalAppCaseData, "directionOrder");

        assertThat(output.get("directionOrderDocStaff")).isNotNull();
        assertThat(output.get("directionOrderDocRespondentSolTwo")).isNotNull();
        assertThat(output.get("directionOrderDocClaimant")).isNotNull();
        assertThat(output.get("directionOrderDocRespondentSol")).isNotNull();
    }

    @Test
    void testUpdateDocCollectionWithNoticeGaCreatedByResp2() throws Exception {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(
                new CaseDataBuilder().build(),
                true,
                true,
                true,
                true,
                Map.of("1234", "Order Made")
            );
        CaseData generalAppCaseData = new CaseDataBuilder().ccdCaseReference(1234L).build();
        generalAppCaseData.setDirectionOrderDocument(singletonList(new Element<CaseDocument>()
                                                                       .setId(UUID.fromString(uid))
                                                                       .setValue(pdfDocument)));
        Map<String, Object> output = new HashMap<>();

        handler.updateDocCollectionField(output, caseData, generalAppCaseData, "directionOrder");

        assertThat(output.get("directionOrderDocStaff")).isNotNull();
        assertThat(output.get("directionOrderDocRespondentSolTwo")).isNotNull();
        assertThat(output.get("directionOrderDocClaimant")).isNotNull();
        assertThat(output.get("directionOrderDocRespondentSol")).isNotNull();
    }

    @Test
    void testShouldAddConsentOrderDocument() {
        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithConsentOrderPDFDocument(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithConsentOrderStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddGaDraftApplicationDocument() {
        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocument(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddGaDraftApplicationDocument_LipRespondent() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = new CaseDataBuilder().atStateClaimDraftLip()
            .respondent1Represented(YesOrNo.NO)
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocumentLip(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddGaDraftApplicationDocument_LipRespondentWhenSizeIs1() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = new CaseDataBuilder().atStateClaimDraftLip()
            .respondent1Represented(YesOrNo.NO)
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocument(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddGaDraftApplicationDocument_LipRespondent2() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = new CaseDataBuilder().atStateClaimDraftLip()
            .respondent2Represented(YesOrNo.NO)
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocumentLip(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddGaDraftApplicationDocument_LipApplicant() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = new CaseDataBuilder().atStateClaimDraftLip()
            .applicant1Represented(YesOrNo.NO)
            .gaDraftDocument(singletonList(new Element<CaseDocument>()
                                               .setId(UUID.fromString(uid1))
                                               .setValue(pdfDocument)))
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocumentLip(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddGaDraftApplicationDocument_LipApplicantAndGaDraftsizelessthan1() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .applicant1Represented(YesOrNo.NO)
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftApplicationPDFDocumentLip(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDraftStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void testShouldAddGaAdditionalDocument() {
        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        CaseData caseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();

        CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithAdditionalDocument(new CaseDataBuilder().build());

        CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithAddlDocStaffPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(updatedCaseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
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

        CaseData updatedCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        updatedCaseData
            .setDirectionOrderDocument(singletonList(new Element<CaseDocument>()
                                                         .setId(UUID.fromString(uid))
                                                         .setValue(pdfDocument)));

        CaseData updatedCaseData1 = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        updatedCaseData1
            .setGeneralOrderDocument(singletonList(new Element<CaseDocument>()
                                                       .setId(UUID.fromString(uid1))
                                                       .setValue(pdfDocument)))
            .setDirectionOrderDocument(singletonList(new Element<CaseDocument>()
                                                         .setId(UUID.fromString(uid))
                                                         .setValue(pdfDocument)));

        CaseDetails caseDetails = new CaseDetailsBuilder().data(updatedCaseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDirectionOrderPDFDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(updatedCaseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(updatedCaseData1);

        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        handler.execute(mockExternalTask, externalTaskService);

        assertThat(updatedCaseData1.getDirectionOrderDocument().size())
            .isEqualTo(generalCaseData.getDirectionOrderDocument().size());
        assertThat(updatedCaseData1.getGeneralOrderDocument().size())
            .isEqualTo(generalCaseData.getGeneralOrderDocument().size());

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    @Test
    void shouldNotAddgaResponseDocument() {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        String uid1 = "f000aa01-0451-4000-b000-000000000111";

        CaseData updatedCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        updatedCaseData
            .setDirectionOrderDocument(singletonList(new Element<CaseDocument>()
                                                         .setId(UUID.fromString(uid))
                                                         .setValue(pdfDocument)));

        CaseData updatedCaseData1 = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        updatedCaseData1
            .setGaRespDocStaff(singletonList(new Element<Document>()
                                                 .setId(UUID.fromString(uid1))
                                                 .setValue(pdfDocument1)))
            .setGaRespDocClaimant(singletonList(new Element<Document>()
                                                    .setId(UUID.fromString(uid1))
                                                    .setValue(pdfDocument1)));

        CaseDetails caseDetails = new CaseDetailsBuilder().data(updatedCaseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDirectionResponseDocument(new CaseDataBuilder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(updatedCaseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(updatedCaseData1);

        when(mockExternalTask.getTopicName()).thenReturn("test");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        handler.execute(mockExternalTask, externalTaskService);

        assertThat(updatedCaseData1.getGaRespDocClaimant().size())
            .isEqualTo(generalCaseData.getGaRespDocument().size());
        assertThat(updatedCaseData1.getGaRespDocStaff().size())
            .isEqualTo(generalCaseData.getGaRespDocument().size());

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
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
        Element<?> same = new Element<CaseDocument>()
            .setId(UUID.randomUUID())
            .setValue(new CaseDocument().setDocumentLink(new Document().setDocumentUrl("string")));
        List<Element<?>> civilCaseDocumentList = new ArrayList<>();
        List<Element<?>> gaDocumentList = new ArrayList<>();
        gaDocumentList.add(same);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isNotPositive();
        civilCaseDocumentList.add(same);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(1);
    }

    @Test
    void checkIfDocumentExists_whenDocumentTypeIsDocumentClass() {
        Element<Document> documentElement = new Element<Document>()
            .setId(UUID.randomUUID())
            .setValue(new Document().setDocumentUrl("string"));
        List<Element<?>> gaDocumentList = new ArrayList<>();
        List<Element<?>> civilCaseDocumentList = new ArrayList<>();
        gaDocumentList.add(documentElement);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(0);
        civilCaseDocumentList.add(documentElement);
        assertThat(handler.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(1);
    }

    @Test
    void shouldMergeBundle() {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData gaCaseData = new CaseDataBuilder().atStateClaimDraft().build()
            .setGaAddlDocBundle(singletonList(new Element<CaseDocument>()
                                                  .setId(UUID.fromString(uid))
                                                  .setValue(pdfDocument)));

        assertThat(handler.mergeBundle(gaCaseData).getGaAddlDoc().size()).isEqualTo(1);

        List<Element<CaseDocument>> addlDoc = new ArrayList<>() {{
                add(new Element<CaseDocument>()
                        .setId(UUID.fromString(uid))
                        .setValue(pdfDocument));
            }};

        List<Element<CaseDocument>> addlDocBundle = new ArrayList<>() {{
                add(new Element<CaseDocument>()
                        .setId(UUID.fromString(uid))
                        .setValue(pdfDocument));
            }};

        CaseData nextGaCaseData = new CaseDataBuilder().atStateClaimDraft().build()
                .setGaAddlDoc(addlDoc)
                .setGaAddlDocBundle(addlDocBundle);

        assertThat(handler.mergeBundle(nextGaCaseData).getGaAddlDoc().size()).isEqualTo(2);
    }

    @Test
    void expectInvalidCaseDataExceptionOnToGaCaseData() {
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        when(caseDetailsConverter.toGACaseData(any())).thenThrow(NumberFormatException.class);

        InvalidCaseDataException exceptionThrown = assertThrows(InvalidCaseDataException.class, () -> {
            handler.handleTask(mockExternalTask);
        });

        String messageFromException = exceptionThrown.getMessage();
        System.out.println(messageFromException);
        assertTrue(messageFromException.contains("Conversion to long datatype failed for general application for a case "));
    }

    @Test
    void expectExceptionOnGaGetter() throws Exception {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseData gaCaseData = caseDataAtStateClaimDraftWithBusinessProcessStatusReady();
        gaCaseData.setDirectionOrderDocument(singletonList(new Element<CaseDocument>()
                                                               .setId(UUID.fromString(uid))
                                                               .setValue(pdfDocument)));
        Method gaGetter = ReflectionUtils.findMethod(
            CaseData.class,
            "get" + StringUtils.capitalize("gaRespondDoc")
        );
        gaGetter.setAccessible(false);
        CaseData finalGaCaseData = gaCaseData;
        CaseData.class.getDeclaredField("gaRespondDoc").setAccessible(false);
        finalGaCaseData.getClass().getDeclaredField("gaRespondDoc").setAccessible(false);
        finalGaCaseData.getClass().getMethod("getGaRespondDoc").setAccessible(false);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();
        Map<String, Object> output = new HashMap<>();
        assertThrows(
            Exception.class, () -> {
                when(ReflectionUtils.findMethod(
                    CaseData.class,
                    "get" + StringUtils.capitalize("GaRespondDoc")
                )).thenReturn(gaGetter);

                handler.updateDocCollection(
                    output, finalGaCaseData, "get" + StringUtils.capitalize("gaRespondDoc"),
                    caseData, "directionOrderDocStaff"
                );
            }
        );
    }

    public final CaseDocument pdfDocument = new CaseDocument()
        .setCreatedBy("John")
        .setDocumentName("documentName")
        .setDocumentSize(0L)
        .setDocumentType(GENERAL_ORDER)
        .setCreatedDatetime(now())
        .setDocumentLink(new Document()
                          .setDocumentUrl("fake-url")
                          .setDocumentFileName("file-name")
                          .setDocumentBinaryUrl("binary-url"));

    public final CaseDocument makeWithNoticeDocument = new CaseDocument()
        .setCreatedBy("John")
        .setDocumentName("documentName")
        .setDocumentSize(0L)
        .setDocumentType(SEND_APP_TO_OTHER_PARTY)
        .setCreatedDatetime(now())
        .setDocumentLink(new Document()
                          .setDocumentUrl("fake-url")
                          .setDocumentFileName("file-name")
                          .setDocumentBinaryUrl("binary-url"));

    public final Document pdfDocument1 = new Document()
        .setDocumentUrl("fake-url")
        .setDocumentFileName("file-name")
        .setDocumentBinaryUrl("binary-url");

    private static CaseData caseDataAtStateClaimDraftWithBusinessProcessStatusReady() {
        return new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .build();
    }
}
