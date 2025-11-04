package uk.gov.hmcts.reform.civil.helpers.bundle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertBundleCreateRequestIsValid;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertCostsBudgets;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertDirectionsQuestionnaires;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertDisclosedDocuments;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertExpertEvidences;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertJointStatementOfExperts;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertOrdersDocuments;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertStatementsOfCaseDocuments;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertTrialDocumentFileNames;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertWitnessStatements;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseDataNoCategoryId;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseDataWithNoId;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.CostsBudgetsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.DQMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.DisclosedDocumentsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.ExpertEvidenceMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.JointExpertsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.OrdersMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.StatementsOfCaseMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.SystemGeneratedDocMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.TrialDocumentsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.WitnessStatementsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.util.FilenameGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BundleRequestMapperTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private FilenameGenerator filenameGenerator;

    private BundleRequestMapper bundleRequestMapper;

    @BeforeEach
    void setUp() {
        BundleRequestDocsOrganizer docsOrganizer = new BundleRequestDocsOrganizer();
        ConversionToBundleRequestDocs conversionDocs =
            new ConversionToBundleRequestDocs(featureToggleService, docsOrganizer);
        BundleDocumentsRetrieval documentsRetrieval =
            new BundleDocumentsRetrieval(conversionDocs, docsOrganizer);
        SystemGeneratedDocMapper sysGenDocMapper = new SystemGeneratedDocMapper();

        bundleRequestMapper = new BundleRequestMapper(
            new TrialDocumentsMapper(documentsRetrieval, conversionDocs),
            new StatementsOfCaseMapper(documentsRetrieval, conversionDocs, sysGenDocMapper),
            new WitnessStatementsMapper(documentsRetrieval, conversionDocs, docsOrganizer),
            new ExpertEvidenceMapper(documentsRetrieval),
            new DisclosedDocumentsMapper(documentsRetrieval, conversionDocs),
            new CostsBudgetsMapper(conversionDocs),
            new JointExpertsMapper(documentsRetrieval),
            new DQMapper(documentsRetrieval, featureToggleService, sysGenDocMapper),
            new OrdersMapper(sysGenDocMapper),
            filenameGenerator
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMapCaseDataToBundleCreateRequest(boolean caseProgressionEnabled) {
        // Given
        CaseData caseData = getCaseData();
        mockFeatureToggles(caseProgressionEnabled, false);

        // When
        BundleCreateRequest result = mapCaseData(caseData);

        // Then
        assertBundleCreateRequestIsValid(result);
        assertTrialDocumentFileNames(result);
        assertStatementsOfCaseDocuments(result);
        assertDirectionsQuestionnaires(caseProgressionEnabled, result);
        assertOrdersDocuments(result);
        assertWitnessStatements(result);
        assertExpertEvidences(result);
        assertJointStatementOfExperts(result);
        assertDisclosedDocuments(result);
        assertCostsBudgets(result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMapCaseDataWithAmendBundleEnabled(boolean caseProgressionEnabled) {
        // Given
        CaseData caseData = getCaseDataNoCategoryId();
        mockFeatureToggles(caseProgressionEnabled, true);

        // When
        BundleCreateRequest result = mapCaseData(caseData);

        // Then
        assertBundleCreateRequestIsValid(result);
        assertTrialDocumentFileNames(result);
        assertStatementsOfCaseDocuments(result);
        assertDirectionsQuestionnaires(featureToggleService.isCaseProgressionEnabled(), result);
        assertOrdersDocuments(result);
        assertWitnessStatements(result);
        assertExpertEvidences(result);
        assertJointStatementOfExperts(result);
        assertDisclosedDocuments(result);
        assertCostsBudgets(result);
    }

    @Test
    void shouldIncludeDefendantDefenceWhenOnlyInSystemGeneratedDocs() {
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(false);
        given(featureToggleService.isAmendBundleEnabled()).willReturn(false);

        CaseDocument defendantDefence = CaseDocument.builder()
            .documentType(DocumentType.DEFENDANT_DEFENCE)
            .createdBy("Defendant")
            .documentLink(Document.builder()
                             .documentUrl(TEST_URL)
                             .documentBinaryUrl(TEST_URL)
                             .documentFileName(TEST_FILE_NAME)
                             .build())
            .createdDatetime(LocalDateTime.of(2023, 2, 10, 2, 2, 2))
            .build();

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(12345L)
            .applicant1(Party.builder().type(Party.Type.COMPANY).partyName("Claimant Ltd").build())
            .respondent1(Party.builder().type(Party.Type.COMPANY).partyName("Defendant Ltd").build())
            .hearingDate(LocalDate.now())
            .submittedDate(LocalDateTime.of(2023, 2, 10, 2, 2, 2))
            .systemGeneratedCaseDocuments(ElementUtils.wrapElements(defendantDefence))
            .build();

        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(
            caseData,
            "sample.yaml",
            "JUR",
            "CASE"
        );

        assertTrue(
            bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments()
                .stream()
                .anyMatch(doc -> DocumentType.DEFENDANT_DEFENCE.name().equals(doc.getValue().getDocumentType())),
            "Defendant defence document should be included when only present in system generated docs"
        );
    }

    @Test
    void shouldHandleCaseDataWithNoCategoryId() {
        CaseData caseData = getCaseDataWithNoId();
        given(featureToggleService.isAmendBundleEnabled()).willReturn(false);

        BundleCreateRequest result = mapCaseData(caseData);

        assertBundleCreateRequestIsValid(result);
        assertDirectionsQuestionnaires(result);
    }

    @Test
    void shouldHandleCaseDataWithNoCategoryIdAndAmendEnabled() {
        CaseData caseData = getCaseDataWithNoId();
        given(featureToggleService.isAmendBundleEnabled()).willReturn(true);

        BundleCreateRequest result = mapCaseData(caseData);

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocsUnbundledFolderId() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDQDef1 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("ONE").categoryID("UnbundledFolder").build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQApp1 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("TWO").categoryID("UnbundledFolder").build())
                .createdDatetime(LocalDateTime.of(2023, 3, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef22 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("THREE").categoryID("UnbundledFolder").build())
                .createdDatetime(LocalDateTime.of(2023, 4, 11, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef21 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("FOUR").categoryID("UnbundledFolder").build())
                .createdDatetime(LocalDateTime.of(2023, 5, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef23 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("FIVE").build())
                .createdDatetime(LocalDateTime.of(2023, 6, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef22));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef21));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef23));
        return systemGeneratedCaseDocuments;
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).documentLink(Document.builder().documentUrl(
                TEST_URL).documentFileName(TEST_FILE_NAME).categoryID("detailsOfClaim").build()).createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                 2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQDef1 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF1_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                2, 2)).build();
        CaseDocument caseDocumentDQApp1 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).categoryID(DocCategory.APP1_DQ.getValue())
                        .documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef22 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 2, 11, 2,
                                2, 2)).build();
        CaseDocument caseDocumentDQDef21 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                2, 2)).build();
        CaseDocument caseDocumentDQDef23 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 3, 10, 2,
                                2, 2)).build();
        CaseDocument caseDocumentDQNoId =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("DQ_NO_CATEGORY_ID").build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQApp1LiP =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).categoryID(DocCategory.DQ_APP1.getValue())
                                  .documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 3, 11, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef1LiP =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).categoryID(DocCategory.DQ_DEF1.getValue())
                                  .documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 3, 12, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef22));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef21));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef23));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQNoId));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1LiP));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1LiP));
        CaseDocument caseDocumentDJ =
            CaseDocument.builder()
                .documentType(DocumentType.DEFAULT_JUDGMENT_SDO_ORDER)
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDJ));
        return systemGeneratedCaseDocuments;
    }

    @Test
    void shouldHandleEmptyCaseDataGracefully() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .applicant1(party("applicant1"))
            .respondent1(party("respondent1"))
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .build();

        BundleCreateRequest result = mapCaseData(caseData);

        assertBundleCreateRequestIsValid(result);
    }

    @Test
    void shouldHandleSingleApplicantAndRespondent() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .applicant1(party("applicant1"))
            .respondent1(party("respondent1"))
            .build();

        BundleCreateRequest result = mapCaseData(caseData);

        assertFalse(result.getCaseDetails().getCaseData().isHasApplicant2());
        assertFalse(result.getCaseDetails().getCaseData().isHasRespondant2());
    }

    private void mockFeatureToggles(boolean caseProgressionEnabled, boolean amendBundleEnabled) {
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(caseProgressionEnabled);
        given(featureToggleService.isAmendBundleEnabled()).willReturn(amendBundleEnabled);
    }

    private BundleCreateRequest mapCaseData(CaseData caseData) {
        return bundleRequestMapper.mapCaseDataToBundleCreateRequest(
            caseData, "sample.yaml", "test", "test"
        );
    }

    private static Party party(String name) {
        return Party.builder()
            .individualLastName("lastname")
            .partyName(name)
            .type(Party.Type.INDIVIDUAL)
            .build();
    }
}
