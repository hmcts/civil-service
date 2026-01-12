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
        return new BundleCreateRequest()
            .setCaseDetails(new BundlingCaseDetails()
                                .setCaseData(mapCaseData(
                                    caseData,
                                    bundleConfigFileName
                                ))
                                .setFilenamePrefix(fileNameIdentifier)
            )
            .setCaseTypeId(caseTypeId)
            .setJurisdictionId(jurisdiction);
    }

    private BundlingCaseData mapCaseData(CaseData caseData, String bundleConfigFileName) {

        BundlingCaseData bundlingCaseData =
            new BundlingCaseData()
                .setId(caseData.getCcdCaseReference())
                .setBundleConfiguration(bundleConfigFileName)
                .setTrialDocuments(trialDocumentsMapper.map(caseData))
                .setStatementsOfCaseDocuments(statementsOfCaseMapper.map(caseData))
                .setDirectionsQuestionnaires(dqMapper.map(caseData))
                .setOrdersDocuments(ordersMapper.map(caseData))
                .setClaimant1WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.CLAIMANT1))
                .setClaimant2WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.CLAIMANT2))
                .setDefendant1WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.DEFENDANT1))
                .setDefendant2WitnessStatements(witnessStatementsMapper.map(caseData, PartyType.DEFENDANT2))
                .setClaimant1ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.CLAIMANT1))
                .setClaimant2ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.CLAIMANT2))
                .setDefendant1ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.DEFENDANT1))
                .setDefendant2ExpertEvidence(expertEvidenceMapper.map(caseData, PartyType.DEFENDANT2))
                .setJointStatementOfExperts(jointExpertsMapper.map(caseData))
                .setClaimant1DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.CLAIMANT1))
                .setClaimant2DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.CLAIMANT2))
                .setDefendant1DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.DEFENDANT1))
                .setDefendant2DisclosedDocuments(disclosedDocumentsMapper.map(caseData, PartyType.DEFENDANT2))
                .setClaimant1CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.CLAIMANT1))
                .setClaimant2CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.CLAIMANT2))
                .setDefendant1CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.DEFENDANT1))
                .setDefendant2CostsBudgets(costsBudgetsMapper.map(caseData, PartyType.DEFENDANT2))
                .setApplicant1(caseData.getApplicant1())
                .setRespondent1(caseData.getRespondent1())
                .setHearingDate(null != caseData.getHearingDate()
                                    ? DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "dd-MM-yyyy") : null)
                .setCcdCaseReference(caseData.getCcdCaseReference());
        return mapRespondent2Applicant2Details(bundlingCaseData, caseData);
    }

    private BundlingCaseData mapRespondent2Applicant2Details(BundlingCaseData bundlingCaseData, CaseData caseData) {
        if (null != caseData.getAddApplicant2() && YesOrNo.YES.equals(caseData.getAddApplicant2())) {
            bundlingCaseData.setHasApplicant2(true);
        }
        if (null != caseData.getAddRespondent2() && YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            bundlingCaseData.setHasRespondant2(true);
        }
        if (null != caseData.getApplicant2()) {
            bundlingCaseData.setApplicant2(caseData.getApplicant2());
        }
        if (null != caseData.getRespondent2()) {
            bundlingCaseData.setRespondent2(caseData.getRespondent2());
        }
        return bundlingCaseData;
    }
}
