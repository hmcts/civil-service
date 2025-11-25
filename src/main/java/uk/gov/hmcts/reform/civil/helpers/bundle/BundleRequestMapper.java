package uk.gov.hmcts.reform.civil.helpers.bundle;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
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
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestMapper {

    private final TrialDocumentsMapper trialDocumentsMapper;
    private final StatementsOfCaseMapper statementsOfCaseMapper;
    private final WitnessStatementsMapper witnessStatementsMapper;
    private final ExpertEvidenceMapper expertEvidenceMapper;
    private final DisclosedDocumentsMapper disclosedDocumentsMapper;
    private final CostsBudgetsMapper costsBudgetsMapper;
    private final JointExpertsMapper jointExpertsMapper;
    private final DQMapper dqMapper;
    private final OrdersMapper ordersMapper;
    private final FilenameGenerator filenameGenerator;

    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData,
                                                                String bundleConfigFileName, String jurisdiction,
                                                                String caseTypeId) {
        log.info("Mapping case data to BundleCreateRequest for case ID: {}", caseData.getCcdCaseReference());
        String fileNameIdentifier = filenameGenerator.generateBundleFilenamePrefix(caseData);
        return BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                             .caseData(mapCaseData(
                                 caseData,
                                 bundleConfigFileName
                             ))
                             .filenamePrefix(fileNameIdentifier)
                             .build()
            )
            .caseTypeId(caseTypeId)
            .jurisdictionId(jurisdiction).build();
    }

    private BundlingCaseData mapCaseData(CaseData caseData, String bundleConfigFileName) {

        BundlingCaseData bundlingCaseData =
            BundlingCaseData.builder().id(caseData.getCcdCaseReference()).bundleConfiguration(
                    bundleConfigFileName)
                .trialDocuments(trialDocumentsMapper.map(caseData))
                .statementsOfCaseDocuments(statementsOfCaseMapper.map(caseData))
                .directionsQuestionnaires(dqMapper.map(caseData))
                .ordersDocuments(ordersMapper.map(caseData))
                .claimant1WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.CLAIMANT1))
                .claimant2WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.CLAIMANT2))
                .defendant1WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.DEFENDANT1))
                .defendant2WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.DEFENDANT2))
                .claimant1ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.CLAIMANT1))
                .claimant2ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.CLAIMANT2))
                .defendant1ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.DEFENDANT1))
                .defendant2ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.DEFENDANT2))
                .jointStatementOfExperts(jointExpertsMapper.map(caseData))
                .claimant1DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.CLAIMANT1))
                .claimant2DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.CLAIMANT2))
                .defendant1DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.DEFENDANT1))
                .defendant2DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.DEFENDANT2))
                .claimant1CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.CLAIMANT1))
                .claimant2CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.CLAIMANT2))
                .defendant1CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.DEFENDANT1))
                .defendant2CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.DEFENDANT2))
                .applicant1(caseData.getApplicant1())
                .respondent1(caseData.getRespondent1())
                .hearingDate(null != caseData.getHearingDate()
                                 ? DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "dd-MM-yyyy") : null)
                .ccdCaseReference(caseData.getCcdCaseReference())
                .build();
        return mapRespondent2Applicant2Details(bundlingCaseData, caseData);
    }

    private BundlingCaseData mapRespondent2Applicant2Details(BundlingCaseData bundlingCaseData, CaseData caseData) {
        if (null != caseData.getAddApplicant2() && YesOrNo.YES.equals(caseData.getAddApplicant2())) {
            bundlingCaseData.toBuilder().hasApplicant2(true);
        }
        if (null != caseData.getAddRespondent2() && YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            bundlingCaseData.toBuilder().hasRespondant2(true);
        }
        if (null != caseData.getApplicant2()) {
            bundlingCaseData.toBuilder().applicant2(caseData.getApplicant2());
        }
        if (null != caseData.getRespondent2()) {
            bundlingCaseData.toBuilder().respondent2(caseData.getRespondent2());
        }
        return bundlingCaseData;
    }
}
