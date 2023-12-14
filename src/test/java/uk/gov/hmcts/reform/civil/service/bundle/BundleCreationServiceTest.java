package uk.gov.hmcts.reform.civil.service.bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.client.BundleApiClient;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleRequestMapper;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
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
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class BundleCreationServiceTest {

    @InjectMocks
    private BundleCreationService bundlingService;
    @Mock
    private BundleApiClient bundleApiClient;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private UserService userService;
    @Mock
    SystemUpdateUserConfiguration userConfig;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private BundleRequestMapper bundleRequestMapper;
    @Mock
    private ObjectMapper objectMapper;

    private final String testUrl = "url";
    private final String testFileName = "testFileName.pdf";
    CaseData caseData;

    @BeforeEach
    public void setUp() {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = setupWitnessEvidenceDocs();
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = setupExpertEvidenceDocs();
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = setupOtherEvidenceDocs();
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = setupSystemGeneratedCaseDocs();
        ServedDocumentFiles servedDocumentFiles = setupParticularsOfClaimDocs();

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
            .build();
    }

    private List<Element<UploadEvidenceWitness>> setupWitnessEvidenceDocs() {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 testUrl)
                                                                                    .documentFileName(testFileName).build()).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> setupExpertEvidenceDocs() {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(testUrl)
                                                                            .documentFileName(testFileName).build()).build()));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(testUrl)
                                                                           .documentFileName(testFileName).build()).build()));
        return otherEvidenceDocs;
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
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
        return systemGeneratedCaseDocuments;
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document = Document.builder().documentFileName(testFileName).documentUrl(testUrl).build();
        particularsOfClaim.add(ElementUtils.element(document));
        return ServedDocumentFiles.builder().particularsOfClaimDocument(particularsOfClaim).build();
    }

    @Test
    void testBundleApiClientIsInvoked() throws Exception {
        //Given: case details with all document type
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        given(bundleRequestMapper.mapCaseDataToBundleCreateRequest(any(), any(), any(), any(), any())).willReturn(null);
        given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(null);
        given(coreCaseDataService.getCase(1L)).willReturn(caseDetails);
        given(userConfig.getUserName()).willReturn("test");
        given(userConfig.getPassword()).willReturn("test");
        given(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).willReturn(caseData);
        given(authTokenGenerator.generate()).willReturn("test");
        given(userService.getAccessToken("test", "test")).willReturn("test");

        //When: bundlecreation service is called
        bundlingService.createBundle(new BundleCreationTriggerEvent(1L));

        //Then: BundleRest API should be called
        verify(bundleApiClient).createBundleServiceRequest(anyString(), anyString(), any());
    }

}
