package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

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
    void setup() {
        BundleDetails bundleDetails = new BundleDetails()
            .setId("1")
            .setTitle("Trial Bundle")
            .setDescription("This is trial bundle")
            .setStitchStatus("new")
            .setStitchedDocument(new Document().setDocumentUrl(TEST_URL).setDocumentFileName(TEST_FILE_NAME))
            .setStitchingFailureMessage(null)
            .setFileName("Trial Bundle.pdf")
            .setCreatedOn(LocalDateTime.of(2023, 11, 12, 1, 1, 1))
            .setBundleHearingDate(LocalDate.of(2023, 12, 12));
        bundle = new Bundle(bundleDetails);
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
        BundleData bundleData = new BundleData(bundlesList);
        bundleCreateResponse = new BundleCreateResponse(bundleData, null);
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
        witnessEvidenceDocs.add(ElementUtils.element(new UploadEvidenceWitness()
                                                         .setWitnessOptionDocument(new Document().setDocumentBinaryUrl(
                                                             TEST_URL)
                                                                                         .setDocumentFileName(TEST_FILE_NAME))));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> setupExpertEvidenceDocs() {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(new UploadEvidenceExpert()
                                                        .setExpertDocument(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                                .setDocumentFileName(TEST_FILE_NAME))));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(new UploadEvidenceDocumentType()
                                                       .setDocumentUpload(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                           .setDocumentFileName(TEST_FILE_NAME))));
        return otherEvidenceDocs;
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            new CaseDocument().setDocumentType(DocumentType.SEALED_CLAIM).setDocumentLink(new Document().setDocumentUrl(
                TEST_URL).setDocumentFileName(TEST_FILE_NAME));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQ =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setDocumentFileName(TEST_FILE_NAME));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQ));
        return systemGeneratedCaseDocuments;
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document = new Document().setDocumentFileName(TEST_FILE_NAME).setDocumentUrl(TEST_URL);
        particularsOfClaim.add(ElementUtils.element(document));
        return new ServedDocumentFiles().setParticularsOfClaimDocument(particularsOfClaim);
    }

    @Test
    void testSendBundleCreationTriggerDoesNotThrowExceptionWhenItsAllGood() {
        // Given: Case details with all type of documents require for bundles
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(1L, ACCESS_TOKEN);
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        StartEventResponse response = StartEventResponse.builder()
            .caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("event1").token("test").build();
        when(coreCaseDataService.startUpdate(event.getCaseId().toString(), CREATE_BUNDLE)).thenReturn(response);
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
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPrepareCaseContent() {
        // Given: a collection of case bundles and a valid startEventResponse
        StartEventResponse startEventResponse =
            StartEventResponse.builder().token("123").eventId("event1").caseDetails(caseDetails).build();
        List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1",
                                      new uk.gov.hmcts.reform.civil.model.Bundle()
                                          .setTitle("Trial Bundle").setFileName("TrialBundle.pdf")
                                          .setStitchStatus(Optional.of("NEW"))));
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
        caseBundles.add(new IdValue<>("1", new uk.gov.hmcts.reform.civil.model.Bundle().setId("1")
            .setTitle("Trial Bundle")
            .setStitchStatus(Optional.of("NEW")).setDescription("Trial Bundle")
            .setCreatedOn(Optional.of(LocalDateTime.now()))
            .setBundleHearingDate(Optional.of(LocalDate.of(2023, 12, 12)))));
        caseBundles.add(new IdValue<>("1", new uk.gov.hmcts.reform.civil.model.Bundle().setId("1")
            .setTitle("Trial Bundle")
            .setStitchStatus(Optional.of("NEW")).setDescription("Trial Bundle")
            .setCreatedOn(Optional.of(LocalDateTime.now()))
            .setBundleHearingDate(Optional.of(LocalDate.of(2023, 1, 12)))));
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
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(1L, ACCESS_TOKEN);
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        StartEventResponse response = StartEventResponse.builder()
            .caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("event1").token("test").build();
        when(coreCaseDataService.startUpdate(event.getCaseId().toString(), CREATE_BUNDLE)).thenReturn(response);
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
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(1L, ACCESS_TOKEN);
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        StartEventResponse response = StartEventResponse.builder()
            .caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("event1").token("test").build();
        when(coreCaseDataService.startUpdate(event.getCaseId().toString(), CREATE_BUNDLE)).thenReturn(response);
        when(bundleCreationService.createBundle(event)).thenThrow(new RuntimeException("Runtime Exception"));
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);

        // When: Bundle creation trigger is called
        // Then: BUNDLE_CREATION_NOTIFICATION Event should not be triggered
        verify(coreCaseDataService, times(0)).triggerEvent(event.getCaseId(), BUNDLE_CREATION_NOTIFICATION);
    }
}
