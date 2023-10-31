package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.bundle.DocumentLink;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import java.time.LocalDate;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getExpertDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getWitnessDocsByPartyAndDocType;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestMapper {

    private static final String DOC_FILE_NAME = "DOC_FILE_NAME";
    private static final String DOC_FILE_NAME_WITH_DATE = "DOC_FILE_NAME %s";
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData,
                                                                String bundleConfigFileName, String jurisdiction,
                                                                String caseTypeId, Long id) {
        String fileNameIdentifier = generateFileName(caseData);
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

    private String generateFileName(CaseData caseData) {
        String applicantName = caseData.getApplicant1().isIndividual()
            ? caseData.getApplicant1().getIndividualLastName() : caseData.getApplicant1().getPartyName();
        String respondentName = caseData.getRespondent1().isIndividual()
            ? caseData.getRespondent1().getIndividualLastName() : caseData.getRespondent1().getPartyName();
        return applicantName + " v " + respondentName +
            "-" + DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "ddMMyyyy");
    }

    private BundlingCaseData mapCaseData(CaseData caseData, String bundleConfigFileName) {
        BundlingCaseData bundlingCaseData =
            BundlingCaseData.builder().id(caseData.getCcdCaseReference()).bundleConfiguration(
                    bundleConfigFileName)
                    .trialDocuments(mapTrialDocuments(caseData))
                    .statementsOfCaseDocuments(mapStatementOfcaseDocs(caseData))
                    .particularsOfClaim(mapParticularsOfClaimDocs(caseData))
                    .ordersDocuments(mapOrdersDocument(caseData))
                    .claimant1WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT1))
                    .claimant2WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT2))
                    .defendant1WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT1))
                    .defendant2WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT2))
                    .claimant1ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.CLAIMANT1))
                    .claimant2ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.CLAIMANT2))
                    .defendant1ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.DEFENDANT1))
                    .defendant2ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.DEFENDANT2))
                    .jointStatementOfExperts(mapJointStatementOfExperts(caseData))
                    .claimant1DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.CLAIMANT1))
                    .claimant2DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.CLAIMANT2))
                    .defendant1DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.DEFENDANT1))
                    .defendant2DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.DEFENDANT2))
                    .claimant1CostsBudgets(mapCostBudgetDocs(caseData, PartyType.CLAIMANT1))
                    .claimant2CostsBudgets(mapCostBudgetDocs(caseData, PartyType.CLAIMANT2))
                    .defendant1CostsBudgets(mapCostBudgetDocs(caseData, PartyType.DEFENDANT1))
                    .defendant2CostsBudgets(mapCostBudgetDocs(caseData, PartyType.DEFENDANT2))
                    .applicant1(caseData.getApplicant1())
                    .respondent1(caseData.getRespondent1())
                    .hearingDate(null != caseData.getHearingDate()
                            ? DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "dd-MM-yyyy") : null)
                .ccdCaseReference(caseData.getCcdCaseReference())
                .build();
        bundlingCaseData = mapRespondent2Applicant2Details(bundlingCaseData, caseData);
        return bundlingCaseData;
    }

    private List<Element<BundlingRequestDocument>> mapParticularsOfClaimDocs(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (Objects.nonNull(caseData.getServedDocumentFiles())
                && Objects.nonNull((caseData.getServedDocumentFiles().getParticularsOfClaimDocument()))) {
            caseData.getServedDocumentFiles()
                    .getParticularsOfClaimDocument()
                    .forEach(poc -> bundlingRequestDocuments.add(
                            buildBundlingRequestDoc(getParticularsOfClaimName(caseData),
                            poc.getValue(), "")));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private String getParticularsOfClaimName(CaseData caseData) {
        LocalDate pocDate;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            pocDate = caseData.getIssueDate();
        } else if (Objects.nonNull(caseData.getClaimDetailsNotificationDate())) {
            pocDate = caseData.getClaimDetailsNotificationDate().toLocalDate();
        } else {
            pocDate = caseData.getSubmittedDate().toLocalDate();
        }
        return generateDocName(BundleFileNameList.PARTICULARS_OF_CLAIM.getDisplayName(),
                null, null, pocDate);
    }

    private List<Element<BundlingRequestDocument>> mapJointStatementOfExperts(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            Set<String> allJointExpertsNames = getAllExpertsNames(partyType, EvidenceUploadFiles.JOINT_STATEMENT,
                                                                   caseData);
            bundlingRequestDocuments.addAll(getAllExpertReports(partyType, EvidenceUploadFiles.JOINT_STATEMENT, caseData,
                                                                BundleFileNameList.JOINT_STATEMENTS_OF_EXPERTS, allJointExpertsNames
            ));
            bundlingRequestDocuments.addAll(getAllOtherPartyQuestions(partyType, caseData,
                                                                      allJointExpertsNames));
            bundlingRequestDocuments.addAll(getAllExpertReports(partyType, EvidenceUploadFiles.ANSWERS_FOR_EXPERTS, caseData,
                                                                BundleFileNameList.REPLIES_FROM, allJointExpertsNames));
        });

        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapCostBudgetDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                                                           EvidenceUploadFiles.COSTS, caseData),
                                                                                    DOC_FILE_NAME_WITH_DATE,
                                                                                    EvidenceUploadFiles.COSTS.name(),
                                                                                    partyType));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapDisclosedDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                   EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE, caseData),
                                                                                    DOC_FILE_NAME,
                                                                                 EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.name(),
                                                                                    partyType));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialList =
            getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData);

        if (documentEvidenceForTrialList != null) {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(documentEvidenceForTrialList,
                                             TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames(), true),
                DOC_FILE_NAME,
                EvidenceUploadFiles.DOCUMENTARY.name(),
                partyType
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapExpertEvidenceDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Set<String> allExpertsNames = getAllExpertsNames(partyType, EvidenceUploadFiles.EXPERT_REPORT, caseData);
        Set<String> allJointExpertsNames = getAllExpertsNames(partyType, EvidenceUploadFiles.JOINT_STATEMENT,
                                                              caseData);
        bundlingRequestDocuments.addAll(getAllExpertReports(partyType, EvidenceUploadFiles.EXPERT_REPORT, caseData,
                                                                BundleFileNameList.EXPERT_EVIDENCE, allExpertsNames));

        bundlingRequestDocuments.addAll(getAllOtherPartyQuestions(partyType,
                                                                  caseData, allExpertsNames));
        bundlingRequestDocuments.addAll(getAllExpertReports(partyType, EvidenceUploadFiles.ANSWERS_FOR_EXPERTS,
                                                                caseData, BundleFileNameList.REPLIES_FROM, allExpertsNames));
        bundlingRequestDocuments.addAll(getAllRemainingExpertQuestions(partyType,
                                                                    EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS, caseData));
        bundlingRequestDocuments.addAll(getAllRemainingExpertReports(partyType, EvidenceUploadFiles.ANSWERS_FOR_EXPERTS,
                                                             caseData,
                                                            BundleFileNameList.REPLIES_FROM, allExpertsNames, allJointExpertsNames));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> getAllRemainingExpertQuestions(PartyType partyType,
                                                                      EvidenceUploadFiles questionsForExperts, CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        List<Element<UploadEvidenceExpert>> listOfDocsOtherPartyQues = getExpertDocsByPartyAndDocType(partyType,
                                                                                          questionsForExperts, caseData);
        Set<String> allExpertFromOtherParty1 = getAllExpertFromOtherParty(partyType, EvidenceUploadFiles.EXPERT_REPORT,
                                                                          caseData, true);
        Set<String> allExpertFromOtherParty2 = getAllExpertFromOtherParty(partyType, EvidenceUploadFiles.EXPERT_REPORT,
                                                                          caseData, false);
        Set<String> allJointExpertsFromOtherParty1 = getAllExpertFromOtherParty(partyType, EvidenceUploadFiles.JOINT_STATEMENT,
                                                                                caseData, true);
        Set<String> allJointExpertsFromOtherParty2 = getAllExpertFromOtherParty(partyType, EvidenceUploadFiles.JOINT_STATEMENT,
                                                                                caseData, false);
        Party otherParty1;
        Party otherParty2;
        if (partyType.equals(PartyType.CLAIMANT1) || partyType.equals(PartyType.CLAIMANT2)) {
            otherParty1 = getPartyByPartyType(PartyType.DEFENDANT1, caseData);
            otherParty2 = getPartyByPartyType(PartyType.DEFENDANT2, caseData);

        } else {
            otherParty1 = getPartyByPartyType(PartyType.CLAIMANT1, caseData);
            otherParty2 = getPartyByPartyType(PartyType.CLAIMANT2, caseData);
        }
        List<Element<UploadEvidenceExpert>> tempList = new ArrayList<>();
        listOfDocsOtherPartyQues.forEach(expertElement -> {
            if (!((matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty1)
                && matchType(expertElement.getValue().getExpertOptionName(), allExpertFromOtherParty1, false))
                || (matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty2)
                && matchType(expertElement.getValue().getExpertOptionName(), allExpertFromOtherParty2, false))
                || (matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty1)
                && matchType(expertElement.getValue().getExpertOptionName(), allJointExpertsFromOtherParty1, false))
                || (matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty2)
                && matchType(expertElement.getValue().getExpertOptionName(), allJointExpertsFromOtherParty2, false)))) {
                tempList.add(expertElement);
            }
        });
        bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(
            tempList, BundleFileNameList.QUESTIONS_TO.getDisplayName(),
            EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.name()));

        return bundlingRequestDocuments;
    }

    private Set<String> getAllExpertFromOtherParty(PartyType partyType, EvidenceUploadFiles expertReport,
                                                   CaseData caseData, boolean isDefendant1) {
        if (partyType.equals(PartyType.CLAIMANT1) || partyType.equals(PartyType.CLAIMANT2)) {
            return getAllExpertsNames(isDefendant1 ? PartyType.DEFENDANT1 : PartyType.DEFENDANT2,
                                      expertReport, caseData);
        } else {
            return getAllExpertsNames(isDefendant1 ? PartyType.CLAIMANT1 : PartyType.CLAIMANT2,
                                      expertReport, caseData);
        }
    }

    private List<BundlingRequestDocument> getAllOtherPartyQuestions(PartyType partyType,
                                                                    CaseData caseData,
                                                                    Set<String> allExpertsNames) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        List<Element<UploadEvidenceExpert>> questionsFromOtherPartyDocs = getAllDocsFromOtherParty(partyType, caseData,
                                                                                                   EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS
        );
        if (!questionsFromOtherPartyDocs.isEmpty()) {
            List<Element<UploadEvidenceExpert>> tempList = questionsFromOtherPartyDocs.stream().filter(expertElement -> matchType(
                    expertElement.getValue().getExpertOptionName(), allExpertsNames, false
                ))
                .filter(expertElement -> matchParty(
                    expertElement.getValue().getExpertOptionOtherParty(),
                    getPartyByPartyType(partyType, caseData)
                )).collect(Collectors.toList());
            if (!tempList.isEmpty()) {
                Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap = groupExpertStatementsByName(tempList);
                expertReportMap.forEach((expertName, expertEvidenceList) -> bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(
                    expertEvidenceList, BundleFileNameList.QUESTIONS_TO.getDisplayName(), EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.name())));
            }
        }
        return bundlingRequestDocuments;
    }

    private boolean matchParty(String expertOptionOtherParty, Party party) {
        if (party != null && party.getPartyName() != null && expertOptionOtherParty.equalsIgnoreCase(party.getPartyName())) {
            return true;
        }
        return party != null && party.isIndividual() && party.getIndividualFirstName() != null && expertOptionOtherParty.equalsIgnoreCase(
            party.getIndividualFirstName());
    }

    private List<Element<UploadEvidenceExpert>> getAllDocsFromOtherParty(PartyType partyType, CaseData caseData,
                                                                         EvidenceUploadFiles evidenceUploadFileType) {
        List<Element<UploadEvidenceExpert>> list = new ArrayList<>();
        if (partyType.equals(PartyType.CLAIMANT1) || partyType.equals(PartyType.CLAIMANT2)) {
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.DEFENDANT1,
                                                          evidenceUploadFileType, caseData));
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.DEFENDANT2, evidenceUploadFileType, caseData));
        } else  {
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.CLAIMANT1, evidenceUploadFileType, caseData));
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.CLAIMANT2, evidenceUploadFileType, caseData));
        }
        return list;
    }

    private List<BundlingRequestDocument> getAllRemainingExpertReports(PartyType partyType,
                                                                             EvidenceUploadFiles evidenceUploadFiles, CaseData caseData,
                                                                       BundleFileNameList bundleFileNameList, Set<String> allExpertsNames, Set<String> allJointExpertsNames) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap =
            groupExpertStatementsByName(getExpertDocsByPartyAndDocType(partyType, evidenceUploadFiles, caseData));
        if (expertReportMap != null) {
            expertReportMap.forEach((expertName, expertEvidence) -> {
                if ((allExpertsNames != null && allExpertsNames.stream().anyMatch(s -> s.equalsIgnoreCase(expertName)))
                    || (allJointExpertsNames != null && allJointExpertsNames.stream().anyMatch(s -> s.equalsIgnoreCase(expertName)))) {
                    return;
                }
                bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(
                        expertEvidence,
                        bundleFileNameList.getDisplayName(),
                        evidenceUploadFiles.name()
                ));
            });
        }
        return bundlingRequestDocuments;
    }

    private Set<String> getAllExpertsNames(PartyType partyType, EvidenceUploadFiles evidenceUploadFileType,
                                            CaseData caseData) {
        List<Element<UploadEvidenceExpert>> expertsList = getExpertDocsByPartyAndDocType(partyType,
                                                                                         evidenceUploadFileType, caseData);
        if (expertsList != null) {
            return expertsList.stream().map(expertElement -> expertElement.getValue().getExpertOptionName().trim().toLowerCase())
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private List<BundlingRequestDocument> getAllExpertReports(PartyType partyType, EvidenceUploadFiles evidenceUploadFiles,
                                                              CaseData caseData, BundleFileNameList bundleFileNameList,
                                                              Set<String> allExpertsNames) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap =
            groupExpertStatementsByName(getExpertDocsByPartyAndDocType(partyType, evidenceUploadFiles, caseData));
        if (expertReportMap != null) {
            expertReportMap.forEach((expertName, expertEvidence) -> {
                if (allExpertsNames != null && allExpertsNames.stream().anyMatch(s -> s.equalsIgnoreCase(expertName))) {
                    bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(
                        expertEvidence,
                        bundleFileNameList.getDisplayName(),
                        evidenceUploadFiles.name()
                    ));
                }
            });
        }
        return bundlingRequestDocuments;
    }

    private Map<String, List<Element<UploadEvidenceExpert>>> groupExpertStatementsByName(
        List<Element<UploadEvidenceExpert>> documentExpertReport) {
        Map<String, List<Element<UploadEvidenceExpert>>> expertStatementMap = new TreeMap<>();
        if (documentExpertReport != null) {
            expertStatementMap = documentExpertReport.stream().collect(Collectors
                                                                        .groupingBy(uploadEvidenceExpertElement -> uploadEvidenceExpertElement
                                                                            .getValue().getExpertOptionName().trim().toLowerCase()));
        }
        return expertStatementMap;
    }

    private List<Element<BundlingRequestDocument>> mapWitnessStatements(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Party party = getPartyByPartyType(partyType, caseData);
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
            groupWitnessStatementsByName(getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadFiles.WITNESS_STATEMENT, caseData));
        List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap, party);
        bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                 BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                 EvidenceUploadFiles.WITNESS_STATEMENT.name(), partyType, true));
        bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                      BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                      EvidenceUploadFiles.WITNESS_STATEMENT.name(), partyType, party));
        bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadFiles.WITNESS_SUMMARY, caseData),
                                                                                BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                EvidenceUploadFiles.WITNESS_SUMMARY.name(), partyType, false));
        bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                                                           EvidenceUploadFiles.DOCUMENTS_REFERRED, caseData),
                                                                                 BundleFileNameList.DOC_REFERRED_TO.getDisplayName(),
                                                                                 EvidenceUploadFiles.DOCUMENTS_REFERRED.name(), partyType));
        bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadFiles.NOTICE_OF_INTENTION, caseData),
                                                                                 BundleFileNameList.HEARSAY_NOTICE.getDisplayName(),
                                                                                 EvidenceUploadFiles.NOTICE_OF_INTENTION.name(), partyType, false));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial = getEvidenceUploadDocsByPartyAndDocType(partyType,
            EvidenceUploadFiles.DOCUMENTARY, caseData);
        if (documentEvidenceForTrial != null) {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(documentEvidenceForTrial,
                                             TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.getDisplayNames(), false),
                BundleFileNameList.NOTICE_TO_ADMIT_FACTS.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.name(),
                partyType
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private Party getPartyByPartyType(PartyType partyType, CaseData caseData) {
        return switch (partyType) {
            case CLAIMANT1 -> caseData.getApplicant1() != null ? caseData.getApplicant1() : null;
            case CLAIMANT2 -> caseData.getApplicant2() != null ? caseData.getApplicant2() : null;
            case DEFENDANT1 -> caseData.getRespondent1() != null ? caseData.getRespondent1() : null;
            case DEFENDANT2 -> caseData.getRespondent2() != null ? caseData.getRespondent2() : null;
        };
    }

    private List<BundlingRequestDocument> covertOtherWitnessEvidenceToBundleRequestDocs(
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap, String displayName, String documentType,
        PartyType partyType, Party party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (party != null) {
            if (party.getPartyName() != null) {
                witnessStatmentsMap.remove(party.getPartyName().trim().toLowerCase());
            }
            if (party.isIndividual() && party.getIndividualFirstName() != null) {
                witnessStatmentsMap.remove(party.getIndividualFirstName().trim().toLowerCase());
            }
        }
        witnessStatmentsMap.forEach((witnessName, witnessEvidence) -> {
            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                String docName = generateDocName(displayName,
                                                 uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                 String.valueOf(witnessEvidence.indexOf(uploadEvidenceWitnessElement) + 1),
                                                 uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate());
                bundlingRequestDocuments.add(buildBundlingRequestDoc(docName,
                                             uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument(),
                                                                     documentType));
            });
        });
        return bundlingRequestDocuments;
    }

    private void  sortWitnessListByDate(List<Element<UploadEvidenceWitness>> witnessEvidence,
                                       boolean sortByCreatedDate) {
        if (sortByCreatedDate) {
            witnessEvidence.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getCreatedDatetime(),
                Comparator.reverseOrder()
            ));
        } else {
            witnessEvidence.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate(),
                Comparator.reverseOrder()
            ));
        }
    }

    private void  sortEvidenceUploadByDate(List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocType,
                                                                        boolean sortByCreatedDate) {
        if (sortByCreatedDate) {
            uploadEvidenceDocType.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getCreatedDatetime(),
                Comparator.reverseOrder()
            ));
        } else {
            uploadEvidenceDocType.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getDocumentIssuedDate(),
                Comparator.reverseOrder()
            ));
        }
    }

    private void  sortExpertListByDate(List<Element<UploadEvidenceExpert>> expertEvidence,
                                                                        boolean sortByCreatedDate) {
        expertEvidence.sort(Comparator.comparing(
                uploadEvidenceExpertElement -> uploadEvidenceExpertElement.getValue().getExpertOptionUploadDate(),
                Comparator.reverseOrder()
        ));
    }

    private List<Element<UploadEvidenceWitness>> getSelfStatement(Map<String,
        List<Element<UploadEvidenceWitness>>> witnessStatementsMap, Party party) {
        List<Element<UploadEvidenceWitness>> selfStatementList = new ArrayList<>();
        if (party != null && party.getPartyName() != null && witnessStatementsMap.get(party.getPartyName().trim().toLowerCase()) != null) {
            selfStatementList.addAll(witnessStatementsMap.get(party.getPartyName().trim().toLowerCase()));
        }
        if (party != null && party.isIndividual() && party.getIndividualFirstName() != null && witnessStatementsMap.get(party.getIndividualFirstName()
                                                                                                                           .trim().toLowerCase()) != null) {
            selfStatementList.addAll(witnessStatementsMap.get(party.getIndividualFirstName().trim().toLowerCase()));
        }
        return selfStatementList;
    }

    private Map<String, List<Element<UploadEvidenceWitness>>> groupWitnessStatementsByName(
        List<Element<UploadEvidenceWitness>> witnessStatement) {
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementMap = new HashMap<String,
            List<Element<UploadEvidenceWitness>>>();
        if (witnessStatement != null) {
            witnessStatementMap = witnessStatement.stream().collect(Collectors.groupingBy(uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement
                                                          .getValue().getWitnessOptionName().trim().toLowerCase()));
        }
        return witnessStatementMap;
    }

    private List<Element<BundlingRequestDocument>> mapOrdersDocument(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                           .equals(DocumentType.DEFAULT_JUDGMENT_SDO_ORDER)).collect(Collectors.toList()),
                                                                       BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()));
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                           .equals(DocumentType.SDO_ORDER)).collect(Collectors.toList()),
                                                                       BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()));
        if (caseData.getGeneralOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getGeneralOrderDocStaff(),
                                                                           BundleFileNameList.ORDER.getDisplayName()));
        }
        if (caseData.getDismissalOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getDismissalOrderDocStaff(), BundleFileNameList.ORDER.getDisplayName()));
        }
        if (caseData.getDirectionOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getDirectionOrderDocStaff(),
                                                                           BundleFileNameList.ORDER.getDisplayName()));
        }
        if (caseData.getFinalOrderDocument() != null) {
            bundlingRequestDocuments.add(buildBundlingRequestDoc(caseData.getFinalOrderDocument().getDocumentFileName(), caseData.getFinalOrderDocument(), ""));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapStatementOfcaseDocs(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                           .equals(DocumentType.SEALED_CLAIM)
                                                                               && null != caseDocumentElement.getValue().getDocumentLink().getCategoryID()
                                                                               && caseDocumentElement.getValue().getDocumentLink().getCategoryID().equals("detailsOfClaim"))
                                                                           .collect(Collectors.toList()),
                                                                       BundleFileNameList.CLAIM_FORM.getDisplayName()));
        List<Element<CaseDocument>> clAndDfDocList = caseData.getDefendantResponseDocuments();
        clAndDfDocList.addAll(caseData.getClaimantResponseDocuments());
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply =
            getSortedDefendantDefenceAndClaimantReply(clAndDfDocList);
        sortedDefendantDefenceAndClaimantReply.forEach(caseDocumentElement -> {
            String docType = caseDocumentElement.getValue().getDocumentType().equals(DocumentType.DEFENDANT_DEFENCE)
                ? BundleFileNameList.DEFENCE.getDisplayName() : BundleFileNameList.CL_REPLY.getDisplayName();
            String party = caseDocumentElement.getValue().getCreatedBy().equalsIgnoreCase("Defendant")
                ? PartyType.DEFENDANT1.getDisplayName() :
                    caseDocumentElement.getValue().getCreatedBy().equalsIgnoreCase("Defendant 2")
                        ? PartyType.DEFENDANT2.getDisplayName() : "";
            String docName = generateDocName(docType, party, null,
                                                           caseDocumentElement.getValue().getCreatedDatetime().toLocalDate());
            bundlingRequestDocuments.add(buildBundlingRequestDoc(docName, caseDocumentElement.getValue().getDocumentLink(), docType));
        });
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData),
                                             TypeOfDocDocumentaryEvidenceOfTrial.PART18.getDisplayNames(), false),
                BundleFileNameList.REPLY_TO_PART_18.getDisplayName(), TypeOfDocDocumentaryEvidenceOfTrial.PART18.name(),
                partyType
            ));
        });
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                               .equals(DocumentType.DIRECTIONS_QUESTIONNAIRE)).collect(
                                                                               Collectors.toList()),
                                                                       BundleFileNameList.DIRECTIONS_QUESTIONNAIRE.getDisplayName()));
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData),
                                             TypeOfDocDocumentaryEvidenceOfTrial.SCHEDULE_OF_LOSS.getDisplayNames(), false),
                BundleFileNameList.SCHEDULE_OF_LOSS_FILE_DISPLAY_NAME.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.SCHEDULE_OF_LOSS.name(), partyType
            ));
        });
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<CaseDocument>> getSortedDefendantDefenceAndClaimantReply(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply = new ArrayList<>();
        List<Element<CaseDocument>> dfDefence =
            systemGeneratedCaseDocuments.stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                .equals(DocumentType.DEFENDANT_DEFENCE)).collect(Collectors.toList());

        List<Element<CaseDocument>> clDefence =
            systemGeneratedCaseDocuments.stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                .equals(DocumentType.CLAIMANT_DEFENCE)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dfDefence) || CollectionUtils.isEmpty(clDefence)) {
            sortedDefendantDefenceAndClaimantReply = CollectionUtils.isEmpty(dfDefence) ? clDefence : dfDefence;
            sortedDefendantDefenceAndClaimantReply.sort(Comparator.comparing(caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime()));
            return sortedDefendantDefenceAndClaimantReply;
        }
        dfDefence.sort(Comparator.comparing(caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime()));
        clDefence.sort(Comparator.comparing(caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime()));
        sortedDefendantDefenceAndClaimantReply = IntStream.range(0, Math.min(dfDefence.size(), clDefence.size()))
            .mapToObj(index -> Arrays.asList(dfDefence.get(index), clDefence.get(index)))
            .flatMap(List::stream)
            .collect(Collectors.toList());

        int remainingIndex = Math.min(dfDefence.size(), clDefence.size());
        List<Element<CaseDocument>> remainingList = (dfDefence.size() > clDefence.size()) ? dfDefence : clDefence;

        sortedDefendantDefenceAndClaimantReply.addAll(remainingList.subList(remainingIndex, remainingList.size()));
        return sortedDefendantDefenceAndClaimantReply;
    }

    private List<Element<BundlingRequestDocument>> mapTrialDocuments(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                                                     EvidenceUploadFiles.CASE_SUMMARY, caseData),
                                                                                     BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME.getDisplayName(),
                                                                                     EvidenceUploadFiles.CASE_SUMMARY.name(),
                                                                                        partyType));
        });
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData),
                                             TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames(), false),
                BundleFileNameList.CHRONOLOGY_FILE_DISPLAY_NAME.getDisplayName(), TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.name(), partyType
            ));
        });
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData),
                                             TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames(), false),
                BundleFileNameList.TRIAL_TIMETABLE_FILE_DISPLAY_NAME.getDisplayName(), TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.name(), partyType
            ));
        });
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.SKELETON_ARGUMENT, caseData),
                BundleFileNameList.SKELETON_ARGUMENT.getDisplayName(), EvidenceUploadFiles.SKELETON_ARGUMENT.name(),
                partyType));
        });
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<UploadEvidenceDocumentType>> getDocumentaryEvidenceByType(
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames, boolean doesNotMatchType) {
        if (documentEvidenceForTrial != null) {
            return
                filterDocumentaryEvidenceForTrialDocs(documentEvidenceForTrial, displayNames, doesNotMatchType);
        } else {
            return Collections.emptyList();
        }
    }

    public List<Element<UploadEvidenceDocumentType>> filterDocumentaryEvidenceForTrialDocs(
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames,
        boolean doesNotMatchType) {
        sortEvidenceUploadByDate(documentEvidenceForTrial, false);
        return documentEvidenceForTrial.stream().filter(uploadEvidenceDocumentTypeElement -> matchType(
            uploadEvidenceDocumentTypeElement.getValue().getTypeOfDocument(),
            displayNames, doesNotMatchType
        )).collect(Collectors.toList());
    }

    private boolean matchType(String name, Collection<String> displayNames, boolean doesNotMatchType) {
        if (doesNotMatchType) {
            return displayNames.stream().noneMatch(s -> s.equalsIgnoreCase(name.trim()));
        } else {
            return displayNames.stream().anyMatch(s -> s.equalsIgnoreCase(name.trim()));
        }
    }

    private List<BundlingRequestDocument> covertWitnessEvidenceToBundleRequestDocs(List<Element<UploadEvidenceWitness>> witnessEvidence,
                                                                                   String fileNamePrefix,
                                                                                   String documentType,
                                                                                   PartyType party,
                                                                                   boolean isWitnessSelf) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (witnessEvidence != null) {
            if (documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT.name())) {
                sortWitnessListByDate(witnessEvidence, false);
            } else {
                sortWitnessListByDate(witnessEvidence, true);
            }
            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                String docName = generateDocName(fileNamePrefix,
                                                 isWitnessSelf ? party.getDisplayName() :
                                                     uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                 null,
                                                 documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT.name())
                                                     ? uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate() : uploadEvidenceWitnessElement
                                                     .getValue().getCreatedDatetime().toLocalDate());
                bundlingRequestDocuments.add(buildBundlingRequestDoc(docName, uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument(), documentType));
            });
        }
        return bundlingRequestDocuments;
    }

    private String generateDocName(String fileName, String strParam, String strParam2, LocalDate date) {
        if (StringUtils.isBlank(strParam)) {
            return String.format(fileName, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else if (StringUtils.isBlank(strParam2)) {
            return String.format(fileName, strParam, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else {
            return String.format(fileName, strParam, strParam2, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        }
    }

    private List<BundlingRequestDocument> covertEvidenceUploadTypeToBundleRequestDocs(List<Element<UploadEvidenceDocumentType>> evidenceUploadDocList,
                                                                                      String fileNamePrefix, String documentType,
                                                                                      PartyType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (evidenceUploadDocList != null) {
            if (documentType.equals(EvidenceUploadFiles.CASE_SUMMARY.name())
                || documentType.equals(EvidenceUploadFiles.SKELETON_ARGUMENT.name())
                || documentType.equals(EvidenceUploadFiles.COSTS.name())) {
                sortEvidenceUploadByDate(evidenceUploadDocList, true);
            } else {
                sortEvidenceUploadByDate(evidenceUploadDocList, false);
            }

            evidenceUploadDocList.forEach(uploadEvidenceDocumentTypeElement -> {
                String docName = getFileNameBaseOnType(fileNamePrefix, uploadEvidenceDocumentTypeElement,
                                                       documentType, party,
                                                       String.valueOf(evidenceUploadDocList.indexOf(uploadEvidenceDocumentTypeElement) + 1));
                bundlingRequestDocuments.add(buildBundlingRequestDoc(docName, uploadEvidenceDocumentTypeElement.getValue().getDocumentUpload(), documentType));
            });
        }
        return bundlingRequestDocuments;
    }

    private String getFileNameBaseOnType(String fileNamePrefix, Element<UploadEvidenceDocumentType> uploadEvidence,
                                         String documentType, PartyType party, String index) {
        if (fileNamePrefix.equals(DOC_FILE_NAME)) {
            return uploadEvidence.getValue().getDocumentUpload().getDocumentFileName()
                .substring(0, uploadEvidence.getValue().getDocumentUpload().getDocumentFileName().lastIndexOf("."));
        } else if (fileNamePrefix.equals(DOC_FILE_NAME_WITH_DATE)) {
            return generateDocName(uploadEvidence.getValue().getDocumentUpload().getDocumentFileName()
                                       .substring(0, uploadEvidence.getValue().getDocumentUpload().getDocumentFileName().lastIndexOf(".")) + " %s", null,
                                   null,
                            documentType.equals(EvidenceUploadFiles.COSTS.name())
                                ? uploadEvidence.getValue().getCreatedDatetime().toLocalDate() :
                                uploadEvidence.getValue().getDocumentIssuedDate());
        } else {
            String partyName = party.getDisplayName();
            if (fileNamePrefix.equals(BundleFileNameList.SCHEDULE_OF_LOSS_FILE_DISPLAY_NAME.getDisplayName())
                && (party.equals(PartyType.DEFENDANT1) || party.equals(PartyType.DEFENDANT2))) {
                partyName = partyName.concat(" counter");
            }
            if (documentType.equals(EvidenceUploadFiles.DOCUMENTS_REFERRED.name())) {
                return generateDocName(fileNamePrefix,
                                       index,
                                       null,
                                       uploadEvidence.getValue().getDocumentIssuedDate());
            } else {
                return generateDocName(fileNamePrefix, partyName, null,
                                       documentType.equals(EvidenceUploadFiles.CASE_SUMMARY.name()) || documentType.equals(
                                           EvidenceUploadFiles.SKELETON_ARGUMENT.name())
                                           ? uploadEvidence.getValue().getCreatedDatetime().toLocalDate() :
                                           uploadEvidence.getValue().getDocumentIssuedDate()
                );
            }
        }
    }

    private List<BundlingRequestDocument> covertExpertEvidenceTypeToBundleRequestDocs(List<Element<UploadEvidenceExpert>> evidenceUploadExpert,
                                                                                      String fileNamePrefix, String documentType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        if (evidenceUploadExpert != null) {
            sortExpertListByDate(evidenceUploadExpert, false);
            evidenceUploadExpert.forEach(expertElement -> {
                String docName = generateDocName(fileNamePrefix,
                                                 expertElement.getValue().getExpertOptionName(),
                                                 documentType.equals(EvidenceUploadFiles.EXPERT_REPORT.name())
                                                     ? expertElement.getValue().getExpertOptionExpertise() :
                                                     documentType.equals(EvidenceUploadFiles.JOINT_STATEMENT.name())
                                                         ? expertElement.getValue().getExpertOptionExpertises() : null,
                                                 expertElement.getValue().getExpertOptionUploadDate()
                );
                bundlingRequestDocuments.add(buildBundlingRequestDoc(docName, expertElement.getValue().getExpertDocument(), documentType));
            });
        }
        return bundlingRequestDocuments;
    }

    private BundlingCaseData mapRespondent2Applicant2Details(BundlingCaseData bundlingCaseData, CaseData caseData) {
        if (null != caseData.getAddApplicant2() && caseData.getAddApplicant2().equals(YesOrNo.YES)) {
            bundlingCaseData.toBuilder().hasApplicant2(true);
        }
        if (null != caseData.getAddRespondent2() && caseData.getAddRespondent2().equals(YesOrNo.YES)) {
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

    private List<BundlingRequestDocument> mapSystemGeneratedCaseDocument(List<Element<CaseDocument>> systemGeneratedCaseDocuments, String displayName) {
        List<BundlingRequestDocument> bundlingSystemGeneratedCaseDocs = new ArrayList<>();
        if (systemGeneratedCaseDocuments != null) {
            systemGeneratedCaseDocuments.forEach(caseDocumentElement -> {
                String docName = generateDocName(displayName, null, null,
                                                 caseDocumentElement.getValue().getCreatedDatetime().toLocalDate());
                bundlingSystemGeneratedCaseDocs.add(buildBundlingRequestDoc(docName, caseDocumentElement.getValue().getDocumentLink(),
                                                                            caseDocumentElement.getValue().getDocumentType().name()));
            });
        }
        return bundlingSystemGeneratedCaseDocs;
    }

    private BundlingRequestDocument buildBundlingRequestDoc(String docName, Document document, String docType) {
        return BundlingRequestDocument.builder()
            .documentFileName(docName)
            .documentType(docType)
            .documentLink(DocumentLink.builder()
                              .documentUrl(document.getDocumentUrl())
                              .documentBinaryUrl(document.getDocumentBinaryUrl())
                              .documentFilename(document.getDocumentFileName()).build())
            .build();
    }

    protected enum PartyType {
        CLAIMANT1("CL 1"),
        CLAIMANT2("CL 2"),
        DEFENDANT1("DF 1"),
        DEFENDANT2("DF 2");
        String displayName;

        public String getDisplayName() {
            return displayName;
        }

        PartyType(String displayName) {
            this.displayName = displayName;
        }
    }
}
