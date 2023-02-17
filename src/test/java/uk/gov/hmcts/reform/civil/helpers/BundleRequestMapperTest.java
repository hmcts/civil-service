package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
public class BundleRequestMapperTest {

    @InjectMocks
    private BundleRequestMapper bundleRequestMapper;
    private final String testUrl = "url";
    private final String testFileName = "testFileName.pdf";

    @Test
    public void testBundleRequestMapperWithAllDocs() {
        //Create document with type UploadEvidenceWitness
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = getWitnessDocs();
        //Create document with type UploadEvidenceExpert
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = getExpertDocs();
        //Create document with type UploadEvidenceDocumentType
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = setupOtherEvidenceDocs();
        //Create system generated Doc
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = setupSystemGeneratedCaseDocs();
        //Create servedDocument files
        ServedDocumentFiles servedDocumentFiles = setupParticularsOfClaimDocs();

        //Add all type of documents and other request details in case data
        CaseData caseData = getCaseData(witnessEvidenceDocs, expertEvidenceDocs, otherEvidenceDocs,
                                        systemGeneratedCaseDocuments, servedDocumentFiles);
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
            ".yaml", "test", "test", 1L
        );
        assertNotNull(bundleCreateRequest);
        assertEquals(bundleCreateRequest.getCaseDetails().getCaseData().getDocumentWitnessStatement().get(0).getValue().getDocumentFileName(),
                     "Witness Statement_FirstName LastName_10022023");
    }

    private CaseData getCaseData(List<Element<UploadEvidenceWitness>> witnessEvidenceDocs,
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
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .build();
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document = Document.builder().documentFileName(testFileName).documentUrl(testUrl).build();
        particularsOfClaim.add(ElementUtils.element(document));
        ServedDocumentFiles servedDocumentFiles =
            ServedDocumentFiles.builder().particularsOfClaimDocument(particularsOfClaim).build();
        return servedDocumentFiles;
    }

    private List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(testUrl)
                                                                           .documentFileName(testFileName).build()).build()));
        return otherEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs() {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(testUrl)
                                                                            .documentFileName(testFileName).build()).build()));

        return  expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs() {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(testUrl)
                                                                                    .documentFileName(testFileName).build())
                                                         .witnessOptionName("FirstName LastName")
                                                         .witnessOptionUploadDate(LocalDate.of(2023, 02, 10)).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).documentLink(Document.builder().documentUrl(testUrl).documentFileName(testFileName).build()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQ =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(testUrl).documentFileName(testFileName).build()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQ));
        return systemGeneratedCaseDocuments;
    }

    @Test
    public void testBundleCreateRequestMapperForEmptyDetails() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L)
            .applicant1(Party.builder().partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("respondent1").type(Party.Type.INDIVIDUAL).build()).hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .build();
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
                                                                                                           ".yaml",
                                                                                                       "test", "test",
                                                                                                       1L
        );
        assertNotNull(bundleCreateRequest);
    }
}
