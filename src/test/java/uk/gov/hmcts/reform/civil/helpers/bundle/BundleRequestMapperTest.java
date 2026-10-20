package uk.gov.hmcts.reform.civil.helpers.bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertBundleCreateRequestIsValid;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertCostsBudgets;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertDirectionsQuestionnaires;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.assertDirectionsQuestionnairesWithCategoryIds;
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

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BundleRequestMapperTest {

    @Mock
    private FilenameGenerator filenameGenerator;

    private BundleRequestMapper bundleRequestMapper;

    @BeforeEach
    void setUp() {
        BundleRequestDocsOrganizer docsOrganizer = new BundleRequestDocsOrganizer();
        ConversionToBundleRequestDocs conversionDocs =
            new ConversionToBundleRequestDocs(docsOrganizer);
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
            new DQMapper(documentsRetrieval, sysGenDocMapper),
            new OrdersMapper(sysGenDocMapper),
            filenameGenerator
        );
    }

    @Test
    void shouldMapCaseDataToBundleCreateRequest() {
        // Given
        CaseData caseData = getCaseDataNoCategoryId();

        // When
        BundleCreateRequest result = mapCaseData(caseData);

        // Then
        assertBundleCreateRequestIsValid(result);
        assertTrialDocumentFileNames(result);
        assertStatementsOfCaseDocuments(result);
        assertDirectionsQuestionnairesWithCategoryIds(result);
        assertOrdersDocuments(result);
        assertWitnessStatements(result);
        assertExpertEvidences(result);
        assertJointStatementOfExperts(result);
        assertDisclosedDocuments(result);
        assertCostsBudgets(result);
    }

    @Test
    void shouldFilterDocumentsWithoutCategoryIdWhenMappingBundleDocuments() {
        // Given
        CaseData caseData = getCaseData();

        // When
        BundleCreateRequest result = mapCaseData(caseData);

        // Then
        assertBundleCreateRequestIsValid(result);
        assertThat(result.getCaseDetails().getCaseData().getClaimant1WitnessStatements())
            .extracting(element -> element.getValue().getDocumentFileName())
            .contains("Email referred to in the statement of witness 12/12/2022")
            .doesNotContain("CL 1 - Statement 10/02/2023");
    }

    @Test
    void shouldHandleCaseDataWithNoCategoryId() {
        CaseData caseData = getCaseDataWithNoId();

        BundleCreateRequest result = mapCaseData(caseData);

        assertBundleCreateRequestIsValid(result);
        assertDirectionsQuestionnaires(result);
    }

    @Test
    void shouldHandleEmptyCaseDataGracefully() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .applicant1(party("applicant1"))
            .respondent1(party("respondent1"))
            .hearingDate(LocalDate.now())
            .hearingLocation(new DynamicList().setValue(new DynamicListElement().setLabel("County Court")))
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
            .hearingLocation(new DynamicList().setValue(new DynamicListElement().setLabel("County Court")))
            .applicant1(party("applicant1"))
            .respondent1(party("respondent1"))
            .build();

        BundleCreateRequest result = mapCaseData(caseData);

        assertFalse(result.getCaseDetails().getCaseData().isHasApplicant2());
        assertFalse(result.getCaseDetails().getCaseData().isHasRespondant2());
    }

    private BundleCreateRequest mapCaseData(CaseData caseData) {
        return bundleRequestMapper.mapCaseDataToBundleCreateRequest(
            caseData, "sample.yaml", "test", "test"
        );
    }

    private static Party party(String name) {
        return new Party()
            .setIndividualLastName("lastname")
            .setPartyName(name)
            .setType(Party.Type.INDIVIDUAL);
    }
}
