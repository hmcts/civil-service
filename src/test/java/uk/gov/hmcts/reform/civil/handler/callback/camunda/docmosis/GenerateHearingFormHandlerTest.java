package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.hearing.HearingFormGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_FORM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class GenerateHearingFormHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateHearingFormHandler handler;
    @Mock
    private HearingFormGenerator hearingFormGenerator;
    @Mock
    private FeatureToggleService featureToggleService;
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateHearingFormHandler(hearingFormGenerator, mapper, featureToggleService);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldGenerateForm_when1v1() {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName("document name");
        document.setDocumentSize(0L);
        document.setDocumentType(HEARING_FORM);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2SameLegalRepresentative(YES);
        BusinessProcess businessProcess =  new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(1);
    }

    @Test
    void shouldGenerateForm_when1v1ButHideIt_IfClaimantIsWelsh() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName("document name");
        document.setDocumentSize(0L);
        document.setDocumentType(HEARING_FORM);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setInformation("test");
        caseData.setClaimantBilingualLanguagePreference("WELSH");
        BusinessProcess businessProcess =  new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(0);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
    }

    @Test
    void shouldGenerate2Forms_whenListHave1PreviousForm() {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName("document name");
        document.setDocumentSize(0L);
        document.setDocumentType(HEARING_FORM);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(document));
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2SameLegalRepresentative(YES);
        caseData.setHearingDocuments(systemGeneratedCaseDocuments);
        BusinessProcess businessProcess =  new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(2);
    }
}
