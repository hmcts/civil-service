package uk.gov.hmcts.reform.civil.helpers.bundle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.CostsBudgetsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.DQMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.DisclosedDocumentsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.ExpertEvidenceMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.JointExpertsMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.OrdersMapper;
import uk.gov.hmcts.reform.civil.helpers.bundle.mappers.StatementsOfCaseMapper;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BundleRequestMapperTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private TrialDocumentsMapper trialDocumentsMapper;

    @Mock
    private StatementsOfCaseMapper statementsOfCaseMapper;

    @Mock
    private WitnessStatementsMapper witnessStatementsMapper;

    @Mock
    private ExpertEvidenceMapper expertEvidenceMapper;

    @Mock
    private DisclosedDocumentsMapper disclosedDocumentsMapper;

    @Mock
    private CostsBudgetsMapper costsBudgetsMapper;

    @Mock
    private JointExpertsMapper jointExpertsMapper;

    @Mock
    private DQMapper dqMapper;

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private FilenameGenerator filenameGenerator;

    @InjectMocks
    private BundleRequestMapper bundleRequestMapper;

    @BeforeEach
    void setUp() {
        BundleRequestDocsOrganizer bundleRequestDocsOrganizer = new BundleRequestDocsOrganizer();
        ConversionToBundleRequestDocs conversionToBundleRequestDocs = new ConversionToBundleRequestDocs(
            featureToggleService, bundleRequestDocsOrganizer);
    }

    @Test
    void testBundleCreateRequestMapperForEmptyDetailsAndCaseEventEnable() {
        // Given
        CaseData caseData = CaseData.builder().ccdCaseReference(1L)
            .applicant1(Party.builder().individualLastName("lastname").partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualLastName("lastname").partyName("respondent1").type(Party.Type.INDIVIDUAL).build())
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .build();

        // When
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
                                                                                                           ".yaml",
                                                                                                       "test", "test"
        );
        // Then
        assertNotNull(bundleCreateRequest);
    }

    @Test
    void testBundleCreateRequestMapperForOneRespondentAndOneApplicant() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L)
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1(Party.builder().individualLastName("lastname").partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualLastName("lastname").partyName("respondent1").type(Party.Type.INDIVIDUAL).build())
            .build();

        // When: mapCaseDataToBundleCreateRequest is called
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
                                                                                                           ".yaml",
                                                                                                       "test", "test"
        );
        // Then: hasApplicant2 and hasRespondant2 should return false
        assertFalse(bundleCreateRequest.getCaseDetails().getCaseData().isHasApplicant2());
        assertFalse(bundleCreateRequest.getCaseDetails().getCaseData().isHasRespondant2());
    }

    @Test
    void testBundleCreateRequestMapperForOneRespondentAndOneApplicantAndCaseEventEnable() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L)
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1(Party.builder().individualLastName("lastname").partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualLastName("lastname").partyName("respondent1").type(Party.Type.INDIVIDUAL).build())
            .build();

        // When: mapCaseDataToBundleCreateRequest is called
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
                                                                                                           ".yaml",
                                                                                                       "test", "test"
        );
        // Then: hasApplicant2 and hasRespondant2 should return false
        assertFalse(bundleCreateRequest.getCaseDetails().getCaseData().isHasApplicant2());
        assertFalse(bundleCreateRequest.getCaseDetails().getCaseData().isHasRespondant2());
    }
}
