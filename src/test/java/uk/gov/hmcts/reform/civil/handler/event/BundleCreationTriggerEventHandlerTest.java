package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.bundle.Bundle;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundleData;
import uk.gov.hmcts.reform.civil.model.bundle.BundleDetails;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.BUNDLE_CREATION_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_BUNDLE;

@ExtendWith(SpringExtension.class)
class BundleCreationTriggerEventHandlerTest {

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";

    @Mock
    private BundleCreationService bundleCreationService;
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    CaseDetailsConverter caseDetailsConverter;
    private BundleCreateResponse bundleCreateResponse;
    @InjectMocks
    private BundleCreationTriggerEventHandler bundleCreationTriggerEventHandler;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private Bundle bundle;

    @BeforeEach
    public void setup() {
        bundle = Bundle.builder().value(BundleDetails.builder().title("Trial Bundle").id("1")
                                            .stitchStatus("new")
                                            .stitchedDocument(null)
                                            .fileName("Trial Bundle.pdf")
                                            .description("This is trial bundle")
                                            .bundleHearingDate(LocalDate.of(2023, 12, 12))
                                            .stitchedDocument(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                                            .createdOn(LocalDateTime.of(2023, 11, 12, 1, 1, 1))
                                            .build()).build();
        List<Bundle> bundlesList = new ArrayList<>();
        bundlesList.add(bundle);
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = setupWitnessEvidenceDocs();
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = setupExpertEvidenceDocs();
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = setupOtherEvidenceDocs();
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = setupSystemGeneratedCaseDocs();
        ServedDocumentFiles servedDocumentFiles = setupParticularsOfClaimDocs();
        caseData = generateCaseData(witnessEvidenceDocs, expertEvidenceDocs, otherEvidenceDocs,
                                    systemGeneratedCaseDocuments, servedDocumentFiles);
        caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        bundleCreateResponse =
            BundleCreateResponse.builder().data(BundleData.builder().caseBundles(bundlesList).build()).build();
    }

    private CaseData generateCaseData(List<Element<UploadEvidenceWitness>> witnessEvidenceDocs,
                                      List<Element<UploadEvidenceExpert>> expertEvidenceDocs,
                                      List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs,
                                      List<Element<CaseDocument>> systemGeneratedCaseDocuments,
                                      ServedDocumentFiles servedDocumentFiles) {
        return CaseData.builder().ccdCaseReference(1L)
            .documentWitnessStatement(witnessEvidenceDocs)
            .documentWitnessSummary(witnessEvidenceDocs)
            .documentHearsayNotice(witnessEvidenceDocs)
            .documentReferredInStatement(otherEvidenceDocs)
            .documentWitnessStatementRes(witnessEvidenceDocs)
            .documentWitnessSummaryRes(witnessEvidenceDocs)
            .documentHearsayNoticeRes(witnessEvidenceDocs)
            .documentReferredInStatementRes(otherEvidenceDocs)
            .documentWitnessStatementRes2(witnessEvidenceDocs)
            .documentWitnessSummaryRes2(witnessEvidenceDocs)
            .documentHearsayNoticeRes2(witnessEvidenceDocs)
            .documentReferredInStatementRes2(otherEvidenceDocs)
            .documentExpertReport(expertEvidenceDocs)
            .documentJointStatement(expertEvidenceDocs)
            .documentAnswers(expertEvidenceDocs)
            .documentQuestions(expertEvidenceDocs)
            .documentExpertReportRes(expertEvidenceDocs)
            .documentJointStatementRes(expertEvidenceDocs)
            .documentAnswersRes(expertEvidenceDocs)
            .documentQuestionsRes(expertEvidenceDocs)
            .documentExpertReportRes2(expertEvidenceDocs)
            .documentJointStatementRes2(expertEvidenceDocs)
            .documentAnswersRes2(expertEvidenceDocs)
            .documentQuestionsRes2(expertEvidenceDocs)
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
            .servedDocumentFiles(servedDocumentFiles)
            .applicant1(Party.builder().partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("respondent1").type(Party.Type.INDIVIDUAL).build())
            .addApplicant2(YesOrNo.YES)
            .addRespondent2(YesOrNo.YES)
            .applicant2(Party.builder().partyName("applicant2").type(Party.Type.INDIVIDUAL).build())
            .respondent2(Party.builder().partyName("respondent2").type(Party.Type.INDIVIDUAL).build())
            .hearingDate(LocalDate.of(2023, 3, 12))
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .caseBundles(prepareCaseBundles())
            .build();
    }

    private List<Element<UploadEvidenceWitness>> setupWitnessEvidenceDocs() {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 TEST_URL)
                                                                                    .documentFileName(TEST_FILE_NAME).build()).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> setupExpertEvidenceDocs() {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build()).build()));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).build()).build()));
        return otherEvidenceDocs;
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).documentLink(Document.builder().documentUrl(
                TEST_URL).documentFileName(TEST_FILE_NAME).build()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQ =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQ));
        return systemGeneratedCaseDocuments;
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document = Document.builder().documentFileName(TEST_FILE_NAME).documentUrl(TEST_URL).build();
        particularsOfClaim.add(ElementUtils.element(document));
        return ServedDocumentFiles.builder().particularsOfClaimDocument(particularsOfClaim).build();
    }

    @Test
    void testSendBundleCreationTriggerDoesNotThrowExceptionWhenItsAllGood() {
        // Given: Case details with all type of documents require for bundles
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(1L);
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate(event.getCaseId().toString(), CREATE_BUNDLE))
            .thenReturn(StartEventResponse.builder().caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("event1").token("test").build());
        when(bundleCreationService.createBundle(event)).thenReturn(bundleCreateResponse);
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);

        // When: Bundle creation trigger is called
        // Then: Any Exception should not be thrown
        Assertions.assertDoesNotThrow(() -> bundleCreationTriggerEventHandler.sendBundleCreationTrigger(event));
    }

    @Test
    void testPrepareNewBundlePopulatesAllFields() {
        // When: I call the prepareNewBundle method
        IdValue<uk.gov.hmcts.reform.civil.model.Bundle> generatedBundle =
            bundleCreationTriggerEventHandler.prepareNewBundle(bundle, caseData);
        // Then: the bundleHearingDate, stitchedDocument, filename, title, description, stitchStatus, createdOn and id fields must be populated
        Assertions.assertEquals(bundle.getValue().getId(), generatedBundle.getId());
        Assertions.assertEquals(bundle.getValue().getStitchStatus(), generatedBundle.getValue().getStitchStatus().get());
        Assertions.assertEquals(
            bundle.getValue().getDescription(),
            generatedBundle.getValue().getDescription()
        );
        Assertions.assertEquals(bundle.getValue().getTitle(), generatedBundle.getValue().getTitle());
        Assertions.assertEquals(bundle.getValue().getFileName(), generatedBundle.getValue().getFileName());
        Assertions.assertEquals(bundle.getValue().getStitchedDocument().getDocumentFileName(),
                                generatedBundle.getValue().getStitchedDocument().get().getDocumentFileName());
        Assertions.assertEquals(caseData.getHearingDate(), generatedBundle.getValue().getBundleHearingDate().get());
        Assertions.assertNotNull(generatedBundle.getValue().getCreatedOn());
        Assertions.assertEquals("bundles", bundle.getValue().getStitchedDocument().getCategoryID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPrepareCaseContent() {
        // Given: a collection of case bundles and a valid startEventResponse
        StartEventResponse startEventResponse =
            StartEventResponse.builder().token("123").eventId("event1").caseDetails(caseDetails).build();
        List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1",
                                      uk.gov.hmcts.reform.civil.model.Bundle.builder()
                                          .title("Trial Bundle").fileName("TrialBundle.pdf")
                                          .stitchStatus(Optional.of("NEW")).build()));
        // When: I call the prepareCaseContent method
        CaseDataContent caseDataContent = bundleCreationTriggerEventHandler.prepareCaseContent(caseBundles,
                                                                                               startEventResponse);
        // Then: all fields are populated correctly
        Assertions.assertEquals("event1", caseDataContent.getEvent().getId());
        Assertions.assertEquals("123", caseDataContent.getEventToken());
        Object caseBundlesObj = (((HashMap<String, Object>)caseDataContent.getData()).get("caseBundles"));
        List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> caseBundlesList = (List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>>) caseBundlesObj;
        Assertions.assertEquals(caseBundles.get(0).getValue().getTitle(), caseBundlesList.get(0).getValue().getTitle()
                                );
    }

    private List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> prepareCaseBundles() {
        List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", uk.gov.hmcts.reform.civil.model.Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(LocalDateTime.now()))
            .bundleHearingDate(Optional.of(LocalDate.of(2023, 12, 12)))
            .build()));
        caseBundles.add(new IdValue<>("1", uk.gov.hmcts.reform.civil.model.Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(LocalDateTime.now()))
            .bundleHearingDate(Optional.of(LocalDate.of(2023, 1, 12)))
            .build()));
        return caseBundles;
    }

    @Test
    void shouldReturnBlankWhenDescriptionIsNull() {
        //Given : bundle with null description
        bundle.getValue().setDescription(null);
        // When: I call the prepareNewBundle method
        IdValue<uk.gov.hmcts.reform.civil.model.Bundle> generatedBundle =
            bundleCreationTriggerEventHandler.prepareNewBundle(bundle, caseData);
        // Then: the bundleHearingDate, stitchedDocument, filename, title, description, stitchStatus, createdOn and id fields must be populated
        Assertions.assertEquals(
            "", generatedBundle.getValue().getDescription()
        );
    }

    @Test
    void verifyBundleNotificationEventTriggeredWhenBundleCreated() {
        // Given: Case details with all type of documents require for bundles
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(1L);
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate(event.getCaseId().toString(), CREATE_BUNDLE))
            .thenReturn(StartEventResponse.builder().caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("event1").token("test").build());
        when(bundleCreationService.createBundle(event)).thenReturn(bundleCreateResponse);
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);

        // When: Bundle creation trigger is called
        // Then: BUNDLE_CREATION_NOTIFICATION Event should be triggered
        Assertions.assertDoesNotThrow(() -> bundleCreationTriggerEventHandler.sendBundleCreationTrigger(event));
        verify(coreCaseDataService, times(1)).triggerEvent(event.getCaseId(), BUNDLE_CREATION_NOTIFICATION);
    }

    @Test
    void verifyNoBundleNotificationEventTriggeredWhenBundleNotCreated() {
        // Given: Case details with all type of documents require for bundles and throws exception from
        // createBundle service
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(1L);
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate(event.getCaseId().toString(), CREATE_BUNDLE))
            .thenReturn(StartEventResponse.builder().caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("event1").token("test").build());
        when(bundleCreationService.createBundle(event)).thenThrow(new RuntimeException("Runtime Exception"));
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);

        // When: Bundle creation trigger is called
        // Then: BUNDLE_CREATION_NOTIFICATION Event should not be triggered
        verify(coreCaseDataService, times(0)).triggerEvent(event.getCaseId(), BUNDLE_CREATION_NOTIFICATION);
    }
}
