package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.QueryDocumentGenerator;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.QUERY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class GenerateQueryDocumentHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private GenerateQueryDocumentHandler handler;
    @Mock
    private QueryManagementCamundaService camundaService;
    @Mock
    private QueryDocumentGenerator queryDocumentGenerator;
    @Mock
    private SecuredDocumentManagementService documentManagementService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        handler = new GenerateQueryDocumentHandler(
            camundaService,
            queryDocumentGenerator,
            documentManagementService,
            mapper
        );
    }

    private static LocalDateTime DATE_QUERY_RAISED = LocalDateTime.now();
    private static Long CASE_ID = 1L;
    private static String QUERY_ID = "query-id";
    private static String PARENT_QUERY_ID = "parent-id";
    private static String PROCESS_INSTANCE_ID = "instance-id";

    private static final String queryDocumentFilename = String.format(
        QUERY_DOCUMENT.getDocumentTitle(), "Query 01");

    private static final CaseDocument INITIAL_QUERY_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(queryDocumentFilename)
        .documentType(DocumentType.QUERY_DOCUMENT)
        .createdDatetime(DATE_QUERY_RAISED)
        .build().toBuilder()
        .documentLink(Document.builder().documentBinaryUrl(
                "http://dm-store:4506/documents/73526424-8434-4b1f-aaaa-bd33a3f8338f/binary")
                          .documentUrl("http://dm-store:4506/documents/73526424-8434-4b1f-aaaa-bd33a3f8338f")
                          .build())
        .build();

    private static final CaseDocument OTHER_QUERY_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(String.format(QUERY_DOCUMENT.getDocumentTitle(), "Other query doc"))
        .documentType(DocumentType.QUERY_DOCUMENT)
        .createdDatetime(DATE_QUERY_RAISED.minusDays(2))
        .build().toBuilder()
        .documentLink(Document.builder().documentBinaryUrl(
                "http://dm-store:4506/documents/73526424-8434-4b1f-bbbb-bd33a3f8338f/binary")
                          .documentUrl("http://dm-store:4506/documents/73526424-8434-4b1f-bbbb-bd33a3f8338f")
                          .build())
        .build();

    @Test
    void shouldPopulateCamundaProcessVars_andPopulateQueryDocuments_whenNoExistingQueryDocExists() {
        CaseQueriesCollection queriesCollection = buildQueries("App Sol");
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .qmApplicantSolicitorQueries(queriesCollection)
            .queryDocuments(new ArrayList<>() {{
                    add(element(OTHER_QUERY_DOCUMENT));
                }
            })
            .build();

        QueryManagementVariables inputVariables = QueryManagementVariables.builder()
            .queryId(PARENT_QUERY_ID)
            .build();

        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        when(queryDocumentGenerator.generate(
            eq(CASE_ID),
            eq(queriesCollection.messageThread(PARENT_QUERY_ID)),
            anyString(),
            eq(DocCategory.CLAIMANT_QUERY_DOCUMENTS)
        )).thenReturn(INITIAL_QUERY_DOCUMENT);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(camundaService).setProcessVariables(
            PROCESS_INSTANCE_ID,
            QueryManagementVariables.builder()
                .removeDocument(false)
                .build()
        );

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getQueryDocuments()).hasSize(2);
        assertThat(updatedData.getQueryDocuments().get(0).getValue().getDocumentLink().getDocumentBinaryUrl())
            .isEqualTo(OTHER_QUERY_DOCUMENT.getDocumentLink().getDocumentBinaryUrl());
        assertThat(updatedData.getQueryDocuments().get(1).getValue().getDocumentLink().getDocumentBinaryUrl())
            .isEqualTo(INITIAL_QUERY_DOCUMENT.getDocumentLink().getDocumentBinaryUrl());
    }

    @Test
    void shouldPopulateCamundaProcessVars_andReplaceExistingQueryDocument_whenDocumentExistsForQueryThread() {
        CaseQueriesCollection queriesCollection = buildQueriesWithResponse("App Sol");
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .queryDocuments(new ArrayList<>() {{
                    add(element(INITIAL_QUERY_DOCUMENT));
                    add(element(OTHER_QUERY_DOCUMENT));
                }})
            .qmApplicantSolicitorQueries(queriesCollection)
            .build();

        QueryManagementVariables inputVariables = QueryManagementVariables.builder()
            .queryId(QUERY_ID)
            .build();

        CaseDocument updatedQueryDocument = CaseDocumentBuilder.builder()
            .documentName(queryDocumentFilename)
            .documentType(DocumentType.QUERY_DOCUMENT)
            .createdDatetime(DATE_QUERY_RAISED)
            .build().toBuilder()
            .documentLink(Document.builder().documentBinaryUrl(
                    "http://dm-store:4506/documents/73526424-8434-4b1f-cccc-bd33a3f8338f/binary")
                              .documentUrl("http://dm-store:4506/documents/73526424-8434-4b1f-cccc-bd33a3f8338f")
                              .build())
            .build();

        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        when(queryDocumentGenerator.generate(
            eq(CASE_ID),
            eq(queriesCollection.messageThread(PARENT_QUERY_ID)),
            anyString(),
            eq(DocCategory.CLAIMANT_QUERY_DOCUMENTS)
        )).thenReturn(updatedQueryDocument);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(camundaService).setProcessVariables(
            PROCESS_INSTANCE_ID,
            QueryManagementVariables.builder()
                .removeDocument(true)
                .documentToRemoveId("73526424-8434-4b1f-aaaa-bd33a3f8338f")
                .build()
        );

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getQueryDocuments()).hasSize(2);
        assertThat(updatedData.getQueryDocuments().get(0).getValue().getDocumentLink().getDocumentBinaryUrl())
            .isEqualTo(OTHER_QUERY_DOCUMENT.getDocumentLink().getDocumentBinaryUrl());
        assertThat(updatedData.getQueryDocuments().get(1).getValue().getDocumentLink().getDocumentBinaryUrl())
            .isEqualTo(updatedQueryDocument.getDocumentLink().getDocumentBinaryUrl());

    }

    private CaseQueriesCollection buildQueries(String partyName) {
        return CaseQueriesCollection.builder()
            .partyName(partyName)
            .caseMessages(
                List.of(
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id(PARENT_QUERY_ID)
                                .isHearingRelated(YES)
                                .createdOn(DATE_QUERY_RAISED)
                                .build()).build()
                ))
            .build();
    }

    private CaseQueriesCollection buildQueriesWithResponse(String partyName) {
        return CaseQueriesCollection.builder()
            .partyName(partyName)
            .caseMessages(
                List.of(
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id(PARENT_QUERY_ID)
                                .isHearingRelated(YES)
                                .subject("Query")
                                .createdOn(DATE_QUERY_RAISED)
                                .build()).build(),
                    Element.<CaseMessage>builder()
                        .id(UUID.randomUUID())
                        .value(
                            CaseMessage.builder()
                                .id(QUERY_ID)
                                .subject("Response")
                                .parentId(PARENT_QUERY_ID)
                                .isHearingRelated(YES)
                                .createdOn(DATE_QUERY_RAISED.plusDays(1))
                                .build()).build()
                ))
            .build();
    }

}
