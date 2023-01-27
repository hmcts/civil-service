package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.bundle.Bundle;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundleData;
import uk.gov.hmcts.reform.civil.model.bundle.BundleDetails;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingInformation;
import uk.gov.hmcts.reform.civil.model.bundle.DocumentLink;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_BUNDLE;

@ExtendWith(SpringExtension.class)
public class BundleCreationTriggerEventHandlerTest {

    @Mock
    private BundleCreationService bundleCreationService;
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    CaseDetailsConverter caseDetailsConverter;
    private BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(1L);
    private BundleCreateResponse bundleCreateResponse;
    @InjectMocks
    private BundleCreationTriggerEventHandler bundleCreationTriggerEventHandler;
    private final String testUrl = "url";
    private final String testFileName = "testFileName.pdf";
    CaseData caseData;
    CaseDetails caseDetails;

    @BeforeEach
    public void setup() {
        List<Bundle> bundleList = new ArrayList<>();
        bundleList.add(Bundle.builder().value(BundleDetails.builder().stitchedDocument(DocumentLink.builder().build())
                                                  .stitchStatus("New").build()).build());
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 testUrl)
                                                                                    .documentFileName(testFileName).build()).build()));
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(testUrl)
                                                                            .documentFileName(testFileName).build()).build()));
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(testUrl)
                                                                           .documentFileName(testFileName).build()).build()));
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).documentLink(Document.builder().documentUrl(
                testUrl).documentFileName(testFileName).build()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQ =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(testUrl).documentFileName(testFileName).build()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQ));

        List<Element<Document>> particulersOfClaim = new ArrayList<>();
        Document document = Document.builder().documentFileName(testFileName).documentUrl(testUrl).build();
        particulersOfClaim.add(ElementUtils.element(document));
        ServedDocumentFiles servedDocumentFiles =
            ServedDocumentFiles.builder().particularsOfClaimDocument(particulersOfClaim).build();
        caseData = CaseData.builder().ccdCaseReference(1L)
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
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .bundleInformation(BundlingInformation.builder().historicalBundles(bundleList).build())
            .build();
        caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        bundleCreateResponse =
            BundleCreateResponse.builder().data(BundleData.builder().caseBundles(bundleList).hearingDate("2023-12-20").bundleConfiguration("test").build()).build();
    }

    @Test
    public void testSendBundleCreationTrigger() throws Exception {
        when(coreCaseDataService.getCase(1L)).thenReturn(caseDetails);
        when(bundleCreationService.createBundle(event)).thenReturn(bundleCreateResponse);
        when(coreCaseDataService.startUpdate(event.getCaseId().toString(), CREATE_BUNDLE))
            .thenReturn(StartEventResponse.builder().caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("event1").token("test").build());
        when(caseDetailsConverter.toCaseData(anyMap())).thenReturn(caseData);
        bundleCreationTriggerEventHandler.sendBundleCreationTrigger(event);
    }
}
